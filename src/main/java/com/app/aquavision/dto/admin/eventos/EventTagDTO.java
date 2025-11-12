package com.app.aquavision.dto.admin.eventos;

public class EventTagDTO {
    private Integer id;
    private String nombre;
    private String color;

    public EventTagDTO() {}
    public EventTagDTO(Integer id, String nombre, String color) {
        this.id = id; this.nombre = nombre; this.color = color;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    
    
}
