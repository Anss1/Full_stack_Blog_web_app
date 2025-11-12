package com.anas.springblog.authentecation.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String bio;
    private String website;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;
}
