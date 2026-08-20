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

    @GetMapping("/abc")
    public List<ABCAnalysisResponseDto> getABCAnalysis() {
        return analyticsService.calculateABCAnalysis();
    }
    @GetMapping("/safety-stock")
    public List<SafetyStockResponseDto> getSafetyStock(
            @RequestParam(defaultValue = "0.95") double serviceLevel) {
        return analyticsService.calculateSafetyStock(serviceLevel);
    }

    @GetMapping("/safety-stock/{productId}")
    public SafetyStockResponseDto getSafetyStockForProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0.95") double serviceLevel) {
        return analyticsService.calculateSafetyStockForProduct(productId, serviceLevel);
    }
    @GetMapping("/reorder-point")
    public List<ReorderPointResponseDto> getReorderPoints(
            @RequestParam(defaultValue = "0.95") double serviceLevel) {
        return analyticsService.calculateReorderPoints(serviceLevel);
    }

    @GetMapping("/reorder-point/{productId}")
    public ReorderPointResponseDto getReorderPointForProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0.95") double serviceLevel) {
        return analyticsService.calculateReorderPointForProduct(productId, serviceLevel);
    }
    @GetMapping("/dead-stock")
    public List<DeadStockResponseDto> getDeadStock(
            @RequestParam(defaultValue = "90") int thresholdDays) {
        return analyticsService.detectDeadStock(thresholdDays);
    }
}

