/* --
   Our crawler file
*/
import java.util.Vector;
import java.util.*;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import java.util.StringTokenizer;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream; 
import java.io.PrintStream; 
import java.util.Locale; 
import java.io.IOException;
import java.io.DataInputStream;
import java.io.EOFException;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.MalformedURLException;



public class Crawler
{
	private String url;
	Crawler(String _url)
	{
		url = _url;
	}

  public String getUrl() {
    String ret = this.url;
    return ret;
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

	public int pageSize() throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) new URL(this.url).openConnection();
		int fileLength = connection.getContentLength();
		return fileLength;
	}

	public String lastModified() throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) new URL(this.url).openConnection();
		long unixSeconds = connection.getLastModified();
		Date date = new java.util.Date(unixSeconds); 
		return date.toString();
	}
	
	public String getTitle() throws IOException, MalformedURLException, ParserException
	{
		Parser parser = new Parser(this.url);
		// parser.setInputHTML(MyHTML);
		parser.setEncoding("UTF-8");
		NodeList nl = parser.parse(null); 
		NodeList node_list= nl.extractAllNodesThatMatch(new TagNameFilter("title"),true);

		return node_list.elementAt(0).toString();
	}
	public static void main (String[] args)
	{
		final int SIZE = 30;
		final String BASE_URL = "http://www.cse.ust.hk";
		final String FILE_NAME = "CrawledResults.txt";
		try
		{
			Crawler crawler = new Crawler(BASE_URL);
			FileWriter crawled = new FileWriter(FILE_NAME);
			Parser e = new Parser(BASE_URL);
			System.out.println(e.getVersion());

			HttpURLConnection content = (HttpURLConnection) new URL(BASE_URL).openConnection();
			System.out.println(content.getContentLength());
			
			// write words from the BASE URL
			Vector<String> words = crawler.extractWords();		
			crawled.write(crawler.url + " :\n");
			for(int i = 0; i < words.size(); i++){
				crawled.write(words.get(i) + " ");
			}
			crawled.write("\n");
	
			// get the links from the base url
			Vector<String> links = crawler.extractLinks();

			// write child links from the base url
			for(int k = 0; k < SIZE; ++k){
				crawled.write(links.get(k) + " ");
			}
			crawled.write("\n");

			// write words contained in each of the child links for the first 30 pages
			for(int i = 0; i < SIZE; i++) {
				crawled.write(links.get(i) + " :\n");
				Crawler c = new Crawler(links.get(i));
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
	
