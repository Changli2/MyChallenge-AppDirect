package webapp.controller;

import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;


import org.genericdao.RollbackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import webapp.dao.SubscriptionDAO;
import webapp.dao.UserDAO;
import webapp.databean.SubscriptionBean;
import webapp.databean.UserBean;
import webapp.util.ConnectionUtil;
import webapp.util.ErrorCodes;
import webapp.util.ErrorReturnResult;
import webapp.util.ReturnResult;
import webapp.util.SuccessReturnResult;

@Controller
public class SubscriptionController {
	@Autowired
	UserDAO userDAO;

	@Autowired
	SubscriptionDAO subDAO;
	
	@Autowired
	ConnectionUtil connectionUtil;
	
	/**
	 * Handle subscription create
	 */
	@RequestMapping("/create")
	public @ResponseBody ReturnResult subcription(
			@RequestParam(value = "url", required = false) String url,
			HttpServletRequest request) {

		// two types of result, one of them would be returned later
		SuccessReturnResult res = new SuccessReturnResult();
		ErrorReturnResult errorRes = new ErrorReturnResult();

		HttpURLConnection connection = null;
		try {
			// get connection
			connection = connectionUtil.getConnection(url);
		} catch (Exception e) {
			// url not accessible
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}

		SubscriptionBean newSubscription = null;
		try {
			// extract info from xml
			newSubscription = readSubscription(connection);
		} catch (Exception e) {
			// xml is not valid
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}
		
		// when a subscription is created, set the user as one
		newSubscription.setCurUser(1);
		newSubscription.setStatus("Active");
		
		// if is invalid state if there existing such subscription 
		// idenfified by the creator openid
		SubscriptionBean checkPreviousExistBean = subDAO
				.getSubByOpenId(newSubscription.getOpenid());
		if (checkPreviousExistBean != null) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.USER_ALREADY_EXISTS);
			return errorRes;
		}

		try {
			// put the new subscription bean in the database
			subDAO.createAutoIncrement(newSubscription);
		} catch (RollbackException e) {
			// do nothing is okay, database handles that
			e.printStackTrace();
		}

		// the creator is the first user of my app
		// based on the new subscription bean, get useful info to build
		// creator user bean
		UserBean userBean = buildUserBeanBySubscription(newSubscription);
		try {
			userDAO.createAutoIncrement(userBean);
		} catch (RollbackException e) {
			// do nothing is okay, database handles that
			e.printStackTrace();
		}

		res.setAccountIdentifier(Integer.toString(userBean.getIdentifier()));
		res.setSuccess("true");
		res.setMessage("account created successfully");
		return res;
	}

	private UserBean buildUserBeanBySubscription(SubscriptionBean subBean) {
		UserBean userBean = new UserBean();
		userBean.setEmail(subBean.getCreator());
		userBean.setFirstName(subBean.getFirstName());
		userBean.setLastName(subBean.getLastName());
		userBean.setIdentifier(subBean.getIdentifier());
		userBean.setOpenid(subBean.getOpenid());
		return userBean;
	}
	
	/**
	 * Extract info and put in a bean,
	 * for current subscription case
	 */
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

		// if there is a user number limit, use it
		// otherwise make it int max value
		try {
			Element elem = (Element) doc.getDocumentElement();
			elem = (Element) elem.getElementsByTagName("payload").item(0);
			elem = (Element) elem.getElementsByTagName("order").item(0);
			NodeList list = elem.getElementsByTagName("item");
			if (list == null || list.getLength() == 0) {
				bean.setMaxUser(Integer.MAX_VALUE);
			} else {
				for (int i = 0; i < list.getLength(); i++) {
					Element n = (Element) list.item(i);
					NodeList unit = n.getElementsByTagName("unit");
					Element unitElem = (Element) unit.item(0);
					String unitName = unitElem.getFirstChild().getNodeValue();
					if (unitName.equals("USER")) {
						NodeList quantity = n.getElementsByTagName("quantity");
						Element quanElem = (Element) quantity.item(0);
						String maxValue = quanElem.getFirstChild()
								.getNodeValue();
						System.out.println(quanElem.getFirstChild()
								.getNodeValue());
						bean.setMaxUser(Integer.parseInt(maxValue));
						break;
					}

				}
			}
		} catch (Exception e) {
			bean.setMaxUser(Integer.MAX_VALUE);
		}

		return bean;
	}

}
