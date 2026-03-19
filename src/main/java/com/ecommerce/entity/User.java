package com.ecommerce.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="users")
@EntityListeners(AuditingEntityListener.class) 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
	  	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "user_id")
	    private Long id;

	    @Column(name = "full_name")
	    private String fullName;

	    @Column(name = "email", nullable = false, unique = true)
	    private String email;

	    @Column(name = "password_hash", nullable = false)
	    private String passwordHash;

	    private String phone;

	    @CreatedDate
	    @Column(name = "created_at", nullable = false, updatable = false)
	    private LocalDateTime createdAt;

	    @LastModifiedDate
	    @Column(name = "updated_at", nullable = false)
	    private LocalDateTime updatedAt;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id")
		)
	
	private Set<Role> roles = new HashSet<>();
	
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	private Cart cart;
}

