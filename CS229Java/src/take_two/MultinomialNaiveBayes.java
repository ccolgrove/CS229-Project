package take_two;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class MultinomialNaiveBayes {
	
	public static final double ADD_ALPHA = 0.1;
	public static final String UNK_TOKEN = "<UNKNOWN>";
	
	public Map<String, Double> wordProbs;
	public Map<String, Double> wordNonCounts;
	public Map<String, Double> wordNonProbs;
	public Map<String, Double> wordAllCounts;
	public Map<String, Double> wordAllProbs;
	public double probRevision;
	
	public boolean countDocumentWords = true;
	public boolean selectFeatures = false;
	public double documentWeight = 0.1;
	public int numFeatures = 100;
	
	public static class Pair {
		public double score;
		public String paragraph;
		
		public Pair(String paragraph, double score) {
			this.score = score;
			this.paragraph = paragraph;
		}
		
		public String toString() {
			return "Pair[" + paragraph + "=" + score +"]";
		}
	}
	
	
	public Map<String, Double> getWordCounts(List<Revision> revisions, List<TestDocument> documents) {
		Map<String, Double> counts = new HashMap<String, Double>();
		wordNonCounts = new HashMap<String, Double>();
		wordAllCounts = new HashMap<String, Double>();
		
		double totalP = 0;
		for (TestDocument doc: documents) {
			totalP += doc.paragraphs.size();
		}
		
		probRevision = (double) revisions.size()/(revisions.size() + totalP);
		
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
		        
		        if (wordAllCounts.containsKey(label)) {
		        	wordAllCounts.put(label, wordAllCounts.get(label)+1);
		        } else {
		        	wordAllCounts.put(label, 1.0);
		        }
		    }
		}
		
		Map<String, Double>  docCountMap = null;
		if (selectFeatures) {
			docCountMap = wordNonCounts;
		} else {
			docCountMap = counts;
		}
		
		for (TestDocument doc: documents) {
			for (String paragraph : doc.paragraphs) {
				PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(paragraph),
		              factory, "");
				for (; ptbt.hasNext(); ) {
					String label = ptbt.next().originalText();
			        if (!docCountMap.containsKey(label)) {
			        	if (!countDocumentWords) {
			        		docCountMap.put(label, 0.0);
			        	} else {
			        		docCountMap.put(label, documentWeight);
			        	}
			        } else if (countDocumentWords) {
			        	docCountMap.put(label, docCountMap.get(label) + documentWeight);
			        }
			        
			        if (wordAllCounts.containsKey(label)) {
			        	wordAllCounts.put(label, wordAllCounts.get(label)+1);
			        } else {
			        	wordAllCounts.put(label, 1.0);
			        }
			    }	
			}
		}
		
		counts.put(UNK_TOKEN, 0.0);
		
		for (String word: wordAllCounts.keySet()) {
			if (!counts.containsKey(word)) {
				counts.put(word, 0.0);
			}
			if (!wordNonCounts.containsKey(word)) {
				wordNonCounts.put(word, 0.0);
			}
		}
		
		for (String key : counts.keySet()) {
			counts.put(key, counts.get(key) + ADD_ALPHA);
		}
		
		for (String key: wordNonCounts.keySet()) {
			wordNonCounts.put(key, wordNonCounts.get(key) + ADD_ALPHA);
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
			probs.put(key, counts.get(key)/total);
		}
		
		total = 0;
		wordNonProbs = new HashMap<String, Double>();
		for (String key: wordNonCounts.keySet()) {
			total += wordNonCounts.get(key);
		}
		
		for (String key: wordNonCounts.keySet()) {
			wordNonProbs.put(key, wordNonCounts.get(key)/total);
		}
		
		total = 0;
		wordAllProbs = new HashMap<String, Double>();
		for (String key: wordAllCounts.keySet()) {
			total += wordAllCounts.get(key);
		}
		
		for (String key: wordAllCounts.keySet()) {
			wordAllProbs.put(key, wordAllCounts.get(key)/total);
		}
		
		return probs;
	}
	
	public void selectFeatures() {
		final Map<String, Double> miMap = new HashMap<String, Double>();
		for (String word: wordAllProbs.keySet()) {
			double miTT = wordProbs.get(word)*probRevision*Math.log(wordProbs.get(word)/wordAllProbs.get(word));
			double miTF = wordNonProbs.get(word)*(1-probRevision)*Math.log(wordNonProbs.get(word)/wordAllProbs.get(word));
			double miFT = (1 - wordProbs.get(word))*probRevision*Math.log((1 - wordProbs.get(word))/wordAllProbs.get(word));
			double miFF = (1 - wordNonProbs.get(word))*(1-probRevision)*Math.log((1 - wordNonProbs.get(word))/wordAllProbs.get(word));
			double mi = miTT + miTF + miFT + miFF;
			
			System.out.println(word + " " + mi);
			miMap.put(word, mi);
		}
		
		List<String> wordList = new ArrayList<String>(miMap.keySet());
		
		Collections.sort(wordList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return -1*Double.compare(miMap.get(o1), miMap.get(o2));
			}	
		});
		
		System.out.println(wordList);
		
		for (int i = numFeatures; i < wordList.size(); i++) {
			String word = wordList.get(i);
			wordProbs.remove(word);
		}
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
	
	public double getSentenceProb(Map<String, Double> wordProbs, String sentence, int total) {
		double logProb = 0;
		CoreLabelTokenFactory factory = new CoreLabelTokenFactory();
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(sentence),
	              factory, "");
		
		int numTokens = 0;
		
	    for (; ptbt.hasNext(); ) {
	    	String label = ptbt.next().originalText();
	        if (wordProbs.containsKey(label)) {
	        	logProb += Math.log(wordProbs.get(label));
	        	numTokens ++;
	        } else if (!selectFeatures) {
	        	numTokens ++;
	        	logProb += Math.log(wordProbs.get(UNK_TOKEN));
	        }
	    }
	    
	    if (numTokens > 0)
	      //return 1.0/numTokens;
	    	//return logProb/numTokens
	    	return logProb + Math.log((double) numTokens/total);
	    else
	    	return Double.NEGATIVE_INFINITY;
	}
	
	public void calculateProbabilities(List<Revision> revisions, List<TestDocument> docs) {
		Map<String, Double> wordCounts = getWordCounts(revisions, docs);
		wordProbs = getWordProbs(wordCounts);
		if (selectFeatures) {
			selectFeatures();
		}
	}
	
	public List<Pair> mostLikelyParagraphs(TestDocument document) {
	
		List<Pair> paragraphs = new ArrayList<Pair>();
		
		int docSize = calculateDocumentSize(document);
		
		for (String paragraph : document.paragraphs) {
			double prob = getSentenceProb(wordProbs, paragraph, docSize);
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
		
		return paragraphs;
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
		
		List<Pair> mostLikely = mnb.mostLikelyParagraphs(doc);
		
		System.out.println(mostLikely);
		
	}
	
}
