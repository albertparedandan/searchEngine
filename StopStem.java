import IRUtilities.*;
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


public class StopStem
{
	private Porter porter;
	private java.util.HashSet stopWords;
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);	
	}
	public StopStem(String str)
	{
		super();
		porter = new Porter();
		stopWords = new java.util.HashSet();
				
		// stopWords.add("is");
		// stopWords.add("am");
		// stopWords.add("are");
		// stopWords.add("was");
		// stopWords.add("were");

		try {
			BufferedReader reader = new BufferedReader(new FileReader(str));
			String line;
			while ((line = reader.readLine()) != null) {
				stopWords.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}
	public static void main(String[] arg)
	{
		StopStem stopStem = new StopStem("stopwords.txt");
           
		try{
			BufferedReader crawled = new BufferedReader(new FileReader("CrawledResults.txt"));
            FileWriter result = new FileWriter("CrawledResults-StopStem.txt");
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
	}
}