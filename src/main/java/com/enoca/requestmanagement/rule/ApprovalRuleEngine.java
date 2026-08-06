package com.enoca.requestmanagement.rule;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;

import java.util.List;

public interface ApprovalRuleEngine {

    RequestType supportedType();

    List<Role> determineApprovalChain(Request request);
}
