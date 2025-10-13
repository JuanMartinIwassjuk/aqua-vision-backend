package com.app.aquavision.services;

import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.NotificacionRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository repository;

    @Autowired
    private HogarRepository hogarRepository;


    @Transactional(readOnly = true)
    public List<Notificacion> getNotifications(Long hogarId) {
        return repository.findByHogar_Id(hogarId);
    }


    @Transactional(readOnly = true)
    public List<Notificacion> getUnreadNotifications(Long hogarId) {
        return repository.findByHogar_IdAndLeidoFalse(hogarId);
    }


    @Transactional
    public Notificacion markAsRead(Long id) {
        return repository.findById(id).map(n -> {
            n.setLeido(true);
            return repository.save(n);
        }).orElse(null);
    }


    @Transactional
    public boolean markAllAsRead(Long hogarId) {
        List<Notificacion> notifs = repository.findByHogar_Id(hogarId);
        if (notifs.isEmpty()) return false;
        notifs.forEach(n -> n.setLeido(true));
        repository.saveAll(notifs);
        return true;
    }


    @Transactional
    public boolean deleteNotification(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }


    @Transactional
    public boolean deleteAllNotifications(Long hogarId) {
        repository.deleteByHogar_Id(hogarId);
        return true;
    }


    @Transactional
    public Notificacion createNotification(Long hogarId, Notificacion notif) {
        return hogarRepository.findById(hogarId).map(hogar -> {
            notif.setId(null);
            notif.setLeido(false);
            notif.setFechaEnvio(LocalDateTime.now());
            hogar.getNotificaciones().add(notif);
            hogarRepository.save(hogar);
            return notif;
        }).orElseThrow(() -> new EntityNotFoundException("Hogar no encontrado"));
    }
}
