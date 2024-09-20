/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.ExportListener;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;
import java.util.function.Consumer;

public class SplitToSameDocument {

    public static void main(String[] args) {
        DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8003,
            new DatabaseClientFactory.DigestAuthContext("ai-examples-user", "password"));

        DataMovementManager dataMovementManager = client.newDataMovementManager();
        QueryBatcher queryBatcher = dataMovementManager.newQueryBatcher(
                client.newQueryManager().newStructuredQueryBuilder().collection("enron")
            )
            .withThreadCount(16)
            .withBatchSize(100)
            .onUrisReady(new ExportListener()
                .onDocumentPageReady(new DocumentsConsumer(client))
                .onFailure((batch, throwable) -> throwable.printStackTrace())
            );

        dataMovementManager.startJob(queryBatcher);
        queryBatcher.awaitCompletion();
        dataMovementManager.stopJob(queryBatcher);
    }

    private static class DocumentsConsumer implements Consumer<DocumentPage> {

        private final DocumentSplitter splitter = DocumentSplitters.recursive(1000, 100);
        private final JSONDocumentManager documentManager;
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final DocumentMetadataHandle metadata = new DocumentMetadataHandle()
            .withCollections("enron-chunks-aggregated")
            .withPermission("ai-examples-role", DocumentMetadataHandle.Capability.READ, DocumentMetadataHandle.Capability.UPDATE);

        DocumentsConsumer(DatabaseClient client) {
            documentManager = client.newJSONDocumentManager();
        }

        @Override
        public void accept(DocumentPage documentRecords) {
            DocumentWriteSet writeSet = documentManager.newWriteSet();
            while (documentRecords.hasNext()) {
                DocumentRecord documentRecord = documentRecords.next();
                ObjectNode doc = buildDocumentWithAllChunks(documentRecord);
                int chunkCount = doc.get("chunks").size();
                String uri = String.format("%s-chunks-%d.json", documentRecord.getUri(), chunkCount);
                writeSet.add(uri, metadata, new JacksonHandle(doc));
            }
            documentManager.write(writeSet);
            System.out.println("Wrote " + writeSet.size() + " documents");
        }

        private ObjectNode buildDocumentWithAllChunks(DocumentRecord documentRecord) {
            String text = documentRecord.getContent(new StringHandle()).get();
            List<TextSegment> segments = splitter.split(new Document(text));
            ObjectNode doc = objectMapper.createObjectNode();
            doc.put("sourceUri", documentRecord.getUri());
            ArrayNode chunks = doc.putArray("chunks");
            for (int i = 0; i < segments.size(); i++) {
                ObjectNode chunk = chunks.addObject();
                chunk.put("text", segments.get(i).text());
            }
            return doc;
        }
    }
}
