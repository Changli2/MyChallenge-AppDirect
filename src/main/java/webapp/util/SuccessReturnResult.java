package webapp.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result")
public class SuccessReturnResult implements ReturnResult {
	private String success;
	private String message;
	private String accountIdentifier;

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

	public String getAccountIdentifier() {
		return accountIdentifier;
	}

	@XmlElement
	public void setAccountIdentifier(String accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

}
