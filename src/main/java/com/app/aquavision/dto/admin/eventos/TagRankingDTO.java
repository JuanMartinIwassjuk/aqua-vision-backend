package com.app.aquavision.dto.admin.eventos;

public class TagRankingDTO {
    private Integer id;        // opcional si lo tenés
    private String nombre;
    private Integer count;
    private Double avgLitros;

    public TagRankingDTO() {}

    public TagRankingDTO(Integer id, String nombre, Integer count, Double avgLitros) {
        this.id = id;
        this.nombre = nombre;
        this.count = count;
        this.avgLitros = avgLitros;
    }


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public Double getAvgLitros() { return avgLitros; }
    public void setAvgLitros(Double avgLitros) { this.avgLitros = avgLitros; }
}