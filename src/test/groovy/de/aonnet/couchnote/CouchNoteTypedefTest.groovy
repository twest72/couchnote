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

    @Test
    void testFindMetaDataSimpleInRoot() {

        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        CouchNoteTypedef couchNoteTypedef = new CouchNoteTypedef(new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB))

        Map<String, Object> metaData = couchNoteTypedef.findFieldMetaData('NOTE', 'title')
        assert metaData.toMapString() == '[data_type:String, required:true, sort:true, minlength:1, maxlength:255]'
    }

    @Test
    void testFindMetaDataMapInRoot() {

        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        CouchNoteTypedef couchNoteTypedef = new CouchNoteTypedef(new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB))

        Map<String, Object> metaData = couchNoteTypedef.findFieldMetaData('NOTE', 'note_attributes')
        assert metaData.toMapString() == '[data_type:Map, required:false, sort:false, import_name:note-attributes, fields:[subject_date:[data_type:Datetime, required:false, sort:true, import_name:subject-date], latitude:[data_type:BigDecimal, required:false, sort:false], longitude:[data_type:BigDecimal, required:false, sort:false], altitude:[data_type:BigDecimal, required:false, sort:false], author:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096], source:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096], source_url:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:source-url], source_application:[data_type:String, required:false, sort:true, minlength:1, maxlength:4096, import_name:source-application]]]'
    }

    @Test
    void testFindMetaDataListInRoot() {

        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        CouchNoteTypedef couchNoteTypedef = new CouchNoteTypedef(new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB))

        Map<String, Object> metaData = couchNoteTypedef.findFieldMetaData('NOTE', 'tag')
        assert metaData.toMapString() == '[data_type:List, list_properties:[data_type:String, sort:true, minlength:1, maxlength:100]]'
    }

    @Test
    void testFindMetaDataInMap() {

        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        CouchNoteTypedef couchNoteTypedef = new CouchNoteTypedef(new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB))

        Map<String, Object> metaData = couchNoteTypedef.findFieldMetaData('NOTE', 'latitude')
        assert metaData.toMapString() == '[data_type:BigDecimal, required:false, sort:false]'
    }

    @Test
    void testTransformTypeInstance() {

        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        CouchNoteTypedef couchNoteTypedef = new CouchNoteTypedef(new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB))


        Map<String, Object> value = [
                _id: '9b4e35ee261b19293cb55dd85b032a5b',
                _rev: '1-129b0db122f9576a2ce49b7ef4a8e455',
                type: 'NOTE',
                imported: '2012-04-27T11:45:16+0000',
                title: '10 LESS CSS Examples You Should Steal for Your Projects',
                content: [
                        attachment: [id: 'e6485f1d-feff-4354-ab08-1c442bc231a6', mime: 'text/html']
                ],
                created: '2012-03-15T08:16:27+0000',
                updated: '2012-03-15T08:18:18+0000',
                tag: ['Work', 'Web', 'CSS'],
                note_attributes: [
                        source: 'web.clip',
                        source_url: 'http://designshack.net/articles/css/10-less-css-examples-you-should-steal-for-your-projects/'
                ],
                resource: [
                        [attachment: [id: '3617810b-4285-4c58-a474-a4a55bd01b92', hash: 'efe6cdc70fdbfd5ee325db83228b1b6', mime: 'image/png', width: 75, height: 75]],
                        [attachment: [id: 'e0164070-68ac-4c1b-925a-806faccbaf1d', hash: 'fc4cad962c3038c8c76b3de6f3adf3e', mime: 'image/jpeg', width: 75, height: 75]]],
                _attachments: [
                        'e6485f1d-feff-4354-ab08-1c442bc231a6': [content_type: 'text/html', revpos: 1, length: 572969, stub: true],
                        '3617810b-4285-4c58-a474-a4a55bd01b92': [content_type: 'image/png', revpos: 1, length: 7575, stub: true],
                        'e0164070-68ac-4c1b-925a-806faccbaf1d': [content_type: 'image/jpeg', revpos: 1, length: 2862, stub: true]
                ]]

        Map<String, Object> expectedValue = value.clone()
        expectedValue.content.attachment = value.content.attachment.clone()
        expectedValue.content.attachment.link = 'http://localhost:5984/unittest/9b4e35ee261b19293cb55dd85b032a5b/e6485f1d-feff-4354-ab08-1c442bc231a6'

        Map<String, Object> newValue = couchNoteTypedef.transformTypeInstance('NOTE', value._id, value)

        assert newValue.toMapString() == expectedValue.toMapString()

    }

    @Before
    void prepareDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB)
        if (couchDb.existsDb()) {
            couchDb.dropDb()
        }
    }
}
