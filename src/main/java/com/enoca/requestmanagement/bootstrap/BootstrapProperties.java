package com.enoca.requestmanagement.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.bootstrap")
public record BootstrapProperties(

        String adminEmail,

        String adminPassword,

        @DefaultValue("Sistem") String adminFirstName,

        @DefaultValue("Yoneticisi") String adminLastName,

        @DefaultValue("Yonetim") String departmentName,

        @DefaultValue("MGMT") String departmentCode,

        @DefaultValue("12") int minimumPasswordLength
) {

    public boolean credentialsProvided() {
        return hasText(adminEmail) || hasText(adminPassword);
    }

    public boolean credentialsComplete() {
        return hasText(adminEmail) && hasText(adminPassword);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
