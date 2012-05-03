couchdb {
    connection = [host: 'localhost', dbName: 'couchnote']
    clean = true
}

typedef {
    simplenote = [
            type: 'TYPEDEF',
            type_name: 'SIMPLENOTE',
            fields: [
                    title: [data_type: 'String', sort: true, required: true],
                    description: [data_type: 'String']
            ],
            sort_order: ['title']
    ]

    // Evernote Note Export Format "http://xml.evernote.com/pub/evernote-export.dtd"

    // one or more digits separated by periods: Version
    // date and time information. ISO 8601 date format: Datetime

    // without top level element!
    // en-export with attr: export-date (Datetime), application, version (Version)

    //Corresponds to EDAM Note type
    note = [
            type: 'TYPEDEF',
            type_name: 'NOTE',
            fields: [

                    // Corresponds to Note.title field.
                    // May not begin or end with whitespace, may not contain line endings or
                    // Unicode control characters.  Must be between 1 and 255 characters.
                    title: [data_type: 'String', required: true, sort: true, minlength: 1, maxlength: 255],

                    // Corresponds to Note.content field.
                    // May not be longer than 5242880 Unicode characters.
                    // The contents of this character block must be a valid ENML document, which
                    // must be validated against the ENML DTD upon import:
                    //   http://xml.evernote.com/pub/enml.dtd
                    content: [data_type: 'Attachment', list_properties: [
                            data_type: 'String', required: true, sort: false, minlength: 1, maxlength: 5242880
                    ]],

                    imported: [data_type: 'Datetime', required: false, sort: true],

                    // Corresponds to the Note.created field.
                    // Must contain a valid date and time, if present.
                    created: [data_type: 'Datetime', required: false, sort: true],

                    // Corresponds to the Note.updated field.
                    // Must contain a valid date and time, if present.
                    updated: [data_type: 'Datetime', required: false, sort: true],

                    // Corresponds to the Tag.name field for one of the tags on the note.
                    // May not begin or end with whitespace, may not contain line endings, commas
                    // or Unicode control characters.  Must be between 1 and 100 characters.
                    tag: [data_type: 'List', list_properties: [
                            data_type: 'String', sort: true, minlength: 1, maxlength: 100
                    ]],

                    // Corresponds to the Note.attributes field, and NoteAttributes type.
                    note_attributes: [data_type: 'Map', required: false, sort: false, import_name: 'note-attributes', fields: [

                            // Corresponds to the NoteAttributes.subjectDate field.
                            // Must contain a valid date and time, if present.
                            subject_date: [data_type: 'Datetime', required: false, sort: true, import_name: 'subject-date'],

                            // Corresponds to the NoteAttributes.latitude or
                            // ResourceAttributes.latitude field.
                            // Must be encoded as a single decimal number.
                            latitude: [data_type: 'BigDecimal', required: false, sort: false],

                            // Corresponds to the NoteAttributes.longitude or
                            // ResourceAttributes.longitude field.
                            // Must be encoded as a single decimal number.
                            longitude: [data_type: 'BigDecimal', required: false, sort: false],

                            // Corresponds to the NoteAttributes.altitude or
                            // ResourceAttributes.altitude field.
                            // Must be encoded as a single decimal number.
                            altitude: [data_type: 'BigDecimal', required: false, sort: false],

                            // Corresponds to the NoteAttributes.author field.
                            // Must be between 1 and 4096 characters.
                            author: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096],

                            // Corresponds to the NoteAttributes.source field.
                            // Must be between 1 and 4096 characters.
                            source: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096],

                            // Corresponds to the NoteAttributes.sourceURL or
                            // ResourceAttributes.sourceURL field.
                            // Must be between 1 and 4096 characters, and must contain a valid Internet
                            // URL (e.g. starting with "http" or "https".)
                            source_url: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096, import_name: 'source-url'],

                            // Corresponds to the NoteAttributes.sourceApplication field.
                            // Must be between 1 and 4096 characters.
                            source_application: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096, import_name: 'source-application']
                    ]],

                    // Corresponds to the EDAM Resource type.
                    resource: [data_type: 'AttachmentList', list_properties: [
                            data_type: 'Map', sort: false, fields: [

                            // Corresponds to the Resource.data field.
                            // The binary body of the resource must be encoded into Base-64 format.  The
                            // encoding may contain whitespace (e.g. to break into lines), or may be
                            // continuous without break.  Total length of the original binary body may not
                            // exceed 25MB.
                            data: [data_type: 'base64', required: true, sort: false],

                            // Corresponds to the Resource.mime field.
                            // Must contain one of the permitted MIME types:
                            //    image/gif
                            //    image/jpeg
                            //    image/png
                            //    audio/wav
                            //    audio/mpeg
                            //    application/pdf
                            //    application/vnd.evernote.ink
                            mime: [data_type: 'String', required: true, sort: false, maxlength: 100],

                            // Corresponds to the Resource.width field.
                            // If present, it must contain a positive integer.
                            width: [data_type: 'Integer', required: false, sort: false, min: 0],

                            // Corresponds to the Resource.height field.
                            // If present, it must contain a positive integer.
                            height: [data_type: 'Integer', required: false, sort: false, min: 0],

                            // Corresponds to the Resource.duration field.
                            // If present, it must contain a positive integer.
                            // TODO not now: duration: [data_type: 'Integer', required: false, sort: false, min: 0],

                            // Corresponds to the Resource.recognition field.
                            // If present, it must contain a valid recoIndex document, and it may be
                            // validated against the recoIndex DTD:
                            //   http://xml.evernote.com/pub/recoIndex.dtd
                            // recognition?,

                            // Corresponds to the Resource.alternateData field.
                            // The binary body of the resource's alternate representation must be encoded
                            // into Base-64 format.  The encoding may contain whitespace (e.g. to break into
                            // lines), or may be continuous without break.
                            // TODO not now: alternate_data: [data_type: 'base64', required: true, sort: false, import_name:'alternate-data'],

                            // Corresponds to the Resource.attributes field, and ResourceAttributes type.
                            resource_attributes: [data_type: 'Map', sort: false, import_name: 'resource-attributes', fields: [

                                    // Corresponds to the NoteAttributes.sourceURL or
                                    // ResourceAttributes.sourceURL field.
                                    // Must be between 1 and 4096 characters, and must contain a valid Internet
                                    // URL (e.g. starting with "http" or "https".)
                                    source_url: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096, import_name: 'source-url'],

                                    // Corresponds to the ResourceAttributes.timestamp field.
                                    // Must contain a valid date and time, if present.
                                    timestamp: [data_type: 'Datetime', required: false, sort: true],

                                    // Corresponds to the NoteAttributes.latitude or
                                    // ResourceAttributes.latitude field.
                                    // Must be encoded as a single decimal number.
                                    latitude: [data_type: 'BigDecimal', required: false, sort: false],

                                    // Corresponds to the NoteAttributes.longitude or
                                    // ResourceAttributes.longitude field.
                                    // Must be encoded as a single decimal number.
                                    longitude: [data_type: 'BigDecimal', required: false, sort: false],

                                    // Corresponds to the NoteAttributes.altitude or
                                    // ResourceAttributes.altitude field.
                                    // Must be encoded as a single decimal number.
                                    altitude: [data_type: 'BigDecimal', required: false, sort: false],

                                    // Corresponds to the ResourceAttributes.cameraMake field.
                                    // Must be between 1 and 4096 characters.
                                    camera_make: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096, import_name: 'camera-make'],

                                    // Corresponds to the ResourceAttributes.cameraModel field.
                                    // Must be between 1 and 4096 characters.
                                    camera_model: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096, import_name: 'camera-model'],

                                    // Corresponds to the ResourceAttributes.recoType field.
                                    // Must be between 1 and 4096 characters.
                                    reco_type: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096, import_name: 'reco-type'],

                                    // Corresponds to the ResourceAttributes.fileName field.
                                    // Must be between 1 and 4096 characters.
                                    file_name: [data_type: 'String', required: false, sort: true, minlength: 1, maxlength: 4096, import_name: 'file-name'],

                                    // Corresponds to the ResourceAttributes.attachment field.
                                    // Should be 'true' or 'false'.
                                    attachment: [data_type: 'Boolean', required: false, sort: true]
                            ]]
                    ]]
                    ]],
            sort_order: ['title', 'created', 'updated']
    ]
}
