package com.sjsu.wascengine;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Program for extracting words from a PDF document
 * @author Akshat Kukreti
 */
public class PdfExtract {
    private static final int START_SIZE = 2000;
    
    /**
     * function for reading a PDF document and extracting words from it
     * @param filename is the path to the PDF document
     * @return cleanwords is an ArrayList containing all the words present in 
     * the document
     * @throws DocumentException
     * @throws IOException 
     */
    public static ArrayList<String> convertToText(InputStream filename) 
            throws DocumentException, IOException
    {
        boolean brackopen = false;
        boolean brackclose = false;
        
        //Stores the words extracted from the pdf
        ArrayList<String> words = new ArrayList<String>(START_SIZE);
        
        //Accumuates a word
        StringBuilder word = new StringBuilder();

        if(filename == null){
            System.exit(1);
        }
        
        try{
            PdfReader reader = new PdfReader(filename);
            int numpages = reader.getNumberOfPages();
            for(int i=1; i<=numpages; i++){
                byte[] pagecontent = reader.getPageContent(i);
                int contentlength = pagecontent.length;
                char[] charcontent = new char[contentlength];
                
                for(int j=0; j<contentlength; j++){
                    char c = (char)pagecontent[j];
                    charcontent[j] = c;
                }
                
                //Escaped characters
                for(int k=0; k<contentlength; k++){
                    if(charcontent[k] == '\\'){
                        charcontent[k] = ' ';
                        charcontent[k+1] = ' ';
                    }
                }
                
                for(int l=0; l<contentlength; l++){
                    if(charcontent[l] == '('){
                        brackopen = true;
                        continue;
                    }
                    if(charcontent[l] == ')'){
                        brackclose = true;
                    }
                    if(brackopen && !brackclose){
                        char c = charcontent[l];
                        if((int)c >= 65 && (int)c <= 90 || 
                                (int)c >= 97 && (int)c <= 122 ){
                            char lowerc = Character.toLowerCase(c);
                            word.append(lowerc);
                        }
                        else if(c == '-'){
                            word.append(c);
                        }
                        if(c == ' '){
                            String wordstring = word.toString();
                            if (!wordstring.isEmpty()){
                                words.add(wordstring);
                                word.delete(0, word.length());
                            }
                        }
                    }
                    if(brackopen && brackclose){
                        brackopen = false;
                        brackclose = false;
                    }
                }
            }
        }
        catch(IOException e){
            System.out.println(e.toString());
        }
        
        /**
          *Cleaning up words. Getting rid of strings that are only hyphens and
          *words that are only spaces
          */ 
        ArrayList<String> cleanwords = new ArrayList<String>(START_SIZE);
        Iterator<String> worditerator = words.iterator();
        while(worditerator.hasNext()){
            String str = worditerator.next().toString();
            if(str.indexOf('-') == 0 || str.indexOf(' ') == 0){
            }
            else{
                cleanwords.add(str);
            }
        }
        
        return cleanwords;      
    }
}

