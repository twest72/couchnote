package de.aonnet.couchnote

import de.aonnet.gcouch.GroovyCouchDb
import org.junit.Before
import org.junit.Test

class CouchNoteSetupTest {

    @Test
    void testConfig() {

        assert TestConfig.CONFIG
        assert TestConfig.CONFIG.couchdb
        assert TestConfig.CONFIG.couchdb.connection == [host: TestConfig.HOST, dbName: TestConfig.TEST_DB]
        assert TestConfig.CONFIG.couchdb.clean

        assert TestConfig.CONFIG.typedef
        assert TestConfig.CONFIG.typedef.simplenote == [
                type: 'TYPEDEF',
                type_name: 'SIMPLENOTE',
                fields: [
                        title: [data_type: 'String', sort: true, required: true],
                        description: [data_type: 'String']
                ],
                sort_order: ['title']
        ]
    }

    @Test
    void testPrepareDb() {
        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()
    }

    @Test
    void testImportData() {
        CouchNoteSetup setup = new CouchNoteSetup(TestConfig.CONFIG)
        setup.prepareDb()

        String file = 'src/test/resources/VMwarevFabricDataDirector-Funktionen.enex'
        //String file = '/home/westphal/tmp/evernote/20120417.enex'
        new File(file).withInputStream { InputStream from ->
            setup.importData('note', from)
        }
    }

    @Before
    void prepareDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB)
        if (couchDb.existsDb()) {
            couchDb.dropDb()
        }
    }
}
