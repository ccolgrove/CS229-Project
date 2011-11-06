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
	private static final String CONTROVERSIAL_ARTICLES_URL = "http://en.wikipedia.org/wiki/Wikipedia:List_of_controversial_issues";
	private static final String CONTROVERSIAL_PAGE_IDS_DIR = "../revhistories/controversial_page_ids/";
	private static final String CONTROVERSIAL_PAGE_IDS_LIST_FILE_NAME_PREFIX = "controversial_page_ids-";
	private static final String CONTROVERSIAL_PAGE_IDS_TEXT_FILE_NAME = "completeList.txt";
	private static final String CONTROVERSIAL_PAGE_IDS_TEXT_FILE_PATH = CONTROVERSIAL_PAGE_IDS_DIR
			+ CONTROVERSIAL_PAGE_IDS_TEXT_FILE_NAME;
	
	
    public static void main(String[] args) throws Exception {
        //List<String> pageIds = getRandomQueryIds();
        /* To get more random articles, go to
         * http://en.wikipedia.org/w/api.php?action=query&list=random&rnlimit=5&rnnamespace=0 */
        //DownloadRevHistories(pageIds, NEGATIVE_DIR);
        
        // get some controversial ones - we should crawl this later
    	downloadControversialIssuesPages(20);
    }
    
    
    public static List<String> getRandomQueryIds() {
    	List<String> idList = new ArrayList<String>();
    	try {
    		for (int i = 0; i < 1; i++) {
    			URL url = new URL("http://en.wikipedia.org/w/api.php?action=query&list=random&rnlimit=1&rnnamespace=0&format=xml");
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
    

    private static void downloadControversialIssuesPages(int maxPagesToDownload) throws Exception {
    	//compilePageIdsTextFile();
    	List<String> pageIds = new ArrayList<String>();
    	BufferedReader rd = new BufferedReader(new FileReader(CONTROVERSIAL_PAGE_IDS_TEXT_FILE_PATH));
    	for (int i = 0; i < maxPagesToDownload; i++) {
    		String line = rd.readLine();
    		if (line == null) break;
    		String id = line.split("::")[0];
    		pageIds.add(id);
    	}
    	downloadRevHistories(pageIds, POSITIVE_DIR);
    }
    
    private static void compilePageIdsTextFile() throws Exception {
    	List<String> controversialPageTitles = getControversialPageTitles();
    	int nPageTitlesPerRequest = 50; // Wikipedia API's limit on number of parameters in titles field
    	int curPageNum = 1;
    	Map<String, String> pageMap = new TreeMap<String, String>();
    	for (int i = 0; i < controversialPageTitles.size(); i += nPageTitlesPerRequest) {
    		String joinedPageTitles = joinStringsInList(controversialPageTitles, i,
    				nPageTitlesPerRequest, "%7C");
    		String urlStr = "http://en.wikipedia.org/w/api.php?action=query&format=xml&titles=" + joinedPageTitles;
    		String filename = CONTROVERSIAL_PAGE_IDS_DIR + CONTROVERSIAL_PAGE_IDS_LIST_FILE_NAME_PREFIX + curPageNum + ".xml";
    		downloadGeneralUrlToFile(new URL(urlStr), filename);
    		addPageIdsInFileToList(filename, pageMap);
    		curPageNum++;
    	}
    	writePageMapToFile(pageMap, CONTROVERSIAL_PAGE_IDS_TEXT_FILE_PATH);
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
    	PrintWriter writer = new PrintWriter(new FileWriter(filename));
    	for (String id : pageMap.keySet()) {
    		writer.println(id + "::" + pageMap.get(id));
    	}
    	writer.close();
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
    
    public static BufferedReader getBufferedReader(URL u) throws Exception {
    	URLConnection connection = u.openConnection();
    	BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
    	return rd;
    }
    
    private static Map<String, String> getQueryParams(String pageIds, String revStartId) {
    	Map<String, String> queryMap = new HashMap<String, String>();
    	queryMap.put("action", "query");
    	queryMap.put("format", "xml");
    	queryMap.put("prop", "revisions");
    	queryMap.put("pageids", pageIds);
    	queryMap.put("rvlimit", "max");
    	queryMap.put("rvprop", "flags%7Ctimestamp%7Cuser%7Ccomment%7Csize%7Ctags");
    	if (revStartId != null) {
    		queryMap.put("rvstartid", revStartId);
    	}
    	return queryMap;
    	
    }
    
    public static void downloadRevHistories(List<String> pageIds, String label) throws Exception{
    	for (String pageId : pageIds) {
    		System.out.println("BEGIN DOWNLOAD FOR PAGE ID: " + pageId);
    		URLQuery query = new URLQuery("http://en.wikipedia.org/w/api.php");
    		String revStartId = null;
    		int xmlDocNum = 1;
    		while (true) {
    			Map<String, String> params = getQueryParams(pageId, revStartId);
    			URL u = query.withQueryParams(params);
    			String filename = "../revhistories/" + label + "/" +  pageId + "-" + xmlDocNum + ".xml";
    			revStartId = downloadRevQueryToFile(u, filename);
    			if (revStartId == null) break;
    			xmlDocNum++;
    		}
    		System.out.println("END DOWNLOAD FOR PAGE ID: " + pageId);
    	}
    }
    
    private static void downloadGeneralUrlToFile(URL u, String filename) throws Exception {
    	BufferedReader rd = getBufferedReader(u);
    	System.out.println("Downloading: " + u.toString());
    	PrintWriter pw = new PrintWriter(new FileWriter(filename));
    	String inputLine;
    	String revStartId = null;
    	while ((inputLine = rd.readLine()) != null) {
    		pw.println(inputLine);
    	}
    	pw.close();
    	rd.close();
    	System.out.println("Saved to: " + filename);
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
    
}
