package webapp.controller;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;

import webapp.dao.SubscriptionDAO;
import webapp.dao.UserDAO;
import webapp.databean.SubscriptionBean;
import webapp.databean.UserBean;
import webapp.util.ErrorCodes;
import webapp.util.ErrorReturnResult;
import webapp.util.ReturnResult;
import webapp.util.SuccessReturnResult;

@Controller
public class UserUnassignment {
	@Autowired
	UserDAO userDAO;

	@Autowired
	SubscriptionDAO subDAO;

	@RequestMapping("/unassign")
	public @ResponseBody ReturnResult unassignUser(
			@RequestParam(value = "url", required = false) String url,
			HttpServletRequest request) {
		
		System.out.println("in unassign, url: " + url);
		// two types of result, one of them would be returned later
		SuccessReturnResult res = new SuccessReturnResult();
		ErrorReturnResult errorRes = new ErrorReturnResult();

		HttpURLConnection connection = null;
		try {
			connection = getConnection(url);
		} catch (Exception e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}

		UserBean user = null;
		try {
			user = readAccessChangePayload(connection);
		} catch (Exception e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}

		// check if this new user previously already existed
		UserBean prevExistingUserBean = userDAO.getUserByOpenidAndIdentifier(
				user.getOpenid(), user.getIdentifier());
		if (prevExistingUserBean == null) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
			return errorRes;
		}

		// check if there is no such subscription
		SubscriptionBean subscription = subDAO.getSubscriptionById(user
				.getIdentifier());
		if (subscription == null) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
			return errorRes;
		}
		
		// we do not allow the creator to be unassigned
		if (user.getOpenid().endsWith(subscription.getOpenid())) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.UNAUTHORIZED);
			return errorRes;
		}
		
		try {
			Transaction.begin();
			subscription.setCurUser(subscription.getCurUser() - 1);
			subDAO.update(subscription);
			UserBean targetUser = userDAO.getUserByOpenidAndIdentifier(user.getOpenid(), user.getIdentifier());
			userDAO.delete(targetUser.getId());
			Transaction.commit();
		} catch (RollbackException e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.UNKNOWN_ERROR);
			return errorRes;
		}

		res.setSuccess("true");
		res.setMessage("User unassigned successfully");
		return res;
	}

	private UserBean readAccessChangePayload(HttpURLConnection connection)
			throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(connection.getInputStream());

		UserBean bean = new UserBean();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String path = "/event/payload/account/accountIdentifier";
		System.out.println(xPath.compile(path).evaluate(doc));
		bean.setIdentifier(Integer.parseInt(xPath.compile(path).evaluate(doc)));

		path = "/event/payload/user/email";
		bean.setEmail(xPath.compile(path).evaluate(doc));

		path = "/event/payload/user/firstName";
		bean.setFirstName(xPath.compile(path).evaluate(doc));

		path = "/event/payload/user/lastName";
		bean.setLastName(xPath.compile(path).evaluate(doc));

		path = "/event/payload/user/openId";
		bean.setOpenid(xPath.compile(path).evaluate(doc));

		return bean;
	}

	private HttpURLConnection getConnection(String url) throws Exception {
		OAuthConsumer consumer = new DefaultOAuthConsumer("cl-40027",
				"6DljzI4YNQxij1Mv");
		URL returnURL = null;
		returnURL = new URL(url);
		HttpURLConnection connection = null;
		connection = (HttpURLConnection) returnURL.openConnection();

		connection.setRequestMethod("GET");

		consumer.sign(connection);
		connection.connect();
		return connection;
	}
}
