package com.dealership.storage.infrastructure.db.entity;


import com.dealership.storage.domain.entity.car.CarStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Car")
@Getter
@Setter
public class CarJpaEntity extends BaseJpaEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CarModelId", nullable = false)
    private CarModelJpaEntity carModel;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CarDetailsId", nullable = false, updatable = true)
    private CarDetailsJpaEntity carDetails;

    @Column(name = "Price", nullable = false)
    private BigDecimal price;

    @Column(name = "Brand", nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private CarStatus status;

    @Column(name = "TestDriveEnabled", nullable = false)
    private boolean testDriveEnabled;
}
