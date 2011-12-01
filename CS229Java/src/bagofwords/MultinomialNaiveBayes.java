package bagofwords;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class MultinomialNaiveBayes {
	
	public static final double ADD_K = 1;
	public static final String UNK_TOKEN = "<UNKNOWN>";

	public static Map<Integer, String> getDocumentMap() {
		Map<Integer, String> testMap = new HashMap<Integer, String>();
		testMap.put(1, "hello world");
		testMap.put(2, "goodbye world");
		testMap.put(3, "test test blah blah blah");
		
		return testMap;
	}
	
	public static Map<String, Integer> getEditWordCounts(List<String> edits) {
		Map<String, Integer> counts = new HashMap<String, Integer>();
		
		CoreLabelTokenFactory factory = new CoreLabelTokenFactory();
		
		for(String edit : edits) {
			PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(edit),
		              factory, "");
		    for (; ptbt.hasNext(); ) {
		    	String label = ptbt.next().originalText();
		        if (counts.containsKey(label)) {
		        	counts.put(label, counts.get(label)+1);
		        } else {
		        	counts.put(label, 1);
		        }
		    }
		}
		
		counts.put(UNK_TOKEN, 1);
		return counts;
	}
	
	public static Map<String, Double> getEditWordProbs(Map<String, Integer> counts) {
		double total = 0;
		Map<String, Double> probs = new HashMap<String, Double>();
		for (String key: counts.keySet()) {
			total += counts.get(key);
		}
		
		for (String key: counts.keySet()) {
			probs.put(key, Math.log(counts.get(key)/total));
		}
		
		return probs;
	}
	
	public static double getSentenceProb(Map<String, Double> wordProbs, String sentence) {
		double logProb = 0;
		CoreLabelTokenFactory factory = new CoreLabelTokenFactory();
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(sentence),
	              factory, "");
	    for (; ptbt.hasNext(); ) {
	    	String label = ptbt.next().originalText();
	        if (wordProbs.containsKey(label)) {
	        	logProb += wordProbs.get(label);
	        } else {
	        	logProb += wordProbs.get(UNK_TOKEN);
	        }
	    }
	    return logProb;
	}
	
	public static String mostLikelySentence(Map<String, Double> wordProbs, Map<Integer, String> sentences) {
		double maxProb = Double.NEGATIVE_INFINITY;
		String maxSentence = "";
		for (String sentence : sentences.values()) {
			double prob = getSentenceProb(wordProbs, sentence);
			if (prob > maxProb) {
				maxProb = prob;
				maxSentence = sentence;
			}
		}
		
		return maxSentence;
	}
	
	public static void main(String[] args) {
		List<String> edits = new ArrayList<String>();
		edits.add("hello");
		edits.add("goodbye goodbye");
		
		Map<String, Integer> counts = getEditWordCounts(edits);
		Map<String, Double> probs = getEditWordProbs(counts);
		String mls = mostLikelySentence(probs, getDocumentMap());
	}
	
}
