package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.TestFixtures;
import com.enoca.requestmanagement.dto.request.UpdateUserRoleRequest;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.repository.UserRepository;
import com.enoca.requestmanagement.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kullanıcı yönetimi kuralları")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Department department;
    private User admin;

    @BeforeEach
    void setUp() {
        department = TestFixtures.department(1L, "MGMT");
        admin = TestFixtures.user(1L, Role.ADMIN, department);
    }

    @Test
    @DisplayName("Son aktif yönetici pasifleştirilemez")
    void lastActiveAdminCannotBeDeactivated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.setActive(1L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("son aktif yönetici");

        assertThat(admin.isActive()).as("koruma tetiklenince durum değişmemeli").isTrue();
    }

    @Test
    @DisplayName("Başka yönetici varsa bir yönetici pasifleştirilebilir")
    void adminCanBeDeactivatedWhenAnotherExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(2L);

        userService.setActive(1L, false);

        assertThat(admin.isActive()).isFalse();
    }

    @Test
    @DisplayName("Son aktif yöneticinin rolü düşürülemez")
    void lastActiveAdminRoleCannotBeDowngraded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.updateRole(1L, new UpdateUserRoleRequest(Role.EMPLOYEE)))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(admin.getRole()).as("koruma tetiklenince rol değişmemeli").isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Yönetici olmayan bir kullanıcı serbestçe pasifleştirilir")
    void nonAdminIsNotProtected() {
        User employee = TestFixtures.user(2L, Role.EMPLOYEE, department);
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));

        userService.setActive(2L, false);

        assertThat(employee.isActive()).isFalse();
    }

    @Test
    @DisplayName("Bir yöneticiye ADMIN rolü vermek korumayı tetiklemez")
    void promotingToAdminNeedsNoCheck() {
        User employee = TestFixtures.user(2L, Role.EMPLOYEE, department);
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));

        userService.updateRole(2L, new UpdateUserRoleRequest(Role.ADMIN));

        assertThat(employee.getRole()).isEqualTo(Role.ADMIN);
    }
}
