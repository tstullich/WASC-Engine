package com.sjsu.wascengine;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Akshat Kukreti
 */
public class PdfExtract {
    private static final int START_SIZE = 2000;
    public static ArrayList<String> convertToText(InputStream fileStream) throws DocumentException, IOException
    {
        boolean brackopen = false;
        boolean brackclose = false;
        
        //Stores the words extracted from the pdf
        ArrayList<String> words = new ArrayList<String>(START_SIZE);
        
        //Accumuates a word
        StringBuilder word = new StringBuilder();

        if(fileStream == null){
            System.exit(1);
        }
        
        try{
            PdfReader reader = new PdfReader(fileStream);
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
        
        //Correcting split up words and adding them
        ArrayList<String> unsplitwords; 
        unsplitwords= new ArrayList<String>(START_SIZE);
        Iterator<String> wordit = words.iterator();
            while(wordit.hasNext()){
                String word1 = wordit.next().toString();
                if(word1.charAt(word1.length() -1) == '-' && 
                        word1.length() > 1){
                    String word2 = wordit.next().toString();
                    word1 = word1.replace('-',' ');
                    String concatword = word1.concat(word2);
                    concatword = concatword.replaceAll(" ","");
                    unsplitwords.add(concatword);
                }
                else if(!word1.equals("-")) {
                    unsplitwords.add(word1);
                }
            }
        
        return unsplitwords;        
    }
}

