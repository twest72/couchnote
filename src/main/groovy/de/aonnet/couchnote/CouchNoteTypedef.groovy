package de.aonnet.couchnote

import de.aonnet.gcouch.CouchDbHelper
import de.aonnet.gcouch.GroovyCouchDb
import groovy.json.JsonOutput
import groovy.transform.TypeChecked
import groovy.util.logging.Commons

import java.text.ParseException

@Commons
class CouchNoteTypedef {

    static String VIEW_ALL_TYPEDEF = 'allTypedef'
    static List EXCLUDE_VALUE_DATA_TYPES = ['Attachment', 'AttachmentList']

    static String VIEW_ALL_TYPEDEF_MAP_FUNCTION = """
function(doc) {
    if(doc.type == "TYPEDEF") {

        var returnValue = {};
        for(prop in doc) {
           if (prop.indexOf('_') != 0 && prop != 'type' ) {
              returnValue[prop] = doc[prop];
           }
        }

        emit( {"type_name": doc.type_name}, returnValue );
    }
}"""

    Map<String, Map<String, Object>> typedefs

    private final GroovyCouchDb couchDb

    @TypeChecked
    CouchNoteTypedef(GroovyCouchDb couchDb) {
        this.couchDb = couchDb
        this.typedefs = this.loadAllTypedef()
    }

    private Map<String, Map<String, Object>> loadAllTypedef() {

        Map result = couchDb.view(CouchNote.DESIGN_DOC_ALL, VIEW_ALL_TYPEDEF)

        Map typedefs = [:]
        result.rows.each { Map row ->
            typedefs.put(row.key.type_name, row.value)
        }

        return typedefs
    }

    Map<String, Object> transformTypeInstance(String typeName, String id, Map<String, Object> typeInstance) {

        Map<String, Object> noteTypedef = typedefs.get(typeName)

        Map<String, Object> newTypeInstance = [:]
        typeInstance.each { String fieldName, def fieldValue ->

            log.trace "transform field: $fieldName"
            Map<String, Object> fieldMetaData = findFieldMetaData(typeName, fieldName)
            newTypeInstance.put(fieldName, transformField(id, fieldName, fieldValue, fieldMetaData))
        }

        log.debug "type instance orginal    : $typeInstance"
        log.debug "type instance transformed: $newTypeInstance"

        return newTypeInstance
    }

    private def transformField(String id, String fieldName, def fieldValue, Map<String, Object> fieldMetaData) {

        log.trace "transform field $fieldName with meta data: $fieldMetaData"

        String fieldDataType = fieldMetaData?.data_type

        def newFieldValue
        switch (fieldDataType) {
            case 'Attachment':

                newFieldValue = createLinkForAttachment(id, fieldValue)
                break

            case 'Map':

                newFieldValue = [:]
                fieldValue.each {
                    newFieldValue.put(it.key, transformField(id, it.key, it.value, fieldMetaData.fields.get(it.key)))
                }
                break

            case 'List':

                newFieldValue = []
                fieldValue.each {
                    newFieldValue.add(transformField(id, null, it, fieldMetaData.list_properties))
                }
                break

            case 'String':

                try {
                    newFieldValue = JsonOutput.dateFormat.parse(fieldValue)
                } catch (ParseException e) {
                    newFieldValue = fieldValue
                }
                break

            default:

                newFieldValue = fieldValue
                break
        }

        log.trace "field orginal    : $fieldValue"
        log.trace "field transformed: $newFieldValue"

        return newFieldValue
    }

    private Map<String, Object> createLinkForAttachment(String id, Map<String, Object> fieldValue) {

        assert fieldValue.attachment

        Map<String, Object> newFieldValue = fieldValue.clone()
        newFieldValue.attachment = fieldValue.attachment.clone()

        newFieldValue.attachment.link = "http://${couchDb.host}:${couchDb.port}/${couchDb.dbName}/${id}/${fieldValue.attachment.id}"

        return newFieldValue
    }

    Map<String, Object> findFieldMetaData(String typeName, String fieldName) {

        if (!typedefs.containsKey(typeName)) {
            return
        }

        return findFieldMetaDataImpl(fieldName, typedefs.get(typeName).fields)
    }

