package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.AquaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AquaEventoRepository extends JpaRepository<AquaEvento, Long> {
}
