package webapp.databean;

/**
 * Temporary data structure to 
 * extract key info from subscription 
 * notice payload
 * @author liuchang
 *
 */
public class NoticeDataBean {
	private int identifier;
	private String status;
	private String type;
	
	public int getIdentifier() {
		return identifier;
	}
	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
