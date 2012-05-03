package de.aonnet.couchnote

import de.aonnet.gcouch.GroovyCouchDb
import org.junit.Before
import org.junit.Test

class CouchNoteTypedefTest {

    private final static Map TYPEDEF_NOTE = [
            type: 'TYPEDEF',
            type_name: 'SIMPLENOTE',
            fields: [
                    title: [data_type: 'String', sort: true, required: true],
                    name: [data_type: 'String', sort: true, required: true],
                    description: [data_type: 'String']
            ],
            sort_order: ['title', 'name']
    ]

    @Test
    void testCreateTypeViewName() {
        assert CouchNoteTypedef.createTypeViewName('SIMPLENOTE') == 'allSimplenote'
    }

    @Test
    void testCreateTypeViewNameByTitle() {
        assert CouchNoteTypedef.createTypeViewName('SIMPLENOTE', 'title') == 'allSimplenoteByTitle'
    }

    @Test
    void testCreateTypeViewNameByTitleDate() {
        assert CouchNoteTypedef.createTypeViewName('SIMPLENOTE', ['title', 'date']) == 'allSimplenoteByTitleDate'
    }

    @Test
    void testCreateTypeViewNameByUserTitleDate() {
        assert CouchNoteTypedef.createTypeViewName('SIMPLENOTE', ['user', 'title', 'date']) == 'allSimplenoteByUserTitleDate'
    }

    @Test
    void testCreateViews() {
        Map viewMap = CouchNoteTypedef.createViews(TYPEDEF_NOTE)
        println viewMap
        assert viewMap
        assert viewMap.allSimplenote
        assert viewMap.allSimplenote.map
        assert viewMap.allSimplenoteByTitle
        assert viewMap.allSimplenoteByTitle.map
    }

    @Test
    void testLoadTypedef() {

        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        CouchNoteTypedef couchNoteTypedef = new CouchNoteTypedef(new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB))

        Map<String, Object> typedef = couchNoteTypedef.typedefs['SIMPLENOTE']

        assert typedef.fields.title
        assert typedef.fields.description

        assert typedef.fields.title.data_type == 'String'
        assert typedef.fields.description.data_type == 'String'
    }

    @Test
    void testLoadAllTypedef() {

        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        CouchNoteTypedef couchNoteTypedef = new CouchNoteTypedef(new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB))

        Map<String, Map<String, Object>> typedefs = couchNoteTypedef.typedefs

        assert typedefs.toMapString() == '[NOTE:[type_name:NOTE, fields:[title:[data_type:String, required:true, sort:true, minlength:1, maxlength:255], content:[data_type:Attachment, list_properties:[data_type:String, required:true, sort:false, minlength:1, maxlength:5242880]], imported:[data_type:Datetime, required:false, sort:true], created:[data_type:Datetime, required:false, sort:true], updated:[data_type:Datetime, required:false, sort:true], tag:[data_type:List, list_properties:[data_type:String, sort:true, minlength:1, maxlength:100]], note_attributes:[data_type:Map, required:false, sort:false, import_name:note-attributes, fields:[subject_date:[data_type:Datetime, required:false, sort:true, import_name:subject-date], latitude:[data_type:BigDecimal, required:false, sort:false], longitude:[data_type:BigDecimal, required:false, sort:false], altitude:[data_type:BigDecimal, required:false, sort:false], author:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096], source:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096], source_url:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:source-url], source_application:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:source-application]]], resource:[data_type:AttachmentList, list_properties:[data_type:Map, sort:false, fields:[data:[data_type:base64, required:true, sort:false], mime:[data_type:String, required:true, sort:false, maxlength:100], width:[data_type:Integer, required:false, sort:false, min:0], height:[data_type:Integer, required:false, sort:false, min:0], resource_attributes:[data_type:Map, sort:false, import_name:resource-attributes, fields:[source_url:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:source-url], timestamp:[data_type:Datetime, required:false, sort:true], latitude:[data_type:BigDecimal, required:false, sort:false], longitude:[data_type:BigDecimal, required:false, sort:false], altitude:[data_type:BigDecimal, required:false, sort:false], camera_make:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:camera-make], camera_model:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:camera-model], reco_type:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:reco-type], file_name:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:file-name], attachment:[data_type:Boolean, required:false, sort:true]]]]]]], sort_order:[title, created, updated]], SIMPLENOTE:[type_name:SIMPLENOTE, fields:[title:[data_type:String, sort:true, required:true], description:[data_type:String]], sort_order:[title]]]'
    }

    @Before
    void prepareDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB)
        if (couchDb.existsDb()) {
            couchDb.dropDb()
        }
    }
}
