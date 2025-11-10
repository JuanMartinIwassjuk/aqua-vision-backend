package com.app.aquavision.services;

import com.app.aquavision.dto.gamificacion.PuntosReclamadosDTO;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.entities.domain.gamification.Minijuego;
import com.app.aquavision.entities.domain.gamification.PuntosReclamados;
import com.app.aquavision.entities.domain.gamification.Recompensa;
import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.PuntosReclamadosRepository;
import com.app.aquavision.repositories.RecompensaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HogarService {

    @Autowired
    private HogarRepository repository;

    @Autowired
    private RecompensaRepository recompensaRepository;

    @Autowired
    private PuntosReclamadosRepository puntosReclamadosRepository;

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
            hogar.sumarPuntosDisponibles(puntos);
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
    public List<Recompensa> getRecompensasDisponibles() {
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
            if (sector != null) {
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
            if (notificacion != null) {
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
    public List<Object[]> getRanking() {
        return repository.findAllByOrderByPuntajeDesc();

    }

    @Transactional
    public int getPuntosHogar(Long hogarId) {
        Optional<Hogar> optionalHogar = repository.findById(hogarId);
        return optionalHogar.map(Hogar::getPuntos).orElse(0);
    }

    @Transactional
    public int getTotalDePuntosReclamadosDelHogar(Long hogarId) {
        Optional<Hogar> optionalHogar = repository.findById(hogarId);
        if (optionalHogar.isPresent()) {
            Hogar hogar = optionalHogar.get();
            return hogar.getPuntosReclamados().stream().map(PuntosReclamados::getPuntos).mapToInt(Integer::intValue).sum();
        }
        return 0;
    }


    @Transactional
    public Hogar registrarPuntosReclamados(Long hogarId, PuntosReclamadosDTO dto) {
        Hogar hogar = repository.findById(hogarId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el hogar con el id " + hogarId));

        PuntosReclamados pr = new PuntosReclamados();
        pr.setPuntos(dto.getPuntos());
        pr.setFecha(LocalDateTime.now());
        pr.setMiniJuego(Minijuego.valueOf(dto.getMinijuego()));
        pr.setEscena(dto.getEscena());

        hogar.sumarPuntosDisponibles(dto.getPuntos());

        hogar.getPuntosReclamados().add(pr);
        repository.save(hogar);

        return hogar;
    }

    @Transactional
    public LocalDateTime getUltimoReclamoSegunMinijuego(Long hogarId, String minijuego, String escena) {
        Hogar hogar = repository.findById(hogarId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el hogar con el id " + hogarId));

        return this.puntosReclamadosRepository.getUltimaFechaReclamo(hogarId,minijuego,escena);

    }
}
