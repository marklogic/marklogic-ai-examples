/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package org.example;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;

/**
 * See https://docs.langchain4j.dev/tutorials/rag for more information on RAG assistants.
 */
public interface Assistant {

    @SystemMessage("You are an assistant for question-answering tasks. " +
        "Use the following pieces of retrieved context to answer the question. " +
        "If you don't know the answer, just say that you don't know. " +
        "Use six sentences maximum. ")
    Result<String> chat(String question);
}
