package com.enoca.requestmanagement.bootstrap;

import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.repository.DepartmentRepository;
import com.enoca.requestmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ilk yonetici olusturma")
class AdminBootstrapperTest {

    private static final String EMAIL = "admin@sirket.com";
    private static final String PASSWORD = "CokGizliParola1";

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminBootstrapper bootstrapperWith(String email, String password) {
        BootstrapProperties properties = new BootstrapProperties(
                email, password, "Sistem", "Yoneticisi", "Yonetim", "MGMT", 12);
        return new AdminBootstrapper(properties, userRepository, departmentRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Bos veritabaninda departman ve ADMIN kullanici olusturulur")
    void createsDepartmentAndAdminWhenDatabaseIsEmpty() {
        when(userRepository.count()).thenReturn(0L);
        when(departmentRepository.findByCode("MGMT")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(call -> call.getArgument(0));
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        bootstrapperWith(EMAIL, PASSWORD).run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertThat(saved.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getValue().isActive()).isTrue();
        assertThat(saved.getValue().getDepartment().getCode()).isEqualTo("MGMT");
    }

    @Test
    @DisplayName("Parola veritabanina duz metin olarak yazilmaz")
    void passwordIsStoredHashed() {
        when(userRepository.count()).thenReturn(0L);
        when(departmentRepository.findByCode("MGMT")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(call -> call.getArgument(0));
        when(passwordEncoder.encode(PASSWORD)).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        bootstrapperWith(EMAIL, PASSWORD).run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertThat(saved.getValue().getPassword())
                .isEqualTo("$2a$10$hash")
                .isNotEqualTo(PASSWORD);
    }

    @Test
    @DisplayName("Ayni koda sahip departman varsa yeniden olusturulmaz")
    void reusesAnExistingDepartment() {
        Department existing = Department.builder().name("Yonetim").code("MGMT").build();
        when(userRepository.count()).thenReturn(0L);
        when(departmentRepository.findByCode("MGMT")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        bootstrapperWith(EMAIL, PASSWORD).run(null);

        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Kullanici bulunan veritabaninda hicbir sey olusturulmaz")
    void doesNothingWhenUsersAlreadyExist() {
        when(userRepository.count()).thenReturn(12L);

        bootstrapperWith(EMAIL, PASSWORD).run(null);

        verify(userRepository, never()).save(any(User.class));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Yapilandirma yoksa uygulama sessizce acilir")
    void staysOutOfTheWayWhenNotConfigured() {
        when(userRepository.count()).thenReturn(0L);

        bootstrapperWith(null, null).run(null);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Yalnizca e-posta verilmisse uygulama acilmaz")
    void halfConfiguredBootstrapStopsStartup() {
        assertThatThrownBy(() -> bootstrapperWith(EMAIL, null).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("birlikte tanimlanmalidir");
    }

    @Test
    @DisplayName("Kisa parola ile uygulama acilmaz")
    void weakPasswordStopsStartup() {
        assertThatThrownBy(() -> bootstrapperWith(EMAIL, "kisa").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("en az 12 karakter");
    }

    @Test
    @DisplayName("Gecersiz e-posta ile uygulama acilmaz")
    void invalidEmailStopsStartup() {
        assertThatThrownBy(() -> bootstrapperWith("admin", PASSWORD).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("e-posta adresi olmalidir");
    }
}
