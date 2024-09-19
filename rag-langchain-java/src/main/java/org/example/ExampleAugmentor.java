/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;

/**
 * Overrides the default augmentor by letting us use a transformer to support the existence of a combined query.
 */
public class ExampleAugmentor extends DefaultRetrievalAugmentor {

    public ExampleAugmentor(ContentRetriever contentRetriever) {
        super(
            new CombinedQueryTransformer(),
            new DefaultQueryRouter(contentRetriever),
            null, null, null
        );
    }
}
