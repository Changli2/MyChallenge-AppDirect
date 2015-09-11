package webapp.util;

import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

public class ConnectionUtil {
	@Value("${pipes.apikey}")
	private String apikey;
	@Value("${pipes.apisec}")
	private String apisec;
	public HttpURLConnection getConnection(String url) throws Exception {
		OAuthConsumer consumer = new DefaultOAuthConsumer(apikey,
				apisec);
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
