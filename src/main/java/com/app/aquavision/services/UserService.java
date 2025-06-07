package com.app.aquavision.services;

import java.util.List;

import com.app.aquavision.entities.User;

public interface UserService {
    
    List<User> findAll();

    User save(User user);   

    boolean existsByUsername(String username);

    User findById(Long id);

    void deleteById(Long id);

    void delete(User user);

    User update(Long id, User userDetails);

}
