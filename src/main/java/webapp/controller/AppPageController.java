package webapp.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.openid4java.discovery.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import webapp.dao.SubscriptionDAO;
import webapp.dao.UserDAO;
import webapp.databean.SubscriptionBean;
import webapp.databean.UserBean;
import webapp.databean.UserDisplayBean;
import webapp.util.OpenidUtil;

@Controller
public class AppPageController {
	
	@Autowired
	OpenidUtil openidUtil;
	@Autowired
	UserDAO userDAO;
	@Autowired
	SubscriptionDAO subDAO;

	@RequestMapping("/")
	public String home(@RequestParam(value = "name", required = false) String name, Model model) {
		System.out.println("in root, name: " + name);
		try {
			List<UserDisplayBean> users = new ArrayList<>();
			Transaction.begin();
			SubscriptionBean[] subs = subDAO.match();
			for (SubscriptionBean bean : subs) {
				users.addAll(findUsers(bean));
			}
			Transaction.commit();
			model.addAttribute("users", users);
			model.addAttribute("name", name == null || name.length() == 0 ? "visitor" : name);
		} catch (RollbackException e) {
			// it is okay
			e.printStackTrace();
		}
		
		
		return "index";
	}
	
	private List<UserDisplayBean> findUsers(
			SubscriptionBean bean) {
		List<UserDisplayBean> result = new ArrayList<>();
		try {
			UserBean[] users = userDAO.getUserByIdentifier(bean.getIdentifier());
			for (UserBean user : users) {
				UserDisplayBean displayBean = new UserDisplayBean();
				displayBean.setEmail(user.getEmail());
				displayBean.setFirstName(user.getFirstName());
				displayBean.setStatus(bean.getStatus());
				result.add(displayBean);
			}
		} catch (RollbackException e) {
		}
		
		return result;
	}

	/**
	 * The login url set in the app setting
	 */
	@RequestMapping("/login")
	public void login(@RequestParam(value = "openid", required = false) String openid,
			HttpServletRequest request, HttpServletResponse response) {
		System.out.println("in login");
		openidUtil.authRequest(openid, request, response);
	}
	
	/**
	 * Callback handler for openid
	 */
	@RequestMapping("/login/openid")
	public String openidCallback(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		Identifier identity = openidUtil.verifyResponse(httpRequest);
		String name = "visitor";
		if(identity != null)
			name = identity.getIdentifier();
		return "redirect:/?name=" + name;
	}
}
