import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestDocument {
  public String id;
  public List<String> paragraphs;
  public List<Double> scores; 
  
  public TestDocument() {
    paragraphs = new ArrayList<String>();
    scores = new ArrayList<Double>();
  }
}