package com.app.aquavision.controllers;

import com.app.aquavision.entities.User;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.services.HogarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, originPatterns = "*")
@RestController
@RequestMapping("/hogares")
public class HogarController {

    @Autowired
    private HogarService service;

    @GetMapping
    public List<Hogar> list() {
        return service.findAll();
    }

    //@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}/consumo-actual")
    public ResponseEntity<?> getConsumoActual(@PathVariable Long id) {

        int consumoActual = 0; // Aquí deberías obtener el consumo actual del hogar con ID 'id'

        return ResponseEntity.ok(consumoActual);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid Hogar hogar, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(hogar));
    }



}
