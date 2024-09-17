/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */

public class ImportEventsArchive {

    public static void main(String[] args) {
        String connectionString = args[0];
        com.marklogic.flux.api.Flux.importArchiveFiles()
                .from("data/events-with-embeddings.zip")
                .connectionString(connectionString)
                .to(options -> options.permissionsString("rest-reader,read,rest-writer,update"))
                .execute();
    }
}
