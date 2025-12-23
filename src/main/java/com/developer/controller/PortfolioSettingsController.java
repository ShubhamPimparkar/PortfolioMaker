package com.developer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.dto.request.PortfolioSettingsRequest;
import com.developer.dto.response.PortfolioSettingsResponse;
import com.developer.service.PortfolioSettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portfolio/settings")
@Validated
public class PortfolioSettingsController {

    private final PortfolioSettingsService portfolioSettingsService;

    public PortfolioSettingsController(PortfolioSettingsService portfolioSettingsService) {
        this.portfolioSettingsService = portfolioSettingsService;
    }

    @GetMapping
    public ResponseEntity<PortfolioSettingsResponse> getSettings() {
        PortfolioSettingsResponse response = portfolioSettingsService.getCurrentUserSettings();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<PortfolioSettingsResponse> updateSettings(
            @Valid @RequestBody PortfolioSettingsRequest request) {
        PortfolioSettingsResponse response = portfolioSettingsService.updateSettings(request);
        return ResponseEntity.ok(response);
    }
}

