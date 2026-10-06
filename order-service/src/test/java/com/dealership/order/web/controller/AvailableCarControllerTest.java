package com.dealership.order.web.controller;

import com.dealership.order.application.readmodel.AvailableCar;
import com.dealership.order.application.readmodel.AvailableCarDetails;
import com.dealership.order.application.usecase.AvailableCarCatalogService;
import com.dealership.order.domain.exception.StorageServiceUnavailableException;
import com.dealership.order.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CarController.class)
@Import({GlobalExceptionHandler.class, AvailableCarControllerTest.TestSecurityConfig.class})
class AvailableCarControllerTest {
    private static final UUID CAR_ID = UUID.fromString("66666666-6666-6666-6666-666666666661");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailableCarCatalogService service;

    @Test
    @WithMockUser(roles = "USER")
    void userCanGetAvailableCars() throws Exception {
        when(service.getAvailableCars()).thenReturn(List.of(car(CAR_ID)));

        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(CAR_ID.toString()))
                .andExpect(jsonPath("$[0].brand").value("PORSCHE"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanGetAvailableCarById() throws Exception {
        when(service.getAvailableCarById(CAR_ID)).thenReturn(car(CAR_ID));

        mockMvc.perform(get("/api/v1/cars/{id}", CAR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CAR_ID.toString()))
                .andExpect(jsonPath("$.details.bodyType").value("COUPE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void storageUnavailableMappedTo503() throws Exception {
        when(service.getAvailableCars())
                .thenThrow(new StorageServiceUnavailableException("StorageService is unavailable"));

        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("StorageService is unavailable"))
                .andExpect(jsonPath("$.path").value("/api/v1/cars"));
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE_ADMIN")
    void warehouseAdminCannotViewOrderServiceCarsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotViewCarsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isUnauthorized());
    }

    private AvailableCar car(UUID id) {
        return new AvailableCar(
                id,
                new BigDecimal("18990000.00"),
                "PORSCHE",
                "911 Carrera",
                "AVAILABLE",
                true,
                new AvailableCarDetails("COUPE", "GASOLINE", 480, 3.0, "AUTOMATIC", "AWD", "SILVER")
        );
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }
}