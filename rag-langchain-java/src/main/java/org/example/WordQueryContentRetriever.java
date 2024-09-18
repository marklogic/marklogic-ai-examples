/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Does a simple word query, nothing more yet.
 */
public class WordQueryContentRetriever implements ContentRetriever {

    private final QueryManager queryManager;
    private final GenericDocumentManager documentManager;
    private final StructuredQueryBuilder queryBuilder;

    public WordQueryContentRetriever(DatabaseClient client) {
        queryManager = client.newQueryManager();
        queryBuilder = queryManager.newStructuredQueryBuilder();
        documentManager = client.newDocumentManager();
        documentManager.setPageLength(10);
    }

    @Override
    public List<Content> retrieve(Query query) {
        List<String> terms = Stream.of(query.text().split(" "))
            .filter(term -> term.length() > 3 || Character.isUpperCase(term.charAt(0)))
            .collect(Collectors.toList());

        StructuredQueryDefinition structuredQuery = queryBuilder.and(
            queryBuilder.term(terms.toArray(new String[]{})),
            queryBuilder.collection("events")
        );

        DocumentPage page = documentManager.search(structuredQuery, 1);

        List<Content> results = new ArrayList<>();
        while (page.hasNext()) {
            DocumentRecord record = page.next();
            JsonNode doc = record.getContent(new JacksonHandle()).get();
            String transcript = doc.get("transcript").asText();
            Metadata metadata = new Metadata();
            metadata.put("uri", record.getUri());
            Content content = new Content(new TextSegment(transcript, metadata));
            results.add(content);
        }
        return results;
    }
}
