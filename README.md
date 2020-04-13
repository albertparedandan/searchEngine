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
2. move all .jar files, htmlparser files to lib directory
3. move Porter.java files to IRUtilies folder
4. compile the .java files to classes directory

`javac -d classes src/StopStem.java`

`javac -cp lib/htmlparser.jar -d classes src/Crawler.java`

`javac -cp lib/htmlparser.jar:classes -d classes src/Main.java`

5. Run the Main
java -cp .:lib/htmlparser.jar:classes Main 
