/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.rag.query.Query;

/**
 * Extends the langchain4j {@code Query} class to allow for a MarkLogic combined query, represented as JSON, to be
 * included by the caller.
 */
class ContextualQuery extends Query {

    private final ObjectNode combinedQuery;

    public ContextualQuery(String text, ObjectNode combinedQuery) {
        super(text);
        this.combinedQuery = combinedQuery != null ? combinedQuery.deepCopy() : null;
    }

    public ObjectNode getCombinedQuery() {
        return combinedQuery;
    }
}
