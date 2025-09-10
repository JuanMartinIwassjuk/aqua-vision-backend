package com.app.aquavision.services;

import com.app.aquavision.entities.domain.AquaEvento;
import com.app.aquavision.entities.domain.Estado;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.repositories.AquaEventoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
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

        if (updatedEvent.getEstado() == Estado.FINALIZADO) {

            if (updatedEvent.getFechaFin() == null) {
                updatedEvent.setFechaFin(LocalDateTime.now());
            }

          
            Sector sector = updatedEvent.getSector();
            if (sector != null && sector.getMediciones() != null) {
                List<Medicion> mediciones = sector.getMediciones().stream()
                        .filter(m -> m.getTimestamp() != null
                                && !m.getTimestamp().isBefore(updatedEvent.getFechaInicio())
                                && !m.getTimestamp().isAfter(updatedEvent.getFechaFin()))
                        .sorted(Comparator.comparing(Medicion::getTimestamp))
                        .toList();

                double litros = 0.0;
                for (int i = 0; i < mediciones.size() - 1; i++) {
                    Medicion actual = mediciones.get(i);
                    Medicion siguiente = mediciones.get(i + 1);

                    long minutos = Duration.between(
                            actual.getTimestamp(),
                            siguiente.getTimestamp()
                    ).toMinutes();

                    
                    litros += actual.getFlow() * minutos;
                }

                updatedEvent.setLitrosConsumidos(litros);
            }
        }

        return repository.save(updatedEvent);
    }
    return null;
}


}