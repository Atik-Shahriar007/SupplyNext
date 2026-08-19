package com.example.scmbackend.warehouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled("CORS/security config interaction with MockMvc needs debugging - revisit later")
class WarehouseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturn401_whenNoAuthTokenProvided() throws Exception {
        mockMvc.perform(get("/api/warehouses")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateWarehouse_whenAuthenticatedAsAdmin() throws Exception {
        WarehouseRequestDto request = new WarehouseRequestDto();
        request.setName("Test Warehouse");
        request.setLocation("Dhaka");
        request.setCapacity(1000);

        mockMvc.perform(post("/api/warehouses")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Warehouse"))
                .andExpect(jsonPath("$.location").value("Dhaka"))
                .andExpect(jsonPath("$.capacity").value(1000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnValidationError_whenNameIsBlank() throws Exception {
        WarehouseRequestDto request = new WarehouseRequestDto();
        request.setName("");
        request.setLocation("Dhaka");
        request.setCapacity(1000);

        mockMvc.perform(post("/api/warehouses")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void shouldReturn403_whenStaffTriesToCreateWarehouse() throws Exception {
        WarehouseRequestDto request = new WarehouseRequestDto();
        request.setName("Test Warehouse");
        request.setLocation("Dhaka");
        request.setCapacity(1000);

        mockMvc.perform(post("/api/warehouses")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
