package com.app.aquavision.dto;



import java.util.List;

public class EventoSectorDTO  {
    private Long id;
    private String nombre;
    private List<EventoSimpleDTO> eventos;

    public EventoSectorDTO(Long id, String nombre, List<EventoSimpleDTO> eventos) {
        this.id = id;
        this.nombre = nombre;
        this.eventos = eventos;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<EventoSimpleDTO> getEventos() {
        return eventos;
    }
}
