package com.ecommerce.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private BigDecimal amount;

    private String reason;

    private String status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

}