package mindbadger.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;

@Configuration
@ComponentScan (lazyInit = true, basePackages = "mindbadger")
@EnableAutoConfiguration
//@ImportResource("classpath:spring-config.xml")
public class ApiConfiguration {
	public ApiConfiguration () {
	}
	
	@Bean
	public InstrumentationLoadTimeWeaver loadTimeWeaver()  throws Throwable {
	    InstrumentationLoadTimeWeaver loadTimeWeaver = new InstrumentationLoadTimeWeaver();
	    return loadTimeWeaver;
	}
}
