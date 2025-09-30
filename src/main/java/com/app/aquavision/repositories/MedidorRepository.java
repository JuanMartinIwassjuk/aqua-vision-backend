package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Medidor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MedidorRepository extends CrudRepository<Medidor, Long>{

    Optional<Medidor> findByNumeroSerie(int numeroSerie);
}
