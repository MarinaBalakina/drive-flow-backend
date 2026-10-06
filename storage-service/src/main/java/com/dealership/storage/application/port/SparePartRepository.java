package com.dealership.storage.application.port;

import com.dealership.storage.domain.entity.sparePart.SparePart;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SparePartRepository {
    SparePart save(SparePart part);

    Optional<SparePart> findById(UUID id);

    Optional<SparePart> findByComponentOptionId(UUID componentOptionId);

    List<SparePart> findAll();

    void deleteById(UUID partId);
}
