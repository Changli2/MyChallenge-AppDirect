package webapp.databean;

import org.genericdao.PrimaryKey;

@PrimaryKey("openid")
public class AppUserBean {
	String openid;
	String name;
	String email;


	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
