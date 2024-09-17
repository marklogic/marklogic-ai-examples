from typing import List
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from langchain_openai import AzureOpenAIEmbeddings
from marklogic import Client


class MarkLogicVectorQueryRetriever(BaseRetriever):

    client: Client
    embedding_generator: AzureOpenAIEmbeddings
    max_results: int = 10
    collections: List[str] = []
    tde_schema: str
    tde_view: str
    scoring_method: str

    @classmethod
    def create(
        cls,
        client: Client,
        embedding_generator: AzureOpenAIEmbeddings,
        tde_schema: str = None,
        tde_view: str = None,
        scoring_method: str = "score-bm25",
    ):
        return cls(
            client=client,
            embedding_generator=embedding_generator,
            tde_schema=tde_schema or "crime",
            tde_view=tde_view or "event",
            scoring_method=scoring_method,
        )

    def _build_javascript_query_query(self, query, query_embedding):
        # Returning first self.max_results documents based on token limitations
        #
        # If limits are hit, consider different models:
        # gpt-35-turbo (0125): 16,385/4,096
        # gpt-35-turbo (1106): 16,385/4,096
        # gpt-35-turbo-16k (0613):

        # This JavaScript consists of a single Optic query. The Optic query
        # starts with a word query to find candidate documents. It then uses
        # a join to match the embeddings up with the candidate documents.
        # Next, it performs a cosine similarity operation and sorts the
        # candidate documents based on the results of that operation. Finally,
        # it returns those documents to the chain.

        search_words = []
        for word in query.split():
            search_words.append(word.lower().replace("?", ""))
        return """
const op = require('/MarkLogic/optic');
 
op.fromSearchDocs(
      cts.andQuery([cts.wordQuery({}),cts.collectionQuery({})]),
      null,
      {{'scoreMethod': '{}'}}
  )
  .limit({})
  .joinInner(
    op.fromView('{}','{}', '', op.fragmentIdCol('vectorsDocId')),
    op.on(
      op.fragmentIdCol('fragmentId'),
      op.fragmentIdCol('vectorsDocId')
    )
  )
  .bind(op.as('summaryCosineSim', 
      op.vec.cosineSimilarity(
          op.vec.vector(op.col('embedding')),
          op.vec.vector(vec.vector({}))
      )
  ))
  .result()
        """.format(
            search_words,
            self.collections,
            self.scoring_method,
            self.max_results,
            self.tde_schema,
            self.tde_view,
            query_embedding,
        )

    def _get_relevant_documents(self, query: str) -> List[Document]:
        print(f"Searching with query: {query}")

        query_embedding = self.embedding_generator.embed_query(query)
        javascript_query = self._build_javascript_query_query(
            query, query_embedding
        )
        results = self.client.eval(javascript=javascript_query)

        print(f"Count of matching MarkLogic documents: {len(results)}")
        return map(lambda doc: Document(page_content=doc["text"]), results)
