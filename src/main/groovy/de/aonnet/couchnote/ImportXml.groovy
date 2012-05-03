package de.aonnet.couchnote

import de.aonnet.gcouch.CouchDbHelper
import de.aonnet.gcouch.GroovyCouchDb
import groovy.json.JsonOutput
import groovy.util.logging.Commons
import groovyx.net.http.ContentType
import org.apache.commons.lang.StringUtils

import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat

@Commons
class ImportXml {

    List<Map<String, Object>> attachments = []

    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssX")

    void doImport(Map<String, Object> typedef, InputStream inputStream, GroovyCouchDb couchDb) {

        log.info "import type $typedef.type_name"

        Long startTime = System.currentTimeMillis()
        Integer items = 0

        def inputData = new XmlSlurper().parse(inputStream)

        inputData."${ typedef.type_name.toLowerCase() }".each { def fromType ->

            Map<String, Object> toType = convertType(fromType, typedef)

            log.info "import into db: $toType.title"
            log.trace "import into db: $toType"
            couchDb.create(toType)
            items++
        }

        log.info "import $items entries of type $typedef.type_name done in ${ System.currentTimeMillis() - startTime } ms"
    }

    private def convertType(def fromType, Map<String, Object> typedef) {

        Map<String, Object> toType = [type: typedef.type_name]
        toType.put('imported', JsonOutput.toJson(new Date()))

        attachments.clear()

        Map<String, Object> toMap = convertMap(fromType, typedef.fields)
        if (toMap) {
            toType << toMap
        }

        attachments.each { Map<String, Object> attachment ->
            log.debug "add attachment: id: $attachment.id , mime: $attachment.mime"
            CouchDbHelper.setAttachmentAtObject(toType, attachment.id, attachment.mime, attachment.data)
        }
        attachments.clear()

        return toType
    }

    private def convertMap(def fromMap, Map<String, Object> fields) {

        Map<String, Object> toMap = [:]

        fields.each { String fieldName, Map fieldProperties ->

            log.debug "read field $fieldName with type ${ fieldProperties.data_type }"

            def fromFieldValue = fromMap."${ importName fieldName, fieldProperties }"

            log.debug "form field value $fromFieldValue"
            def toFieldValue = convertFieldValue(fieldName, fieldProperties, fromFieldValue)
            log.debug "to field value $toFieldValue"

            if (toFieldValue) {
                toMap.put(fieldName, toFieldValue)
            }
        }

        //log.info toMap

        return toMap.size() > 0 ? toMap : null
    }

    private def convertFieldValue(String fieldName, Map fieldProperties, def fromFieldValue) {

        log.debug "convert field $fieldName with type ${ fieldProperties.data_type }"

        switch (fieldProperties.data_type) {

            case 'base64':
            case 'String':

                String toValue = StringUtils.trimToNull(fromFieldValue?.text())
                return toValue

            case 'Datetime':

                String toValue = StringUtils.trimToNull(fromFieldValue?.text())
                if (!toValue) {
                    return toValue
                }
                Date toDate = dateFormat.parse(toValue)
                return JsonOutput.toJson(toDate)

            case 'Integer':

                String toValue = StringUtils.trimToNull(fromFieldValue?.text())
                return toValue ? toValue.toInteger() : null

            case 'BigDecimal':

                String toValue = StringUtils.trimToNull(fromFieldValue?.text())
                return toValue ? toValue.toBigDecimal() : null

            case 'Boolean':

                String toValue = StringUtils.trimToNull(fromFieldValue?.text())
                return toValue ? toValue.toBoolean() : null

            case 'List':
            case 'AttachmentList':

                List toFieldValue = fromFieldValue.collect {
                    if (fieldProperties.data_type == 'AttachmentList') {
                        convertAttachmentList(fieldName, fieldProperties.list_properties, it)
                    } else {
                        convertFieldValue(fieldName, fieldProperties.list_properties, it)
                    }
                }
                return toFieldValue.size() > 0 ? toFieldValue : null

            case 'Attachment':

                return convertAttachment(fieldName, fieldProperties.list_properties, fromFieldValue)

            case 'Map':

                return convertMap(fromFieldValue, fieldProperties.fields)
        }

        return null
    }

    private Map<String, Map<String, Object>> convertAttachmentList(String fieldName, Map fieldProperties, def fromResourceValue) {

        Map<String, Object> toResourceValue = convertFieldValue(fieldName, fieldProperties, fromResourceValue)

        String attachmentId = toResourceValue?.'resource-attributes'?.'file-name'
        if (!attachmentId) {
            attachmentId = UUID.randomUUID().toString()
        }

        // remove the mime, is not useful for the user
        String attachmentMime = toResourceValue.remove('mime')

        // remove the resource data!
        String attachmentEncodedData = StringUtils.remove(toResourceValue.remove('data'), '\n')

        // hash for replace in the content
        String attachmentDataHash = md5Hash(attachmentEncodedData.decodeBase64())

        attachments.add([id: attachmentId, data: attachmentEncodedData, mime: attachmentMime])

        // metadata to receive the attachment
        Map attachmentMetaData = [id: attachmentId, hash: attachmentDataHash, mime: attachmentMime]

        if (toResourceValue.width) {
            attachmentMetaData.put 'width', toResourceValue.remove('width')
        }

        if (toResourceValue.height) {
            attachmentMetaData.put 'height', toResourceValue.remove('height')
        }

        toResourceValue.put('attachment', attachmentMetaData)

        return toResourceValue
    }

    private Map<String, Map<String, Object>> convertAttachment(String fieldName, Map fieldProperties, def fromResourceValue) {

        String attachmentData = convertFieldValue(fieldName, fieldProperties, fromResourceValue)

        String attachmentId = UUID.randomUUID().toString()
        String attachmentMime = ContentType.HTML.toString()

        String attachmentEncodedData = attachmentData.bytes.encodeBase64()

        attachments.add([id: attachmentId, data: attachmentEncodedData, mime: attachmentMime])

        // metadata to receive the attachment
        return ['attachment': [id: attachmentId, mime: attachmentMime]]
    }

    private convertContentToAttachment(def content) {
        //String content =

    }

    private def md5Hash = { byte[] data ->
        MessageDigest digest = MessageDigest.getInstance('MD5')
        digest.update(data)
        BigInteger big = new BigInteger(1, digest.digest())
        return big.toString(16)
    }

    private importName = { String fieldName, Map fieldProperties ->
        fieldProperties.containsKey('import_name') ? fieldProperties['import_name'] : fieldName
    }
}
