package query;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class QueryGetter {

	public static final String NEGATIVE_DIR = "negative";
	public static final String POSITIVE_DIR = "positive";
	public static final String LONG_NEGATIVE_DIR = "long_negative";
	private static final String CONTROVERSIAL_ARTICLES_URL = "http://en.wikipedia.org/wiki/Wikipedia:List_of_controversial_issues";
	private static final String CONTROVERSIAL_PAGE_IDS_DIR = "../revhistories/controversial_page_ids/";
	private static final String CONTROVERSIAL_PAGE_IDS_LIST_FILE_NAME_PREFIX = "controversial_page_ids-";
	private static final String CONTROVERSIAL_PAGE_IDS_TEXT_FILE_NAME = "completeList.txt";
	private static final String CONTROVERSIAL_PAGE_IDS_TEXT_FILE_PATH = CONTROVERSIAL_PAGE_IDS_DIR 
			+ CONTROVERSIAL_PAGE_IDS_TEXT_FILE_NAME;
	private static final String FEATURED_ARTICLES_URL = "http://en.wikipedia.org/wiki/Wikipedia:Featured_articles";
	private static final String FEATURED_PAGE_IDS_DIR = "../revhistories/featured_page_ids/";
	private static final String FEATURED_PAGE_IDS_LIST_FILE_NAME_PREFIX = "featured_page_ids-";
	private static final String FEATURED_PAGE_IDS_TEXT_FILE_NAME = "completeList.txt";
	private static final String FEATURED_PAGE_IDS_TEXT_FILE_PATH = FEATURED_PAGE_IDS_DIR 
			+ FEATURED_PAGE_IDS_TEXT_FILE_NAME;
	private static final String FEATURED_DIR = "featured";
	private static final String REV_DIFFS_DIR = "revision_diffs";
	
	/* We want non-controversial pages downloaded to have at least 8 * 500 = 4000 revisions */
	private static final int MIN_NUM_REV_PAGES_PER_NEG_PAGE_ID = 6;
	
    public static void main(String[] args) throws Exception {
        //List<String> pageIds = getRandomQueryIds();
        /* To get more random articles, go to
         * http://en.wikipedia.org/w/api.php?action=query&list=random&rnlimit=5&rnnamespace=0 */
        //DownloadRevHistories(pageIds, NEGATIVE_DIR);
        
        // get some controversial ones - we should crawl this later
    	//downloadControversialIssuesPages(100);
    	//downloadRevisionDiff("1623", "461928362", true, true);
    	//downloadFeaturedIssuesPages(0);
    	//downloadNRandomRevHistories(1);
    	//downloadRevisionDiffsForPage("14335296", true); // Necrophilia
    	//downloadRevisionDiffsForPage("1092923", true); // Google
    	//downloadRevisionDiffsForUser("SF007", true);
    	//downloadRevisionDiffsForUser("The_Egyptian_Liberal", true);
    	downloadNMostRecentlyEditedArticles("Tangledorange", -1);
    	downloadNMostRecentlyEditedArticles("Hammersoft", -1);
    	downloadNMostRecentlyEditedArticles("SF007", -1);
    	downloadNMostRecentlyEditedArticles("JerryOrr", -1);
    	downloadNMostRecentlyEditedArticles("The_Egyptian_Liberal", -1);
    }
	
	
    private static void addPageIdsInFileToList(String filename, Map<String, String> pageMap)
    		throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document xmlDocument = builder.parse(filename);
		NodeList pageList = xmlDocument.getElementsByTagName("page");
		for (int i = 0; i < pageList.getLength(); i++) {
			NamedNodeMap attributes = pageList.item(i).getAttributes();
			String id = attributes.getNamedItem("pageid").getNodeValue();
			String title = attributes.getNamedItem("title").getNodeValue();
			pageMap.put(id, title);
		}
    }
    
    private static void compileControversialPageIdsTextFile() throws Exception {
    	List<String> controversialPageTitles = getControversialPageTitles();
    	compilePageIdsTextFile(CONTROVERSIAL_PAGE_IDS_DIR,
    			CONTROVERSIAL_PAGE_IDS_LIST_FILE_NAME_PREFIX,
    			CONTROVERSIAL_PAGE_IDS_TEXT_FILE_PATH,
    			controversialPageTitles);
    }
    
    private static void compileFeaturedPageIdsTextFile() throws Exception {
    	List<String> featuredPageTitles = getFeaturedPageTitles();
    	compilePageIdsTextFile(FEATURED_PAGE_IDS_DIR,
    			FEATURED_PAGE_IDS_LIST_FILE_NAME_PREFIX,
    			FEATURED_PAGE_IDS_TEXT_FILE_PATH,
    			featuredPageTitles);
    }
    

    private static void compilePageIdsTextFile(String pageIdsDir,
    		String pageIdsListFileNamePrefix, String pageIdsTextFilePath,
    		List<String> pageTitles) throws Exception {
    	int nPageTitlesPerRequest = 50; // Wikipedia API's limit on number of parameters in titles field
    	int curPageNum = 1;
    	Map<String, String> pageMap = new TreeMap<String, String>();
    	for (int i = 0; i < pageTitles.size(); i += nPageTitlesPerRequest) {
    		String joinedPageTitles = joinStringsInList(pageTitles, i,
    				nPageTitlesPerRequest, "%7C");
    		String urlStr = "http://en.wikipedia.org/w/api.php?action=query&format=xml&redirects&titles=" + joinedPageTitles;
    		String filename = pageIdsDir + pageIdsListFileNamePrefix + curPageNum + ".xml";
    		downloadGeneralUrlToFile(new URL(urlStr), filename);
    		addPageIdsInFileToList(filename, pageMap);
    		curPageNum++;
    	}
    	writePageMapToFile(pageMap, pageIdsTextFilePath);
    }
    
    private static void deleteFiles(List<String> filenames) throws Exception {
    	for (String filename : filenames) {
    		File f = new File(filename);
    		f.delete();
    	}
    }
    
    private static void downloadArticleContent(String pageId) throws Exception {
    	String urlString = "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&redirects&rvprop=content&format=xml&pageids="
    			+ pageId;
    	String filename = "../articles/" + pageId + ".xml";
    	downloadGeneralUrlToFile(new URL(urlString), filename);
    }
    
    private static void downloadArticleContentWithSuffix(String pageId, String suffix) throws Exception {
    	String urlString = "http://en.wikipedia.org/w/api.php?action=query&prop=revisions&redirects&rvprop=content&format=xml&pageids="
    			+ pageId;
    	String filename = "../articles/" + pageId + "-" + suffix + ".xml";
    	downloadGeneralUrlToFile(new URL(urlString), filename);
    }
    
    private static void downloadControversialIssuesPages(int maxPagesToDownload) throws Exception {
    	compileControversialPageIdsTextFile();
    	List<String> pageIds = new ArrayList<String>();
    	BufferedReader rd = new BufferedReader(new FileReader(CONTROVERSIAL_PAGE_IDS_TEXT_FILE_PATH));
    	for (int i = 0; i < maxPagesToDownload; i++) {
    		String line = rd.readLine();
    		if (line == null) break;
    		String id = line.split("::")[0];
    		pageIds.add(id);
    	}
    	downloadRevHistories(pageIds, POSITIVE_DIR, 0);
    }
    
    
    private static void downloadFeaturedIssuesPages(int maxPagesToDownload) throws Exception {
    	compileFeaturedPageIdsTextFile();
    	if (maxPagesToDownload <= 0) {
    		return;
    	}
    	List<String> pageIds = new ArrayList<String>();
    	BufferedReader rd = new BufferedReader(new FileReader(FEATURED_PAGE_IDS_TEXT_FILE_PATH));
    	for (int i = 0; i < maxPagesToDownload; i++) {
    		String line = rd.readLine();
    		if (line == null) break;
    		String id = line.split("::")[0];
    		pageIds.add(id);
    	}
    	downloadRevHistories(pageIds, FEATURED_DIR, MIN_NUM_REV_PAGES_PER_NEG_PAGE_ID);
    }
    
    private static void downloadGeneralUrlToFile(URL u, String filename) throws Exception {
    	BufferedReader rd = getBufferedReader(u);
    	System.out.println("Downloading: " + u.toString());
    	PrintWriter pw = new PrintWriter(filename, "UTF-8");
    	String inputLine;
    	while ((inputLine = rd.readLine()) != null) {
    		pw.println(inputLine);
    	}
    	pw.close();
    	rd.close();
    	System.out.println("Saved to: " + filename);
    }
    
    private static void downloadNMostRecentlyEditedArticles(String username, int n) throws Exception {
    	for (String pageId : getNMostRecentlyEditedPageIds(username, n)) {
    		downloadArticleContentWithSuffix(pageId, username);
    	}
    }
    
    private static Set<String> getNMostRecentlyEditedPageIds(String username, int n) throws Exception {
    	File dir = new File("../revhistories/user_contribs");
    	TreeMap<Integer, File> files = new TreeMap<Integer, File>();
    	for (File f : dir.listFiles()) {
    		if (f.getName().startsWith(username)) {
    			int numberBegin = username.length() + 1;
    			int numberEnd = f.getName().indexOf(".xml", numberBegin);
    			int docNum = Integer.parseInt(f.getName().substring(numberBegin, numberEnd));
    			files.put(docNum, f);
    		}
    	}
    	
    	HashSet<String> topPageIds = new HashSet<String>();
    	
    	for (Integer i : files.keySet()) {
    		if (n >= 0 && topPageIds.size() >= n) break;
    		File f = files.get(i);
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder builder = factory.newDocumentBuilder();
    		Document xmlDocument = builder.parse(f);
    		NodeList items = xmlDocument.getElementsByTagName("item");
    		for (int j = 0; j < items.getLength(); j++) {
    			if (n >= 0 && topPageIds.size() >= n) break;
    			NamedNodeMap attributes = items.item(j).getAttributes();
    			String pageId = attributes.getNamedItem("pageid").getNodeValue();
    			topPageIds.add(pageId);
    		}
    	}
    	
    	return topPageIds;
    }

    private static void downloadRevisionDiffsForPage(String pageId, boolean diffWithPrev) 
    throws Exception {
    	System.out.println("DOWNLOADING REVISION DIFFS BY PAGE FOR PAGE ID: " + pageId);
    	String nextRevId = downloadRevisionDiff(pageId, null, diffWithPrev, null);
    	while (nextRevId != null) {
    		nextRevId = downloadRevisionDiff(pageId, nextRevId, diffWithPrev, null);
    	}
    	System.out.println("DONE DOWNLOADING REVISION DIFFS BY PAGE FOR PAGE ID: " + pageId);
    }
    
    private static void downloadRevisionDiffsForUser(String username, boolean diffWithPrev) 
    throws Exception {
    	downloadUserContribs(username);
    	System.out.println("DOWNLOADING REVISION DIFFS FOR USERNAME: " + username);
    	File dir = new File("../revhistories/user_contribs");
    	String prefix = username + "-";
    	for (File f : dir.listFiles()) {
    		if (f.getName().startsWith(prefix))	{
    			downloadRevisionDiffsForUserInContribsFile(f, username, diffWithPrev);
    		}
    	}
    	System.out.println("DONE DOWNLOADING REVISION DIFFS FOR USERNAME: " + username);
    }
    
    private static void downloadRevisionDiffsForUserInContribsFile(File f,
    		String username, boolean diffWithPrev) throws Exception {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document xmlDocument = builder.parse(f);
		NodeList items = xmlDocument.getElementsByTagName("item");
		for (int j = 0; j < items.getLength(); j++) {
			NamedNodeMap attributes = items.item(j).getAttributes();
			String pageId = attributes.getNamedItem("pageid").getNodeValue();
			String revId = attributes.getNamedItem("revid").getNodeValue();
			downloadRevisionDiff(pageId, revId, diffWithPrev, username);
		}
    }
    
    private static void downloadUserContribs(String username)
    throws Exception {
    	String urlStringPrefix = "http://en.wikipedia.org/w/api.php?action=query"
    			+ "&list=usercontribs&uclimit=max&format=xml&ucnamespace=0"
    			+ "&ucuser=" + username;
    	String revStartStr = "";
    	int curDocNum = 1;
    	while (true) {
    		String nextUcStart = null;
        	String filename = "../revhistories/user_contribs/" + username + "-" + curDocNum + ".xml";
        	String urlString = urlStringPrefix + revStartStr;
    		BufferedReader reader = getBufferedReader(urlString);
    		System.out.println("Downloading: " + urlString);
        	PrintWriter writer = new PrintWriter(filename, "UTF-8");
    		while (true) {
    			String line = reader.readLine();
    			if (line == null) break;
        		String possibleNextUcStart = getAttrInLine(line, "<usercontribs ucstart=\"");
        		if (possibleNextUcStart != null) {
        			nextUcStart = possibleNextUcStart;
        		}
        		writer.println(line);
    		}
    		reader.close();
    		writer.close();
    		System.out.println("Saved to: " + filename);
    		curDocNum++;
    		if (nextUcStart == null) break;
			revStartStr = "&ucstart=" + nextUcStart;
    	}
    }
    
    /**
     * If diffWithPrev is true, downloads into the revision diffs directory
     * the diff of the given revision (the page id somehow is also needed
     * for the API) with the revision that came before it on the page.
     * If diffWithPrev is false, the diff is done with the given revision
     * and the page's latest revision (cur).
     * @param pageId
     * @param revId
     * @param diffWithPrev
     */
    private static String downloadRevisionDiff(String pageId, String revId,
    		boolean diffWithPrev, String username) 
    throws Exception {
    	String rvStartIdStr = "";
    	String nextRevId = null;
    	if (revId != null) {
    		rvStartIdStr = "&rvstartid=" + revId;
    	}
    	String diffTo = diffWithPrev ? "prev" : "cur";
    	String urlString = "http://en.wikipedia.org/w/api.php?action=query&format=xml&prop=revisions&rvlimit=1"
    			+ "&pageids=" + pageId
    			+ "&rvprop=ids%7Ctimestamp%7Cuser%7Ccomment%7Csize"
    			+ "&rvdiffto=" + diffTo
    			+ rvStartIdStr;
    	
    	BufferedReader reader = getBufferedReader(urlString);
    	System.out.println("Downloading: " + urlString);
    	/* What's a bit annoying is that if it's the latest revision,
    	 * I need to look at the file itself to see what the revision
    	 * id is.
    	 */
    	ArrayList<String> firstLines = new ArrayList<String>();
    	while (true) {
    		String line = reader.readLine();
    		if (line == null) break;
    		firstLines.add(line);
    		String possibleNextRevId = getAttrInLine(line, "<revisions rvstartid=\"");
    		if (possibleNextRevId != null) {
    			nextRevId = possibleNextRevId;
    		}
    		String revIdInFile = getAttrInLine(line, "<rev revid=\"");
    		if (revIdInFile != null) {
    			revId = revIdInFile;
    			break;
    		}
    	}
    	String filename = getRevDiffFileName(pageId, revId, diffWithPrev, username);
    	PrintWriter writer = new PrintWriter(filename, "UTF-8");
    	for (String firstLine : firstLines) {
    		writer.println(firstLine);
    	}
    
    	while (true) {
    		String line = reader.readLine();
    		if (line == null) break;
    		String possibleNextRevId = getAttrInLine(line, "<revisions rvstartid=\"");
    		if (possibleNextRevId != null) {
    			nextRevId = possibleNextRevId;
    		}
    		writer.println(line);
    	}
    	reader.close();
    	writer.close();
    	System.out.println("Saved to: " + filename);
    	return nextRevId;
    }
    
    
    
    private static void downloadNRandomRevHistories(int n) throws Exception {
    	int nLongPagesDownloaded = 0;
    	int nPageIdsRetrieved = 0;
    	while (nLongPagesDownloaded < n) {
    		List<String> randomIds = getRandomQueryIds();
    		nPageIdsRetrieved += randomIds.size();
    		int nLongPages = downloadRevHistories(randomIds, LONG_NEGATIVE_DIR, MIN_NUM_REV_PAGES_PER_NEG_PAGE_ID);
    		nLongPagesDownloaded += nLongPages;
    		System.out.println("SO FAR: " + nPageIdsRetrieved + " PAGE IDS RETRIEVED, " + nLongPagesDownloaded + " LONG PAGES DOWNLOADED" );
    	}
    	System.out.println("NUMBER OF LONG PAGES DOWNLOADED: " + nLongPagesDownloaded);
    }
    
    /**
     * 
     * @param pageIds
     * @param label
     * @return the number of pages that are long
     * @throws Exception
     */
    public static int downloadRevHistories(List<String> pageIds, String label, int minNumRevPagesPerPageId) throws Exception{
    	int nLongPages = 0;
    	for (String pageId : pageIds) {
    		System.out.println("BEGIN DOWNLOAD FOR PAGE ID: " + pageId);
    		URLQuery query = new URLQuery("http://en.wikipedia.org/w/api.php");
    		String revStartId = null;
    		int xmlDocNum = 1;
    		List<String> downloadedFilenames = new ArrayList<String>();
    		while (true) {
    			Map<String, String> params = getQueryParams(pageId, revStartId);
    			URL u = query.withQueryParams(params);
    			String filename = "../revhistories/" + label + "/" +  pageId + "-" + xmlDocNum + ".xml";
    			downloadedFilenames.add(filename);
    			revStartId = downloadRevQueryToFile(u, filename);
    			if (revStartId == null) break;
    			xmlDocNum++;
    		}
    		System.out.println(downloadedFilenames.size() + " PAGES OF REVISIONS DOWNLOADED FOR PAGE ID: " + pageId);
    		if (downloadedFilenames.size() < minNumRevPagesPerPageId) {
    			deleteFiles(downloadedFilenames);
    			System.out.println("NOT ENOUGH REVISIONS, DELETING REVISION PAGES FOR ID: " + pageId);
    		} else {
    			nLongPages++;
    		}
    	}
    	return nLongPages;
    }
    
    public static String downloadRevQueryToFile(URL u, String filename) throws Exception {
    	BufferedReader rd = getBufferedReader(u);
    	System.out.println("Downloading: " + u.toString());
    	PrintWriter pw = new PrintWriter(filename, "UTF-8");
    	String inputLine;
    	String revStartId = null;
    	String toFind = "<revisions rvstartid=\"";
    	while ((inputLine = rd.readLine()) != null) {
    		int revStartIdAttrIndex = inputLine.indexOf(toFind);
    		if (revStartIdAttrIndex != -1) {
    			int nextQuoteIndex = inputLine.indexOf("\"", revStartIdAttrIndex + toFind.length());
    			revStartId = inputLine.substring(revStartIdAttrIndex + toFind.length(), nextQuoteIndex);
    		}
    		pw.println(inputLine);
    		//System.out.println("input " + inputLine);
    	}
    	pw.close();
    	rd.close();
    	System.out.println("Saved to: " + filename);
    	return revStartId;
    }
    
    /**
     * If line is '<a href="blah">'
     * and prefix is '<a href="'
     * then returns 'blah' as a String,
     * or null if the prefix cannot be found
     * @param line
     * @param prefix
     * @return
     */
    private static String getAttrInLine(String line, String prefix) {
		int index = line.indexOf(prefix);
		if (index < 0) return null;
		int nextQuoteIndex = line.indexOf("\"", index + prefix.length());
		if (nextQuoteIndex < 0) return null;
		return line.substring(index + prefix.length(), nextQuoteIndex);
    }
    
    public static BufferedReader getBufferedReader(String urlString) throws Exception {
    	return getBufferedReader(new URL(urlString));
    }
    
    public static BufferedReader getBufferedReader(URL u) throws Exception {
    	URLConnection connection = u.openConnection();
    	BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "ISO-8859-1"));
    	return rd;
    }
    
    private static List<String> getControversialPageTitles() throws Exception {
    	BufferedReader rd = getBufferedReader(new URL(CONTROVERSIAL_ARTICLES_URL));
    	List<String> pageTitles = new ArrayList<String>();
    	String inputLine = null;
    	while ((inputLine = rd.readLine()) != null) {
    		if (inputLine.contains("Categories of past controversial issues")) {
    			break;
    		}
    	}
    	String prefix = "<li><a href=\"/wiki/";
    	while ((inputLine = rd.readLine()) != null) {
    		if (inputLine.contains("<span class=\"mw-headline\" id=\"See_also\">See also</span>")) {
    			break;
    		}
    		int prefixIndex = inputLine.indexOf(prefix);
    		if (prefixIndex == -1) continue;
    		int nextQuoteIndex = inputLine.indexOf("\"",
    				prefixIndex + prefix.length());
    		String pageTitle = inputLine.substring(prefixIndex + prefix.length(),
    				nextQuoteIndex);
    		pageTitles.add(pageTitle);
    	}
    	return pageTitles;
    }
    
    private static List<String> getFeaturedPageTitles() throws Exception {
    	BufferedReader rd = getBufferedReader(new URL(FEATURED_ARTICLES_URL));
    	List<String> pageTitles = new ArrayList<String>();
    	String inputLine = null;
    	while ((inputLine = rd.readLine()) != null) {
    		if (inputLine.contains("<span class=\"mw-headline\" id=\"Art.2C_architecture_and_archaeology\">Art, architecture and archaeology</span></h2>")) {
    			break;
    		}
    	}
    	String prefix = "<a href=\"/wiki/";
    	while ((inputLine = rd.readLine()) != null) {
    		if (inputLine.contains("<a href=\"/wiki/Portal:Contents\" title=\"Portal:Contents\">Content listings</a>")) {
    			break;
    		}
    		int prefixIndex = inputLine.indexOf(prefix);
    		if (prefixIndex == -1) continue;
    		int nextQuoteIndex = inputLine.indexOf("\"",
    				prefixIndex + prefix.length());
    		String pageTitle = inputLine.substring(prefixIndex + prefix.length(),
    				nextQuoteIndex);
    		pageTitles.add(pageTitle);
    		System.out.println(pageTitle);
    	}
    	return pageTitles;
    }
    
    private static Map<String, String> getQueryParams(String pageIds, String revStartId) {
    	Map<String, String> queryMap = new HashMap<String, String>();
    	queryMap.put("action", "query");
    	queryMap.put("format", "xml");
    	queryMap.put("prop", "revisions");
    	queryMap.put("pageids", pageIds);
    	queryMap.put("rvlimit", "max");
    	queryMap.put("rvprop", "ids%7Cflags%7Ctimestamp%7Cuser%7Ccomment%7Csize%7Ctags");
    	if (revStartId != null) {
    		queryMap.put("rvstartid", revStartId);
    	}
    	return queryMap;
    	
    }
    
    public static List<String> getRandomQueryIds() {
    	List<String> idList = new ArrayList<String>();
    	try {
    		for (int i = 0; i < 1; i++) {
    			URL url = new URL("http://en.wikipedia.org/w/api.php?action=query&list=random&rnlimit=10&rnnamespace=0&format=xml");
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder builder = factory.newDocumentBuilder();
    			Document xmlDocument = builder.parse(url.openConnection().getInputStream());
    			NodeList pages = xmlDocument.getElementsByTagName("page");
    			for (int j = 0; j < pages.getLength(); j++) {
    				NamedNodeMap attributes = pages.item(j).getAttributes();
    				idList.add(attributes.getNamedItem("id").getNodeValue());
    			}
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		// do nothing
    	}
    	return idList;
    }
    
    private static String getRevDiffFileName(String pageId, String revId, 
    		boolean diffWithPrev, String username) {
    	String revisionDiffsDirName;
    	if (username == null) {
    		revisionDiffsDirName = "revision_diffs_by_page" + "/" + pageId;
    	} else {
    		revisionDiffsDirName = "revision_diffs_by_user/" + username; 
    	}
    	String directory = "../revhistories/" + revisionDiffsDirName;
    	File dirFile = new File(directory);
    	if (!dirFile.exists()) {
    		dirFile.mkdirs();
    	}
    	String diffWith = diffWithPrev ? "prev" : "cur";
    	String filename = pageId + "-" + revId + "-" + diffWith + ".xml";
    	return directory + "/" + filename; 
    }
    
    
    private static String joinStringsInList(List<String> strList, int startIndex,
    		int maxElemsToJoin, String delimiter) {
    	if (startIndex >= strList.size() || maxElemsToJoin == 0) return "";
    	String result = strList.get(startIndex);
    	int maxIndex = Math.min(startIndex + maxElemsToJoin - 1,
    			strList.size() - 1);
    	for (int i = startIndex + 1; i <= maxIndex; i++) {
    		result += delimiter + strList.get(i);
    	}
    	return result;
    }
    
    
    private static void writePageMapToFile(Map<String, String> pageMap, String filename)
    		throws Exception{
    	PrintWriter writer = new PrintWriter(filename, "UTF-8");
    	for (String id : pageMap.keySet()) {
    		writer.println(id + "::" + pageMap.get(id));
    	}
    	writer.close();
	}
    
}
