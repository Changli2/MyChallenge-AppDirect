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
public class SubscriptionCancelController {
	@Autowired
	UserDAO userDAO;

	@Autowired
	SubscriptionDAO subDAO;

	@RequestMapping("/cancel")
	public @ResponseBody ReturnResult cancellSubcription(
			@RequestParam(value = "url", required = false) String url,
			HttpServletRequest request) {
		System.out.println("get into cancel: " + url);
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

		SubscriptionBean updatedSubscription = null;
		try {
			updatedSubscription = readSubscription(connection);
		} catch (Exception e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}

		try {
			deleteRecordsByIdentifier(updatedSubscription.getIdentifier());
		} catch (RollbackException e1) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
			return errorRes;
		}

		res.setSuccess("true");
		res.setMessage("account deleted successfully");
		return res;
	}

	private SubscriptionBean readSubscription(HttpURLConnection connection)
			throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(connection.getInputStream());

		SubscriptionBean bean = new SubscriptionBean();

		XPath xPath = XPathFactory.newInstance().newXPath();

		String path = "/event/creator/email";
		String email = xPath.compile(path).evaluate(doc);
		System.out.println("get email: " + email);
		bean.setCreator(email);

		path = "/event/payload/order/editionCode";
		bean.setEdition(xPath.compile(path).evaluate(doc));

		path = "/event/payload/order/item/quantity";
		String maxNum = xPath.compile(path).evaluate(doc);
		bean.setMaxUser(maxNum.length() == 0 ? Integer.MAX_VALUE : Integer
				.parseInt(maxNum));

		path = "/event/payload/account/accountIdentifier";
		String id = xPath.compile(path).evaluate(doc);
		if (id != null && id.length() > 0) {
			bean.setIdentifier(Integer.parseInt(id));
		}
		// long timestamp = System.currentTimeMillis();
		// bean.setIdentifier(email + timestamp);

		path = "/event/creator/openId";
		bean.setOpenid(xPath.compile(path).evaluate(doc));

		path = "/event/creator/address/firstName";
		bean.setFirstName(xPath.compile(path).evaluate(doc));

		path = "/event/creator/address/lastName";
		bean.setLastName(xPath.compile(path).evaluate(doc));

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

	private void deleteRecordsByIdentifier(int identifier)
			throws RollbackException {
		Transaction.begin();
		subDAO.delete(identifier);

		UserBean[] users = userDAO.getUserByIdentifier(identifier);
		for (UserBean user : users) {
			userDAO.delete(user.getId());
		}
		if (Transaction.isActive()) {
			Transaction.commit();
		}
	}

}
