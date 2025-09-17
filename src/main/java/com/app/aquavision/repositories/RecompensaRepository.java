package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.gamification.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecompensaRepository extends JpaRepository<Recompensa, Long> {
}
