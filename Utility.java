package sample;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;

public class Utility {

	/*
	 * Method to get the DB Connection.
	 */
	public Connection getDBConnection()
	{
		String dbUrl = "jdbc:mysql://localhost:3307/db”;
		Connection con=null;
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(dbUrl,"root","password");
		}

		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}

		catch(SQLException e) {
			e.printStackTrace();
		}
		return con;
	}

	/*
	 * Method to create a Character Set to store the Stop words List.
	 */
	public Set<char[]> createStopWordsSet()
	{
		Set<char[]> stopWordsSet = new HashSet<char[]>();
		try
		{
			FileInputStream fstream = new FileInputStream("StopWords.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			char[] charline;

			while ((strLine = br.readLine()) != null) 	
			{
				charline = strLine.toCharArray();
				stopWordsSet.add(charline);
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return stopWordsSet;
	}

	/*
	 * Method to calculate the TF for a given set of Keywords.
	 */
	public Map<String,Float> createTF(TokenStream keywords,String rowData) throws IOException
	{
		Map<String,Float> termFreq = new HashMap<String,Float>();
		List<String> keywordsList = new ArrayList<String>(); 
		while(keywords.incrementToken())
		{
			keywordsList.add(keywords.getAttribute(CharTermAttribute.class).toString());
		}
		String keyword="";
		String[] rowDataArr = rowData.split("[ ]+");
		int noOfWords = rowDataArr.length;
		float counter = 0;
		try 
		{
			for(int j=0;j<keywordsList.size();j++)
			{
				keyword = keywordsList.get(j);
				counter=0;
				for (int i=0;i<keywordsList.size();i++)
				{
					if(keywordsList.get(i).equalsIgnoreCase(keyword))
					{
						counter++;
					}
				}
				termFreq.put(keyword, counter/(float)noOfWords);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return termFreq;
	}
	
	public Map<String,Float> countijvalue(String keyword,List<String> rowData) throws IOException
	{
		Map<String,Float> termFreq = new HashMap<String,Float>();
		float counter = 0;
		boolean b=true;
		try 
		{
			for(int i=0;i<rowData.size();i++)
			{
				String[] rowDataArr = rowData.get(i).split("[ ]+");
				for(int j=0;j<rowDataArr.length;j++)
				{
					if(rowDataArr[j].equalsIgnoreCase(keyword))
					{
						counter++;
					}
				}
			}
			termFreq.put(keyword, counter);
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return termFreq;
	}

	public Map<String,Float> createauthorTF(TokenStream keywords,String rowData) throws IOException
	{
		Map<String,Float> termFreq = new HashMap<String,Float>();
		List<String> keywordsList = new ArrayList<String>(); 
		while(keywords.incrementToken())
		{
			keywordsList.add(keywords.getAttribute(CharTermAttribute.class).toString());
		}
		String keyword="";
		float counter = 0;
		try 
		{
			for(int j=0;j<keywordsList.size();j++)
			{
				keyword = keywordsList.get(j);
				counter=0;
				for (int i=0;i<keywordsList.size();i++)
				{
					if(keywordsList.get(i).equalsIgnoreCase(keyword))
					{
						counter++;
					}
				}
				termFreq.put(keyword, counter);
			}
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return termFreq;
	}

	/*
	 * Method to calculate the weighted count of a given set of Keywords.
	 */
	public Map<String,Float> createTF(TokenStream keywords,String rowData,float weight) throws IOException
	{
		Map<String,Float> termFreq = new HashMap<String,Float>();
		List<String> keywordsList = new ArrayList<String>(); 
		while(keywords.incrementToken())
		{
			keywordsList.add(keywords.getAttribute(CharTermAttribute.class).toString());
		}
		String keyword="";
		float counter = 0;
		try 
		{
			for(int j=0;j<keywordsList.size();j++)
			{
				keyword = keywordsList.get(j);
				counter=0;
				for (int i=0;i<keywordsList.size();i++)
				{
					if(keywordsList.get(i).equalsIgnoreCase(keyword))
					{
						counter++;
					}
				}
				termFreq.put(keyword, counter*weight);
			}
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return termFreq;
	}

	/*
	 * Method to generate the TF and the TF-IDF vector mapping
	 */
	public void createTFIDF(int noOfDocs, Directory indexDir, Map<String,Float> termFreq, String arg) throws IOException
	{
		IndexReader iReader = IndexReader.open(indexDir);

		int docFreq = 0;
		Similarity similarity = new DefaultSimilarity();
		float tf = 0;
		float idf = 0;
		float tfidf=0;

		Map<String,Float> idfMap = new HashMap<String, Float>();

		for(Map.Entry<String, Float> keyword: termFreq.entrySet()){
			Term t = new Term("doc",keyword.getKey().toLowerCase());
			docFreq = iReader.docFreq(t);
			idf = similarity.idf(docFreq, noOfDocs);
			tf = keyword.getValue();
			tfidf = tf*idf;
			idfMap.put(keyword.getKey(), tfidf);
			
		}

		termFreq = sortByComparator(termFreq);
		idfMap = sortByComparator(idfMap);

		if(arg.equalsIgnoreCase("TF"))
		{
			for(Map.Entry<String, Float> keyword: termFreq.entrySet()){
				tf = keyword.getValue();
				System.out.println("TF: " + "{" + keyword.getKey() + "," + tf + "}");
			}
		}

		if(arg.equalsIgnoreCase("TF-IDF") || arg.equalsIgnoreCase("TF-IDF2"))
		{
			for(Map.Entry<String, Float> keyword: idfMap.entrySet()){
				tf = keyword.getValue();
				if(arg.equalsIgnoreCase("TF-IDF"))
					System.out.println("****TF-IDF****: " + "{" + keyword.getKey() + "," + keyword.getValue() + "}");
				else
					System.out.println("****TF-IDF2****: " + "{" + keyword.getKey() + "," + keyword.getValue() + "}");
			}
		}
	}


	/*
	 * Method to sort the vector maps in descending order
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String,Float> sortByComparator(Map<String,Float> inputMap) {

		LinkedList linkedList = new LinkedList(inputMap.entrySet());
		List list = linkedList;
		Collections.sort(list, new Comparator() 
		{
			public int compare(Object ele1, Object ele2)
			{
				return ((Comparable) ((Map.Entry) (ele2)).getValue()).compareTo(((Map.Entry) (ele1)).getValue());
			}
		}
				);
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry)it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