    private Map<String, Object> findFieldMetaDataImpl(String fieldName, Map<String, Object> fields) {

        if (!fieldName || !fields) {
            return
        }

        Map<String, Object> metaData = fields.find { it.key == fieldName }?.value

        if (!metaData) {

            List<Map<String, Object>> maps = fields.findAll { it.value.data_type == 'Map' }.collect { it.value.fields }

            for (Map<String, Object> map : maps) {

                metaData = findFieldMetaDataImpl(fieldName, map)
                if (metaData) {
                    break
                }
            }
        }

        return metaData
    }

    /**
     * Create the name of the view to load all entries for the given type.
     *
     * @param typeName The name of the type.
     *
     * @return The name of the view.
     */
    @TypeChecked
    static String createTypeViewName(String typeName) {

        return "all${ firstUpperCase(typeName) }"
    }

    /**
     * Create the name of the view to load all entries for the given type. The result is sorted by the specified field.
     *
     * @param typeName The name of the type.
     * @param fieldName The result is sorted by the field with this name.
     *
     * @return The name of the view.
     */
    @TypeChecked
    static String createTypeViewName(String typeName, String fieldName) {
        return createTypeViewName(typeName, [fieldName])
    }

    /**
     * Create the name of the view to load all entries for the given type. The result is sorted by the specified field.
     *
     * @param typeName The name of the type.
     * @param fieldName The result is sorted by the field with this name.
     *
     * @return The name of the view.
     */
    @TypeChecked
    static String createTypeViewName(String typeName, List<String> fieldName) {
        return "${ createTypeViewName(typeName) }By${ fieldName.collect { firstUpperCase(it) }.join('') }"
    }

    /**
     * Create all views for the specified type.
     *
     * @param typedef The metadata for the type.
     *
     * @return The views to load data for the type.
     */
    static Map createViews(Map<String, Map<String, Object>> typedef) {

        log.info "create views for typedef $typedef.type_name ${typedef}"

        // the view to load all entries sorted
        assert typedef.fields.size() > 0

        log.debug "sort order: ${typedef.sort_order.getClass()}"

        Map views = createView typedef, false, typedef.sort_order

        typedef.fields.each { String fieldName, Map fieldData ->

            if (fieldData.sort) {
                log.info "create view for typedef $typedef.type_name and sortable field ${fieldName}"

                views << createView(typedef, true, [fieldName])
            }
        }

        return views
    }

    @TypeChecked
    private static String firstUpperCase(String name) {

        return name[0].toUpperCase() + name.substring(1).toLowerCase()
    }

    @TypeChecked
    private static Map<String, Map> createView(Map<String, Map<String, Object>> typedef, boolean nameWithByField, List<String> fieldName) {

        String typeName = typedef.type_name
        String viewByFieldName = nameWithByField ? CouchNoteTypedef.createTypeViewName(typeName, fieldName) : CouchNoteTypedef.createTypeViewName(typeName)
        String mapByFieldFunction = createMapFunctionToLoadAllEntries typedef, fieldName

        return CouchDbHelper.createViewMap(viewByFieldName, mapByFieldFunction)
    }

    @TypeChecked
    private static String createMapFunctionToLoadAllEntries(Map<String, Map<String, Object>> typedef, List<String> keyFieldNames) {

        String jsonKey = keyFieldNames.collect { """"$it": doc.$it""" }.join(', ')
        String orderByComment = keyFieldNames.collect { it }.join(', ')

        List<String> jsonValueList = []
        typedef.fields.each { String fieldName, Map fieldProperties ->

            if (!fieldName.startsWith('_') && !EXCLUDE_VALUE_DATA_TYPES.contains(fieldProperties.data_type)) {
                jsonValueList << (""""$fieldName": doc.$fieldName""" as String)
            }
        }
        String jsonValue = jsonValueList.join(', ')


        return """
// load all entries for type $typedef.type_name order by $orderByComment
function(doc) {
    if(doc.type == "$typedef.type_name") {

        emit( {$jsonKey}, {$jsonValue} );
    }
}
"""
    }
}
