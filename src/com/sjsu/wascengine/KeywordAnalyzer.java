package com.sjsu.wascengine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import com.sjsu.wascengine.PrefixTree.Node;

/**
 * A KeywordAnalyzer that is capable of performing keyword statistical analysis
 * on text. Calling parseText multiple times without a reset will continuously 
 * build word counts. Also, multiple keyword files can be read into the tree.
 * @author Michael Riha
 */
public class KeywordAnalyzer 
{
    private SortedSet<String>[][] keywordsUsed;
    private PrefixTree keywordTree;
    private int[][] wordCounts;
    private double[] scores;
    private int totalWords;
    
    private static final int RUBRICS = 5, WEIGHTS = 2;
    private static final int a = 6, b = 1, c = 4;
    
    /** Constructs a KeywordAnalyzer with no statistical data */
    @SuppressWarnings({ "unchecked", "rawtypes" })
   public KeywordAnalyzer()
    {
        keywordsUsed = new SortedSet[RUBRICS][WEIGHTS];
        wordCounts = new int[RUBRICS][WEIGHTS];
        
        for (int i = 0; i < RUBRICS; ++i)
            for (int j = 0; j < WEIGHTS; ++j)
            {
                keywordsUsed[i][j] = new TreeSet();
                wordCounts[i][j] = 0;
            }
        
        scores = new double[RUBRICS+1];
        keywordTree = new PrefixTree();
        totalWords = 0;
    }
    
    /** Resets word statistics and keyword occurrences in the tree */
    public void reset()
    { 
        for (int i = 0; i < RUBRICS; ++i)
            for (int j = 0; i < WEIGHTS; ++i)
            {
                keywordsUsed[i][j].clear();
                wordCounts[i][j] = 0;
            }
        
        scores = new double[RUBRICS+1];
        keywordTree.reset();
        totalWords = 0;
    }
    
    /** Creates a new, empty keyword tree */
    public void purgeKeywords() { keywordTree = new PrefixTree(); }
    
    /** 
     * Adds the keywords from the specified file to the keyword tree
     * Each line should contain one entry in the format: keyword,weight,rubric
     * Example: Critical,2,1 would create an entry with weight 2 for rubric 1
     * @param filename the file to read
     */
    public void readKeywordFile(String filename) throws FileNotFoundException, IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        try 
        {
            String line = br.readLine();
            String[] parts = line.split(",");
            keywordTree.add(parts[0], Integer.valueOf(parts[1]), Integer.valueOf(parts[2]));
            while (true) // while (line != null) causes NullPointerException
            {
                line = br.readLine();
                if (line == null) break;
                parts = line.split(",");
                keywordTree.add(parts[0], Integer.valueOf(parts[1]), Integer.valueOf(parts[2]));
            }
        } 
        finally { br.close(); }
    }
    
    /**
     * Parses each word in the input and updates the keyword statistics fields
     * @param words Plaintext alphabetic non-numeric words. one word per entry
     */
    public void parseText(ArrayList<String> words)
    {
        Node values;
        int rubric, weight;        
        for (String word : words)
        {
           if(word.length() != 1)
           {
              values = keywordTree.find(word);
              if (values != null)
              {
                 rubric = values.getRubric();
                 weight = values.getWeight();
                 ++wordCounts[rubric - 1][weight - 1];
                 keywordsUsed[rubric - 1][weight - 1].add(word);
              }
           }
           ++totalWords;
        }
    }
    
    /**
     * Calculates each rubric score and returns them in order with the total 
     * rubric score (simple average) at the end
     * @return an array of scores between 0.0 and 4.0. [r1, r2, r3, r4, r5, Tot]
     */
    public double[] calculateScores()
    {
        double score, sum = 0.0;
        for (int i = 0; i < RUBRICS; ++i)
        {
            score = calculateScore(wordCounts[i][0], wordCounts[i][1], totalWords);
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
     * @return a rubric score between 0.0 and 4.0 based on the formula
     * score = (N/a)*((1+d1)^b)*((1+dr)^c)
     * N: Total number of keywords
     * d1: Density of weight one words (weightOneCount / N)
     * dr: Density of related words (N / totalWordCount)
     */
    public static double calculateScore(int weightOneCount, int weightTwoCount,
                                        int totalWordCount)
    {
        if (totalWordCount <= 0) return 0.0;
        double N = (double) weightOneCount + weightTwoCount;
        double dr = N / totalWordCount;
        double d1 = (N == 0) ? 0 : weightOneCount / N;
        return Math.min(4, (N/a) * Math.pow(1+d1, b) * Math.pow(1+dr, c));
    }
    
   /**@param keyword the word to look for
    * @return the number of times the keyword was found
    */
    public int getKeywordOccurrences(String keyword)
    {
        return keywordTree.findNoIncrement(keyword).getOccurrences();
    }
    
    /**@return 2 dimensional array of sorted sets containing the keywords for
       keywordsUsed[rubric][weight] */
    public SortedSet<String>[][] getKeywordsUsed() { return keywordsUsed; }
    
    /**@return Scores 0-4 in this format [rub1, rub2, rub3, rub4, rub5, total] */
    public double[] getScores() { return scores; };
    
    /**@return the wordcounts for each [rubric][weight] */
    public int[][] getWordCounts() { return wordCounts; }
    
    /**@return the total number of words parsed since reset */
    public int getTotalWords() { return totalWords; }
}
