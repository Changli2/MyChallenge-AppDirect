package webapp.dao;

import org.genericdao.ConnectionPool;
import org.genericdao.DAOException;
import org.genericdao.GenericDAO;
import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import webapp.databean.SubscriptionBean;

@Component
public class SubscriptionDAO extends GenericDAO<SubscriptionBean> {
	@Autowired
	public SubscriptionDAO(ConnectionPool pool) throws DAOException {
		super(SubscriptionBean.class, "subscription", pool);
	}
	
	public SubscriptionBean getSubByEmail(String email) {
		if (email == null) {
			System.out.println("wawawa, email is null in subDAO");
			return null;
		}
		
		
		SubscriptionBean[] sub = null;
		try {
			sub = match(MatchArg.equals("creator", email));
		} catch (RollbackException e) {
			e.printStackTrace();
		}
		
		if (sub == null || sub.length == 0) {
			return null;
		} else {
			return sub[0];
		}
	}
	
	public SubscriptionBean getSubByOpenId(String id) {
		if (id == null) {
			System.out.println("wawawa, openid is null in subDAO");
			return null;
		}
		
		
		SubscriptionBean[] sub = null;
		try {
			sub = match(MatchArg.equals("openid", id));
		} catch (RollbackException e) {
			e.printStackTrace();
		}
		
		if (sub == null || sub.length == 0) {
			return null;
		} else {
			return sub[0];
		}
	}

	public SubscriptionBean getSubscriptionById(int identifier) {
		SubscriptionBean[] sub = null;
		try {
			sub = match(MatchArg.equals("identifier", identifier));
		} catch (RollbackException e) {
			// it is okay
			e.printStackTrace();
		}
		
		if (sub == null || sub.length == 0) {
			return null;
		} else {
			return sub[0];
		}
	}
	
}
