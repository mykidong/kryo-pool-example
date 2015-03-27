package kryo.pool.example.util;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class Log4jConfigurer implements InitializingBean{
	
	static Log4jConfigurer instance;
	
	private String confPath;

	public void setConfPath(String confPath) {
		this.confPath = confPath;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		java.net.URL url = this.getClass().getResource(confPath);
		System.out.println("log4j url: " + url.toString());

		DOMConfigurator.configure(url);

		final Logger log = LoggerFactory.getLogger(Log4jConfigurer.class);
		log.debug("log4j loaded...");	
	}
}

