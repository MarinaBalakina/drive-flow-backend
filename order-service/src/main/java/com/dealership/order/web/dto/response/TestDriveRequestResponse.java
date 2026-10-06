package com.dealership.order.web.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestDriveRequestResponse (
        UUID id,
        UUID clientId,
        UUID carId,
        LocalDateTime startAt
){}
