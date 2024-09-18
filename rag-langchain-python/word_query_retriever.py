from typing import List
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from marklogic import Client


class WordQueryRetriever(BaseRetriever):
    client: Client

    @classmethod
    def create(cls, client: Client):
        return cls(client=client)

    def _get_relevant_documents(self, query: str) -> List[Document]:
        words = []
        for word in query.split():
            if len(word) > 2:
                words.append(word.lower().replace("?", ""))

        word_query = "<word-query xmlns='http://marklogic.com/cts'>"
        for word in words:
            word_query = f"{word_query}<text>{word}</text>"
        word_query = f"{word_query}</word-query>"

        print(f"Searching with query: {word_query}")
        results = self.client.documents.search(
            query=word_query,
            page_length=10,
            collections=["events"],
        )
        print(f"Count of MarkLogic documents sent to the LLM: {len(results)}")
        for result in results:
            print(f"URI: {result.uri}")
        return map(lambda doc: Document(page_content=doc.content["transcript"]), results)
