package take_two;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import query.QueryGetter;

public class Main {
  public static final String TEST_DIR = "/Users/jtibs/Dropbox/cs229files/articles/";
  private static final String TRAIN_DIR = "/Users/jtibs/Dropbox/cs229files/revhistories/revision_diffs_by_user/";
  public static final String USER = "Hammersoft";
  //public static final String USER = "SF007";
  //public static final String USER = "The_Egyptian_Liberal";
  //public static final String USER = "JerryOrr";
  //public static final String USER = "Tangledorange";
  
  public static void main(String[] args) {
    List<Revision> revisions = new ArrayList<Revision>();
    List<TestDocument> documents = new ArrayList<TestDocument>();
    List<TestDocument> allDocuments = new ArrayList<TestDocument>();
    
    // read in the train and test data
    System.out.println("reading in train data!");
    XMLParser parser = new XMLParser();
    File dir = new File(TRAIN_DIR + USER);
    for (File file : dir.listFiles()) {
      //System.err.println(file);
      Revision revision = parser.parseRevision(file);
      if (revision != null) revisions.add(revision);
    }

    System.out.println("reading in test data!");
    
    List<String> documentsToUse = null;
    try {
      documentsToUse = QueryGetter.getNMostRecentlyEditedPageIds(USER, 50);
    } catch (Exception e) { e.printStackTrace(); }
    
    dir = new File(TEST_DIR);
    for (File file : dir.listFiles()) {
      String name = file.getName();
      int dash = name.indexOf("-");
      int dot = name.indexOf(".");
      String id = name.substring(0, dash);
      if (! name.substring(dash + 1, dot).equals(USER) ||
          documentsToUse.indexOf(id) == -1) 
        continue;
      
      TestDocument document = parser.parseDocument(file);
      if (document.paragraphs.size() == 0) continue;
      //if (document.paragraphs.size() < 10) continue;
      document.id = id;
      //if (documentsToUse.indexOf(id) < 25 && documents.size() < 10)
      if (documentsToUse.indexOf(id) < 10)
      //if (documents.size() < 10)
        documents.add(document);
    
      allDocuments.add(document);
    }
    
    // prepare test data
    System.out.println("calculating scores!");
    for (TestDocument document : allDocuments)
      calculateScores(document, revisions);
    
    // train + test using leave-one-out cross-validation
    System.out.println("testing!");
    MultinomialNaiveBayes classifier = new MultinomialNaiveBayes();
    for (TestDocument testDocument : documents) {
      List<Revision> trainRevisions = new ArrayList<Revision>(revisions);
      for (Revision revision : revisions) {
        if (revision.pageId.equals(testDocument.id))
          trainRevisions.remove(revision);
      }
      
      List<TestDocument> trainDocuments = new ArrayList<TestDocument>();
      for (TestDocument document : allDocuments) {
        if (document.equals(testDocument)) continue;
        
        TestDocument newDocument = new TestDocument();  
        for (int i = 0; i < document.paragraphs.size(); i++) {
          if (document.scores.get(i) == 0) {
            newDocument.paragraphs.add(document.paragraphs.get(i));
            newDocument.scores.add(document.scores.get(i));
          }
        }
        trainDocuments.add(newDocument);
      }
      
      //classifier.countDocumentWords = true;
      classifier.calculateProbabilities(trainRevisions, trainDocuments);
      List<MultinomialNaiveBayes.Pair> predictions = classifier.mostLikelyParagraphs(testDocument);
      
      
      Set<String> revisedParagraphs =  new HashSet<String>();
      for (int i = 0; i < testDocument.paragraphs.size(); i++) {
        if (testDocument.scores.get(i) > 0)
          revisedParagraphs.add(testDocument.paragraphs.get(i));  
      }
      System.out.println("size: " + revisedParagraphs.size()); 
      
      System.out.println("num paragraphs: " + predictions.size());
      int rank = -1;
      boolean done = false;
      for (int i = 0; i < predictions.size(); i++) {
        //System.out.println("~~~PREDICTION" + i + "~~~\n");
        //System.out.println(predictions.get(i).paragraph);
        //System.out.println("score: " + predictions.get(i).score);
        if (revisedParagraphs.contains(predictions.get(i).paragraph)) { 
          rank = i;
          done = true;
        }
        if (done) break;
      }
      System.out.println("rank: " + rank);
      //System.out.println("PREDICTED:\n" + predictions.get(0).paragraph);
      //System.out.println("ACTUAL:\n" + actual);    
    }
  }

  /** updates document.scores */
  private static void calculateScores(TestDocument document, List<Revision> revisions) {
    for (String paragraph : document.paragraphs)
      // initialize scores list
      document.scores.add(0.0);
    int count = 0;
    for (Revision revision : revisions) {
      if (! revision.pageId.equals(document.id))
        continue;

      String[] revisionSplit = revision.content.split("\\s+");
      String[] paragraphSplit = revision.content.split("\\s+");
      List<Double> jaccardScores = new ArrayList<Double>();
      for (int i = 0; i < document.paragraphs.size(); i++) {
        String paragraph = document.paragraphs.get(i);

        int matches = 0;
        for (String word : revisionSplit) {
          if (paragraph.contains(word) && word.length() > 0)
            matches++;
        }
        for (String word : paragraphSplit) {
          if (revision.content.contains(word) && word.length() > 0)
            matches++;
        }
        jaccardScores.add((double) matches / (revisionSplit.length + paragraphSplit.length));
      }
      double max = -1.0;
      int index = -1;
      for (int i = 0; i < jaccardScores.size(); i++) {
        if (jaccardScores.get(i) > max) {
          max = jaccardScores.get(i);
          index = i;
        }
      }

      document.scores.set(index, document.scores.get(index) + 1);
      //System.out.println("\n~~~REVISION~~~\n" + revision.content);
      //System.out.println("\n~~~PARAGRAPH~~~\n" + document.paragraphs.get(index));
    }
  }
}