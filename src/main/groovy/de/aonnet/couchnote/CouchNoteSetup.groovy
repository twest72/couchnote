package de.aonnet.couchnote

import de.aonnet.gcouch.CouchDbHelper
import de.aonnet.gcouch.GroovyCouchDb
import groovy.util.logging.Commons

@Commons
class CouchNoteSetup {

    private final def config
    private GroovyCouchDb couchDb

    CouchNoteSetup(def config) {
        this.config = config
        this.connect()
    }

    static void main(String[] args) {

        if (args.length <= 0) {
            log.info 'missing parameter config dir'
            System.exit(0)
        }

        String configDir = args[0]
        log.info "config dir: $configDir"

        def config = new ConfigSlurper().parse(new File("$configDir/config.groovy").toURL())

        CouchNoteSetup couchNoteSetup = new CouchNoteSetup(config)
        couchNoteSetup.prepareDb()
    }

    void prepareDb() {
        cleanUp()
        Map typedefViews = createTypedefViews()
        putViewsIntoCouchDb(typedefViews)
    }

    void importData(String typeName, InputStream fromData) {
        ImportXml importXml = new ImportXml()
        importXml.doImport(config.typedef."$typeName", fromData, couchDb)
    }

    private void connect() {

        assert config.couchdb
        assert config.couchdb.connection
        log.info "connect to couchdb ${config.couchdb.connection}"

        couchDb = new GroovyCouchDb(config.couchdb.connection)
        log.info "couchdb version ${ couchDb.couchDbVersion() }"
    }

    private void cleanUp() {

        if (config.couchdb.clean) {

            log.info 'clean db'
            couchDb.cleanDb()
        } else if (!couchDb.existsDb()) {

            log.info 'create db'
            couchDb.createDb()
        }
    }

    private Map createTypedefViews() {

        Map views = [:]

        assert config.typedef
        log.info "load typedefs into couchdb ${config.typedef}"

        config.typedef.each { String typedefKey, Map typedef ->
            log.info "load typedef $typedefKey into couchdb ${typedef}"
            couchDb.create(typedef)

            views << CouchNoteTypedef.createViews(typedef)
        }

        return views
    }


    private void putViewsIntoCouchDb(Map typedefViews) {

        Map allViews = CouchDbHelper.createViewMap(CouchNoteTypedef.VIEW_ALL_TYPEDEF, CouchNoteTypedef.VIEW_ALL_TYPEDEF_MAP_FUNCTION)
        allViews << typedefViews
        log.info "load views into couchdb ${allViews}"

        couchDb.putViewsIntoCouchDb CouchNote.DESIGN_DOC_ALL, allViews
    }
}
