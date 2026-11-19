package br.cefetmg.ocva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OcvaApplication {
	public static void main(String[] args) {
		SpringApplication.run(OcvaApplication.class, args);
	} 

}
