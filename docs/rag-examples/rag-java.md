---
layout: default
title: RAG with langchain4j
parent: RAG Examples
nav_order: 2
---

[Retrieval Augmented Generation (RAG)](https://docs.langchain4j.dev/tutorials/rag) can be implemented in Java with
[langchain4j](https://docs.langchain4j.dev/intro) and MarkLogic via a "retriever". The examples in this
directory demonstrate three different kinds of retrievers that you can consider for your own AI application.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Setup

The only system requirement for running these examples is Java 8 or higher. You can run the examples via an IDE such as
Visual Code or IntelliJ. You can also use [Gradle](https://gradle.org/) to run the examples, but you do not
need Gradle installed - this repository uses the [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
to download an appropriate version of Gradle. To start using these examples, you'll need to be in this example
directory. If you are not already in this example directory, run this command:

```
cd rag-langchain-java
```


## RAG with a simple word query

A key feature of MarkLogic is its ability to index all text in a document during ingest. Thus, a simple approach to RAG
with MarkLogic is to select documents based on the words in a user's question.

To demonstrate this, you can run the Gradle `askWordQuery` task with any question. This example program uses a custom
langchain4j retriever that selects documents in the `ai-examples-content` MarkLogic database containing one or more words
in the given question. It then includes the top 10 most relevant documents in the request that it sends to Azure OpenAI.
For example:

    ../gradlew askWordQuery -Pquestion="What disturbances has Jane Doe caused?"

Running this will yield an answer similar to the below (the answer can vary based on the LLM in use and the nature
of the configured deployment model):

> Jane Johnson is a suspect in several incidents, including cybercrime, public intoxication, vandalism, assault,
> looting, and shoplifting. She is described as a Caucasian female in her mid-30s, with blonde hair and blue eyes.
> She is approximately 5'6" and has a slim build. In some incidents, she was wearing a black hoodie and jeans,
> while in others she wore a red coat or a black jacket. The motives for her actions are unclear, but there are
> some speculations that she is struggling financially, dealing with personal issues, or protesting against
> certain actions. The police have been notified and are investigating the incidents.

You can alter the value of the `-Pquestion=` parameter to be any question you wish.

Note as well that if you have tried the [Python LangChain examples](rag-python.md), you will notice
some differences in the results. These differences are primarily due to the different prompts used by LangChain and
langchain4j. See [the langchain4j documentation](https://docs.langchain4j.dev/intro) for more information on prompt
templates when using langchain4j.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-java/src/main/java/org/example/WordQueryRetriever.java).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-java/src/main/java/org/example/AskWordQuery.java) that uses the retriever.


## RAG with a contextual query

In many applications built on MarkLogic, a user will search the documents in a database by leveraging a variety of
indexes in MarkLogic, such as the universal text index, date range indexes, and geospatial indexes. This query - which
can feature any of the many dozens of different query functions supported by MarkLogic - is referred to as a
"contextual query" - it captures the user's context in terms of what documents they are interested in. A RAG approach
can then account for both this contextual query and a user's question by enhancing the contextual query with a word
query based on the words in a user's question.

The `askContextualQuery` Gradle task demonstrates this approach by defining a simple contextual query that only
selects documents containing a JSON property named `type` with a value of `public intoxication`.
Try running the following:

    ../gradlew askContextualQuery -Pquestion="What disturbances has Jane Doe caused?" 

The answer will be similar to the one below. You can see how the results are based only on documents involving public
intoxication as opposed to the entire set of fictional crime events. In addition, due to the equal weighting of the
word query and the combined query, the retriever is likely to pull in documents involving public intoxication but
not involving Jane Doe:

> Regarding Jane Doe, Ashley Frazier reports a public intoxication incident in which the suspect is wearing a
> red dress and has long blonde hair while stumbling around and slurring her words. The suspect is alone and
> causing a disturbance. Ashley's motive for calling 911 is to ensure everyone's safety and to get the suspect
> the help she needs. In the case of John Smith, Christina Mahoney reports a public intoxication incident at
> 754 Main Road in San Francisco. John is stumbling around, slurring his words, and causing a disturbance
> while wearing a red shirt. Christina's motive for calling 911 is to report the incident to the authorities
> as she was worried about John's safety and that of others. Rachel Sandoval reports a public intoxication
> incident involving Jane Smith at 542 Hill Lane in San Francisco. Jane is stumbling around and slurring
> her words and causing a disturbance. Her motive for reporting the incident is also to ensure everyone's safety.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-java/src/main/java/org/example/ContextualQueryRetriever.java).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-java/src/main/java/org/example/AskContextualQuery.java) that uses the retriever.


## RAG with a vector query

MarkLogic 12 has
[new support for generative AI capabilities](https://investors.progress.com/news-releases/news-release-details/progress-announces-powerful-new-generative-ai-capabilities)
via a set of [vector operations](https://docs.marklogic.com/12.0/vec/vector-operations). With this approach,
documents are first selected in a manner similar to the approaches shown above - by leveraging the powerful and flexible
set of indexes that have long been available in MarkLogic. The documents are then further filtered and sorted via
the following process:

1. An embedding of the user's question is generated using [langchain4j and Azure OpenAI](https://docs.langchain4j.dev/integrations/embedding-models/azure-open-ai).
2. Using MarkLogic's new vector API, the generated embedding is compared against the embeddings in each
   selected crime event document to generate a similarity score for each document.
3. The documents with the highest similarity scores are sent to the LLM to augment the user's question.

To try RAG with a vector query, you will need to have installed MarkLogic 12 and also have defined
`AZURE_EMBEDDING_DEPLOYMENT_NAME` in your `.env` file. Please see the
[setup guide](../setup.md) for more information.

You can now run the Gradle `vectorQueryExample` task:

    ../gradlew askVectorQuery -Pquestion="What disturbances has Jane Doe caused?" 

An example result is shown below:

> The retrieved context provides information on multiple disturbances caused by a woman named Jane Doe in various
> locations around San Francisco. The disturbances include vandalism, disturbing the peace, yelling and making a lot
> of noise, cybercrime, public intoxication, and more. The motives behind these disturbances are not entirely clear,
> but some possible reasons include personal disputes, mental instability, and attempts to steal intellectual
> property. Eyewitnesses have described Jane Doe as a woman in her mid-30s with long blonde hair, often wearing a
> hoodie or a red dress. Responders have been dispatched to investigate and resolve the situations.

The results are similar but slightly different to the results shown above for a simple word query. You can compare
the document URIs printed by each program to see that a different set of document is selected by each approach.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-java/src/main/java/org/example/VectorQueryRetriever.java).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-java/src/main/java/org/example/AskVectorQuery.java) that uses the retriever.

For an example of how to add embeddings to your data, please see [this embeddings example](../embedding.md).

## Summary

The three RAG approaches shown above - a simple word query, a contextual query, and a vector query - demonstrate how
easily data can be queried and retrieved from MarkLogic using langchain4j. Identifying the optimal approach for your own
data will require testing the approaches you choose and possibly leveraging additional MarkLogic indexes and/or
further enriching your data. 
