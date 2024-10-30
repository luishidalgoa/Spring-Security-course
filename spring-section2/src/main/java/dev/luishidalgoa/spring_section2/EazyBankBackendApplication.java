package dev.luishidalgoa.spring_section2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan("dev.luishidalgoa.spring_section1") //es usado para buscar los componenentes cuando estan fuera del paquete principal
public class EazyBankBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EazyBankBackendApplication.class, args);
	}

}
