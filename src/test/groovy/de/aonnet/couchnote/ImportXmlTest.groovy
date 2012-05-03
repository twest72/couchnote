package de.aonnet.couchnote

import de.aonnet.gcouch.GroovyCouchDb
import org.junit.Before
import org.junit.Test

class ImportXmlTest {

    @Test
    void testExtractXml() {

        String file = 'src/test/resources/VMwarevFabricDataDirector-Funktionen.enex'
        GroovyCouchDb toDb = new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB)
        //String file = '/home/westphal/tmp/evernote/20120417.enex'
        //GroovyCouchDb toDb = new GroovyCouchDb(host: TestConfig.HOST, dbName: 'couchnote')

        ImportXml importXml = new ImportXml()

        new File(file).withInputStream { InputStream from ->
            importXml.doImport(TestConfig.CONFIG.typedef.note, from, toDb)
        }
    }

    @Before
    void prepareDb() {
        GroovyCouchDb couchDb = new GroovyCouchDb(host: TestConfig.HOST, dbName: TestConfig.TEST_DB)
        couchDb.cleanDb()
    }
}
