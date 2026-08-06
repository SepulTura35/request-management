package com.enoca.requestmanagement.rule;

import com.enoca.requestmanagement.TestFixtures;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApproverResolverTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApproverResolver resolver;

    private Department it;
    private Department management;
    private User employee;
    private User manager;
    private User director;

    @BeforeEach
    void setUp() {
        it = TestFixtures.department(1L, "IT");
        management = TestFixtures.department(2L, "MGMT");
        employee = TestFixtures.user(10L, Role.EMPLOYEE, it);
        manager = TestFixtures.user(11L, Role.MANAGER, it);
        director = TestFixtures.user(12L, Role.DIRECTOR, management);
    }

    private Request requestBy(User requester) {
        return TestFixtures.request(1L, RequestType.LEAVE, requester, TestFixtures.leaveDetail(2));
    }

    @Test
    void managerRoleIsScopedToRequesterDepartment() {
        when(userRepository.findByRoleAndDepartmentAndActiveTrue(Role.MANAGER, it))
                .thenReturn(List.of(manager));

        assertThat(resolver.resolve(Role.MANAGER, requestBy(employee))).isEqualTo(manager);
    }

    @Test
    void otherRolesAreResolvedCompanyWide() {
        User financeUser = TestFixtures.user(13L, Role.FINANCE, management);
        when(userRepository.findByRoleAndActiveTrue(Role.FINANCE)).thenReturn(List.of(financeUser));

        assertThat(resolver.resolve(Role.FINANCE, requestBy(employee))).isEqualTo(financeUser);
    }

    @Test
    void requesterNeverApprovesOwnRequest() {
        User secondManager = TestFixtures.user(14L, Role.MANAGER, it);
        when(userRepository.findByRoleAndDepartmentAndActiveTrue(Role.MANAGER, it))
                .thenReturn(List.of(manager, secondManager));

        assertThat(resolver.resolve(Role.MANAGER, requestBy(manager)))
                .as("kendi talebini onaylayamaz, diger yoneticiye gider")
                .isEqualTo(secondManager);
    }

    @Test
    void soleManagerOfDepartmentEscalatesToDirector() {
        when(userRepository.findByRoleAndDepartmentAndActiveTrue(Role.MANAGER, it))
                .thenReturn(List.of(manager));
        when(userRepository.findByRoleAndActiveTrue(Role.DIRECTOR)).thenReturn(List.of(director));

        assertThat(resolver.resolve(Role.MANAGER, requestBy(manager)))
                .as("departmandaki tek yonetici kendisi, direktore yukselir")
                .isEqualTo(director);
    }

    @Test
    void picksLowestIdWhenSeveralCandidatesExist() {
        User laterManager = TestFixtures.user(30L, Role.MANAGER, it);
        User earlierManager = TestFixtures.user(20L, Role.MANAGER, it);
        when(userRepository.findByRoleAndDepartmentAndActiveTrue(Role.MANAGER, it))
                .thenReturn(List.of(laterManager, earlierManager));

        assertThat(resolver.resolve(Role.MANAGER, requestBy(employee))).isEqualTo(earlierManager);
    }

    @Test
    void failsClearlyWhenNoApproverExists() {
        when(userRepository.findByRoleAndActiveTrue(any())).thenReturn(List.of());

        assertThatThrownBy(() -> resolver.resolve(Role.HR, requestBy(employee)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("HR");
    }
}
