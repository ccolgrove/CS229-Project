import java.io.File;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {
	// TODO(jtibs): fix these paths
	private static final String[] INPUT_DIRS = {"../../revhistories/long_negative", "../../revhistories/positive"};
	
	// TODO(jtibs): actually deal with these exceptions
	public List<WikiDocument> parse() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			List<WikiDocument> documents = new ArrayList<WikiDocument>();
			String lastId = null;  // keep track of the ID of the last page we parsed, in case
								             // revisions for a document are split across several files
			WikiDocument result = null;
			for (int i = 0; i < INPUT_DIRS.length; i++) {
				File dir = new File(INPUT_DIRS[i]);
				for (File file : dir.listFiles()) {
					//System.err.println(file);
					Document xmlDocument = builder.parse(file);
				
					String fileName = file.getName();
					String id = fileName.substring(0, fileName.indexOf('-'));
					if (! id.equals(lastId)) {
						if (result != null) documents.add(result);  // we have finished parsing the previous document
						if (result != null) System.out.println(id + ", " + result.revisions.size());
						result = new WikiDocument(fileName, i);  // start a new document
						lastId = id;
					}
					parseDocument(xmlDocument, result);
				}
			}
			return documents;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Parses the given XML document and updates result.revisions */
	private WikiDocument parseDocument(Document xmlDocument, WikiDocument result) {
		NodeList revisions = xmlDocument.getElementsByTagName("rev");
		for (int i = 0; i < revisions.getLength(); i++) {
			NamedNodeMap attributes = revisions.item(i).getAttributes();

			Node userAttr = attributes.getNamedItem("user");
			String user = "";
			if (userAttr != null) user = userAttr.getNodeValue();
			
			Node timestampAttr = attributes.getNamedItem("timestamp");
			Date timestamp = parseTimestamp(timestampAttr.getNodeValue());
	
			Node commentAttr = attributes.getNamedItem("comment");
			String comment = "";
			if (commentAttr != null) comment = commentAttr.getNodeValue();
			
			Node sizeAttr = attributes.getNamedItem("size");
			int size = 0;
			if (sizeAttr != null) 
				size = Integer.parseInt(sizeAttr.getNodeValue());

			result.revisions.add(new Revision("", timestamp, user, comment, size));
		}
		return result;
	}
	
	/** timestamp will be of the form 2007-08-21T01:44:47Z */
	private Date parseTimestamp(String str) {
		str = str.replaceAll("[TZ]", " ");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date date = null;
		try {
			date = format.parse(str);
		} catch(ParseException e) {}
		
		return date;
		
	}
}