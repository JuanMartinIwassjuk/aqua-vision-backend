package com.app.aquavision.services;

import com.app.aquavision.repositories.HogarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricasService {

    @Autowired
    private HogarRepository hogarRepository;

    @Transactional
    public Long contarHogares() {
        return hogarRepository.count();
    }




}
