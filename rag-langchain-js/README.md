# RAG with LangChainJS and MarkLogic

[Retrieval Augmented Generation (RAG)](https://docs.langchain4j.dev/tutorials/rag) is implemented with
[LangChainJS](https://js.langchain.com/docs/introduction/) and MarkLogic via a "retriever". The example in this
directory demonstrates one kind of retriever that you can consider for your own AI application.

## Setup

The only system requirements for running these examples are Node 18.x, 19.x, or 20.x
(see [LangChainJS Installation]https://js.langchain.com/v0.1/docs/get_started/installation/) and npm. 
Minimum versions of npm are dependent on the version of Node.
See [Node Releases](https://nodejs.org/en/about/previous-releases#looking-for-latest-release-of-a-version-branch)
for more information.

For this LangChainJS example, in addition to the environment variables in the `.env` file described in the README in the
root directory of this project, you'll also need to add the `AZURE_OPENAI_API_INSTANCE_NAME` setting to the `.env` file.
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
npm install
```

## RAG with a simple word query

A key feature of MarkLogic is its ability to index all text in a document during ingest. Thus, a simple approach to RAG
with MarkLogic is to select documents based on the words in a user's question.

To demonstrate this, you can run the `askWordQuery.js` module with any question. The module uses a custom LangChainJS
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


## Summary

The three RAG approaches shown above - a simple word query, a contextual query, and a vector query - demonstrate how
easily data can be queried and retrieved from MarkLogic using langchain. Identifying the optimal approach for your own
data will require testing the approaches you choose and possibly leveraging additional MarkLogic indexes and/or
further enriching your data.
