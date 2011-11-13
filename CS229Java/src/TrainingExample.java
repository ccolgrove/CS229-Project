import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrainingExample {
	public String documentTitle;
	public int label;
	public Map<Feature, Double> featureMap;
	
	private enum Feature {
		NUM_REVISIONS,
		NUM_USERS,  // the number of distinct revisors
		AV_REVISION_LENGTH,
		AV_COMMENT_LENGTH
	}
	
	public TrainingExample(WikiDocument document) {
		documentTitle = document.title;
		label = document.label;
		
		featureMap = new HashMap<Feature, Double>();
		extractFeatures(document);
	}
	
	// TODO(jtibs): extract variance features
	private void extractFeatures(WikiDocument document) {
		Set<String> users = new HashSet<String>();  // keeps track of what revisors we have seen
		int numRevisions = document.revisions.size();
		int sumRevisionLengths = 0;
		int sumCommentLengths = 0;
		
		for (Revision revision : document.revisions) {
			users.add(revision.user);
			sumRevisionLengths += revision.size;
			sumCommentLengths += revision.comment.length();
		}
	
		featureMap.put(Feature.NUM_REVISIONS, (double)numRevisions);
		featureMap.put(Feature.NUM_USERS, (double)users.size());
		featureMap.put(Feature.AV_REVISION_LENGTH, (double)sumRevisionLengths / numRevisions);
		featureMap.put(Feature.AV_COMMENT_LENGTH, (double)sumCommentLengths / numRevisions);
		
		featureMap.put(Feature.NUM_REVISIONS, featureMap.get(Feature.NUM_REVISIONS) / 50000);
		featureMap.put(Feature.NUM_USERS, featureMap.get(Feature.NUM_USERS) / 5000);
		featureMap.put(Feature.AV_REVISION_LENGTH, featureMap.get(Feature.AV_REVISION_LENGTH) / 10000);
		featureMap.put(Feature.AV_COMMENT_LENGTH, featureMap.get(Feature.AV_COMMENT_LENGTH) / 80);
	}
	
	public String featuresToString() {
		StringBuilder builder = new StringBuilder();
		for (Feature key : featureMap.keySet()) {
			System.out.println(key.toString());
			builder.append(featureMap.get(key) + " ");
		}
		
		return builder.toString();
	}
}