package take_two;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static final String TEST_DIR = "../../../articles/";
  private static final String TRAIN_DIR = "../../../revhistories/revision_diffs_by_user/";
  public static final String USER = "Hammersoft";
  //public static final String USER = "SF007";
  //public static final String USER = "The_Egyptian_Liberal";
  //public static final String USER = "JerryOrr";
  //public static final String USER = "Tangledorange";
  
  public static void main(String[] args) {
    List<Revision> revisions = new ArrayList<Revision>();
    List<TestDocument> documents = new ArrayList<TestDocument>();

    // read in the train and test data
    System.out.println("reading in train data!");
    XMLParser parser = new XMLParser();
    int count = 0;
    File dir = new File(TRAIN_DIR + USER);
    for (File file : dir.listFiles()) {
      //System.err.println(file);
      Revision revision = parser.parseRevision(file);
      if (revision != null) revisions.add(revision);
    }

    System.out.println("reading in test data!");
    dir = new File(TEST_DIR);
    for (File file : dir.listFiles()) {
      String name = file.getName();
      int dash = name.indexOf("-");
      int dot = name.indexOf(".");
      if (! name.substring(dash + 1, dot).equals(USER)) 
        continue;
      TestDocument document = parser.parseDocument(file);
      if (document.paragraphs.size() == 0) continue;
      document.id = name.substring(0, dash);
      documents.add(document);
    }
    
    // prepare test data
    System.out.println("calculating scores!");
    for (TestDocument document : documents)
      calculateScores(document, revisions);
    
    // train + test using leave-one-out cross-validation
    System.out.println("testing!");
    MultinomialNaiveBayes classifier = new MultinomialNaiveBayes();
    for (TestDocument document : documents) {
      List<Revision> train = new ArrayList<Revision>(revisions);
      for (Revision revision : revisions) {
        if (revision.pageId.equals(document.id))
          train.remove(revision);
      }
      
      classifier.calculateProbabilities(train, documents);
      List<MultinomialNaiveBayes.Pair> predictions = classifier.mostLikelyParagraphs(document);
      
      document.orderParagraphs();
      String actual = document.paragraphs.get(0);
      
      System.out.println("num paragraphs: " + predictions.size());
      int rank = -1;
      for (int i = 0; i < predictions.size(); i++) {
        //System.out.println("~~~PREDICTION" + i + "~~~\n");
        //System.out.println(predictions.get(i).paragraph);
        System.out.println("score: " + predictions.get(i).score);
        if (predictions.get(i).paragraph.equals(actual))
          rank = i;
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
      System.out.println("\n~~~REVISION~~~\n" + revision.content);
      System.out.println("\n~~~PARAGRAPH~~~\n" + document.paragraphs.get(index));
    }
  }
}