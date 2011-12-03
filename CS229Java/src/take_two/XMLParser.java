import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringBufferInputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class XMLParser {
  private static final String USER = "Hammersoft";
	private static final String INPUT_DIR = "../../../revhistories/revision_diffs_by_user/" + USER;
	private static final String XML_HEADER = "<?xml version=\"1.0\"?> <!DOCTYPE some_name [ <!ENTITY nbsp \"&#160;\"> ]><api>"; 
	private static final String XML_FOOTER = "</api>";

	private DocumentBuilder builder;
	
	public XMLParser() {
	  try {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	  builder = factory.newDocumentBuilder();
	  } catch (ParserConfigurationException e) {
      e.printStackTrace();
	  }
	}

	// TODO(jtibs): actually deal with these exceptions
	public List<Revision> parse() {
	  try {
	    List<Revision> revisions = new ArrayList<Revision>();

	    File dir = new File(INPUT_DIR);
	    System.out.println(INPUT_DIR);
	    for (File file : dir.listFiles()) {
	      //System.err.println(file);
	      Document document = builder.parse(file);
	      Revision revision = parseRevision(document);
	      if (revision != null)
	        revisions.add(revision);
	    }

	    return revisions;
	  } catch (IOException e) {
	    e.printStackTrace();
	  } catch (SAXException e) {
	    e.printStackTrace();
	  }
	  return null;
	}

	/** Parses the given revision diff and returns a Revision representing it */
	private Revision parseRevision(Document xmlDocument) {
	  // get information from <rev> tag
	  NodeList revision = xmlDocument.getElementsByTagName("rev");
	  NamedNodeMap attributes = revision.item(0).getAttributes();

	  Node userAttr = attributes.getNamedItem("user");
	  String user = "";
	  if (userAttr != null) user = userAttr.getNodeValue();
	  
	  Node idAttr = attributes.getNamedItem("revid");
	  String id = idAttr.getNodeValue();

	  Node timestampAttr = attributes.getNamedItem("timestamp");
	  Date timestamp = parseTimestamp(timestampAttr.getNodeValue());

	  Node commentAttr = attributes.getNamedItem("comment");
	  String comment = "";
	  if (commentAttr != null) comment = commentAttr.getNodeValue();

	  // get information from <diff> tag
	  NodeList diff = xmlDocument.getElementsByTagName("diff");
	
	  Node contentAttr = diff.item(0).getFirstChild();
	  if (contentAttr == null) return null;
	  String content = cleanContent(contentAttr.getNodeValue());
	  if (content == null) return null;
	  
	  return new Revision(user, id, timestamp, comment, content);
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
	
	private String cleanContent(String content) {
	  StringBuilder result = new StringBuilder();
    content = XML_HEADER + content + XML_FOOTER;
    
	  try {
	    Document document = builder.parse(new StringBufferInputStream(content));	    
	    NodeList tdNodes = document.getElementsByTagName("td");
	    for (int i = 0; i < tdNodes.getLength(); i++) {   // for all <td> nodes
	      Node node = tdNodes.item(i);
	      NamedNodeMap attributes = node.getAttributes();
	      
	      Node classNode = attributes.getNamedItem("class");
	      if (classNode == null) continue;
	      String type = classNode.getNodeValue();
	      
	      if (type.equals("diff-deletedline") || type.equals("diff-addedline")
	          || type.equals("diff-context")) {  // if we are interested in this line
	        Node div = ((Element) node).getElementsByTagName("div").item(0);
	        if (div == null) continue;
	        NodeList divChildren = div.getChildNodes();
	        
	        // children of <div> will be either text, or <span> nodes
	        for (int j = 0; j < divChildren.getLength(); j++) {
	          Node child = divChildren.item(0);
	          if (child instanceof Text)
	            result.append(child.getNodeValue());
	          else
	            result.append(child.getFirstChild().getNodeValue());
	        }
	      }
	    } 
	  } catch (IOException e) {
	    e.printStackTrace();
	  } catch (SAXException e) {
	    e.printStackTrace();
	  } catch (NullPointerException e) {
	    return null;
	  }
	  System.out.println(result.toString());
	  return result.toString();
	}
}