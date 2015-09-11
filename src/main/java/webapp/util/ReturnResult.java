package webapp.util;

import javax.xml.bind.annotation.XmlElement;

public abstract class ReturnResult {
	private String success;
	private String message;
	
	public String getSuccess() {
		return success;
	}

	@XmlElement
	public void setSuccess(String success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	@XmlElement
	public void setMessage(String message) {
		this.message = message;
	}

}
