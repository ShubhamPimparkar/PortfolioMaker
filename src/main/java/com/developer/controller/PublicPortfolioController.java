package com.developer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.dto.response.PublicPortfolioResponse;
import com.developer.service.PublicPortfolioService;

@RestController
@RequestMapping("/api/public")
public class PublicPortfolioController {

    private final PublicPortfolioService publicPortfolioService;

    public PublicPortfolioController(PublicPortfolioService publicPortfolioService) {
        this.publicPortfolioService = publicPortfolioService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicPortfolioResponse> getPublicPortfolio(@PathVariable String username) {
        PublicPortfolioResponse response = publicPortfolioService.getPublicPortfolioByUsername(username);
        return ResponseEntity.ok(response);
    }
}

