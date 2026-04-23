package com.mobileproject.mobileprojectbackend.place;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.auth.token-secret=test-token-secret-for-spring-tests")
@AutoConfigureMockMvc
class PlaceFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void invalidDistanceSortWithoutCoordinatesShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/places")
                .param("sort", "distance"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidRadiusWithoutCoordinatesShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/places")
                .param("radiusKm", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidTypeShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/places")
                .param("type", "invalidType"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void baseSearchShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/places")
                .param("size", "1"))
                .andExpect(status().isOk());
    }
}
