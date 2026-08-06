package com.enoca.requestmanagement.repository;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

    Optional<Request> findByRequestNumber(String requestNumber);

    Page<Request> findByRequester(User requester, Pageable pageable);

    Page<Request> findByRequesterAndStatus(User requester, RequestStatus status, Pageable pageable);

    Page<Request> findByRequesterAndRequestType(User requester, RequestType requestType, Pageable pageable);

    Page<Request> findByRequesterAndStatusAndRequestType(User requester, RequestStatus status,
                                                         RequestType requestType, Pageable pageable);

    @Query("""
            SELECT r FROM Request r
            JOIN FETCH r.requester requester
            JOIN FETCH requester.department
            WHERE r.id = :id
            """)
    Optional<Request> findByIdWithRequester(@Param("id") Long id);

    @Query(value = "SELECT nextval('request_number_seq')", nativeQuery = true)
    long nextRequestNumberSequenceValue();
}
