package bagofwords;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import take_two.Revision;
import take_two.TestDocument;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class MultinomialNaiveBayes {
	
	public static final double ADD_ALPHA = 0.1;
	public static final String UNK_TOKEN = "<UNKNOWN>";
	
	public Map<String, Double> wordProbs;
	
	private static class Pair {
		public double score;
		public String paragraph;
		
		public Pair(String paragraph, double score) {
			this.score = score;
			this.paragraph = paragraph;
		}
	}
	
	public Map<String, Double> getWordCounts(List<Revision> revisions, List<TestDocument> documents) {
		Map<String, Double> counts = new HashMap<String, Double>();
		
		CoreLabelTokenFactory factory = new CoreLabelTokenFactory();
		
		for(Revision rev : revisions) {
			String edit = rev.content;
			PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(edit),
		              factory, "");
		    for (; ptbt.hasNext(); ) {
		    	String label = ptbt.next().originalText();
		        if (counts.containsKey(label)) {
		        	counts.put(label, counts.get(label)+1);
		        } else {
		        	counts.put(label, 1.0);
		        }
		    }
		}
		
		for (TestDocument doc: documents) {
			for (String paragraph : doc.paragraphs) {
				PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(paragraph),
		              factory, "");
				for (; ptbt.hasNext(); ) {
					String label = ptbt.next().originalText();
			        if (!counts.containsKey(label)) {
			        	counts.put(label, 0.0);
			        }
			    }	
			}
		}
		
		counts.put(UNK_TOKEN, 0.0);
		
		for (String key : counts.keySet()) {
			counts.put(key, counts.get(key) + ADD_ALPHA);
		}
		return counts;
	}
	
	public Map<String, Double> getWordProbs(Map<String, Double> counts) {
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
	
	public int calculateDocumentSize(TestDocument document) {
		int totalWords = 0;
		for (String paragraph : document.paragraphs) {
			CoreLabelTokenFactory factory = new CoreLabelTokenFactory();
			PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(paragraph),
		              factory, "");
			totalWords += ptbt.tokenize().size();
		}
		
		return totalWords;
	}
	
	public double getSentenceProb(Map<String, Double> wordProbs, String sentence, int totalWords) {
		double logProb = 0;
		CoreLabelTokenFactory factory = new CoreLabelTokenFactory();
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(sentence),
	              factory, "");
		
		double numTokens = 0;
	    for (; ptbt.hasNext(); ) {
	    	String label = ptbt.next().originalText();
	        if (wordProbs.containsKey(label)) {
	        	logProb += wordProbs.get(label);
	        } else {
	        	logProb += wordProbs.get(UNK_TOKEN);
	        }
	        numTokens ++;
	    }
	    
	    if (numTokens > 0)
	    	return logProb + Math.log(numTokens/totalWords);
	    else
	    	return Double.NEGATIVE_INFINITY;
	}
	
	public void calculateProbabilities(List<Revision> revisions, List<TestDocument> docs) {
		Map<String, Double> wordCounts = getWordCounts(revisions, docs);
		wordProbs = getWordProbs(wordCounts);
	}
	
	public List<String> mostLikelyParagraphs(TestDocument document) {
	
		List<Pair> paragraphs = new ArrayList<Pair>();
		
		for (String paragraph : document.paragraphs) {
			int length = calculateDocumentSize(document);
			System.out.println(length);
			double prob = getSentenceProb(wordProbs, paragraph, length);
			paragraphs.add(new Pair(paragraph, prob));	
		}
		
		Collections.sort(paragraphs, new Comparator<Pair>() {
			@Override
			public int compare(Pair o1, Pair o2) {
				return -1*Double.compare(o1.score, o2.score);
			}	
		});
		
		List<String> orderedParagraphs = new ArrayList<String>();
		for (Pair p: paragraphs) {
			orderedParagraphs.add(p.paragraph);
		}
		
		return orderedParagraphs;
	}
	
	public static void main(String[] args) {
		
		TestDocument doc = new TestDocument();
		doc.paragraphs.add("test test");
		doc.paragraphs.add("hello goodbye");
		doc.paragraphs.add("hi hi hi");
		
		Revision rev1 = new Revision("a", "b", "c",  null, "", "hello hello world test");
		List<Revision> revs = new ArrayList<Revision>();
		revs.add(rev1);
		
		List<TestDocument> documents = new ArrayList<TestDocument>();
		documents.add(doc);
		
		MultinomialNaiveBayes mnb = new MultinomialNaiveBayes();
		mnb.calculateProbabilities(revs, documents);
		
		List<String> mostLikely = mnb.mostLikelyParagraphs(doc);
		
		System.out.println(mostLikely);
		
	}
	
}
