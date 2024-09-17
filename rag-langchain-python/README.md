# RAG with langchain and MarkLogic

This project demonstrates one approach for implementing a 
[langchain retriever](https://python.langchain.com/docs/modules/data_connection/)
that allows for 
[Retrieval Augmented Generation (RAG)](https://python.langchain.com/docs/use_cases/question_answering/)
to be supported via MarkLogic and the MarkLogic Python Client. This example uses the same data as in 
[the langchain RAG quickstart guide](https://python.langchain.com/docs/use_cases/question_answering/quickstart), 
but with the data having first been loaded into MarkLogic.

**This is only intended as an example** of how easily a langchain retriever can be developed
using the MarkLogic Python Client. The queries in this example are simple and naturally 
do not have any knowledge of how your data is modeled in MarkLogic. You are encouraged to use 
this as an example for developing your own retriever, where you can build a query based on a 
question submitted to langchain that fully leverages the indexes and data models in your MarkLogic
application. Additionally, please see the 
[langchain documentation on splitting text](https://python.langchain.com/docs/modules/data_connection/document_transformers/). You may need to restructure your data so that you have a larger number of 
smaller documents in your database so that you do not exceed the limit that langchain imposes on how
much data a retriever can return.

## Install Python Libraries

Next, create a new Python virtual environment - [pyenv](https://github.com/pyenv/pyenv) is recommended for this - 
and install the 
[langchain example dependencies](https://python.langchain.com/docs/use_cases/question_answering/quickstart#dependencies),
along with the MarkLogic Python Client: 

    pip install -U langchain langchain_openai langchain-community langchainhub openai chromadb bs4 marklogic_python_client

## Create Python Environment File

Create a ".env" file to hold your AzureOpenAI environment values. It should look
something like this.
```
OPENAI_API_VERSION=2023-12-01-preview
AZURE_OPENAI_ENDPOINT=<Your Azure OpenAI Endpoint>
AZURE_OPENAI_API_KEY=<Your Azure OpenAI API Key>
AZURE_LLM_DEPLOYMENT_NAME=gpt-test1-gpt-35-turbo
AZURE_LLM_DEPLOYMENT_MODEL=gpt-35-turbo
```

# Testing the retriever

## Testing using a retriever with a basic query

You are now ready to test the example retriever. Run the following to ask a question
with the results augmented via the `word_query_retriever.py` module in this
project:

    python ask_word_query.py "What disturbances has Joe Blow caused?"

The retriever uses MarkLogic's support for indexing and searching on every word in documents loaded into MarkLogic.
By default, the retriever loads at most 10 documents. You can change this by providing a different number of documents
to retrieve after the question:
select from the documents loaded via `load_data.py`. It defaults to a page length of 10.

    python ask_similar_query.py "What disturbances has Joe Blow caused?" 15

## Testing using a retriever with a contextual query

There may be times when your langchain application needs to use both a question and a
structured query during the document retrieval process. To see an example of this, run
the following to ask a question. That question is combined with a hard-coded structured
query using the `marklogic_contextual_query_retriever.py` module in this project.

    python ask_contextual_query.py "What is task decomposition?" posts

This retriever builds a term-query using words from the question. Then the term-query is
added to the structured query and the merged query is used to select from the documents 
loaded via `load_data.py`.

## Testing using MarkLogic 12EA Vector Search

### MarkLogic 12EA Setup

To try out this functionality out, you will need access to an instance of MarkLogic 12
(currently internal or Early Access only).
<TODO>Add info to get ML12</TODO>
You may use docker 
[docker-compose](https://docs.docker.com/compose/) to instantiate a new MarkLogic
instance with port 8003 available (you can use your own MarkLogic instance too, just be
sure that port 8003 is available):

    docker compose -f docker-compose-12.yml up -d --build

### Deploy With Gradle

You will also need to deploy the application. However, for this example, you will need
to include an additional switch on the command line to deploy a TDE schema that takes
advantage of the vector capabilities in MarkLogic 12.

    ./gradlew -i mlDeploy -PmlSchemasPath=src/main/ml-schemas-12

### Install Python Libraries

As above, if you have not yet installed the Python libraries, install this with pip:
```
pip install -U langchain langchain_openai langchain-community langchainhub openai chromadb bs4 marklogic_python_client
```

### Create Python Environment File
The Python script for this example also generates LLM embeddings and includes them in
the documents stored in MarkLogic. In order to generate the embeddings, you'll need to
add the following environment variables (with your values) to the .env file created
above.

```
AZURE_EMBEDDING_DEPLOYMENT_NAME=text-test-embedding-ada-002
AZURE_EMBEDDING_DEPLOYMENT_MODEL=text-embedding-ada-002
```

### Running the Vector Query

You are now ready to test the example vector retriever. Run the following to ask a
question with the results augmented via the `marklogic_vector_query_retriever.py` module
in this project:

    python ask_vector_query.py "What disturbances has Joe Blow caused?" 10

This retriever searches MarkLogic for candidate documents, and defaults to
using the new score-bm25 scoring method in MarkLogic 12EA. If preferred, you can adjust
this to one of the other scoring methods. After retrieving candidate documents based on
the CTS search, the retriever uses the new vector functionality to sort the documents
based on cosine similarity to the user question, and then returns the top N documents
for the retriever to package up.
