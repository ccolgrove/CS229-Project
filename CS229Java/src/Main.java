import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;

public class Main {
	public static void main(String[] args) {
		List<WikiDocument> documents = new XMLParser().parse();
		StringBuilder builder = new StringBuilder();

		TrainingExample example = null;
		for (WikiDocument document : documents) {
			example = new TrainingExample(document);
			builder.append(example.featuresToString() + "\n");
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("MATRIX_OUT"));
			out.write(documents.size() + " " + example.featureMap.size() + "\n");  // output the dimensions
			out.write(builder.toString());  // output the feature matrix
			out.close();
		} catch (IOException e) {}
	}
}