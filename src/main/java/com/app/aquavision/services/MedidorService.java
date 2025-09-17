package com.app.aquavision.services;

import com.app.aquavision.entities.domain.Medidor;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.repositories.MedidorRepository;
import com.app.aquavision.repositories.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedidorService {

    @Autowired
    private MedidorRepository repository;

    @Autowired
    private SectorRepository sectorRepository;

    @Transactional()
    public List<Medidor> findAll() {
        return (List<Medidor>) repository.findAll();
    }

    @Transactional
    public Medidor findById(Long id) {
        Optional<Medidor> optionalMedidor = repository.findById(id);
        return optionalMedidor.orElse(null);
    }

    @Transactional
    public Sector findSectorById(Long id) {
        Optional<Sector> optionalSector = sectorRepository.findById(id);
        return optionalSector.orElse(null);
    }

    @Transactional
    public Medidor save(Medidor medidor) {
        return repository.save(medidor);
    }

    public Sector saveSector(Sector sector) {
        return sectorRepository.save(sector);
    }

}
