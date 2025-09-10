package com.app.aquavision.services;

import com.app.aquavision.entities.domain.AquaEvento;
import com.app.aquavision.entities.domain.Estado;
import com.app.aquavision.repositories.AquaEventoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AquaEventoService {

    @Autowired
    private AquaEventoRepository repository;

    @Transactional
    public List<AquaEvento> findAll() {
        return repository.findAll();
    }

    @Transactional
    public AquaEvento findById(Long id) {
        Optional<AquaEvento> optionalEvento = repository.findById(id);
        return optionalEvento.orElse(null);
    }

    @Transactional
    public AquaEvento save(AquaEvento evento) {

        if (evento.getEstado() == Estado.EN_PROCESO && evento.getFechaInicio() == null) {
            evento.setFechaInicio(LocalDateTime.now());
        }
        return repository.save(evento);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public AquaEvento editEvent(Long id, AquaEvento updatedEvent) {
        Optional<AquaEvento> optionalEvento = repository.findById(id);
        if (optionalEvento.isPresent()) {
            AquaEvento evento = optionalEvento.get();
            if (updatedEvent.getTitulo() != null) evento.setTitulo(updatedEvent.getTitulo());
            if (updatedEvent.getDescripcion() != null) evento.setDescripcion(updatedEvent.getDescripcion());
            if (updatedEvent.getFechaInicio() != null) evento.setFechaInicio(updatedEvent.getFechaInicio());
            if (updatedEvent.getFechaFin() != null) evento.setFechaFin(updatedEvent.getFechaFin());
            if (updatedEvent.getEstado() != null) evento.setEstado(updatedEvent.getEstado());
            if (updatedEvent.getTags() != null) evento.setTags(updatedEvent.getTags());
            if (updatedEvent.getSector() != null) evento.setSector(updatedEvent.getSector());
            if (updatedEvent.getLitrosConsumidos() != null) evento.setLitrosConsumidos(updatedEvent.getLitrosConsumidos());
            if (updatedEvent.getCosto() != null) evento.setCosto(updatedEvent.getCosto());
            return repository.save(evento);
        }
        return null;
    }

@Transactional
public AquaEvento updateEvent(AquaEvento updatedEvent) {
    if (repository.existsById(updatedEvent.getId())) {


        if (updatedEvent.getEstado() == Estado.FINALIZADO && updatedEvent.getFechaFin() == null) {
            updatedEvent.setFechaFin(LocalDateTime.now());
        }

        return repository.save(updatedEvent);
    }
    return null;
}

}