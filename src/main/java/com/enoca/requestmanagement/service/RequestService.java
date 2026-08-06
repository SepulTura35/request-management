package com.enoca.requestmanagement.service;

import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import org.springframework.data.domain.Pageable;

public interface RequestService {

    RequestResponse create(CreateRequestDto dto, User requester);

    RequestResponse update(Long id, CreateRequestDto dto, User requester);

    RequestResponse submit(Long id, User requester);

    RequestResponse findById(Long id, User viewer);

    PageResponse<RequestResponse> findMyRequests(User requester, RequestStatus status,
                                                 RequestType requestType, Pageable pageable);

    RequestResponse cancel(Long id, User requester);

    void delete(Long id, User requester);
}
