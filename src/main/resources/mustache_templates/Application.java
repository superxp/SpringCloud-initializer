package {{packageName}};

import org.springframework.boot.SpringApplication;
{{#useMybatis}}
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
{{/useMybatis}}
{{^useMybatis}}
import org.springframework.cloud.client.SpringCloudApplication;
{{/useMybatis}}

{{#useMybatis}}
@SpringBootApplication(exclude =
				{DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@EnableDiscoveryClient
@EnableCircuitBreaker
{{/useMybatis}}
{{^useMybatis}}
@SpringCloudApplication
{{/useMybatis}}
public class {{bootstrapApplicationName}} {

	public static void main(String[] args) {
		SpringApplication.run({{bootstrapApplicationName}}.class, args);
	}
}
