package com.app.aquavision.repositories;


import com.app.aquavision.entities.domain.gamification.TagEvento;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagEventoRepository extends JpaRepository<TagEvento, Long> {

    Optional<TagEvento> findByNombre(String nombre);
}