package com.app.aquavision.services;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.entities.domain.gamification.Recompensa;
import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.RecompensaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HogarService {

    @Autowired
    private HogarRepository repository;

    @Autowired
    private RecompensaRepository recompensaRepository;

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

    @Transactional
    public Hogar addPuntosToHogar(Long id, int puntos) {
        Optional<Hogar> optionalHogar = repository.findById(id);
        if (optionalHogar.isPresent()) {
            Hogar hogar = optionalHogar.get();
            hogar.sumarPuntos(puntos);
            repository.save(hogar);
        }
        return optionalHogar.orElse(null);
    }

    @Transactional
    public Hogar reclamarRecompensa(Long hogarId, Long recompensaId) {
        Optional<Hogar> optionalHogar = repository.findById(hogarId);
        Optional<Recompensa> optionalRecompensa = recompensaRepository.findById(recompensaId);

        if (optionalHogar.isPresent() && optionalRecompensa.isPresent()) {
            Hogar hogar = optionalHogar.get();
            Recompensa recompensa = optionalRecompensa.get();

            hogar.reclamarRecompensa(recompensa);
            repository.save(hogar);
        }

        return optionalHogar.orElse(null);
    }

    @Transactional
    public List<Recompensa> getRecompensasDisponibles(){
        return (List<Recompensa>) recompensaRepository.findAll();
    }

    @Transactional
    public Hogar aumentarRachaDiaria(Long hogarId) {
        Optional<Hogar> optionalHogar = repository.findById(hogarId);
        if (optionalHogar.isPresent()) {
            Hogar hogar = optionalHogar.get();
            hogar.aumentarRachaDiaria();
            repository.save(hogar);
            return hogar;
        }
        return null;
    }

    @Transactional
    public Hogar resetearRachaDiaria(Long hogarId) {
        Optional<Hogar> optionalHogar = repository.findById(hogarId);
        if (optionalHogar.isPresent()) {
            Hogar hogar = optionalHogar.get();
            hogar.resetearRachaDiaria();
            repository.save(hogar);
            return hogar;
        }
        return null;
    }

    @Transactional
    public Hogar updateUmbralSector(Long hogarId, Long sectorId, Float nuevoUmbral) {
        Optional<Hogar> optionalHogar = repository.findByIdWithSectores(hogarId);
        if (optionalHogar.isPresent()) {
            Sector sector = optionalHogar.get().getSectores().stream()
                    .filter(s -> s.getId().equals(sectorId))
                    .findFirst()
                    .orElse(null);
            if (sector != null){
                sector.setUmbralMensual(nuevoUmbral);
            } else {
                return null;
            }

            repository.save(optionalHogar.get());
            return optionalHogar.get();
        }
        return null;
    }

    @Transactional
    public Hogar visualizarNotificacion(Long hogarId, Long notificacionId) {
        Optional<Hogar> optionalHogar = repository.findById(hogarId);
        if (optionalHogar.isPresent()) {
            Hogar hogar = optionalHogar.get();
            Notificacion notificacion = hogar.getNotificaciones().stream()
                    .filter(n -> n.getId().equals(notificacionId))
                    .findFirst()
                    .orElse(null);
            if (notificacion != null){
                notificacion.leer();
            } else {
                return null;
            }
            repository.save(hogar);
            return hogar;
        }
        return null;
    }

    @Transactional
    public List<Hogar> getRanking(){

        return repository.findAllByOrderByPuntajeDesc();

    }

}
