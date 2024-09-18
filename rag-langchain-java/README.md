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

    ../gradlew wordQueryExample -Pquestion="Tell me about Jane Johnson"

Running this will yield an answer similar to the below (the answer can vary based on the LLM in use and the nature
of the configured deployment model):

> Jane Doe has caused a disturbance of the peace at 211 Meadow Road in San Francisco, where she was yelling and 
> screaming and disturbing the entire neighborhood. She has also vandalized a building by spray painting 
> "Jane Doe was here" on the side of the building located at 967 First Boulevard in San Francisco. Additionally, 
> Jane has been accused of breaking into a house at 121 Oak Drive in San Francisco and stealing valuable items. 
> Lastly, Jane was reported to be heavily intoxicated and causing a disturbance at 775 Meadow Street in San Francisco. 
> She was seen stumbling around and yelling at people on the street.

Note as well that if you have tried the [Python langchain examples](../rag-langchain-python/README.md), you will notice
some differences in the results. These differences are primarily due to the different prompts used by langchain and 
langchain4j. See [the langchain4j documentation](https://docs.langchain4j.dev/intro) for more information on prompt
templates when using langchain4j. 
