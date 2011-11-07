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
	}
	
	public String featuresToString() {
		StringBuilder builder = new StringBuilder();
		for (double value : featureMap.values())
			builder.append(value + " ");
		
		return builder.toString();
	}
}