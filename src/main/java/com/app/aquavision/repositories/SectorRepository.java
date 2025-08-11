package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface SectorRepository extends JpaRepository<Sector, Long> {
}
