import org.apache.log4j.Logger;

import java.util.List;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class BasicCrawlController {

		private static String 	USER_AGENT = 		"UCI IR 76111817 36928917";
		private static String 	STORAGE_FOLDER = 	"/crawl/root/";
		private static int 	  	CRAWL_THREADS = 	2;
		private static int 	  	CRAWL_DELAY_MS = 	300;
		private static int 	  	CRAWL_DEPTH = 		2;
		private static int    	CRAWL_MAX_PG_TO_FETCH = 10;
		private static boolean 	CRAWL_RESUMABLE = 	false;
		private static String 	CRAWL_SEED = 		"http://www.ics.uci.edu/~lopes/";
		
		static Logger log = Logger.getLogger(
                BasicCrawlController.class.getName());
		
        public static void main(String[] args) throws Exception {

        		//Start the logger and timestamp
        		log.info("**********Crawler started at: "+System.currentTimeMillis()+"*************");
                /*
                 * crawlStorageFolder is a folder where intermediate crawl data is
                 * stored.
                 */
                String crawlStorageFolder = STORAGE_FOLDER;

                /*
                 * numberOfCrawlers shows the number of concurrent threads that should
                 * be initiated for crawling.
                 */
                int numberOfCrawlers = CRAWL_THREADS;

                CrawlConfig config = new CrawlConfig();

                config.setCrawlStorageFolder(STORAGE_FOLDER);

                /*
                 * Be polite: Make sure that we don't send more than 1 request per
                 * second (1000 milliseconds between requests).
                 */
                config.setPolitenessDelay(CRAWL_DELAY_MS);

                /*
                 * You can set the maximum crawl depth here. The default value is -1 for
                 * unlimited depth
                 */
                config.setMaxDepthOfCrawling(CRAWL_DEPTH);

                /*
                 * You can set the maximum number of pages to crawl. The default value
                 * is -1 for unlimited number of pages
                 */
                config.setMaxPagesToFetch(CRAWL_MAX_PG_TO_FETCH);

                /*
                 * This config parameter can be used to set your crawl to be resumable
                 * (meaning that you can resume the crawl from a previously
                 * interrupted/crashed crawl). Note: if you enable resuming feature and
                 * want to start a fresh crawl, you need to delete the contents of
                 * rootFolder manually.
                 */
                config.setResumableCrawling(CRAWL_RESUMABLE);
                
                //Set user agent
                config.setUserAgentString(USER_AGENT);
                
                /*
                 * Instantiate the controller for this crawl.
                 */
                PageFetcher pageFetcher = new PageFetcher(config);
                RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
                RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
                CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

                /*
                 * For each crawl, you need to add some seed urls. These are the first
                 * URLs that are fetched and then the crawler starts following links
                 * which are found in these pages
                 */

                //controller.addSeed("http://www.ics.uci.edu/");
                controller.addSeed(CRAWL_SEED);

                /*
                 * Start the crawl. This is a blocking operation, meaning that your code
                 * will reach the line after this only when crawling is finished.
                 */
                controller.start(BasicCrawler.class, numberOfCrawlers);
                
                //Calculate stats
                List<Object> crawlersLocalData = controller.getCrawlersLocalData();
                long totalLinks = 0;
                long totalTextSize = 0;
                int totalProcessedPages = 0;
                for (Object localData : crawlersLocalData) {
                        CrawlStat stat = (CrawlStat) localData;
//                        totalLinks += stat.getTotalLinks();
//                        totalTextSize += stat.getTotalTextSize();
//                        totalProcessedPages += stat.getTotalProcessedPages();
                }
                System.out.println("Aggregated Statistics:");
                System.out.println("   Processed Pages: " + totalProcessedPages);
                System.out.println("   Total Links found: " + totalLinks);
                System.out.println("   Total Text Size: " + totalTextSize);
                
                log.info("**********Crawler stopped at: "+System.currentTimeMillis()+"*************");
        }
}