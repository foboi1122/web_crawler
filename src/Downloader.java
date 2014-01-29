/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * This class is used to extract data from a page for indexing. This includes
 * the URL of the page, list of words, 2-grams, and also ignores english stop words
 */
public class Downloader {

        private Parser parser;
        private PageFetcher pageFetcher;
        private ArrayList<String> wordList;
        private	ArrayList<String> twoGramList;
        private int textLength;
        private WebURL curURL;
        //hard coded here to improve speed
        private String[] stopwords={"a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours ", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"};
        
        public ArrayList<String> getWordList(){ return wordList;}
        public ArrayList<String> getTwoGramList(){ return twoGramList;}
        public int getTextLength(){ return textLength;}
        public WebURL getURL(){ return curURL;}
        
        static Logger log = Logger.getLogger(
        		Downloader.class.getName());
        
        public Downloader() {
                CrawlConfig config = new CrawlConfig();
                parser = new Parser(config);
                pageFetcher = new PageFetcher(config);
        }

        private Page download(String url) {
                curURL = new WebURL();
                curURL.setURL(url);
                PageFetchResult fetchResult = null;
                try {
                        fetchResult = pageFetcher.fetchHeader(curURL);
                        if (fetchResult.getStatusCode() == HttpStatus.SC_OK) {
                                try {
                                        Page page = new Page(curURL);
                                        fetchResult.fetchContent(page);
                                        if (parser.parse(page, curURL.getURL())) {
                                                return page;
                                        }
                                } catch (Exception e) {
                                        e.printStackTrace();
                                        log.info("Warning, link was discarded: " + url);
                                }
                        }
                } finally {
                        if (fetchResult != null)
                        {
                                fetchResult.discardContentIfNotConsumed();
                                
                        }                       
                }
                return null;
        }

        //Tokenizes an input String into words and removes stopwords
        private ArrayList<String> cleanStream(String text){
        	String[] splits = text.toLowerCase().split("[^a-zA-Z']+");			//tokenize on words only
    		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(splits));	//store tokens as array list
    		ArrayList<String> sWords = new ArrayList<String>(Arrays.asList(this.stopwords));
    		ArrayList<String> NewList = new ArrayList<String>();
    		int i=3;
            while(i < tokens.size() ){
                if(!sWords.contains(tokens.get(i))){
                    NewList.add((String) tokens.get(i));
                }
                i++;        
            }
            
            return NewList;
        }
        
        //Returns an arraylist of 2grams with no stopword removal
        //It's hacky, but I also count document word count here to save cycles
        private ArrayList<String> get2Grams(String text) {
    		if(text.length() < 1) return null;
    		
    		//We don't include the removal of stopwords in 2grams
    		String[] splits = text.toLowerCase().split("[^a-zA-Z']+");
    		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(splits));
    		this.textLength = tokens.size();		//count text length here
    		ArrayList<String> NewList = new ArrayList<String>();
    		
    		int idx = 0;
    		//Loop through each 2-gram and index its frequency
    		for (int i = 0; i< tokens.size()-1; i++){
    			String phrase = tokens.get(i) + " " + tokens.get(i+1);
    			NewList.add(phrase);
    		}
    		
    		return NewList;
    	}
        
        public void processUrl(String url) {
              	System.out.println("Processing: " + url);
                Page page = download(url);
                if (page != null) {
                	ParseData parseData = page.getParseData();
                    if (parseData != null) {
                    	if (parseData instanceof HtmlParseData) {
                    		HtmlParseData htmlParseData = (HtmlParseData) parseData;
                    		
                    		//Index all 2-grams, count text
//                    		twoGramList = this.get2Grams(((HtmlParseData) parseData).getText());
                    		
                    		//Tokenize text and remove stop words
//                    		wordList = cleanStream(((HtmlParseData) parseData).getText());
                    		
                    		
                    	}
                    } else {
                    	log.info("Couldn't parse the content: " + url);
                    }
                } else {
                	log.info("Couldn't fetch the content: " + url);
                }
                System.out.println("==============");
                
        }

        public static void main(String[] args) {
                Downloader downloader = new Downloader();
                downloader.processUrl("http://en.wikipedia.org/wiki/Main_Page/");
                
                System.out.println("URL: "+downloader.getURL().toString());
                System.out.println("Text Length: "+downloader.getTextLength());
                System.out.println("Two-Grams: "+downloader.getTwoGramList().toString());
                System.out.println("Word List: "+downloader.getWordList().toString());
        }
}