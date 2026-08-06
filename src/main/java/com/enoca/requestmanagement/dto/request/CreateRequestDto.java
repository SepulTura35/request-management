package com.enoca.requestmanagement.dto.request;

import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "requestType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateLeaveRequestDto.class, name = "LEAVE"),
        @JsonSubTypes.Type(value = CreateExpenseRequestDto.class, name = "EXPENSE"),
        @JsonSubTypes.Type(value = CreateEquipmentRequestDto.class, name = "EQUIPMENT"),
        @JsonSubTypes.Type(value = CreateRemoteWorkRequestDto.class, name = "REMOTE_WORK"),
        @JsonSubTypes.Type(value = CreateAccessPermissionRequestDto.class, name = "ACCESS_PERMISSION")
})
public sealed interface CreateRequestDto permits
        CreateLeaveRequestDto,
        CreateExpenseRequestDto,
        CreateEquipmentRequestDto,
        CreateRemoteWorkRequestDto,
        CreateAccessPermissionRequestDto {

    RequestType requestType();

    String description();

    Priority priority();
}
