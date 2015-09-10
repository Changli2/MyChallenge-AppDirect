package webapp.config;

import org.genericdao.ConnectionPool;
import org.openid4java.consumer.ConsumerManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import webapp.util.OpenidUtil;

@Configuration
@ComponentScan
public class DatabaseConfig {
	@Value("${pipes.db.jdbcDriverName}")
	private String jdbcDriverName;
	@Value("${pipes.db.jdbcURL}")
	private String jdbcURL;
	@Value("${pipes.db.user}")
	private String user;
	@Value("${pipes.db.password}")
	private String password;

	@Bean
	public ConnectionPool connection() {
		return new ConnectionPool(jdbcDriverName, jdbcURL, user, password);
	}
	
	@Bean 
	public OpenidUtil openidUtil() {
		return new OpenidUtil();
	}
}
