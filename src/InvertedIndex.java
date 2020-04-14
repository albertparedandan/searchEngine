import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;  
import org.rocksdb.RocksIterator;


public class InvertedIndex
{
    private RocksDB linkToId;
    private RocksDB pageInfo;
    private RocksDB wordToId;
    private RocksDB wordFreq;
    private Options options;
    private String path;

    InvertedIndex(String dbPath) throws RocksDBException
    {
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.path = dbPath;
        this.linkToId = RocksDB.open(this.options, dbPath + "/" + "link_to_id");
        this.pageInfo = RocksDB.open(this.options, dbPath + "/" + "page_info");
        this.wordToId = RocksDB.open(this.options, dbPath + "/" + "word_to_id");
        this.wordFreq = RocksDB.open(this.options, dbPath + "/" + "word_freq");
    }

    public void parsePages(String read, String read2) {
        this.linkToId(read);
        this.collectPageInfo(read2);
        this.parseTerms(read);
    }

    public void linkToId(String read) {
        // get the link name
        try{
			BufferedReader stemCrawled = new BufferedReader(new FileReader(read));
            String line;
            while ((line = stemCrawled.readLine()) != null) {
                String[] words = line.split("\\s");
                for (String w: words) {
                    if (w.contains("http")){
                        byte[] content = this.linkToId.get(w.getBytes());
                        // check if the link is already in DB
                        // if it is not, content == null
                        if (content == null) {
                            // get last pageID value first
                            int id = this.getLastId(0);
                            id = id + 1;
                            linkToId.put(w.getBytes(), ("doc " + id).getBytes()); 
                        }
                    }
                    else {
                        continue;
                    }
                }
		    }
        }
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
        catch(RocksDBException dbe)
        {
            System.err.println(dbe.toString());
        }
    }

    public void collectPageInfo(String read) {
        String result = "";
        String url = "";
        try {
			BufferedReader stemCrawled = new BufferedReader(new FileReader(read));
            String line;
            int lineNum = 0;
            while ((line = stemCrawled.readLine()) != null) {
                if (lineNum % 6 == 4) {
                    // get keywords
                }
                else if (lineNum % 6 == 0) {
                    // it's the URL
                    url = line;
                    result = line + " ";
                }
                else {
                    result += line + " ";
                }

                if (lineNum % 6 == 5) {
                    url = this.getPageId(url);
                    this.pageInfo.put(url.getBytes(), result.getBytes());
                }
                lineNum++;
		    }
        }
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
        catch(RocksDBException dbe)
        {
            System.err.println(dbe.toString());
        }
    }

