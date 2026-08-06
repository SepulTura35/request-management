package com.enoca.requestmanagement.security;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class RequestAccessPolicy {

    public boolean canView(Request request, User viewer) {
        return isOwner(request, viewer)
                || viewer.getRole() == Role.ADMIN
                || isAssignedApprover(request, viewer);
    }

    public void ensureCanView(Request request, User viewer) {
        if (!canView(request, viewer)) {
            throw new AccessDeniedException("Bu talebi goruntuleme yetkiniz yok");
        }
    }

    public boolean isOwner(Request request, User user) {
        return request.getRequester().getId().equals(user.getId());
    }

    private boolean isAssignedApprover(Request request, User user) {
        return request.getApprovalSteps().stream()
                .anyMatch(step -> step.getApprover() != null
                        && step.getApprover().getId().equals(user.getId()));
    }
}
