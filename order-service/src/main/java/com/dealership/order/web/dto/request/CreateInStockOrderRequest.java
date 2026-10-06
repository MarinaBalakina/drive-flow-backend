package com.dealership.order.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInStockOrderRequest (
    @NotNull UUID carId
){}