    public void getFirstDoc() 
    {
        RocksIterator iter;
        iter = pageInfo.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.value()));
        }
    }

    public Vector<String> getDocDetails(String key)
    {
        RocksIterator iter;
        iter = pageInfo.newIterator();
        Vector<String> results = new Vector<String>();
        for(iter.seekToFirst(); iter.isValid(); iter.next()) 
            {
                String iter_key = new String(iter.key());

                if (key.equals(iter_key)){
                    // Get and print the content of each key
                    String content = new String(iter.value()); 
                    String[] arrOfStr = content.split(" ", 0);
                        String url = arrOfStr[0];
                        String title = "";
                    int i = 0;
                    for (i = 2; i < 15; ++i){
                        if(arrOfStr[i].matches(".*\\d.*"))
                        // if(!((arrOfStr[i] >= 'a' && arrOfStr[i] <= 'z') || (arrOfStr[i] >= 'A' && arrOfStr[i] <= 'Z')))
                            break;
                            // (s.matches(".*[a-z].*"))
                        title += arrOfStr[i] + " ";
                    }
                        String page_size = arrOfStr[i];
                        String last_modified = "";
                    for (int j = i+1; j < i+7; ++j){
                        last_modified += arrOfStr[j] + " ";
                    }
                    i = i + 7;
                        String children = "";
                    for (int j = i; j < arrOfStr.length; ++j) {
                        children += arrOfStr[j] + " ";
                    }
                    
                    results.add(iter_key);
                    results.add(title);
                    results.add(url);
                    results.add(last_modified);
                    results.add(page_size);
                    results.add(children);
                }                
			}
        iter = wordFreq.newIterator();
        RocksIterator iterator = wordToId.newIterator();;
        String wordsInDoc = "";
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            String wordId = new String(iter.key());
            int freq = getWordFreq(results.get(0), wordId);
            if (freq > 0)
                // iterator = wordToId.newIterator();
                for(iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    String id = new String(iterator.value());
                    if (wordId.equals(id)){
                        String word = new String(iterator.key());
                        wordsInDoc += word + " " + freq + "; ";
                    }
                }
        }
        results.add(wordsInDoc);
        return results;
    }
    
    public void parseTerms(String read) {
        try {
            BufferedReader stemCrawled = new BufferedReader(new FileReader(read));
            String line = "";
            String docId = "";
            int lineNum = 0;
            while ((line = stemCrawled.readLine()) != null) {
                if (lineNum % 6 == 0) {
                    docId = line;
                }
                else if (lineNum % 6 == 4) {
                    // if the current line is the list of keywords
                    //System.out.println("docID in line 5 ==3:" + docId);
                    docId = this.getPageId(docId);
                    String[] words = line.split("\\s");
                    for (String w: words) {
                        // check first if it already exists in the database
                        String wordId = getWordId(w);
                        if (wordId == "") {
                            // insert the word first
                            wordId = "word " + (getLastId(2) + 1);
                            this.wordToId.put(w.getBytes(), wordId.getBytes());
                        }
                        // word already exists
                        
                        // now we can update/insert the word frequency for the given docId
                        // check the frequency of the word for this document
                        int freq = getWordFreq(docId, wordId);
                        // call function to insert/update the value of docId and word freq
                        // freq here is still the old freq so that the function can find the exact string to replace and then increment the frequency
                        updateOrInsertWordFreq(docId, wordId, freq);
                    }
                }
                lineNum++;
            }
        }
        catch(IOException ioe) {
            System.err.println(ioe.toString());
        }
        catch(RocksDBException dbe) {
            System.err.println(dbe.toString());
        }
    }

    public void updateOrInsertWordFreq(String docId, String wordId, int freq) {
        // this function updates the word frequency for a given docId
        // if no previous entry exists, insert it either way
        try {
            byte[] content = this.wordFreq.get(wordId.getBytes());
            if (content != null) {
                String value = new String (content);
                if (freq == 0) {
                    // apend the value and update
                    value += docId + " freq " + (freq + 1) + ", ";
                }
                else {
                    value = value.replaceFirst((docId + " freq " + freq + ", "), (docId + " freq " + (freq+1) + ", "));
                }
                this.wordFreq.put(wordId.getBytes(), value.getBytes());
            }
            else {
                String value = docId + " freq " + (freq + 1) + ", ";
                this.wordFreq.put(wordId.getBytes(), value.getBytes());
            }
        }
        catch(RocksDBException dbe) {
            System.err.println(dbe.toString());
        }
    }

    public int getWordFreq(String docId, String wordId) {
        try {
            byte[] content = this.wordFreq.get(wordId.getBytes());
            if (content != null) {
                String value = "" + new String(content);
                String find = docId + " freq ";
                int i = value.indexOf(find);
                if (i > -1) {
                    // found
                    i += find.length();
                    int j = value.indexOf(",", i);
                    value = value.substring(i, j);
                    int answer = Integer.parseInt(value);
                    return answer;
                }
            }
            // TODO handle if wordId does not even exists in wordFreq
            else {
                return 0;
            }
        }
        catch(RocksDBException dbe) {
            System.err.println(dbe.toString());
        }
        return 0;
    }

    public int getLastId(int mode) {
        // gets the lastId of the current db, if db is empty, it will return a -1. first entry is 0
        // mode 0 is link_to_id
        // mode 1 is page_info
        // mode 2 is word_to_id
        // mode 3 is word_freq
        RocksIterator iter;
        if (mode == 0) {
            iter = linkToId.newIterator();
        }    
        else if (mode == 1) {
            iter = pageInfo.newIterator();
        }
        else if (mode == 2) {
            iter = wordToId.newIterator();
        }
        else {
            iter = wordFreq.newIterator();
        }
        int id = -1;
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            id++;
        }
        return id;
    }

    public String getWordId(String word) {
        // returns the word_id of a given word
        // if word does not exist, it will return an empty string
        String result = "";
        try {
            byte[] content = this.wordToId.get(word.getBytes());
            if (content != null) {
                result = new String(content);
            }
        }
        catch(RocksDBException dbe)
        {
            System.err.println(dbe.toString());
        }
        return result;
    }

    public String getPageId(String url) {
        // for a given URL, get the url's pageID in link_to_id database
        // will return empty string if does not exists
        // will return "doc X" where X is the ID if link exists
        String result = "";
        try {
            byte[] content = this.linkToId.get(url.getBytes());
            if (content != null) {
                result = new String(content);
            }
            return result;
        }
        catch(RocksDBException dbe)
        {
            System.err.println(dbe.toString());
        }
        return result;
    }

    public void printAll(int mode) throws RocksDBException
    {
        // Print all the data in the hashtable
        // mode 0 = print link_to_id
        // mode 1 = print page_info
        // mode 2 = print word_to_id
        // mode 3 = print word_freq
        RocksIterator iter;
        if (mode == 0) {
            iter = linkToId.newIterator();
        }    
        else if (mode == 1) {
            iter = pageInfo.newIterator();
        }
        else if (mode == 2) {
            iter = wordToId.newIterator();
        }
        else {
            iter = wordFreq.newIterator();
        }

        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.key()) + "=" + new String(iter.value()));
        }
    }
}
