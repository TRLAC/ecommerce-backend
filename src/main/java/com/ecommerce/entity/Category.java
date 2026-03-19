package com.ecommerce.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    // parent category
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    // children categories
    @OneToMany(mappedBy = "parent")
    private List<Category> children;
}