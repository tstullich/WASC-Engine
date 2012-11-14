package com.sjsu.wascengine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import com.sjsu.wascengine.PrefixTree;
import com.sjsu.wascengine.PrefixTree.Node;

/**
 * A KeywordAnalyzer that is capable of performing keyword searches on an
 * ArrayList<String> based on various criteria and assigning rubric scores to 
 * each string of text
 * @author Michael Riha
 */
public class KeywordAnalyzer 
{
    private SortedSet<String>[][] keywordsUsed;
    private PrefixTree keywordTree;
    private static final int RUBRICS = 5, WEIGHTS = 2;
    private static final int a = 6, b = 1, c = 4;
    
    /**
     * Reads keywordTree.txt in CWD and builds a prefix tree from it. The expected 
     * formatting for each line, one entry per line, is: keyword,weight,rubric
     * CSV values with no spacing. E.g. Critical,2,1
     */
   @SuppressWarnings("unchecked")
   public KeywordAnalyzer() throws FileNotFoundException, IOException
    {
        keywordsUsed= new SortedSet[RUBRICS][WEIGHTS];
        
        keywordTree = new PrefixTree();
        BufferedReader br = new BufferedReader(new FileReader("keywords.txt"));
        try 
        {
            String[] line = br.readLine().split(",");
            keywordTree.add(line[0], Integer.valueOf(line[1]), 
                                                    Integer.valueOf(line[2]));
            
            while (line != null)
            {
                line = br.readLine().split(",");
                keywordTree.add(line[0], Integer.valueOf(line[1]), 
                                                    Integer.valueOf(line[2]));
            }
        } finally 
        {
            br.close();
        }
    }
    
    /**
     * Calculates all rubric scores for an arbitrary amount of ASCII text
     * @param words the text to analyze
     * @return an array of scores between 0.0 and ?.0. Index 5 contains the
     * total score, and 0-4 contain those pertaining to the 5 proficiencies
     * in the order given
     */
    public double[] calculateRubricScores(ArrayList<String> words)
    {
        Node values;
        int totalWords = 0, rubric, weight;
        double[] scores = new double[RUBRICS+1]; 
        
        /* Rubrics 1-5 stored in indices 0-4. The second dimension of the 
         array contains counts of how many keywordsUsedof each weight were found. 
         e.g. wordCounts[4][1] is how many words of weight 2 were in rubric 5 */
        int[][] wordCounts = new int[RUBRICS][WEIGHTS];
        
        // parse the input and collect wordcount statistics for calculation
        for (String word : words)
        {
            values = keywordTree.find(word);
            if (values != null)
            {
                rubric = values.getRubric();
                weight = values.getWeight();
                ++wordCounts[rubric - 1][weight - 1];
                keywordsUsed[rubric - 1][weight - 1].add(word);
            }
            ++totalWords;
        } 
        
        // calculate the scores, average the sum of the scores for the total
        double score, sum = 0.0;
        for (int i = 0; i < RUBRICS; ++i)
        {
            score = calculateRubricScore(wordCounts[i][0], wordCounts[i][1], 
                                                                    totalWords);
            scores[i] = score;
            sum += score;
            
        }
        scores[RUBRICS] = sum / RUBRICS;
        return scores;
    }
    
    /**
     * Calculates an individual rubric score based on the number of weight one
     * and two words as well as the total number of words in the document
     * @param weightOneCount the number of weight one words
     * @param weightTwoCount the number of weight two words
     * @param totalWordCount the total word count
     * @return a rubric score between 0.0 and ?.0 based on the formula
     * score= (N/a)*((1+d1)^b)*((1+dr)^c)
     * N: Total number of keywords
     * d1: Density of weight one words (weightOneCount / N)
     * d2: Density of weight two words (weightTwoCount / N)
     * dr: Density of related words (N / totalWordCount)
     */
    public double calculateRubricScore(int weightOneCount, int weightTwoCount,
                                        int totalWordCount)
    {
        if (totalWordCount == 0) return 0.0;
        
        int N = weightOneCount + weightTwoCount;
        double d1 = weightOneCount / N;
        //double d2 = weightTwoCount / N;
        double dr = N / totalWordCount;
        
        return Math.min(1, (N / a) * Math.pow(1 + d1, b) * Math.pow(1 + dr, c));
    }
}
