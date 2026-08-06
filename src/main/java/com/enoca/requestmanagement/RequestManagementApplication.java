package com.enoca.requestmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class RequestManagementApplication {

	private static final List<String> PRODUCTION_REQUIRED_VARIABLES =
			List.of("DB_USERNAME", "DB_PASSWORD", "JWT_SECRET");

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(RequestManagementApplication.class);
		application.addListeners(validateEnvironment());
		application.run(args);
	}

	private static ApplicationListener<ApplicationEnvironmentPreparedEvent> validateEnvironment() {
		return event -> {
			ConfigurableEnvironment environment = event.getEnvironment();
			List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());

			if (activeProfiles.isEmpty()) {
				throw new IllegalStateException(
						"Aktif profil belirtilmedi. Uygulamayi bir profille baslatin, ornegin: "
								+ "--spring.profiles.active=dev veya --spring.profiles.active=prod");
			}

			if (activeProfiles.contains("prod")) {
				List<String> missing = PRODUCTION_REQUIRED_VARIABLES.stream()
						.filter(name -> isBlank(environment.getProperty(name)))
						.toList();

				if (!missing.isEmpty()) {
					throw new IllegalStateException(
							"Production profili icin zorunlu ortam degiskenleri tanimli degil: "
									+ String.join(", ", missing));
				}
			}
		};
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
