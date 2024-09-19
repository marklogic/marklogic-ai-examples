/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;

import java.util.Collection;
import java.util.Collections;

/**
 * An example of transforming the query text into a different object - in this case, an instance of
 * {@code ContextualQuery} that gives our retriever access to the user's combined query.
 */
public class CombinedQueryTransformer implements QueryTransformer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Collection<Query> transform(Query query) {
        try {
            JsonNode request = objectMapper.readTree(query.text());
            ContextualQuery cq = new ContextualQuery(
                request.get("question").asText(),
                (ObjectNode) request.get("combinedQuery")
            );
            return Collections.singletonList(cq);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
