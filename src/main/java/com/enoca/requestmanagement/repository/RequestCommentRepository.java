package com.enoca.requestmanagement.repository;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {

    @Query("""
            SELECT c FROM RequestComment c
            JOIN FETCH c.user
            WHERE c.request = :request
            ORDER BY c.createdAt ASC
            """)
    List<RequestComment> findByRequestWithUser(@Param("request") Request request);
}
