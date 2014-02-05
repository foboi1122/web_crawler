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
	
		static int numPages;

        Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
                        + "|wav|avi|mov|mpeg|ram|m4v|pdf|csv" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

        static SqlWrapper mySqlWrapper;

        static Logger log = Logger.getLogger(
        		BasicCrawler.class.getName());
        
        public BasicCrawler() {
                mySqlWrapper = new SqlWrapper();
                numPages = 0;
        }

        @Override
        public boolean shouldVisit(WebURL url) {
                String href = url.getURL().toLowerCase();
                
                //we need to be careful around djp3-pc2... source code galore
                if(href.contains("?") && href.contains("djp3-pc2.ics.uci.edu")) {
                	href = href.substring(0, href.indexOf("?"));
					WebURL webUrl = new WebURL();
					webUrl.setURL(href);
					webUrl.setDocid(-1);
					webUrl.setDepth((short) 0);
					if (!this.getMyController().getRobotstxtServer().allows(webUrl)) {
						logger.info("Robots.txt does not allow this seed: " + href);
					} else {
						this.getMyController().getFrontier().schedule(webUrl);
					}
					
					return false;
				}
                
                return !filters.matcher(href).matches() && 
                		href.contains("ics.uci.edu") &&
                		!href.contains("calendar.ics.uci.edu") &&
                		!href.contains("informatics.uci.edu") &&		//lulz.. crawler gets confused
						!href.contains("physics.uci.edu") &&		//lulz.. crawler gets confused
				!href.contains("archive.ics.uci.edu/ml/datasets.html?") && //disallow querries to the server but still allow crawling of the page
				!href.contains("drzaius.ics.uci.edu/cgi-bin/cvsweb.cgi"); // so many dead links
        }

        @Override
        public void visit(Page page) {
        		int insertError = 0;	//flag if we get sqlite insertion error
        		int errorCounter = 0;		//counter for how many times we've got this error on this page
        		numPages++;
        		
                System.out.println(Integer.toString(numPages) +" Visited: " + page.getWebURL().getURL());
                
                if (page.getParseData() instanceof HtmlParseData) {
                        HtmlParseData parseData = (HtmlParseData) page.getParseData();
                        List<WebURL> links = parseData.getOutgoingUrls();
                        try {
                        	mySqlWrapper.InsertItem(page.getWebURL().getURL().toString(), parseData.getText().toString(), parseData.getHtml().toString());
                        } catch (Exception e) {
                        	insertError = 1;
                        	
                        	//TODO: Move this into main insertItem tryy statement
                        	while(insertError == 1 && errorCounter < 10){
                        		try{
                        			insertError = 0;
                        			log.debug("DB_ERR: retrying insert: " + page.getWebURL().getURL().toString());
                        			mySqlWrapper.InsertItem(page.getWebURL().getURL().toString(), parseData.getText().toString(), parseData.getHtml().toString());	
                        		}
                        		catch (Exception ex){
                        			errorCounter ++;
                        			insertError = 1;
                        		}
                        	}
                        	if(errorCounter >= 10)
                        		log.debug("DB_ERR: Unable to insert: " + page.getWebURL().getURL().toString());
                        }
                }
                // We dump this crawler statistics after processing every 50 pages
                if(numPages%200 == 0)
                	log.info("Update: "+Integer.toString(numPages) + " Crawled");
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
       }
}