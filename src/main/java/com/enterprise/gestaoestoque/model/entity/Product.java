package com.enterprise.gestaoestoque.model.entity;

import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import com.enterprise.gestaoestoque.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MeasurementUnit measurementUnit;

    @Column(nullable = false)
    private Long minQuantity;

    @Column(nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Lot> lots = new ArrayList<>();

}
