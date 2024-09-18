# RAG with langchain4j and MarkLogic

[Retrieval Augmented Generation (RAG)](https://docs.langchain4j.dev/tutorials/rag) is implemented with
[langchain4j](https://docs.langchain4j.dev/intro) and MarkLogic via a "retriever". The examples in this
directory demonstrate three different kinds of retrievers that you can consider for your own AI application.

## Setup

The only system requirement for running these examples is Java 8 or higher. You can run the examples via an IDE such as 
Visual Code or IntelliJ. You can also use [Gradle](https://gradle.org/) to run the examples, but you do not
need Gradle installed - this repository uses the [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) 
to download an appropriate version of Gradle.

As these examples depend on Azure OpenAI, you will need to configure several properties to connect and authenticate 
using your own Azure Open AI subscript:

1. Create a file named `gradle-local.properties` in the root of this repository.
2. Add `AZURE_OPENAI_API_KEY=your key` to the file. 
3. Examine the `gradle.properties` file in the root of this repository. Override each of these properties as needed
by defining them with the correct value in `gradle-local.properties`.

## RAG with a simple word query

A key feature of MarkLogic is its ability to index all text in a document during ingest. Thus, a simple approach to RAG
with MarkLogic is to select documents based on the words in a user's question. 

To demonstrate this, you can run the Gradle `wordQueryExample` task with any question. This example program uses a custom 
langchain retriever that selects documents in the `ai-examples-content` MarkLogic database containing one or more words
in the given question. It then includes the top 10 most relevant documents in the request that it sends to Azure OpenAI.
For example:

    ../gradlew wordQueryExample -Pquestion="What disturbances has Jane Doe caused?"

Running this will yield an answer similar to the below (the answer can vary based on the LLM in use and the nature
of the configured deployment model):

> Jane Johnson is a suspect in several incidents, including cybercrime, public intoxication, vandalism, assault, 
> looting, and shoplifting. She is described as a Caucasian female in her mid-30s, with blonde hair and blue eyes. 
> She is approximately 5'6" and has a slim build. In some incidents, she was wearing a black hoodie and jeans, 
> while in others she wore a red coat or a black jacket. The motives for her actions are unclear, but there are 
> some speculations that she is struggling financially, dealing with personal issues, or protesting against 
> certain actions. The police have been notified and are investigating the incidents.

You can alter the value of the `-Pquestion=` parameter to be any question you wish.

Note as well that if you have tried the [Python langchain examples](../rag-langchain-python/README.md), you will notice
some differences in the results. These differences are primarily due to the different prompts used by langchain and 
langchain4j. See [the langchain4j documentation](https://docs.langchain4j.dev/intro) for more information on prompt
templates when using langchain4j. 


## RAG with a contextual query

TBD.


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

To try RAG with a vector query, you will need to have installed MarkLogic 12. Please see the
[top-level README in this repository](../README.md) for information on doing so.

Additionally, check the value of the `AZURE_EMBEDDING_DEPLOYMENT_NAME` properties in the `gradle.properties` file 
in the root of this repository. You can override this value by setting it in `gradle-local.properties`.

You can now run the Gradle `vectorQueryExample` task:

    ./gradlew vectorQueryExample -Pquestion="What disturbances has Jane Doe caused?" 

An example result is shown below:

> The retrieved context provides information on multiple disturbances caused by a woman named Jane Doe in various
> locations around San Francisco. The disturbances include vandalism, disturbing the peace, yelling and making a lot
> of noise, cybercrime, public intoxication, and more. The motives behind these disturbances are not entirely clear,
> but some possible reasons include personal disputes, mental instability, and attempts to steal intellectual
> property. Eyewitnesses have described Jane Doe as a woman in her mid-30s with long blonde hair, often wearing a
> hoodie or a red dress. Responders have been dispatched to investigate and resolve the situations.

The results are similar but slightly different to the results shown above for a simple word query. You can compare
the document URIs printed by each program to see that a different set of document is selected by each approach.

