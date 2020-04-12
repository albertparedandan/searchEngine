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
import org.htmlparser.util.ParserException;

public class Main
{
    private Crawler crawler;
    private StopStem stopStem;

	public Main(String url, String stopwords)
	{
		super();
        crawler = new Crawler(url);
		stopStem = new StopStem(stopwords);
	}

    public void crawl(String name)
    {
        try{
			FileWriter crawled = new FileWriter(name);
			
			// write words from the base url
			Vector<String> words = crawler.extractWords();		
			crawled.write(crawler.url + " :\n");
			for(int i = 0; i < words.size(); i++){
				crawled.write(words.get(i) + " ");
			}
			crawled.write("\n");
	
			// get the links from the base url
			Vector<String> links = crawler.extractLinks();

			// write words contained in each of the child links for the first 30 pages
			for(int i = 0; i < 30; i++) {
				crawled.write(links.get(i) + " :\n");
				Crawler c = new Crawler(links.get(i));
				Vector<String> w = c.extractWords();

				for(int j = 0; j < w.size(); ++j){
					crawled.write(w.get(j) + " ");
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
                while ((line = crawled.readLine()) != null) {
                    String[] words = line.split("\\s");
                    for (String w: words) {
                        if (w.contains("http")){
                            result.write(w);
                        }
                        else if (stopStem.isStopWord(w))
                            continue;
                        else {
                            result.write(stopStem.stem(w) + " ");                            
                        }
                    }
                    result.write("\n");
                }
            result.flush();
            result.close();
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
	public static void main(String[] arg)
	{         
            Main main = new Main("http://www.cse.ust.hk", "stopwords.txt");
            main.crawl("CrawledResults.txt");
            main.stopAndStem("CrawledResults.txt", "CrawledResults-StopStem.txt");
	}
}