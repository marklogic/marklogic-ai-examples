---
layout: default
title: RAG with LangChain
parent: RAG Examples
nav_order: 1
---

[Retrieval Augmented Generation (RAG)](https://python.langchain.com/docs/tutorials/rag/) can be implemented in Python 
with [LangChain](https://python.langchain.com/docs/introduction/) and MarkLogic via a "retriever". The examples in this
directory demonstrate three different kinds of retrievers that you can consider for your own AI application.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Setup

To try these examples, you should first create a new Python virtual environment. There are many ways to do this;
you can use a tool such as [pyenv](https://github.com/pyenv/pyenv), or just follow these simple steps that
[create a virtual environment using `venv`](https://docs.python.org/3/library/venv.html):

```
# Run this if you are not already in this example directory.
cd rag-langchain-python
python -m venv .venv
source .venv/bin/activate
```

Once you have a virtual environment created, run the following to install the necessary LangChain dependencies along
with the [MarkLogic Python client](https://pypi.org/project/marklogic-python-client/):

    pip install --quiet --upgrade langchain langchain-community langchain_openai marklogic_python_client

You are now ready to execute the example RAG programs.

## RAG with a simple word query

A key feature of MarkLogic is its ability to index all text in a document during ingest. Thus, a simple approach to RAG
with MarkLogic is to select documents based on the words in a user's question.

To demonstrate this, you can run the `ask_word_query.py` module with any question. The module uses a custom LangChain
retriever that selects documents in the `ai-examples-content` MarkLogic database containing one or more of the words
in the given question. It then includes the top 10 most relevant documents in the request that it sends to Azure OpenAI.
For example:

    python ask_word_query.py "What disturbances has Jane Doe caused?" 

Running this will yield an answer similar to the below (the answer can vary based on the LLM in use and the nature
of the configured deployment model):

> Jane Doe has caused disturbances such as public intoxication, yelling and banging on doors and windows,
> accessing confidential information without authorization, and vandalizing a building. The motives for
> her behavior are unclear, but it may be related to personal vendettas or coping with personal issues.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-python/word_query_retriever.py).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-python/ask_word_query.py) that uses the retriever.

## RAG with a contextual query

In many applications built on MarkLogic, a user will search the documents in a database by leveraging a variety of
indexes in MarkLogic, such as the universal text index, date range indexes, and geospatial indexes. This query - which
can feature any of the many dozens of different query functions supported by MarkLogic - is referred to as a
"contextual query" - it captures the user's context in terms of what documents they are interested in. A RAG approach
can then account for both this contextual query and a user's question by enhancing the contextual query with a word
query based on the words in a user's question.

The `ask_contextual_query.py` module demonstrates this approach by defining a simple contextual query that only
selects documents containing a JSON property named `type` with a value of `public intoxication`.
Try running the following:

    python ask_contextual_query.py "What disturbances has Jane Doe caused?" 

The answer will be similar to the one below. You can see how the results are based only on documents involving public
intoxication as opposed to the entire set of fictional crime events:

> Jane Doe has caused disturbances by stumbling around, slurring her words, and causing a disturbance in
> public areas. She has been reported to be yelling at people passing by and blocking the entrance to a
> nearby store. There are concerns for her safety and the safety of others around her.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-python/contextual_query_retriever.py).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-python/ask_contextual_query.py) that uses the retriever.

## RAG with a vector query

MarkLogic 12 has
[new support for generative AI capabilities](https://investors.progress.com/news-releases/news-release-details/progress-announces-powerful-new-generative-ai-capabilities)
via a set of [vector operations](https://docs.marklogic.com/12.0/vec/vector-operations). With this approach,
documents are first selected in a manner similar to the approaches shown above - by leveraging the powerful and flexible
set of indexes that have long been available in MarkLogic. The documents are then further filtered and sorted via
the following process:

1. An embedding of the user's question is generated using [LangChain and Azure OpenAI](https://python.langchain.com/docs/integrations/text_embedding/).
2. Using MarkLogic's new vector API, the generated embedding is compared against the embeddings in each
   selected crime event document to generate a similarity score for each document.
3. The documents with the highest similarity scores are sent to the LLM to augment the user's question.

To try the `ask_vector_query.py` module, you will need to have installed MarkLogic 12 and also have defined
`AZURE_EMBEDDING_DEPLOYMENT_NAME` in your `.env` file. Please see the
[setup guide](../setup.md) for more information.

You can now run `ask_vector_query.py`:

    python ask_vector_query.py "What disturbances has Jane Doe caused?"

An example result is shown below:

> Jane Doe has caused disturbances of the peace, including yelling, screaming, banging on doors and windows,
> and vandalism. The motives for her behavior are unclear, but it may be related to personal issues or
> mental health problems. She has been described as agitated, upset, and heavily intoxicated.

The results are similar but slightly different to the results shown above for a simple word query. You can compare
the document URIs printed by each program to see that a different set of document is selected by each approach.

For more information, please see the following code files:

- The [LangChain retriever](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-python/vector_query_retriever.py).
- The [example program](https://github.com/marklogic/marklogic-ai-examples/blob/main/rag-langchain-python/ask_vector_query.py) that uses the retriever.

For an example of how to add embeddings to your data, please see [this embeddings example](../embedding.md).

## RAG with Semaphore Models

[Progress Semaphore](https://www.progress.com/semaphore/platform) is a modular semantic AI platform that provides the
semantic layer of your digital ecosystem so you can manage knowledge models, extract facts and classify the context and
meaning from structured and unstructured information and generate rich semantic metadata. 

Details for classifying text are specific to your Semaphore installation. However, for a Progress Data Cloud
installation, see the
[Classification and Language Service Developer's Guide](https://portal.smartlogic.com/docs/5.6/classification_server_-_developers_guide/welcome).

Once you have [classified](https://www.progress.com/semaphore/platform/semantic-knowledge-classification) your documents
and stored the extracted concepts on the documents, you can also search for those concepts as a part of the RAG
retriever. A typical strategy is to use your custom model and the Semaphore Classifier to extract concepts from the
user's question. With that list of concepts, you can easily search your target documents for those that have matching
concepts, and then include those documents in the list of documents returned by the retriever.

For instance, assume that you have extracted the concepts from a document and stored those concepts in a new JSON block in the
document that looks something like this:
```
"concepts": [
  {
    "CrimeReportsModel-Crimes": "Public Order Crime"
  }, 
  {
    "CrimeReportsModel-Crimes": "Disturbing the Peace"
  },
  ...
]
```
You can search for all documents that have been classified with the `Crimes` concept in the `CrimesReport` model using
a CTS query:
```
cts.jsonPropertyValueQuery('CrimeReportsModel-Crimes', 'Crimes')
```
That query can be used on its own or as part of more complex query that retrieves the documents that provide the best
context information to your LLM. One possibility is to adapt the vector retriever to use that query in the initial
documents query. So, as an adaptation from `vector_query_retriever.py`, this uses the `jsonPropertyValueQuery` instead
of the `wordQuery`.
```
op.fromSearchDocs(
  cts.andQuery([
    cts.jsonPropertyValueQuery('CrimeReportsModel-Crimes', 'Crimes'),
    cts.collectionQuery('events')
  ]),
  null,
  {
    'scoreMethod': 'score-bm25',
    'bm25LengthWeight': 0.5
  }
)
```

## Summary

The three RAG approaches shown above - a simple word query, a contextual query, and a vector query - demonstrate how
easily data can be queried and retrieved from MarkLogic using LangChain. Identifying the optimal approach for your own
data will require testing the approaches you choose and possibly leveraging additional MarkLogic indexes and/or
further enriching your data. 
