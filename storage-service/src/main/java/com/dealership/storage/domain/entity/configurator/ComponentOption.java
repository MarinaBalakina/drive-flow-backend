package com.dealership.storage.domain.entity.configurator;

import com.dealership.storage.domain.exception.DomainValidationException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ComponentOption {
    private final @NonNull UUID id;
    private final @NonNull ComponentType type;
    private final @NonNull String name;
    private final @NonNull BigDecimal priceChange;
    private final @NonNull Set<String> compatibleModels;
    
     public boolean isCompatibleWith(String model){
         if (model == null || model.trim().isEmpty())
             throw new DomainValidationException("Car model must be not empty");

         return compatibleModels.contains(model.trim());
     }
}

