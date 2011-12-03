import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collections;
import java.util.List;

public class Main {
	public static void main(String[] args) {
	  XMLParser parser = new XMLParser();
	  
		List<Revision> revisions = parser.parse();
	}
}