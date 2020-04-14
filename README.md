# COMP 4321 Search Engine

This is our project Phase 1 of COMP 4321 Search Engine project. We are using a Java crawler and some libraries to scrape the web, index and build a retrieval algorithm.

## Getting Started
These instructions will help you get a local project on your machine to develop and test. 

### Pre-requisites
* OpenJDK
* RocksDB
* HTMLParser
* Porter.java (https://course.cse.ust.hk/comp4321/labs/lab3/IRUtilities/Porter.java)
## Authors
* Albert Paredandan
* C William Wijaya
* Nicky Pratama

## How to compile
1. cd to project directory
2. create a directory called "projectdirectory/classes"
3. create a directory "projectdirectory/assets/db"
4. move all .jar files, (htmlparser and rocksdb) files to projectdirectory/lib directory
5. move Porter.java files to "project_directory/IRUtilies" folder
6. make sure to change `DB_PATH` to your base project directory in `Main.java`
7. compile the .java files to classes directory
`javac -cp lib/YOURROCKSDB.jar:classes:.:YOURHTMLPARSER.jar -d classes src/*.java`
8. Run the Main
`javac -cp lib/YOURROCKSDB.jar:classes:.:YOURHTMLPARSER.jar Main` 

## Database Schema
* link_to_id
This is the database that converts the links to pageIDs. This is done so that the the spaceover head is reduced in the other databases.

* page_info
This is the database that stores the info to a parent URL, it stores title, URL, modified date, size and child links

* word_to_id
This is the database that converts the words into their respective wordID, this again reduces space overhead

* word_freq
This is the database that indexes the word frequencies for each document. It is done in an inverted index manner to make things more efficient.
