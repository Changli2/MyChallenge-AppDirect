package webapp.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result")
public class ErrorReturnResult extends ReturnResult {
	private String errorCode;
	
	public String getErrorCode() {
		return errorCode;
	}
	
	@XmlElement
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	
}
