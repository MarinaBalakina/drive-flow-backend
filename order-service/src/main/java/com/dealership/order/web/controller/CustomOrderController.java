package com.dealership.order.web.controller;

import com.dealership.order.application.usecase.CustomOrderService;
import com.dealership.order.domain.entity.order.CustomOrder;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import com.dealership.order.web.dto.request.CreateCustomOrderRequest;
import com.dealership.order.web.dto.response.CustomOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/custom")
@RequiredArgsConstructor
@Validated
public class CustomOrderController {
    private final CustomOrderService customOrderService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CustomOrderResponse create(@Valid @RequestBody CreateCustomOrderRequest request) {
        UUID clientId = currentUserProvider.getCurrentUserId();
        return toResponse(
                customOrderService.create(
                        clientId,
                        request.carModelId(),
                        request.modelKey(),
                        request.selectedOptionsId()
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<CustomOrderResponse> listAll() {
        return customOrderService.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CustomOrderResponse getById(@PathVariable UUID orderId) {
        return toResponse(customOrderService.getById(orderId));
    }

    @PatchMapping("/{orderId}/assign-manager/{managerId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CustomOrderResponse assignManager(
            @PathVariable UUID orderId,
            @PathVariable UUID managerId
    ) {
        return toResponse(customOrderService.assignManager(orderId, managerId));
    }

    @PatchMapping("/{orderId}/mark-waiting-for-payment")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CustomOrderResponse markWaitingForPayment(@PathVariable UUID orderId) {
        return toResponse(customOrderService.markWaitingForPayment(orderId));
    }

    @PatchMapping("/{orderId}/pay")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public CustomOrderResponse pay(@PathVariable UUID orderId) {
        return toResponse(customOrderService.pay(orderId));
    }

    @PatchMapping("/{orderId}/complete")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public CustomOrderResponse complete(@PathVariable UUID orderId) {
        return toResponse(customOrderService.complete(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public CustomOrderResponse cancel(@PathVariable UUID orderId) {
        return toResponse(customOrderService.cancel(orderId));
    }

    private CustomOrderResponse toResponse(CustomOrder order) {
        return new CustomOrderResponse(
                order.getId(),
                order.getClientId(),
                order.getManagerId(),
                order.getCarModelId(),
                order.getFullModel(),
                order.getSelectedOptionsId(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getOrderStatus()
        );
    }
}
