package com.sjsu.wascengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowagie.text.DocumentException;
/**
 * This is going to be the "main" class for the App Engine. It
 * is going to listen on a certain port for an Http Request and
 * should receive a pdf file. It is then going to be doing the
 * work in order to figure out the appropriate rubric scores and
 * return the final result as a JSON object.
 * 
 * **Sorry to whoever has to take over this project after us.
 *   Godspeed, for you are in for a semester full of frustration and
 *   confusion. Don't let them get to you**
 *   
 * @author Tim Stullich
 *
 */
@SuppressWarnings("serial")
public class WASC_EngineServlet extends HttpServlet 
{
   private final int RUBRICS = 5;
   private final int WEIGHT_CATEGORIES = 2;
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException
	{
	   //Gonna try and capture the files that were sent
	   //through the request servlet
	   try{
	      ServletFileUpload upload = new ServletFileUpload();
	      //Sets the MIME type to be JSON
	      resp.setContentType("application/json");
	      
	      FileItemIterator iterator = upload.getItemIterator(req);
	      
	      JsonArray resultsArray = new JsonArray();
	      while (iterator.hasNext())
	      { 
	         FileItemStream item = iterator.next();
	         InputStream stream = item.openStream();
	         
	         //Checks whether there is a form field or not
	         if (!item.isFormField())
	         {
	             //The work for the files will be done here
	             resultsArray.add(analyzeText(resp, stream, item.getName()));
	         }
	      }
	      Gson gson = new GsonBuilder().setPrettyPrinting().create();
	      String json = gson.toJson(resultsArray);
	      resp.getOutputStream().print(json);
	   }
	   catch (Exception e)
	   {
	      throw new ServletException();
	   }
	}
	
	//This is officially the longest method I have ever seen
	//F*** subroutines, get money
	public JsonObject analyzeText(HttpServletResponse resp, InputStream fileStream, String filename) 
	      throws FileNotFoundException, IOException, DocumentException, com.itextpdf.text.DocumentException
    {
        // Test readKeywordFile
        KeywordAnalyzer analyzer = new KeywordAnalyzer();
        analyzer.readKeywordFile("testfiles/keywords.txt");
        
        // Get a test pdf and parse the text
        ArrayList<String> text = PdfExtract.convertToText(fileStream);
        analyzer.parseText(text);
        
        // Print a detailed report about the file
        int totOne = 0, totTwo = 0, total = 0, occurrences;

        //Create A JSON Object that will hold all the results
        JsonObject results = new JsonObject();
        results.addProperty("fileName", filename);
        
        int[][] wordCounts = analyzer.getWordCounts();
        
        //Calculates Some General Statistics
        for (int i = 0; i < RUBRICS; ++i)
        {
            for (int j = 0; j < WEIGHT_CATEGORIES; ++j)
            {
                if (j == 0)
                    totOne += wordCounts[i][j];
                else
                    totTwo += wordCounts[i][j];
                total += wordCounts[i][j];
            }
        }
        
        results.addProperty("totalKeywords", total);
        results.addProperty("weight1Keywords", totOne);
        results.addProperty("weight2Keywords", totTwo);
        results.addProperty("totalWords", analyzer.getTotalWords());
        
        //Scores for each individual rubric will be calculated here
        double[] scores = analyzer.calculateScores();
        results.addProperty("totalScore", scores[RUBRICS]);
        /*This JSON array will hold JsonObjects that contain
         *more information on each rubric including scores,
         *weigths and the frequency of certain keywords.
         */	 
        JsonArray rubricScores = new JsonArray();
        JsonObject aWordGroup;
        // Some stuff to group keywords together like this
        // ["address, addressed, addresses" : 42]
        SortedSet<String>[][] sets = analyzer.getKeywordsUsed();
        StringBuilder group = new StringBuilder(); // group of keywords
        String lastWildcard, word;
        Iterator < String > iter;
        boolean first;
	
        for (int i = 0; i < RUBRICS; ++i)
        {
            JsonObject rubricScore = new JsonObject(); 
            rubricScore.addProperty("rubric" + (i + 1) + "score", scores[i]);
            for (int j = 0; j < WEIGHT_CATEGORIES; ++j)
            {
                rubricScore.addProperty("weight" + (j + 1) + "WordsUsed", wordCounts[i][j]);
                //Counts the frequency of each word based in each weight class
                JsonArray wordFrequency = new JsonArray();
                // just in case there were NO keywords, skip to the next rubric
                if (sets[i][j].size() == 0)
                {
                    group.append("");
                    break; // if weight 1 is empty, this will prematurely skip 2
                }
                lastWildcard = sets[i][j].first();
                iter = sets[i][j].iterator();
                group = new StringBuilder();
                aWordGroup = new JsonObject();
                first = true;
                while (iter.hasNext()) // process each word in sets[i][j]
                {
                    word = iter.next();
                       if (word.startsWith(lastWildcard))
                       { // build part of a keyword group
                          if (first)
                            first = false;
                          else
                            group.append(", ");
                          group.append(word);
                       }
                       else // new keyword group found, so create a json object for
                       {    // the last keyword group with the total occurrences
                        aWordGroup = new JsonObject();
                        occurrences = analyzer.getKeywordOccurrences(lastWildcard);
                        aWordGroup.addProperty(group.toString(), occurrences);
                        wordFrequency.add(aWordGroup);
                        
                        // create a new keyword group and update the wildcard
                        group = new StringBuilder().append(word);
                        lastWildcard = word;
                    }
                } 
                // add the last group since it gets skipped by grouping logic
                aWordGroup = new JsonObject();
                occurrences = analyzer.getKeywordOccurrences(lastWildcard);
                aWordGroup.addProperty(group.toString(), occurrences);
                wordFrequency.add(aWordGroup);
                rubricScore.add("words"+ (j+1) + "Frequency", wordFrequency);
            }
            rubricScores.add(rubricScore);
        }
        results.add("rubricScores", rubricScores);
        return results;
    }
}