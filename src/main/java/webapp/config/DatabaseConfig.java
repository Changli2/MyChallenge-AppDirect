package webapp.config;

import org.genericdao.ConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import webapp.util.ConnectionUtil;
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
	
	/**
	 * This is to get a connection for DAOs
	 */
	@Bean
	public ConnectionPool connection() {
		return new ConnectionPool(jdbcDriverName, jdbcURL, user, password);
	}
	
	/**
	 * Util to use openid
	 */
	@Bean 
	public OpenidUtil openidUtil() {
		return new OpenidUtil();
	}
	
	@Bean
	public ConnectionUtil conUtil() {
		return new ConnectionUtil();
	}
}
