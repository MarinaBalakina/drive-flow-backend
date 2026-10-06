package com.dealership.storage.web.controller;

import com.dealership.storage.application.usecase.ConfiguratorService;
import com.dealership.storage.domain.entity.configurator.SelectedConfiguration;
import com.dealership.storage.web.dto.request.BuildConfigurationRequest;
import com.dealership.storage.web.dto.request.CalculatePriceRequest;
import com.dealership.storage.web.dto.response.SelectedConfigurationResponse;
import com.dealership.storage.web.dto.response.TotalPriceResponse;
import com.dealership.storage.web.mapper.DomainResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configurator")
@RequiredArgsConstructor
@Validated
public class ConfiguratorController {
    private final ConfiguratorService configuratorService;
    private final DomainResponseMapper responseMapper;

    @PostMapping("/build")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public SelectedConfigurationResponse buildConfiguration(@Valid @RequestBody BuildConfigurationRequest request) {
        SelectedConfiguration selected = configuratorService.buildConfiguration(
                request.modelKey(),
                request.selectedOptionsId()
        );

        return responseMapper.toResponse(selected);
    }

    @PostMapping("/price")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public TotalPriceResponse calculateTotalPrice(@Valid @RequestBody CalculatePriceRequest request) {
        SelectedConfiguration selected = configuratorService.buildConfiguration(
                request.modelKey(),
                request.selectedOptionsId()
        );

        return new TotalPriceResponse(configuratorService.calculateTotalPrice(request.modelKey(), selected));
    }
}

