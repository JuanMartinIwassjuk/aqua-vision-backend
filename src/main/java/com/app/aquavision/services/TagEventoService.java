package com.app.aquavision.services;

import com.app.aquavision.entities.domain.gamification.TagEvento;
import com.app.aquavision.repositories.TagEventoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagEventoService {

    @Autowired
    private TagEventoRepository repository;

    @Transactional
    public List<TagEvento> findAll() {
        return repository.findAll();
    }

    @Transactional
    public TagEvento save(TagEvento tag) {
        return repository.save(tag);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}