---
layout: default
title: Embedding Examples
nav_order: 5
---

The vector queries shown in the [langchain](../rag-langchain-python/README.md),
[langchain4j](../rag-langchain-java), and [langchain.js](../rag-langchain-js/README.md) RAG examples
depend on embeddings - vector representations of text - being added to documents in MarkLogic. Vector queries can
then be implemented using [the new vector functions](https://docs.marklogic.com/12.0/js/vec) in MarkLogic 12.
This project demonstrates the use of a
[langchain4j in-process embedding model](https://docs.langchain4j.dev/integrations/embedding-models/in-process) and
the [MarkLogic Data Movement SDK](https://docs.marklogic.com/guide/java/data-movement) for adding embeddings to
documents in MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Setup

This example depends both on the [main setup for all examples](../setup/README.md) and also on having run the
"Split to multiple documents" example program in the
[document splitting examples](../splitting-langchain-java/README.md). That example program used langchain4j to split
the text in Enron email documents and write each chunk of text to a separate document. This example will then use
langchain4j to generate an embedding for the chunk of text and add it to each chunk document.

## Adding embedding to documents

To try the embedding example, run the following Gradle task:

    ../gradlew addEmbeddings

After the task completes, each document in the `enron-chunk` collection will now have an `embedding` field
consisting of an array of floating point numbers. Each document will also have been added to the
`enron-chunk-with-embedding` collection.

As a next step, you would likely create a [MarkLogic TDE view](https://docs.marklogic.com/guide/app-dev/TDE) that
allows you to use the [MarkLogic Optic API](https://docs.marklogic.com/guide/app-dev/OpticAPI) for querying for rows
with similar embeddings. This is the exact approach used in the vector queries for each of the RAG examples mentioned
above. Your TDE could look like the one shown below. Note that the value of `dimension` for the `embedding` column
must match that of the embedding model that you used. In this example, the langchain4j in-process embedding model
requires a value of 384 for the `dimension` column.

```
{
  "template": {
    "context": "/",
    "collections": [
      "enron-chunk-with-embedding"
    ],
    "rows": [
      {
        "schemaName": "example",
        "viewName": "enronChunk",
        "columns": [
          {
            "name": "uri",
            "scalarType": "string",
            "val": "sourceUri"
          },
          {
            "name": "embedding",
            "scalarType": "vector",
            "val": "vec:vector(embedding)",
            "dimension": "384",
            "invalidValues": "reject"
          },
          {
            "name": "text",
            "scalarType": "string",
            "val": "text"
          }
        ]
      }
    ]
  }
}
```

When performing a vector query with MarkLogic, you need to ensure that the embedding that you compare to the values
in the `vector` column defined in your TDE have the same dimension value. Otherwise, MarkLogic will throw a
`XDMP-DIMMISMATCH` error. For example, since an in-process langchain4j embedding model is used in this example program,
you would want to use the same embedding model to generate an embedding of a user's chat question. If you wished to
use an [Azure OpenAI embedding model](https://docs.langchain4j.dev/integrations/embedding-models/azure-open-ai)
in the above example program, you would then need to use the same embedding model when generating an embedding of a
user's chat question.
