from typing import List
from langchain_core.documents import Document
from langchain_core.retrievers import (
    BaseRetriever,
)
from marklogic import Client


class ContextualQueryRetriever(BaseRetriever):
    client: Client

    @classmethod
    def create(cls, client: Client):
        return cls(client=client)

    def _get_relevant_documents(
        self,
        chat_context: object,
    ) -> List[Document]:
        words = []
        for word in chat_context["question"].split():
            if len(word) > 2:
                words.append(word.lower().replace("?", ""))
        term_query = {"term-query": {"text": words}}

        chat_context["contextual_query"]["query"]["queries"].append(term_query)

        print(f"Searching with query: {chat_context['contextual_query']}")
        results = self.client.documents.search(
            query=chat_context["contextual_query"],
            page_length=10,
            collections=["events"],
        )

        print(f"Count of MarkLogic documents sent to the LLM: {len(results)}")
        for result in results:
            print(f"URI: {result.uri}")
        return map(lambda doc: Document(page_content=doc.content["transcript"]), results)
