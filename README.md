# MarkLogic AI Examples

This repository contains a set of examples for demonstrating common AI use cases for applications built on top of 
MarkLogic. These examples are intended to serve as a starting point for your own applications; you are encouraged to 
copy and modify the code as needed. 

The examples in this repository depend on the 
[Azure OpenAI Service](https://azure.microsoft.com/en-us/products/ai-services/openai-service). They can be easily 
tailored to work with any LLM supported by the LLM framework used by each example. Note though that if you wish to 
execute these examples as-is, you will need an Azure OpenAI account and API key. 

## Setup

If you would like to try out the example programs, please follow these steps to set up a local MarkLogic instance
and to deploy a small application to it:

1. Ensure you have Java 8 or higher installed. 
2. In a terminal window, run `cd setup`.
3. If you are using [Docker Desktop](https://www.docker.com/products/docker-desktop/), run `docker compose up -d --build`
to create a new instance of MarkLogic running on Docker. It will attempt to bind to ports 8000 through 8003, so ensure
that those are available. 
4. Otherwise, if you have your own instance of MarkLogic running, ensure that port 8003 is available.
5. Run `../gradlew -i mlDeploy` to deploy a small application to MarkLogic containing 500 fictional crime events.

Some examples require the use of MarkLogic 12, which is currently only available via an early access program or for
Progress MarkLogic employees. If you would like to use these features, run the following command for installing 
MarkLogic via Docker instead of the command above:

    docker compose -f docker-compose-12.yml up -d --build

## RAG Examples

MarkLogic excels at supporting RAG, or ["Retrieval-Augmented Generation"](https://python.langchain.com/docs/tutorials/rag/),
via its schema-agnostic nature as well as it's powerful and flexible indexing. 
Please see the [Langchain RAG examples](rag-langchain-python/README.md) for more information. 
