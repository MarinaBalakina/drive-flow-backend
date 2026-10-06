package com.dealership.storage.web.dto.response;

import com.dealership.storage.domain.entity.configurator.ComponentType;

import java.util.Map;

public record SelectedConfigurationResponse(
        Map<ComponentType, ComponentOptionResponse> selected
) {}
