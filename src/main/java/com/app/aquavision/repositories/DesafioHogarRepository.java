package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.gamification.DesafioHogar; // Asegúrate de usar la ruta correcta a tu entidad
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesafioHogarRepository extends JpaRepository<DesafioHogar, Long> {
}