package com.app.aquavision.boostrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.app.aquavision.entities.domain.gamification.TagEvento;
import com.app.aquavision.repositories.TagEventoRepository;

@Component
@Order(2)
public class TagEventoInitializer implements CommandLineRunner {

    @Autowired
    private TagEventoRepository tagEventoRepository;

    private static final Logger logger = LoggerFactory.getLogger(TagEventoInitializer.class);

    @Override
    public void run(String... args) {

        logger.info("Iniciando la creación de tags de eventos...");

        createTagIfNotExists("Limpieza", "#27ae60");
        createTagIfNotExists("Pileta", "#2980b9");
        createTagIfNotExists("Riego", "#8e44ad");
        createTagIfNotExists("Jardín", "#16a085");
        createTagIfNotExists("Mantenimiento", "#e67e22");
        createTagIfNotExists("Baño", "#c0392b");
        createTagIfNotExists("Cocina", "#d35400");
        createTagIfNotExists("Lavado", "#3498db");
        createTagIfNotExists("Autos", "#2ecc71");
        createTagIfNotExists("Terraza", "#9b59b6");
        createTagIfNotExists("Huerta", "#27ae60");
        createTagIfNotExists("Eventos Sociales", "#e84393");
        createTagIfNotExists("Control de Cloro", "#1abc9c");
        createTagIfNotExists("Bomba de Agua", "#34495e");
    }

    private void createTagIfNotExists(String nombre, String color) {
        if (!tagEventoRepository.findByNombre(nombre).isPresent()) {
            TagEvento tag = new TagEvento(nombre, color);
            tagEventoRepository.save(tag);
            logger.info("Tag creado: {}", nombre);
        }
    }
}