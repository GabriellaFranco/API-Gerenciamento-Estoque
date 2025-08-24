package com.enterprise.gestaoestoque.model.entity;

import com.enterprise.gestaoestoque.enums.LotStatus;
import com.enterprise.gestaoestoque.enums.MeasurementUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "lots")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long initialQtd;

    @Column(nullable = false)
    private Long currentQtd;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MeasurementUnit measurementUnit;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false, unique = true)
    private String lotCode;

    @Column(nullable = false)
    private LotStatus status;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
