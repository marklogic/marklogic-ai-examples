---
layout: default
title: Overview
nav_order: 1
---

This repository contains a set of examples for demonstrating common AI use cases for applications built on top of
MarkLogic. These examples are intended to serve as a starting point for your own applications; you are encouraged to
copy and modify the code as needed.

The examples in this repository depend on the
[Azure OpenAI Service](https://azure.microsoft.com/en-us/products/ai-services/openai-service). They can be easily
tailored to work with any LLM supported by the LLM framework used by each example. Note though that if you wish to
execute these examples as-is, you will need an Azure OpenAI account and API key.

## Setup

If you would like to try out the example programs, please [follow these instructions](setup/README.md).

## RAG Examples

MarkLogic excels at supporting RAG, or ["Retrieval-Augmented Generation"](https://python.langchain.com/docs/tutorials/rag/),
via its schema-agnostic nature as well as it's powerful and flexible indexing. This repository contains the following
examples of RAG with MarkLogic:

- The [rag-langchain-python](rag-langchain-python/README.md) project demonstrates RAG with Python, langchain, and MarkLogic.
- The [rag-langchain-java](rag-langchain-java/README.md) project demonstrates RAG with Java, langchain4j, and MarkLogic.
- The [rag-langchain-js](rag-langchain-js/README.md) project demonstrates RAG with JavaScript, langchain.js, and MarkLogic.

## Splitting / Chunking Examples

A RAG approach typically benefits from sending multiple smaller segments or "chunks" of text to an LLM. Please
see [this guide on splitting documents](splitting-langchain-java/README.md) for more information on how to split
your documents and why you may wish to do so.

## Embedding examples

To utilize the vector queries shown in the RAG Examples listed above, embeddings - vector representations of text -
should be added to your documents in MarkLogic.
See [this guide on adding embeddings](embedding-langchain-java/README.md) for more information. 

