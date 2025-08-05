package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicionRepository extends JpaRepository<Medicion, Long> {
}
