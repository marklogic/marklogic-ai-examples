# RAG with langchain and MarkLogic

[Retrieval Augmented Generation (RAG)](https://python.langchain.com/docs/tutorials/rag/) is implemented with 
[langchain](https://python.langchain.com/docs/introduction/) and MarkLogic via a "retriever". The examples in this
directory demonstrate three different kinds of retrievers that you can consider for your own AI application.

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

Once you have a virtual environment created, run the following to install the necessary langchain dependencies along 
with the [MarkLogic Python client](https://pypi.org/project/marklogic-python-client/):

    pip install --quiet --upgrade langchain langchain-community langchain_openai marklogic_python_client

You are now ready to execute the example RAG programs. 

## RAG with a simple word query

A key feature of MarkLogic is its ability to index all text in a document during ingest. Thus, a simple approach to RAG
with MarkLogic is to select documents based on the words in a user's question. 

To demonstrate this, you can run the `ask_word_query.py` module with any question. The module uses a custom langchain
retriever that selects documents in the `ai-examples-content` MarkLogic database containing one or more of the words
in the given question. It then includes the top 10 most relevant documents in the request that it sends to Azure OpenAI. 
For example:

    python ask_word_query.py "What disturbances has Jane Doe caused?" 

Running this will yield an answer similar to the below (the answer can vary based on the LLM in use and the nature
of the configured deployment model):

> Jane Doe has caused disturbances such as public intoxication, yelling and banging on doors and windows,
> accessing confidential information without authorization, and vandalizing a building. The motives for
> her behavior are unclear, but it may be related to personal vendettas or coping with personal issues.

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

## RAG with a vector query 

MarkLogic 12 has 
[new support for generative AI capabilities](https://investors.progress.com/news-releases/news-release-details/progress-announces-powerful-new-generative-ai-capabilities)
via a set of [vector operations](https://docs.marklogic.com/12.0/vec/vector-operations). With this approach, 
documents are first selected in a manner similar to the approaches shown above - by leveraging the powerful and flexible
set of indexes that have long been available in MarkLogic. The documents are then further filtered and sorted via 
the following process:

1. An embedding of the user's question is generated using [langchain and Azure OpenAI](https://python.langchain.com/docs/integrations/text_embedding/). 
2. Using MarkLogic's new vector API, the generated embedding is compared against the embeddings in each 
selected crime event document to generate a similarity score for each document.
3. The documents with the highest similarity scores are sent to the LLM to augment the user's question.

To try the `ask_vector_query.py` module, you will need to have installed MarkLogic 12 and also have defined 
`AZURE_EMBEDDING_DEPLOYMENT_NAME` in your `.env` file. Please see the
[top-level README in this repository](../README.md) for more information.

You can now run `ask_vector_query.py`:

    python ask_vector_query.py "What disturbances has Jane Doe caused?"

An example result is shown below:

> Jane Doe has caused disturbances of the peace, including yelling, screaming, banging on doors and windows, 
> and vandalism. The motives for her behavior are unclear, but it may be related to personal issues or 
> mental health problems. She has been described as agitated, upset, and heavily intoxicated.

The results are similar but slightly different to the results shown above for a simple word query. You can compare
the document URIs printed by each program to see that a different set of document is selected by each approach.

## Summary

The three RAG approaches shown above - a simple word query, a contextual query, and a vector query - demonstrate how
easily data can be queried and retrieved from MarkLogic using langchain. Identifying the optimal approach for your own
data will require testing the approaches you choose and possibly leveraging additional MarkLogic indexes and/or 
further enriching your data. 
