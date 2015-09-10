package webapp.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result")
public class ErrorReturnResult implements ReturnResult {
	private String success;
	private String errorCode;
	
	public String getSuccess() {
		return success;
	}
	
	@XmlElement
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getErrorCode() {
		return errorCode;
	}
	
	@XmlElement
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	
}
