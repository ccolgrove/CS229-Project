import java.util.Date;

public class Revision {
  public String user;
	public String id;
	public String pageId;
	public Date timestamp;
	public String comment;
	public String content;
	
	public Revision(String user, String id, String pageId,
	    Date timestamp, String comment, String content) {
	  this.user = user;
		this.id = id;
		this.pageId = pageId;
		this.timestamp = timestamp;
		this.comment = comment;
		this.content = content;
	}
}