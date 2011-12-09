import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDocument {
  public String id;
  public List<String> paragraphs;
  public List<Double> scores; 
  
  public TestDocument() {
    paragraphs = new ArrayList<String>();
    scores = new ArrayList<Double>();
  }

  public void orderParagraphs() {
    final Map<String, Double> map = new HashMap<String, Double>();
    for (int i = 0; i < paragraphs.size(); i++)
      map.put(paragraphs.get(i), scores.get(i));
    
    Collections.sort(paragraphs, new Comparator<String>() {
      public int compare(String one, String two) {
        if (map.get(one) > map.get(two))
          return -1;
        return 1;
      }
    });
  }
}