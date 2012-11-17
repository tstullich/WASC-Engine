package com.sjsu.wascengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;

import javax.servlet.http.*;

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
 * @author Tim Stullich
 *
 */
@SuppressWarnings("serial")
public class WASC_EngineServlet extends HttpServlet 
{
   private final int RUBRICS = 5;
   private final int WEIGHT_CATEGORIES = 2;
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException 
	{
	   
	   //Sets the MIME type to be JSON
	   resp.setContentType("text/json");
		
	   //Attempts to open the specific file.
		try 
		{
			ArrayList<String> filenames = new ArrayList<String>();
			//JsonObject results = new JsonObject();
			filenames.add("testfiles/CommStudiesProvostLetterFinal.pdf");
			filenames.add("testfiles/Journalism_Provost_Report_APRIL_5_2011.pdf");
			for(String file : filenames) {
				testKeywordAnalyzer(resp, file);
				resp.getWriter().println("\n-------Finished File Analyzing-------");
			}
		} catch (DocumentException e) {
			resp.getWriter().print("File Not Able To Be Opened");
			e.printStackTrace();
		}
		
		
	}
	
	public void testKeywordAnalyzer(HttpServletResponse resp, String filename) throws FileNotFoundException, IOException, DocumentException
    {
        // Test readKeywordFile
        KeywordAnalyzer instance = new KeywordAnalyzer();
        instance.readKeywordFile("testfiles/keywords.txt");
        
        // Get a test pdf and parse the text
        ArrayList<String> text = PdfExtract.convertToText(filename);
        instance.parseText(text);
        
        // Print a detailed report about the file
        int totOne = 0, totTwo = 0, total = 0, swap;

        //Create A JSON Object that will hold all the results
        JsonObject results = new JsonObject();
        results.addProperty("fileName", filename);
        
        int[][] wordCounts = instance.getWordCounts();
        SortedSet<String>[][] sets = instance.getKeywordsUsed();
        
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
        results.addProperty("totalWords", instance.getTotalWords());
        
        //Scores for each individual rubric will be calculated here
        double[] scores = instance.calculateScores();
        
        /*This JSON array will hold JsonObjects that contain
         *more information on each rubric including scores,
         *weigths and the frequency of certain keywords.
         */
        JsonArray rubricScores = new JsonArray();
        for (int i = 0; i < RUBRICS; ++i)
        {
           JsonObject rubricScore = new JsonObject(); 
           rubricScore.addProperty("rubric" + (i + 1) + "score", scores[i]);
            for (int j = 0; j < 2; ++j)
            {
               rubricScore.addProperty("weight" + (j + 1) + "WordsUsed", wordCounts[i][j]);
               //Counts the frequency of each word based on weight
               JsonArray wordFrequency = new JsonArray();
                for (String word : sets[i][j])
                {
                    swap = instance.getKeywordOccurrences(word);
                    total += swap;
                    JsonObject aWord = new JsonObject();
                    aWord.addProperty(word, swap);
                    wordFrequency.add(aWord);
                }
                rubricScore.add("words"+ (j+1) + "Frequency", wordFrequency);
            }
            rubricScores.add(rubricScore);
        }
        results.add("rubricScores", rubricScores);
        resp.getWriter().print(results.toString());
    }
}