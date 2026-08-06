package com.enoca.requestmanagement.rule;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApproverResolver {

    private final UserRepository userRepository;

    public User resolve(Role approverRole, Request request) {
        return candidatesFor(approverRole, request).stream()
                .filter(candidate -> !candidate.getId().equals(request.getRequester().getId()))
                .min(Comparator.comparing(User::getId))
                .orElseThrow(() -> new BusinessRuleException(
                        "Bu adım için uygun bir onaycı bulunamadı: " + approverRole));
    }

    private List<User> candidatesFor(Role approverRole, Request request) {
        if (approverRole != Role.MANAGER) {
            return userRepository.findByRoleAndActiveTrue(approverRole);
        }

        List<User> departmentManagers = userRepository.findByRoleAndDepartmentAndActiveTrue(
                approverRole, request.getRequester().getDepartment());

        if (hasEligibleApprover(departmentManagers, request)) {
            return departmentManagers;
        }

        return userRepository.findByRoleAndActiveTrue(Role.DIRECTOR);
    }

    private boolean hasEligibleApprover(List<User> candidates, Request request) {
        return candidates.stream()
                .anyMatch(candidate -> !candidate.getId().equals(request.getRequester().getId()));
    }
}
