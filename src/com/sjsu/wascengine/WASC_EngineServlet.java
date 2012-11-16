package com.sjsu.wascengine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;

import javax.servlet.http.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
public class WASC_EngineServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	    resp.setContentType("text/json");
		//Just a quick test to see how we can convert an object to JSON	   
		String[] names = {"Tim", "Eddy", "Michael"};
		JsonObject obj = new JsonObject();
		JsonArray numArray = new JsonArray();
		for (String word : names)
		{
		   numArray.add(new JsonPrimitive(word));
		}
		obj.addProperty("numOfContributors", names.length);
		obj.add("contributors", numArray);
		String json = obj.toString();
		resp.getWriter().println(json + "\n");
		
		resp.getWriter().println("\n--------Starting PDF file analysis---------\n");
		
		try {
			ArrayList<String> filenames = new ArrayList<String>();
			filenames.add("testfiles/CommStudiesProvostLetterFinal.pdf");
			filenames.add("testfiles/Journalism_Provost_Report_APRIL_5_2011.pdf");
			for(String file : filenames) {
				testKeywordAnalyzer(resp, file);
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
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
        StringBuilder sb = new StringBuilder();
        int[][] wordCounts = instance.getWordCounts();
        SortedSet<String>[][] sets = instance.getKeywordsUsed();
        
        for (int i = 0; i < 5; ++i)
            for (int j = 0; j < 2; ++j)
            {
                if (j == 0)
                    totOne += wordCounts[i][j];
                else
                    totTwo += wordCounts[i][j];
                total += wordCounts[i][j];
            }
        sb.append("Keywords in ").append(filename).append(": ").append(total);
        sb.append("\nTotal Weight 1 Keywords: ").append(totOne).append("\n");
        sb.append("Total Weight 2 Keywords: ").append(totTwo);
        sb.append("\nTotal words: ").append(instance.getTotalWords());
        double[] scores = instance.calculateScores();
        for (int i = 0; i < 5; ++i)
        {
            sb.append("\n\nRubric ").append(i+1).append(" Score: ");
            sb.append(String.format("%.2f", scores[i]));
            for (int j = 0; j < 2; ++j)
            {
                sb.append("\n Weight ").append(j+1).append(" keywords used: ").append(wordCounts[i][j]).append("\n ");
                for (String word : sets[i][j])
                {
                    swap = instance.getKeywordOccurrences(word);
                    total += swap;
                    sb.append(word).append(" ").append(swap).append(" ");
                }
            }
        }
        resp.getWriter().println(sb.toString());
        resp.getWriter().println("\n--------------End of file analysis-----------------\n");
    }
}