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
import webapp.databean.NoticeDataBean;
import webapp.databean.SubscriptionBean;
import webapp.databean.UserBean;
import webapp.util.ErrorCodes;
import webapp.util.ErrorReturnResult;
import webapp.util.ReturnResult;
import webapp.util.SuccessReturnResult;

@Controller
public class SubscriptionNotceController {
	@Autowired
	UserDAO userDAO;

	@Autowired
	SubscriptionDAO subDAO;

	@RequestMapping("/status")
	public @ResponseBody ReturnResult noticeSubcription(
			@RequestParam(value = "url", required = false) String url,
			HttpServletRequest request) {

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

		NoticeDataBean noticeBean = null;
		try {
			noticeBean = readSubscriptionNotice(connection);
		} catch (Exception e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}

		// specially take care of "CLOSED" case
		if (noticeBean.getType().equals("CLOSED")) {
			try {
				deleteRecordsByIdentifier(noticeBean.getIdentifier());
			} catch (RollbackException e) {
				errorRes.setSuccess("false");
				errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
				return errorRes;
			}
		}

		SubscriptionBean subBean = null;
		try {
			subBean = subDAO.read(noticeBean.getIdentifier());
		} catch (RollbackException e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
			return errorRes;
		}

		subBean.setStatus(noticeBean.getStatus());
		if (!noticeBean.getType().equals("UPCOMING_INVOICE")) {
			subBean.setSuspended(!subBean.getSuspended());
		}

		try {
			subDAO.update(subBean);
		} catch (RollbackException e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
			return errorRes;
		}
		res.setSuccess("true");
		res.setMessage("account updated successfully");
		return res;
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

	private NoticeDataBean readSubscriptionNotice(HttpURLConnection connection)
			throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(connection.getInputStream());

		NoticeDataBean bean = new NoticeDataBean();

		XPath xPath = XPathFactory.newInstance().newXPath();

		String path = "/event/payload/account/accountIdentifier";
		bean.setIdentifier(Integer.parseInt(xPath.compile(path).evaluate(doc)));

		path = "/event/payload/account/status";
		bean.setStatus(xPath.compile(path).evaluate(doc));

		path = "/event/payload/notice/type";
		bean.setType(xPath.compile(path).evaluate(doc));
		return bean;
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
