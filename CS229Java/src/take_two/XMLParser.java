package take_two;

//import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
//import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class XMLParser {
	private static final String XML_HEADER = "<?xml version=\"1.0\"?> <!DOCTYPE some_name [ <!ENTITY nbsp \"&#160;\"> ]><api>"; 
	private static final String XML_FOOTER = "</api>";
	private static final String INFOBOX_PATTERN = "\\{\\{Infobox.*\\|.*=.*\\}\\}\\n";

	private DocumentBuilder builder;
	
	public XMLParser() {
	  try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    builder = factory.newDocumentBuilder();
	  } catch (ParserConfigurationException e) {
      e.printStackTrace();
	  }
	}

	/** Parses the given revision diff and returns a Revision representing it */
	public Revision parseRevision(File file) {
	  Document xmlDocument = null;
	  try {
	    xmlDocument = builder.parse(file);
	  } catch (IOException e) {
	  } catch (SAXException e) {}
	  // get information from <page> tag
	  NodeList nodes = xmlDocument.getElementsByTagName("page");
	  NamedNodeMap attributes = nodes.item(0).getAttributes();
	  Node pageIdAttr = attributes.getNamedItem("pageid");
	  String pageId = pageIdAttr.getNodeValue();
	  
	  // get information from <rev> tag
	  nodes = xmlDocument.getElementsByTagName("rev");
	  attributes = nodes.item(0).getAttributes();

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
	  nodes = xmlDocument.getElementsByTagName("diff");
	
	  Node contentNode = nodes.item(0).getFirstChild();
	  if (contentNode == null) return null;
	  String content = cleanContent(contentNode.getNodeValue());
	  if (content == null) return null;
	  
	  return new Revision(user, id, pageId, timestamp, comment, content);
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
	
	/** extracts text content from the given html */
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
	          /*|| type.equals("diff-context")*/) {  // if we are interested in this line
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
	  return result.toString();
	}
	
	public TestDocument parseDocument(File file) {
	  TestDocument result = new TestDocument();
	  Document xmlDocument = null;
    try {
      xmlDocument = builder.parse(file);
    } catch (IOException e) {
    } catch (SAXException e) {}
    // get information from <page> tag
    NodeList nodes = xmlDocument.getElementsByTagName("rev");
    String text = nodes.item(0).getFirstChild().getNodeValue();
    
//    // parse out infobox and intro
//    int equals = text.indexOf("==");
//    if (equals == -1) {
//      result.paragraphs.add(text);
//      return result;
//    }
//    
//    String intro = text.substring(0, equals);
//    
//    Pattern pattern = Pattern.compile(INFOBOX_PATTERN, Pattern.DOTALL);
//    Matcher matcher = pattern.matcher(intro);
//    
//    System.out.println("intro: " + intro);
//    if (matcher.find()) {
//      String infobox = matcher.group();
//      result.paragraphs.add(infobox);
//      System.out.println("infobox: " + infobox);
//      intro = intro.substring(matcher.end() + 2);
//    }
//    result.paragraphs.add(intro);
//    
//    // separate out each section
//    while (true) {
//      int index = text.indexOf("\n\n");     
//      if (index == -1) break;
//      
//      String paragraph = text.substring(0, index);
//      text = text.substring(index + 2);
//      
//      if (paragraph.length() > 40)  // TODO: fix this
//        result.paragraphs.add(paragraph);
//    }
    String[] split = text.split("\\n\\n|\\}\\n");
    result.paragraphs = new ArrayList<String>();
    for (String str : split)
      result.paragraphs.add(str);
    return result;
	}
}