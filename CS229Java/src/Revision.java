public class Revision {
	public String id;
	public String timestamp;
	public String user;
	public String comment;
	public int size;  // will be 0 if size information was not available
	
	public Revision(String id, String timestamp, String user,
					String comment, int size) {
		this.id = id;
		this.timestamp = timestamp;
		this.user = user;
		this.comment = comment;
		this.size = size;
	}
}