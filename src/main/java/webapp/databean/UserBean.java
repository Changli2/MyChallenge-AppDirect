package webapp.databean;

import org.genericdao.PrimaryKey;

@PrimaryKey("id")
public class UserBean {
	int id;
	String email;
	String firstName;
	String lastName;
	int identifier;
	String openid;

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int i) {
		this.identifier = i;
	}

}
