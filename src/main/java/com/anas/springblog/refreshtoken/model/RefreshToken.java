package com.anas.springblog.refreshtoken.model;

import com.anas.springblog.authentecation.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String token;
    @Column(nullable = false)
    private Instant expiryDate;
    @Column(nullable = false)
    private boolean revoked;
    @Column(nullable = false)
    private boolean used;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
