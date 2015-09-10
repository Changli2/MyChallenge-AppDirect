package webapp.dao;

import org.genericdao.ConnectionPool;
import org.genericdao.DAOException;
import org.genericdao.GenericDAO;
import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import webapp.databean.UserBean;

@Component
public class UserDAO extends GenericDAO<UserBean> {
	@Autowired
	public UserDAO(ConnectionPool pool) throws DAOException {
		super(UserBean.class, "user", pool);
	}

	public UserBean[] getUserByIdentifier(int identifier) throws RollbackException {
		UserBean[] users = match(MatchArg.equals("identifier", identifier));
		return users;
	}

	public UserBean getUserByOpenidAndIdentifier(String openid, int identifier) {
		UserBean[] user = null;
		try {
			user = match(MatchArg.and(MatchArg.equals("openid", openid)),
					MatchArg.equals("identifier", identifier));
		} catch (RollbackException e) {
			return null;
		}
		return user != null && user.length > 0 ? user[0] : null;
	}
	
	

}
