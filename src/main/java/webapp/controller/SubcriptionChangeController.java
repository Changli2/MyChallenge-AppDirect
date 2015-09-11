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
import webapp.util.ConnectionUtil;
import webapp.util.ErrorCodes;
import webapp.util.ErrorReturnResult;
import webapp.util.ReturnResult;
import webapp.util.SuccessReturnResult;

@Controller
public class SubcriptionChangeController {
	@Autowired
	UserDAO userDAO;

	@Autowired
	SubscriptionDAO subDAO;
	
	@Autowired
	ConnectionUtil conUtil;
	
	/**
	 * Handle subscription change
	 */
	@RequestMapping("/change")
	public @ResponseBody ReturnResult changeSubscription(
			@RequestParam(value = "url", required = false) String url,
			HttpServletRequest request) {

		// two types of result, one of them would be returned later
		SuccessReturnResult res = new SuccessReturnResult();
		ErrorReturnResult errorRes = new ErrorReturnResult();

		HttpURLConnection connection = null;
		try {
			// get connection
			connection = conUtil.getConnection(url);
		} catch (Exception e) {
			// url not accessible
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}

		SubscriptionBean updatedBean = null;
		try {
			updatedBean = readSubscription(connection);
		} catch (Exception e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.INVALID_RESPONSE);
			return errorRes;
		}

		SubscriptionBean checkPreviousExistBean = null;
		try {
			checkPreviousExistBean = subDAO.read(updatedBean.getIdentifier());
		} catch (RollbackException e1) {
			errorRes.setSuccess("false");
			// it is database connection problem
			errorRes.setErrorCode(ErrorCodes.UNKNOWN_ERROR);
			return errorRes;
		}

		if (checkPreviousExistBean == null) {
			// invalid it no such subscription exists before
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
			return errorRes;
		}

		// don't allow the new max_user_value smaller than current user number
		if (checkPreviousExistBean.getCurUser() > updatedBean.getMaxUser()) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.MAX_USERS_REACHED);
			return errorRes;
		}

		updatedBean.setCurUser(checkPreviousExistBean.getCurUser());
		
		try {
			subDAO.delete(updatedBean.getIdentifier());
		} catch (RollbackException e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.ACCOUNT_NOT_FOUND);
			return errorRes;
		}

		try {
			subDAO.create(updatedBean);
		} catch (RollbackException e) {
			errorRes.setSuccess("false");
			errorRes.setErrorCode(ErrorCodes.UNKNOWN_ERROR);
			return errorRes;
		}

		res.setSuccess("true");
		res.setMessage("account changed successfully");
		return res;
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

		path = "/event/payload/account/status";
		bean.setStatus(xPath.compile(path).evaluate(doc));

		path = "/event/creator/openId";
		bean.setOpenid(xPath.compile(path).evaluate(doc));

		path = "/event/creator/address/firstName";
		bean.setFirstName(xPath.compile(path).evaluate(doc));

		path = "/event/creator/address/lastName";
		bean.setLastName(xPath.compile(path).evaluate(doc));

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
			// do nothing, no change to the user value
		}

		return bean;
	}

}
