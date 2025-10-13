package com.app.aquavision.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.app.aquavision.entities.domain.notifications.Notificacion;

@Repository
public interface NotificacionRepository extends  CrudRepository<Notificacion, Long>{
    
    List<Notificacion> findByHogar_Id(Long hogarId);

    List<Notificacion> findByHogar_IdAndLeidoFalse(Long hogarId);

    void deleteByHogar_Id(Long hogarId);
}

