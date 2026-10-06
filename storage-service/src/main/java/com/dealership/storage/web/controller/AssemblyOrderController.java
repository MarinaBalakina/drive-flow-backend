package com.dealership.storage.web.controller;

import com.dealership.storage.application.usecase.AssemblyOrderService;
import com.dealership.storage.domain.entity.assembly.AssemblyOrder;
import com.dealership.storage.web.dto.request.CreateAssemblyOrderRequest;
import com.dealership.storage.web.dto.request.UpdateAssemblyOrderRequest;
import com.dealership.storage.web.dto.response.AssemblyOrderResponse;
import com.dealership.storage.web.mapper.DomainResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouse/assembly-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN','ADMIN')")
public class AssemblyOrderController {
    private final AssemblyOrderService assemblyOrderService;
    private  final DomainResponseMapper responseMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssemblyOrderResponse create(@Valid @RequestBody CreateAssemblyOrderRequest orderRequest) {
        AssemblyOrder order = assemblyOrderService.create(
                orderRequest.sourceOrderId(),
                orderRequest.orderType(),
                orderRequest.carId(),
                orderRequest.carModelId(),
                orderRequest.requiredComponentOptionIds(),
                orderRequest.warehouseEmployeeId(),
                orderRequest.traceId());
        return responseMapper.toResponse(order);
    }

    @GetMapping
    public List<AssemblyOrderResponse> listAll() {
        return assemblyOrderService.listAll().stream().map(responseMapper::toResponse).toList();
    }

    @GetMapping("/{orderId}")
    public AssemblyOrderResponse findOrderById(@PathVariable UUID orderId) {
        return responseMapper.toResponse(assemblyOrderService.getOrderById(orderId));
    }

    @PutMapping("/{orderId}")
    public AssemblyOrderResponse update(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateAssemblyOrderRequest request
    ) {
        return responseMapper.toResponse(
                assemblyOrderService.update(orderId, request.warehouseEmployeeId(), request.status())
        );
    }

    @PatchMapping("/{orderId}/assembled")
    public AssemblyOrderResponse markAssembled(@PathVariable UUID orderId) {
        return responseMapper.toResponse(assemblyOrderService.markAssembled(orderId));
    }

    @PatchMapping("/{orderId}/fail")
    public AssemblyOrderResponse fail(@PathVariable UUID orderId) {
        return responseMapper.toResponse(assemblyOrderService.fail(orderId));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID orderId) {
        assemblyOrderService.deleteById(orderId);
    }
}
