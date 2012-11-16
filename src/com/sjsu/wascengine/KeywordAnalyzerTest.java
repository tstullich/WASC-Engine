package com.sjsu.wascengine;

import com.lowagie.text.DocumentException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;


/**
* @author Michael Riha
*/
public class KeywordAnalyzerTest
{
    public void testKeywordAnalyzer() throws FileNotFoundException, IOException, DocumentException
    {
        // Test readKeywordFile
        KeywordAnalyzer instance = new KeywordAnalyzer();
        instance.readKeywordFile("testfiles/keywords.txt");
        
        // Get a test pdf and parse the text
        String filename = "testfiles/CommStudiesProvostLetterFinal.pdf";
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
        System.out.println(sb.toString());
    }
}