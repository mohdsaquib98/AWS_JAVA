package spring.boot.aws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class SpringBootWithAwsS3Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWithAwsS3Application.class, args);
	}

}
