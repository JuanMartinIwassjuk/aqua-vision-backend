package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.services.HogarService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, originPatterns = "*")
@RestController
@RequestMapping("/hogares")
public class HogarController {

    private static final Logger logger = LoggerFactory.getLogger(HogarController.class);

    @Autowired
    private HogarService service;

    @GetMapping
    public List<Hogar> list() {

        logger.info("list - all hogares");

        return service.findAll();
    }

    @GetMapping("/{id}")
    public Hogar list(@PathVariable Long id) {

        logger.info("list hogar - id: {}", id);

        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid Hogar hogar) {

        logger.info("create - hogar: {}", hogar);

        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(hogar));
    }
}
