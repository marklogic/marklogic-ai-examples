---
layout: default
title: Setup
nav_order: 2
---

If you would like to try out the example programs in this repository, please follow these steps to set up a local 
MarkLogic instance and to deploy a small application to it:

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

## Azure OpenAI configuration

For any AI example program, it needs to connect to an AI service. The examples in this repository depend on
[the Azure OpenAI Service](https://azure.microsoft.com/en-us/products/ai-services/openai-service), though they can be
easily tailored to work with any AI service.

If you would like to run the examples as-is and have an Azure OpenAI account, you will need to create a file in the
root of this repository named `.env` with the following keys and values:

```
OPENAI_API_VERSION=2023-12-01-preview
AZURE_OPENAI_ENDPOINT=<Your Azure OpenAI endpoint>
AZURE_OPENAI_API_KEY=<Your Azure OpenAI API key>
AZURE_LLM_DEPLOYMENT_NAME=<The name of an LLM deployment in your Azure OpenAI service>
```

If you are running MarkLogic 12 and would like to run the examples that involve vector queries, you will also need
to add the following to your `.env` file.

```
AZURE_EMBEDDING_DEPLOYMENT_NAME=<The name of an embedding deployment in your Azure OpenAI service>
```

