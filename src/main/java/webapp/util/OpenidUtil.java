package webapp.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.OpenIDException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.springframework.beans.factory.annotation.Value;

/**
 * I took this from the web
 * https://code.google.com/p/openid4java/wiki/QuickStart
 */
public class OpenidUtil {
	public ConsumerManager manager;
	
	@Value("${pipes.returnUrl}")
	private String returnToUrl;

    public OpenidUtil() {
        manager = new ConsumerManager();
        manager.setNonceVerifier(new InMemoryNonceVerifier(5000)); 
        manager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
    }
    
    
    // --- placing the authentication request ---
    @SuppressWarnings("rawtypes")
    public String authRequest(String userSuppliedString, HttpServletRequest httpReq, HttpServletResponse httpResp) {
        try
        {
            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(userSuppliedString);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = manager.associate(discoveries);

            // store the discovery information in the user's session
            httpReq.getSession().setAttribute("openid-disc", discovered);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email",                         // attribute alias
                    "http://schema.openid.net/contact/email",   // type URI
                    true);                                      // required

            // attach the extension to the authentication request
            authReq.addExtension(fetch);

            try {
				httpResp.sendRedirect(authReq.getDestinationUrl(true));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
            return null;

        }
        catch (OpenIDException e)
        {
            // present error to the user
        }

        return null;
    }

    
    // --- processing the authentication response ---
    @SuppressWarnings("rawtypes")
    public Identifier verifyResponse(HttpServletRequest httpReq)
    {
        try
        {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                    new ParameterList(httpReq.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation)
                    httpReq.getSession().getAttribute("openid-disc");

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = httpReq.getRequestURL();
            try {
				URL urlReceived = new URL(receivingURL.toString());
				if (urlReceived.getPort() == -1) {
					urlReceived = new URL(urlReceived.getProtocol(), urlReceived.getHost(), 80, urlReceived.getFile());
					receivingURL = new StringBuffer(urlReceived.toString());
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("get receiving URL: " + receivingURL);
            
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(httpReq.getQueryString());

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = manager.verify(
                    receivingURL.toString(),
                    response, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null)
            {
                AuthSuccess authSuccess =
                        (AuthSuccess) verification.getAuthResponse();

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
                {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    List emails = fetchResp.getAttributeValues("email");
                    String email = (String) emails.get(0);
                }

                return verified;  // success
            }
        }
        catch (OpenIDException e)
        {
            
        }

        return null;
    }
}
