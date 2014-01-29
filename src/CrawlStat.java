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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import edu.uci.ics.crawler4j.url.WebURL;

public class CrawlStat {
        private int 					mNumPages;			//# of pages
        private long 					mLongestPageLength; //# words in longest page
        private Map<String, Integer> 	mSubdomainsMap;		//subdomain names and frequencies
        private Map<String, Integer> 	mWordMap;			//words and frequencies Map
        private Map<String, Integer> 	m2gramMap;			//2grams and frequencies Map
        private int 					mMaxTextLength;		//Maximum text length

        //Helper function for robust mapping of values
        private void insertMap(Map<String, Integer> map, String word){
        	Integer count = map.get(word);
        	if(count == null)
        		map.put(word, 1);
        	else{
        		map.put(word, count+1);
        	}
        }
        
        //Helper function sorts a map and returns top n values
        private ArrayList<Frequency> topNMap(Map<String, Integer>freqMap, int topNvals){
        	if(freqMap.size() < 1) return null;
        	
        	//Sort by frequency, then by alphabetical order
    		Map.Entry<String, Integer>[] entries = freqMap.entrySet().toArray(new Map.Entry[0]);
    		Arrays.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    Integer v1 = o1.getValue();
                	Integer v2 = o2.getValue();
                    
                    //First compare by frequency
                    int comp = -1*v1.compareTo(v2);
                    if(comp != 0){
                    	return comp;
                    } else {	//if that doesn't work, compare by alphabet
                    	String k1 = o1.getKey();
                    	String k2 = o2.getKey();
                    	return k1.compareTo(k2);
                    }
                }
            });
    		
    		//Create the list
    		ArrayList<Frequency> wordList = new ArrayList<Frequency>();
    		
    		int counter = 0;
    		for(Map.Entry<String, Integer> entry:entries){
    			if(counter < topNvals)
    				wordList.add(new Frequency(entry.getKey(), entry.getValue()));
    			else break;
    		}
    		return wordList;
        }
        
        public int getNumPages() {
                return mNumPages;
        }

        public void incNumPages() {
        	mNumPages += 1;
        }
        
        public long getLongestPageLength() {
            return mLongestPageLength;
        }
        
        public void setLongestPageLength(long pageLength) {
        	if (pageLength > mLongestPageLength) {
        		mLongestPageLength = pageLength;
        	}
        }
        
        public Map<String, Integer> getSubdomainsMap() {
            return mSubdomainsMap;
        }

        //parses the url to its domain and subdomain and indexes its location
        public void insertSubdomainsMap(WebURL url){
        	String link = url.getDomain().toString() + url.getParentUrl().toString();
        	insertMap(this.mSubdomainsMap, link);
        }
        
        public Map<String, Integer> getWordMap() {
            return mWordMap;
        }
        
        //Adds a word into the word map
        public void insertWordMap(String word){
        	insertMap(this.mWordMap, word);
        }
        
        //returns top n words
        public ArrayList<Frequency> getTopNWords(int n){
        	return topNMap(this.mWordMap, n);
        }
        
        //returns 2gram map
        public Map<String, Integer> get2gramMap() {
            return m2gramMap;
        }
        
        //Insert a word into the 2gram map
        public void insert2gramMap(String word){
        	insertMap(this.m2gramMap, word);
        }
        
        //returns top n 2grams
        public ArrayList<Frequency> getTopNGrams(int n){
        	return topNMap(this.m2gramMap, n);
        }
        
        public int getMaxTextLength(){
        	return mMaxTextLength;
        }
        
        //update the max text length
        public void updateMaxTextLength(int n){
        	if(n > mMaxTextLength) 
        		mMaxTextLength = n;
		}
}
		