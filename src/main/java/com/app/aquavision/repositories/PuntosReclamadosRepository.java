package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.gamification.AquaEvento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuntosReclamadosRepository  extends JpaRepository<AquaEvento, Long> {
}
