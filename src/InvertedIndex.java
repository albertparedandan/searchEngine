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
    private RocksDB db;
    private Options options;
    private String path;
    private String linkToIdPath;
    private String pageInfoPath;

    InvertedIndex(String dbPath) throws RocksDBException
    {
        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        this.options = new Options();
        this.options.setCreateIfMissing(true);
        this.path = dbPath;
        this.linkToIdPath = dbPath + "/" + "link_to_id";
        this.pageInfoPath = dbPath + "/" + "page_info";
    }

    public void parsePages(String read) {
        this.linkToId(read);
    }

    public void linkToId(String read) {
        // get the link name
        try{
        db = RocksDB.open(this.options, linkToIdPath);
			BufferedReader stemCrawled = new BufferedReader(new FileReader(read));
            String line;
            while ((line = stemCrawled.readLine()) != null) {
                String[] words = line.split("\\s");
                for (String w: words) {
                    if (w.contains("http")){
                        byte[] content = this.db.get(w.getBytes());
                        // check if the link is already in DB
                        // if it is not, content == null
                        if (content == null) {
                            // get last pageID value first
                            int id = this.getLastId("doc ");
                            id = id + 1;
                            db.put(w.getBytes(), ("doc " + id).getBytes()); 

                            // this also means this link does not exist in the page_info db
                            // so insert it with the current id as key

                            // first switch db
                            db.RocksDB.open(this.options, pageInfoPath);
                            String val = "";
                            // need to get title etc.
                            db.put(("doc " + id).getBytes(), val);
                        }
                        else {
                            // link already in the db
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

    public int getLastId(String type) {
        // gets the lastId of the current db, if db is empty, it will return a -1. first entry is a 0
        // type is the regex to remove from the value 
        // e.g "doc " is removed from value "doc 0" to get "0"
        RocksIterator iter = db.newIterator();
        int id = -1;
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            id++;
        }
        return id;
    }

    public int getPageId(RocksIterator iter) {
        String result = (new String(iter.value())).replaceAll("doc ", "");
        int id = Integer.parseInt(result);
        return id;
    }

    public void addEntry(String word, int x, int y) throws RocksDBException
    {
        // Add a "docX Y" entry for the key "word" into hashtable
        // ADD YOUR CODES HERE
        byte[] content = db.get(word.getBytes());
        if (content == null) {
            content = ("doc" + x + " " + y).getBytes();
        } else {
            content = (new String(content) + " doc" + x + " " + y).getBytes();
        }
        db.put(word.getBytes(), content);
    }
    public void delEntry(String word) throws RocksDBException
    {
        // Delete the word and its list from the hashtable
        // ADD YOUR CODES HERE
        db.remove(word.getBytes());
    } 
    public void printAll() throws RocksDBException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
        RocksIterator iter = db.newIterator();
                    
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.key()) + "=" + new String(iter.value()));
        }
    }    
    
    public static void main(String[] args)
    {
        try
        {
            // a static method that loads the RocksDB C++ library.
            RocksDB.loadLibrary();

            // modify the path to your database
            String path = "/Users/albertpare/Codes/searchEngine/assets/db";
            
            InvertedIndex index = new InvertedIndex(path);
    
            index.addEntry("cat", 2, 6);
            index.addEntry("dog", 1, 33);
            System.out.println("First print");
            index.printAll();
            
            index.addEntry("cat", 8, 3);
            index.addEntry("dog", 6, 73);
            index.addEntry("dog", 8, 83);
            index.addEntry("dog", 10, 5);
            index.addEntry("cat", 11, 106);
            System.out.println("Second print");
            index.printAll();
            
            index.delEntry("dog");
            System.out.println("Third print");
            index.printAll();
        }
        catch(RocksDBException e)
        {
            System.err.println(e.toString());
        }
    }
}
