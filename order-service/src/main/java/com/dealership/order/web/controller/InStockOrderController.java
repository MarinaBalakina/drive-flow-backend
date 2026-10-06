package com.dealership.order.web.controller;

import com.dealership.order.application.usecase.InStockOrderService;
import com.dealership.order.domain.entity.order.InStockOrder;
import com.dealership.order.infrastructure.security.CurrentUserProvider;
import com.dealership.order.web.dto.request.CreateInStockOrderRequest;
import com.dealership.order.web.dto.response.InStockOrderResponse;
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
@RequestMapping("/api/orders/in-stock")
@RequiredArgsConstructor
@Validated
public class InStockOrderController {
    private final InStockOrderService inStockOrderService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public InStockOrderResponse create(@Valid @RequestBody CreateInStockOrderRequest request) {
        UUID clientId = currentUserProvider.getCurrentUserId();
        return toResponse(inStockOrderService.create(clientId, request.carId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public List<InStockOrderResponse> listAll() {
        return inStockOrderService.listAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public InStockOrderResponse getById(@PathVariable UUID orderId) {
        return toResponse(inStockOrderService.getById(orderId));
    }

    @PatchMapping("/{orderId}/approved-by-manager")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrderResponse approvedByManager(@PathVariable UUID orderId) {
        return toResponse(inStockOrderService.approvedByManager(orderId));
    }

    @PatchMapping("/{orderId}/assign-manager/{managerId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrderResponse assignManager(
            @PathVariable UUID orderId,
            @PathVariable UUID managerId
    ) {
        return toResponse(inStockOrderService.assignManager(orderId, managerId));
    }

    @PatchMapping("/{orderId}/mark-waiting-for-payment")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrderResponse markWaitingForPayment(@PathVariable UUID orderId) {
        return toResponse(inStockOrderService.markWaitingForPayment(orderId));
    }

    @PatchMapping("/{orderId}/pay")
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public InStockOrderResponse pay(@PathVariable UUID orderId) {
        return toResponse(inStockOrderService.pay(orderId));
    }

    @PatchMapping("/{orderId}/complete")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public InStockOrderResponse complete(@PathVariable UUID orderId) {
        return toResponse(inStockOrderService.complete(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public InStockOrderResponse cancel(@PathVariable UUID orderId) {
        return toResponse(inStockOrderService.cancel(orderId));
    }

    private InStockOrderResponse toResponse(InStockOrder order) {
        return new InStockOrderResponse(
                order.getId(),
                order.getClientId(),
                order.getManagerId(),
                order.getCarId(),
                order.getOrderStatus(),
                order.getCreatedAt()
        );
    }
}
