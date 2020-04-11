/* --
   Our crawler file
*/
import java.util.Vector;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream; 
import java.io.PrintStream; 
import java.util.Locale; 
import java.io.IOException;


public class Crawler
{
	private String url;
	Crawler(String _url)
	{
		url = _url;
	}
	public Vector<String> extractWords() throws ParserException

	{
		// extract words in url and return them
		// use StringTokenizer to tokenize the result from StringBean
		// ADD YOUR CODES HERE
		 Vector<String> result = new Vector<String>();
                StringBean bean = new StringBean();
                bean.setURL(url);
                bean.setLinks(false);
                String contents = bean.getStrings();
                StringTokenizer st = new StringTokenizer(contents);
                while (st.hasMoreTokens()) {
					if (!result.contains(st))
                    	result.add(st.nextToken());
                }
                return result;
			
	}
	public Vector<String> extractLinks() throws ParserException

	{
		// extract links in url and return them
		// ADD YOUR CODES HERE
		Vector<String> result = new Vector<String>();
                LinkBean bean = new LinkBean();
                bean.setURL(url);
                URL[] urls = bean.getLinks();
                for (URL s : urls) {
					if (!result.contains(s))
                    	result.add(s.toString());
                }
                return result;
                
	}
	
	public static void main (String[] args)
	{
		try
		{
			Crawler crawler = new Crawler("http://www.cse.ust.hk");
			FileWriter crawled = new FileWriter("CrawledResults.txt");
			
			Vector<String> words = crawler.extractWords();		
			
			System.out.println(words.size() + " Words in "+crawler.url+":");
			crawled.write(words.size() + " Words in "+crawler.url+":\n");
			for(int i = 0; i < words.size(); i++){
				System.out.print(words.get(i) + " ");
				crawled.write(words.get(i) + " ");
			}
			crawled.write("\n\n");
			System.out.print("\n\n");
	
			Vector<String> links = crawler.extractLinks();
			System.out.println(links.size() + " Links in "+crawler.url+":");
			crawled.write(links.size() + " Links in "+crawler.url+":\n");
			for(int i = 0; i < links.size(); i++)		
				crawled.write(links.get(i) + "\n");
			crawled.flush();
			crawled.close();			
		}
		catch (ParserException e)
		{
			e.printStackTrace ();
		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}

	}
}
	
