package com.compliance.riskmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Needed later for the transaction generator
public class RiskMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(RiskMonitorApplication.class, args);
	}
}

