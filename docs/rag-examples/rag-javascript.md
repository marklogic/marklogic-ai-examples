---
layout: default
title: RAG with LangChain.js
parent: RAG Examples
nav_order: 3
---

[Retrieval Augmented Generation (RAG)](https://docs.langchain4j.dev/tutorials/rag) can be implemented in JavaScript with
[LangChain.js](https://js.langchain.com/docs/introduction/) and MarkLogic via a "retriever". The example in this
directory demonstrates one kind of retriever that you can consider for your own AI application.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Setup

The only system requirements for running these examples are Node 18.x, 19.x, or 20.x
(see [LangChain.js Installation](https://js.langchain.com/v0.1/docs/get_started/installation/)) and npm.
Minimum versions of npm are dependent on the version of Node.
See [Node Releases](https://nodejs.org/en/about/previous-releases#looking-for-latest-release-of-a-version-branch)
for more information.

For this LangChain.js example, in addition to the environment variables in the `.env` file described in the 
[setup guide](../setup.md), you'll also need to add the `AZURE_OPENAI_API_INSTANCE_NAME` setting to the `.env` file.

```
OPENAI_API_VERSION=2023-12-01-preview
AZURE_OPENAI_ENDPOINT=<Your Azure OpenAI endpoint>
AZURE_OPENAI_API_KEY=<Your Azure OpenAI API key>
AZURE_LLM_DEPLOYMENT_NAME=<The name of an LLM deployment in your Azure OpenAI service>
AZURE_OPENAI_API_INSTANCE_NAME=<The host name of your Azure OpenAI endpoint>
```

Once Node and npm are installed, and your environment is configured, you can run the examples via an IDE such as Visual
Code or IntelliJ. To try these examples, you should first use npm to install the required Node modules:
```
cd rag-langchain-js
npm install
```

## RAG with a simple word query

A key feature of MarkLogic is its ability to index all text in a document during ingest. Thus, a simple approach to RAG
with MarkLogic is to select documents based on the words in a user's question.

To demonstrate this, you can run the `askWordQuery.js` module with any question. The module uses a custom LangChain.js
retriever that selects documents in the `ai-examples-content` MarkLogic database containing one or more of the words
in the given question. It then includes the top 10 most relevant documents in the request that it sends to Azure OpenAI.
For example:

```
node askWordQuery.js "What disturbances has Jane Doe caused?" 
```

Running this will yield an answer similar to the below (the answer can vary based on the LLM in use and the nature
of the configured deployment model):

> Jane Doe has caused disturbances such as public intoxication, yelling and banging on doors and windows, smashing
> windows and graffiti, and disturbing the peace in a residential area. Possible motives for her behavior include
> personal vendettas, revenge, coping mechanisms, and stealing intellectual property.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-js/wordQueryRetriever.js).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-js/askWordQuery.js) that uses the retriever.

## RAG with a contextual query

In many applications built on MarkLogic, a user will search the documents in a database by leveraging a variety of
indexes in MarkLogic, such as the universal text index, date range indexes, and geospatial indexes. This query - which
can feature any of the many dozens of different query functions supported by MarkLogic - is referred to as a
"contextual query" - it captures the user's context in terms of what documents they are interested in. A RAG approach
can then account for both this contextual query and a user's question by enhancing the contextual query with a word
query based on the words in a user's question.

The `askContextualQuery.js` module demonstrates this approach by defining a simple contextual query that only
selects documents containing a JSON property named `type` with a value of `public intoxication`.
Try running the following:

```
node askContextualQuery.js "What disturbances has Jane Doe caused?" 
```

The answer will be similar to the one below. You can see how the results are based only on documents involving public
intoxication as opposed to the entire set of fictional crime events:

> Jane Doe (aka Jane Johnson and Jane Smith) has caused disturbances including vehicle break-ins, shoplifting, assault,
> possible looting, and vandalism. It seems that her motives vary, from looking for something specific to owing money to
> causing trouble. She has been described as a tall woman with blonde hair, often wearing black hoodies and jeans.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-js/contextualQueryRetriever.js).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-js/askContextualQuery.js) that uses the retriever.

## RAG with a vector query

MarkLogic 12 has
[new support for generative AI capabilities](https://investors.progress.com/news-releases/news-release-details/progress-announces-powerful-new-generative-ai-capabilities)
via a set of [vector operations](https://docs.marklogic.com/12.0/vec/vector-operations). With this approach,
documents are first selected in a manner similar to the approaches shown above - by leveraging the powerful and flexible
set of indexes that have long been available in MarkLogic. The documents are then further filtered and sorted via
the following process:

1. An embedding of the user's question is generated using [LangChain.js and Azure OpenAI](https://python.langchain.com/docs/integrations/text_embedding/).
2. Using MarkLogic's new vector API, the generated embedding is compared against the embeddings in each
   selected crime event document to generate a similarity score for each document.
3. The documents with the highest similarity scores are sent to the LLM to augment the user's question.

To try the `askVectorQuery.js` module, you will need to have installed MarkLogic 12 and also have defined
`AZURE_EMBEDDING_DEPLOYMENT_NAME` in your `.env` file. Please see the
[setup guide](../setup.md) for more information.

You can now run `askVectorQuery.js`:
```
node askVectorQuery.js "What disturbances has Jane Doe caused?"
```

An example result is shown below:

> Jane Doe has caused disturbances at various locations, including shouting, banging on doors and windows, screaming and
> yelling, and vandalism. The motive for her behavior is unclear, but it may be related to personal issues or mental
> health challenges. Witnesses have described Jane Doe as a tall woman with long blonde or brown hair, wearing a bright
> or dark-colored dress, and appearing agitated or upset."

The results are similar but slightly different to the results shown above for a simple word query. You can compare
the document URIs printed by each program to see that a different set of document is selected by each approach.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-js/vectorQueryRetriever.js).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-js/askVectorQuery.js) that uses the retriever.

For an example of how to add embeddings to your data, please see [this embeddings example](../embedding.md).


## Summary

The three RAG approaches shown above - a simple word query, a contextual query, and a vector query - demonstrate how
easily data can be queried and retrieved from MarkLogic using LangChain.js. Identifying the optimal approach for your own
data will require testing the approaches you choose and possibly leveraging additional MarkLogic indexes and/or
further enriching your data.
