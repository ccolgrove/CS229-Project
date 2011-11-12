import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collections;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		List<WikiDocument> documents = new XMLParser().parse();

		// TODO(jtibs): better randomize this step and eventually implement cross-validation
		Collections.shuffle(documents);
		int split = (int) (0.7 * documents.size());
		List<WikiDocument> training = documents.subList(0, split);
		List<WikiDocument> testing = documents.subList(split, documents.size());
		String[] names = {"TRAIN", "TEST"};
		
		for (int i = 0; i < names.length; i++) {
			StringBuilder xBuilder = new StringBuilder();
			StringBuilder yBuilder = new StringBuilder();
			int numFeatures = 0;
			
			documents = (i == 0 ? training : testing);
			for (WikiDocument document : documents) {
				TrainingExample example = new TrainingExample(document);
				if (numFeatures == 0)
					numFeatures = example.featureMap.size();
				
				xBuilder.append(example.featuresToString() + "\n");
				yBuilder.append(example.label + " ");
			}

			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("../" + names[i] + "_X"));
				out.write(documents.size() + " " + numFeatures + "\n");  // output the dimensions
				out.write(xBuilder.toString());  // output the feature matrix
				out.close();

				out = new BufferedWriter(new FileWriter("../" + names[i] + "_Y"));
				out.write("1" + " " + documents.size() + "\n");  // output the dimensions
				out.write(yBuilder.toString());  // output the label matrix
				out.close();
			} catch (IOException e) {}
		}
	}
}