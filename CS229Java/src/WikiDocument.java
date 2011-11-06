import java.util.ArrayList;
import java.util.List;

public class WikiDocument {
	public String title;
	public int label;
	public List<Revision> revisions;
	
	public WikiDocument(String title, int label) {
		this.title = title;
		this.label = label;
		revisions = new ArrayList<Revision>();
	}
}