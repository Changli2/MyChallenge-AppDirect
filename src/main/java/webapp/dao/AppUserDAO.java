package webapp.dao;

import org.genericdao.ConnectionPool;
import org.genericdao.DAOException;
import org.genericdao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import webapp.databean.AppUserBean;

@Component
public class AppUserDAO extends GenericDAO<AppUserBean> {
	@Autowired
	public AppUserDAO(ConnectionPool pool) throws DAOException {
		super(AppUserBean.class, "appUser", pool);
	}
}
