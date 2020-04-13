# COMP 4321 Search Engine

This is our project Phase 1 of COMP 4321 Search Engine project. We are using a Java crawler and some libraries to scrape the web, index and build a retrieval algorithm.

## Getting Started
These instructions will help you get a local project on your machine to develop and test. 

### Pre-requisites
* OpenJDK
* RocksDB
* HTMLParser

## Authors
* Albert Paredandan
* C William Wijaya
* Nicky Pratama

## How to compile
1. cd to project directory
2. compile the .java files to classes directory

`javac -d classes src/StopStem.java`
`javac -cp lib/htmlparser.jar -d classes src/Crawler.java`
`javac -cp lib/htmlparser.jar:classes -d classes src/Main.java`

3. Run the Main
java -cp .:lib/htmlparser.jar:classes Main 
