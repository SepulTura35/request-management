package com.enoca.requestmanagement.bootstrap;

import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.repository.DepartmentRepository;
import com.enoca.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapper implements ApplicationRunner {

    private final BootstrapProperties properties;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateConfiguration();

        if (userRepository.count() > 0) {
            if (properties.credentialsComplete()) {
                log.info("Bootstrap atlandi: veritabaninda zaten kullanici var. "
                        + "APP_BOOTSTRAP_ADMIN_* degiskenleri kaldirilabilir.");
            }
            return;
        }

        if (!properties.credentialsComplete()) {
            warnAboutEmptyDatabase();
            return;
        }

        Department department = departmentRepository.findByCode(properties.departmentCode())
                .orElseGet(this::createDepartment);

        User admin = userRepository.save(User.builder()
                .firstName(properties.adminFirstName())
                .lastName(properties.adminLastName())
                .email(properties.adminEmail())
                .password(passwordEncoder.encode(properties.adminPassword()))
                .role(Role.ADMIN)
                .department(department)
                .active(true)
                .build());

        log.info("Ilk yonetici olusturuldu: {} (departman: {}). "
                        + "Parolayi giristen sonra degistirin ve APP_BOOTSTRAP_ADMIN_PASSWORD degiskenini kaldirin.",
                admin.getEmail(), department.getCode());
    }

    private Department createDepartment() {
        return departmentRepository.save(Department.builder()
                .name(properties.departmentName())
                .code(properties.departmentCode())
                .build());
    }

    private void validateConfiguration() {
        if (!properties.credentialsProvided()) {
            return;
        }

        if (!properties.credentialsComplete()) {
            throw new IllegalStateException(
                    "Bootstrap yapilandirmasi eksik: APP_BOOTSTRAP_ADMIN_EMAIL ve "
                            + "APP_BOOTSTRAP_ADMIN_PASSWORD birlikte tanimlanmalidir.");
        }

        if (!properties.adminEmail().contains("@")) {
            throw new IllegalStateException(
                    "Bootstrap yapilandirmasi gecersiz: APP_BOOTSTRAP_ADMIN_EMAIL bir e-posta adresi olmalidir.");
        }

        if (properties.adminPassword().length() < properties.minimumPasswordLength()) {
            throw new IllegalStateException(
                    "Bootstrap yapilandirmasi gecersiz: APP_BOOTSTRAP_ADMIN_PASSWORD en az "
                            + properties.minimumPasswordLength() + " karakter olmalidir.");
        }
    }

    private void warnAboutEmptyDatabase() {
        log.warn("""

                ------------------------------------------------------------------
                Veritabani bos: hicbir kullanici ve departman yok.

                Kayit ucu mevcut bir departman istedigi, yonetim uclari da ADMIN
                rolu istedigi icin su anda sisteme giris yapabilecek kimse yok.

                Ilk yoneticiyi olusturmak icin uygulamayi bir kez su degiskenlerle
                baslatin:

                  APP_BOOTSTRAP_ADMIN_EMAIL=admin@sirket.com
                  APP_BOOTSTRAP_ADMIN_PASSWORD=<en az 12 karakter>

                Hesap olustuktan sonra bu degiskenler kaldirilabilir; kullanici
                bulunan bir veritabaninda bootstrap calismaz.
                ------------------------------------------------------------------""");
    }
}
