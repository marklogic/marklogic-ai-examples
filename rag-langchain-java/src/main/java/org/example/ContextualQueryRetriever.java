/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentPage;
import com.marklogic.client.document.DocumentRecord;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.query.QueryManager;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContextualQueryRetriever implements ContentRetriever {

    private final QueryManager queryManager;
    private final GenericDocumentManager documentManager;

    public ContextualQueryRetriever(DatabaseClient client) {
        queryManager = client.newQueryManager();
        documentManager = client.newDocumentManager();
        documentManager.setPageLength(10);
    }

    @Override
    public List<Content> retrieve(Query query) {
        List<String> terms = Stream.of(query.text().split(" "))
            .filter(term -> term.length() > 3 || Character.isUpperCase(term.charAt(0)))
            .collect(Collectors.toList());

        // Add the terms to the user's combined query
        ObjectNode combinedQuery = ((ContextualQuery) query).getCombinedQuery();
        ArrayNode queries = (ArrayNode) combinedQuery.get("query").get("queries");
        ArrayNode textArray = queries.addObject().putObject("text-query").putArray("text");
        terms.forEach(textArray::add);

        DocumentPage page = documentManager.search(
            queryManager.newRawCombinedQueryDefinition(new JacksonHandle(combinedQuery)), 1);

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
