import java.util.*;
import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;
import org.htmlparser.util.ParserException;
import org.rocksdb.RocksDBException;  
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;


public class Main
{
    private Crawler crawler;
    private StopStem stopStem;
    private InvertedIndex iIndex;
	private String dbPath;

	public Main(String url, String stopwords, String db)
	{
		super();
        crawler = new Crawler(url);
		stopStem = new StopStem(stopwords);
		dbPath = db;
        try {
            iIndex = new InvertedIndex(db);
        }
        catch(RocksDBException dbe) {
            System.err.println(dbe.toString());
        }
	}
	
	public void printResults(String name)
	{
		try {
			FileWriter spider_results = new FileWriter(name);

			for (int i = 0; i < 27; ++i){
				String key = "doc " + i;
				Vector<String> results = iIndex.getDocDetails(key);
				spider_results.write(results.get(1) + "\n");
				spider_results.write(results.get(2) + "\n");
				spider_results.write(results.get(3) + ", " + results.get(4) + "\n");
				String[] keywords = results.get(6).split(";", 0);
				for(int j = 0; j < keywords.length; ++j){
					spider_results.write(keywords[j] + "; ");
				}
				spider_results.write("\n");
				String[] children = results.get(5).split(" ", 0);
				for(int j = 0; j < children.length; ++j){
					spider_results.write(children[j] + "\n");
				}
				spider_results.write("---------------------------------------------------------------------\n");
			}
			spider_results.flush();
			spider_results.close();
		}
		
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
	}

    public void crawl(String name)
    {
        try{
			FileWriter crawled = new FileWriter(name);

			// write data from the base url
			Vector<String> words = crawler.extractWords();		
			crawled.write(crawler.getUrl() + "\n");

				// write the title page
				crawled.write(crawler.getTitle() + "\n");

				// write page size of base url
				crawled.write(crawler.pageSize() + "\n");

				// write last modified date of base url
				crawled.write(crawler.lastModified() + "\n");

			for(int i = 0; i < words.size(); i++){
				crawled.write(words.get(i) + " ");
			}
			crawled.write("\n");
	
			// get the links from the base url
			Vector<String> links = crawler.extractLinks();

			// write child links from the base url
			for(int k = 0; k < 30; ++k){
				crawled.write(links.get(k) + " ");
			}
			crawled.write("\n");

			// write words contained in each of the child links for the first 30 pages
			for(int i = 0; i < 30; i++) {
				crawled.write(links.get(i) + "\n");
				Crawler c = new Crawler(links.get(i));

					// write the title page
					crawled.write(c.getTitle() + "\n");

					// write page size of base url
					crawled.write(c.pageSize() + "\n");

					// write last modified date of base url
					crawled.write(c.lastModified() + "\n");
				Vector<String> w = c.extractWords();

				for(int j = 0; j < w.size(); ++j){
					crawled.write(w.get(j) + " ");
				}
				crawled.write("\n");

				// write child links from the children of base url
				Vector<String> l = c.extractLinks();
				for(int k = 0; k < l.size(); ++k){
					crawled.write(l.get(k) + " ");
				}
				crawled.write("\n");
			}	
				
			crawled.flush();
			crawled.close();
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
        catch (ParserException e)
		{
			e.printStackTrace ();
		}
    }

    public void stopAndStem(String read, String name)
    {
        try{
			BufferedReader crawled = new BufferedReader(new FileReader(read));
            FileWriter result = new FileWriter(name);
            String line;
            int lineNum = 0;
                while ((line = crawled.readLine()) != null) {
					if (line.contains("TITLE:")){
						result.write(line + "\n");
					}
					else{
						String[] words = line.split("\\s");
						for (String w: words) {
                            if (lineNum % 6 == 0) {
                                // parent URL
                                result.write(w + "");
                            }
                            else if (w.contains("http") || w.contains(":") || w.contains("-1")){
								result.write(w + " ");
							}
							else if (stopStem.isStopWord(w))
								continue;
							else {
								result.write(stopStem.stem(w) + " ");                            
							}
						}
						result.write("\n");
					}   
                lineNum++;
                }
            result.flush();
            result.close();
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
		}
    }
    
    public void storePages(String read, String read2) {
       iIndex.parsePages(read, read2); 
    }

	public static void main(String[] arg) 
	{         
		final String DB_PATH= "/Users/albertpare/Codes/searchEngine/assets/db";
            Main main = new Main("http://www.cse.ust.hk", "assets/stopwords.txt", DB_PATH);
            main.crawl("assets/CrawledResults.txt");
            main.stopAndStem("assets/CrawledResults.txt", "assets/CrawledResults-StopStem.txt");			
            main.storePages("assets/CrawledResults-StopStem.txt", "assets/CrawledResults.txt");
			main.printResults("assets/spider_result.txt");
	}
}
