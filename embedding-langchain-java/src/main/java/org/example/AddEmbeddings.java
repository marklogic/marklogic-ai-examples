/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.ExportListener;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.document.*;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.JacksonHandle;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.function.Consumer;

public class AddEmbeddings {

    private static EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    public static void main(String[] args) {
        DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8003,
            new DatabaseClientFactory.DigestAuthContext("ai-examples-user", "password"));

        DataMovementManager dataMovementManager = client.newDataMovementManager();
        QueryBatcher queryBatcher = dataMovementManager
            .newQueryBatcher(client.newQueryManager().newStructuredQueryBuilder().collection("enron-chunk"))
            .withThreadCount(16)
            .withBatchSize(100)
            .onUrisReady(new ExportListener()
                // Need to retrieve all metadata so the document can be overwritten.
                .withMetadataCategory(DocumentManager.Metadata.ALL)
                .withNonDocumentFormat(Format.XML)
                .onDocumentPageReady(new DocumentEmbedder(client))
                .onFailure(((batch, throwable) -> throwable.printStackTrace()))
            );

        dataMovementManager.startJob(queryBatcher);
        queryBatcher.awaitCompletion();
        dataMovementManager.stopJob(queryBatcher);
    }

    private static class DocumentEmbedder implements Consumer<DocumentPage> {

        private JSONDocumentManager documentManager;

        public DocumentEmbedder(DatabaseClient databaseClient) {
            this.documentManager = databaseClient.newJSONDocumentManager();
        }

        @Override
        public void accept(DocumentPage page) {
            DocumentWriteSet writeSet = documentManager.newWriteSet();
            DocumentRecord rec = page.next();
            ObjectNode doc = (ObjectNode) rec.getContent(new JacksonHandle()).get();
            Response<Embedding> response = embeddingModel.embed(doc.get("text").asText());
            ArrayNode array = doc.putArray("embedding");
            for (float v : response.content().vector()) {
                array.add(v);
            }
            DocumentMetadataHandle metadata = rec.getMetadata(new DocumentMetadataHandle());
            metadata.getCollections().add("enron-chunk-with-embedding");
            writeSet.add(rec.getUri(), metadata, new JacksonHandle(doc));
            documentManager.write(writeSet);
        }
    }
}
