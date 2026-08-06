package com.enoca.requestmanagement.entity.detail;

import com.enoca.requestmanagement.entity.BaseEntity;
import com.enoca.requestmanagement.entity.Request;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "request_details")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "detail_type")
@Getter
@Setter
@NoArgsConstructor
public abstract class RequestDetail extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private Request request;
}
