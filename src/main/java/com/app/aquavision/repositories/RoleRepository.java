package com.app.aquavision.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.app.aquavision.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findByName(String name);
    
}
