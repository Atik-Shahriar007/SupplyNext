package com.example.scmbackend.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/eoq")
    public List<EOQResponseDto> getEOQForAllProducts() {
        return analyticsService.calculateEOQForAllProducts();
    }

    @GetMapping("/eoq/{productId}")
    public EOQResponseDto getEOQForProduct(@PathVariable Long productId) {
        return analyticsService.calculateEOQForProduct(productId);
    }
}