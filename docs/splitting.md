---
layout: default
title: Splitting Examples
nav_order: 4
---

A RAG approach typically benefits from sending multiple smaller segments or "chunks" of text to an LLM. While MarkLogic
can efficiently ingest and index large documents, sending all the text in even a single document may either exceed
the number of tokens allowed by your LLM or may result in slower and more expensive responses from the LLM. Thus,
when importing or reprocessing documents in MarkLogic, your RAG approach may benefit from splitting the searchable
text in a document into smaller segments or "chunks" that allow for much smaller and more relevent segments of text
to be sent to the LLM.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Overview

This project demonstrates two different approaches to splitting documents:

1. Splitting the text in a document and storing each chunk in a new separate document.
2. Splitting the text in a document and storing the set of chunks in a new separate document.

You are not limited to these approaches. For example, you may find it beneficial to not create a new document but
rather store the set of chunks in the same document containing the searchable text. These two approaches are intended
to show how easily you can split and store chunks of text and thus get you started with splitting your own data.

## Setup

Assuming you have followed the [setup instructions for these examples](../setup/README.md), then you already have a
database in your MarkLogic cluster named `ai-examples-content`. This database contains a small set - specifically,
3,034 text documents - of the
[Enron email dataset](https://www.loc.gov/item/2018487913/) in a collection named `enron`. These documents are good
candidates for splitting as many of them have amounts of text large enough to exceed common LLM token limits. As the
documents are text, they also are good candidates for the two approaches shown here - i.e. creating separate documents
and leaving the original text documents untouched.

You also need Java 8 in order to run these examples, which is the same version of Java needed by the aforementioned
setup instructions.

## Splitting chunks to separate documents

In this approach, the [langchain4j document splitter API](https://docs.langchain4j.dev/tutorials/rag#document-splitter)
and the [MarkLogic Data Movement SDK](https://docs.marklogic.com/guide/java/data-movement)
are used to create chunks of no more than 1,000 characters each. Each chunk is then saved to a new JSON document in a
collection named `enron-chunk` with the following fields:

- `sourceUri` = the URI of the document that the chunk was extracted from.
- `text` = the chunk of text extracted from the document identified by `sourceUri`.

By limiting the number of tokens in each chunk, a RAG approach can typically send a dozen or more chunks to the LLM
without exceeding a token limit. The exact number of chunks will depend on the max number of characters you specify
along with your LLM token limit.

To create these chunks in separate documents, run the following Gradle task:

    ../gradlew splitToMultipleDocuments

The task will create 7,547 "chunk" documents in a collection named `enron-chunk`. An example document is shown below:

```
{
  "sourceUri": "/enron/allen-p/deleted_items/211",
  "text": "MARKET DATA: No Scheduled Outages.\n
  NT: No Scheduled Outages.

  OS/2:  No Scheduled Outages.

  OTHER SYSTEMS:
Impact:  CORP
  Time:  Fri 10/26/2001 at  7:00:00 PM CT thru Fri 10/26/2001 at  9:00:00 PM CT
  \tFri 10/26/2001 at  5:00:00 PM PT thru Fri 10/26/2001 at  7:00:00 PM PT
  Sat 10/27/2001 at  1:00:00 AM London thru Sat 10/27/2001 at  3:00:00 AM London
  Outage:  Reboot HR-DB-4 to add new disks
  Environments Impacted:  All
  Purpose: We need to remove the old arrays because of reliability issues.
  Backout: Connect the old arrays back to DB-4 and reboot.
  Contact(s): 	Brandon Bangerter 713-345-4904
  Mark Calkin             713-345-7831
  Raj Perubhatla       713-345-8016  281-788-9307"
}
```

## Splitting chunks into the same document

This approach splits text into chunks in the same fashion as the one above. But instead of create one document per
chunk, it creates a JSON document in a collection named `enron-chunks-aggregated`
Similar to the approach above, this approach [langchain4j document splitter API](https://docs.langchain4j.dev/tutorials/rag#document-splitter)
is used to create chunks of no more than 1,000 characters each. Each chunk is then saved to a new JSON document in a
collection named `enron-chunk` with the following fields:

- `sourceUri` = the URI of the document that the chunks were extracted from.
- `chunks` = a JSON array containing each chunk.

To create these documents, run the following Gradle task:

    ../gradlew splitToSameDocument

The task will create 3,034 documents in a collection named `enron-chunks-aggregated`. Each document has a URI with the
following format:

    (source URI)-chunks-(number of chunks).json

An example document, with two chunks, is shown below. Note that it includes some overlap between the two chunks,
as the program defaults to 100 characters of overlap between chunks:

```
{
  "sourceUri": "/enron/allen-p/_sent_mail/494",
  "chunks": [
    {
      "text": "Message-ID: <16552852.1075855726915.JavaMail.evans@thyme>
      \nDate: Tue, 20 Feb 2001 04:08:00 -0800 (PST)

      From: phillip.allen@enron.com

      To: stagecoachmama@hotmail.com

      Subject:

      Mime-Version: 1.0

      Content-Type: text/plain; charset=us-ascii

      Content-Transfer-Encoding: 7bit

      X-From: Phillip K Allen

      X-To: stagecoachmama@hotmail.com

      X-cc:

      X-bcc:

      X-Folder: \Phillip_Allen_June2001\Notes Folders\'sent mail

      X-Origin: Allen-P

      X-FileName: pallen.nsf

      ---------------------- Forwarded by Phillip K Allen/HOU/ECT on 02/20/2001
      11:59 AM ---------------------------

      From:  Phillip K Allen                           02/15/2001 01:13 PM

      To: stagecoachmama@hotmail.com
      cc:
      Subject:

      Lucy,

      Thanks for clearing up the 2/2 file.  Moving on to 2/9, here are some
      questions:

      #1 It looks like he just missed 1/26&2/9 of $110.  I can't tell if he still 
      owes $47 on his deposit.

      #13 I show she missed rent on 1/26 and still owes $140."
    },
    {
      "text": "#13 I show she missed rent on 1/26 and still owes $140.

      #15 Try and follow up with Tomas about the $95.  Hopefully, he won't have a 
      bad reaction.

      #20b Missed rent?

      #26  Has she paid any deposit or rent?

      #27 Missed rent?"
    }
  ]
}
```

