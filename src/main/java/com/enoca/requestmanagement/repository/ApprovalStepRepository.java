package com.enoca.requestmanagement.repository;

import com.enoca.requestmanagement.entity.ApprovalStep;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByRequestOrderByStepOrderAsc(Request request);

    Optional<ApprovalStep> findByRequestAndStepOrder(Request request, Integer stepOrder);

    @Query("""
            SELECT s FROM ApprovalStep s
            JOIN FETCH s.request r
            JOIN FETCH r.requester requester
            JOIN FETCH requester.department
            WHERE s.id = :id
            """)
    Optional<ApprovalStep> findByIdWithRequest(@Param("id") Long id);

    @Query(value = """
            SELECT s FROM ApprovalStep s
            JOIN FETCH s.request r
            JOIN FETCH r.requester requester
            WHERE s.approver = :approver AND s.status = :status
            """,
            countQuery = """
            SELECT COUNT(s) FROM ApprovalStep s
            WHERE s.approver = :approver AND s.status = :status
            """)
    Page<ApprovalStep> findByApproverAndStatus(@Param("approver") User approver,
                                               @Param("status") ApprovalStepStatus status,
                                               Pageable pageable);

    @Query(value = """
            SELECT s FROM ApprovalStep s
            JOIN FETCH s.request r
            JOIN FETCH r.requester requester
            WHERE s.approver = :approver AND s.status <> com.enoca.requestmanagement.enums.ApprovalStepStatus.PENDING
            """,
            countQuery = """
            SELECT COUNT(s) FROM ApprovalStep s
            WHERE s.approver = :approver AND s.status <> com.enoca.requestmanagement.enums.ApprovalStepStatus.PENDING
            """)
    Page<ApprovalStep> findResolvedByApprover(@Param("approver") User approver, Pageable pageable);
}
