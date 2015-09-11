package webapp.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result")
public class SuccessReturnResult extends ReturnResult {
	
	
	private String accountIdentifier;

	public String getAccountIdentifier() {
		return accountIdentifier;
	}

	@XmlElement
	public void setAccountIdentifier(String accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

}
