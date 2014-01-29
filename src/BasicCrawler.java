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


import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class BasicCrawler extends WebCrawler {

        Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
                        + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

        CrawlStat myCrawlStat;
        Downloader myDownloader;
        SqlWrapper mySqlWrapper;

        public BasicCrawler() {
                myCrawlStat = new CrawlStat();
                myDownloader = new Downloader();
                mySqlWrapper = new SqlWrapper();
        }

        @Override
        public boolean shouldVisit(WebURL url) {
                String href = url.getURL().toLowerCase();
                return !filters.matcher(href).matches() && href.startsWith("http://www.ics.uci.edu/");
        }

        @Override
        public void visit(Page page) {
                System.out.println("Visited: " + page.getWebURL().getURL());
                myCrawlStat.incNumPages();
                
                if (page.getParseData() instanceof HtmlParseData) {
                		myCrawlStat.insertSubdomainsMap(page.getWebURL());
                        HtmlParseData parseData = (HtmlParseData) page.getParseData();
                        List<WebURL> links = parseData.getOutgoingUrls();
//                        myCrawlStat.incTotalLinks(links.size());
                        try {
                        		myDownloader.processUrl(page.getWebURL().getURL());
                                myCrawlStat.setLongestPageLength(myDownloader.getTextLength());
                        } catch (Exception ignored) {
                                // Do nothing
                        }
                        try {
                        	mySqlWrapper.InsertItem(page.getWebURL().getURL().toString(), parseData.getText().toString(), parseData.getHtml().toString());
                        } catch (Exception e) {
                        	System.out.println("Oh crap, not inserting!");
                        }
                }
                // We dump this crawler statistics after processing every 50 pages
        }

        // This function is called by controller to get the local data of this
        // crawler when job is finished
        @Override
        public Object getMyLocalData() {
        	return myCrawlStat;
        }

        // This function is called by controller before finishing the job.
        // You can put whatever stuff you need here.
        @Override
        public void onBeforeExit() {
                dumpMyData();
                mySqlWrapper.Close();
        }

        public void dumpMyData() {
                int id = getMyId();
                try {
         
                	String str = "";
                	str += "Number of unique pages: " + Integer.toString(myCrawlStat.getNumPages()) + "\n";
                	str += "Number of unique pages: " + Integer.toString(myCrawlStat.getNumPages()) + "\n";
                	str += "Number of subdomains: " + Integer.toString(myCrawlStat.getSubdomainsMapLength()) + "\n";
                	str += "Number of words in longest page: " + Long.toString(myCrawlStat.getLongestPageLength()) + "\n";
        			File file = new File("output_stats.txt");
         
        			// if file doesnt exists, then create it
        			if (!file.exists()) {
        				file.createNewFile();
        			}
         
        			FileWriter fw = new FileWriter(file.getAbsoluteFile());
        			BufferedWriter bw = new BufferedWriter(fw);
        			bw.write(str);
        			bw.close();
         
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
                // This is just an example. Therefore I print on screen. You may
                // probably want to write in a text file.
//                System.out.println("Crawler " + id + "> Processed Pages: " + myCrawlStat.getTotalProcessedPages());
//                System.out.println("Crawler " + id + "> Total Links Found: " + myCrawlStat.getTotalLinks());
//                System.out.println("Crawler " + id + "> Total Text Size: " + myCrawlStat.getTotalTextSize());
       }
}