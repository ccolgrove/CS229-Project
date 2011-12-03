package take_two;

import java.util.Date;

public class Revision {
  public String user;
	public String id;
	public Date timestamp;
	public String comment;
	public String content;
	
	public Revision(String user, String id, Date timestamp, 
					String comment, String content) {
	  this.user = user;
		this.id = id;
		this.timestamp = timestamp;
		this.comment = comment;
		this.content = content;
	}
}