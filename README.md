# ai-examples

Private for now, will be public soon. 

## Setup

This will be nicer soon, but for now, make sure you have at least Java 11 and run the 
following:

```
cd setup
docker compose up -d --build
./gradlew -i mlDeploy
```

The above will setup a new MarkLogic server via Docker, with a new REST API app server in 
MarkLogic listening on port 8003. In addition, a set of fictional crime transcript documents 
is loaded into a new database in your MarkLogic server. 

Then run `cd ../rag-langchain-python` and follow the instructions in the README in that file.
