package com.app.aquavision.services;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.repositories.HogarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HogarService {

    @Autowired
    private HogarRepository repository;

    @Transactional()
    public List<Hogar> findAll() {
        return (List<Hogar>) repository.findAll();
    }

    @Transactional
    public Hogar findById(Long id) {
        Optional<Hogar> optionalHogar = repository.findById(id);
        return optionalHogar.orElse(null);
    }

    @Transactional
    public Hogar save(Hogar hogar) {

        return repository.save(hogar);
    }

}
