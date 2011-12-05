import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
  public static final String TEST_DIR = "../../../articles/";
  private static final String TRAIN_DIR = "../../../revhistories/revision_diffs_by_user/";
  public static final String USER = "Hammersoft";

  public static void main(String[] args) {
    List<Revision> revisions = new ArrayList<Revision>();
    List<TestDocument> documents = new ArrayList<TestDocument>();

    // read in the train and test data
    XMLParser parser = new XMLParser();

    File dir = new File(TRAIN_DIR + USER);
    for (File file : dir.listFiles()) {
      //System.err.println(file);
      Revision revision = parser.parseRevision(file);
      if (revision != null) revisions.add(revision);
    }

    dir = new File(TEST_DIR);
    for (File file : dir.listFiles()) {
      String name = file.getName();
      int dash = name.indexOf("-");
      int dot = name.indexOf(".");
      if (! name.substring(dash + 1, dot).equals(USER)) 
        continue;
      TestDocument document = parser.parseDocument(file);
      document.id = name.substring(0, dash);
      documents.add(document);
    }

    // prepare test data
    for (TestDocument document : documents)
      calculateScores(document, revisions);
    
    // train + test using leave-one-out cross-validation
    
  }

  /** updates document.scores */
  private static void calculateScores(TestDocument document, List<Revision> revisions) {
    for (String paragraph : document.paragraphs)
      // initialize scores list
      document.scores.add(0.0);

    for (Revision revision : revisions) {
      if (! revision.pageId.equals(document.id))
        continue;

      String[] split = revision.content.split("\\s+");
      List<Double> jaccardScores = new ArrayList<Double>();
      for (int i = 0; i < document.paragraphs.size(); i++) {
        String paragraph = document.paragraphs.get(i);

        int matches = 0;
        for (String word : split) {
          if (paragraph.contains(word) && word.length() > 0)
            matches++;
        }     
        jaccardScores.add((double) matches / split.length);
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