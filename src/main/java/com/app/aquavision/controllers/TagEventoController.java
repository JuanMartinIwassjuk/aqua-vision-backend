package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.TagEvento;
import com.app.aquavision.services.TagEventoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Tags de Eventos",
        description = "Operaciones para gestionar etiquetas de eventos"
)
@RestController
@RequestMapping("/tags")
public class TagEventoController {

    private static final Logger logger = LoggerFactory.getLogger(TagEventoController.class);

    @Autowired
    private TagEventoService service;

    @GetMapping
    public List<TagEvento> list() {
        logger.info("list - all tags");
        return service.findAll();
    }

    @PostMapping
    public TagEvento create(@RequestBody TagEvento tag) {
        logger.info("create - tag: {}", tag);
        return service.save(tag);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        logger.info("delete - tag id: {}", id);
        service.deleteById(id);
    }
}
