package com.app.aquavision.entities.domain;

import com.app.aquavision.entities.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Hogar")
public class Hogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int miembros = 1;

    @Column
    private String localidad = "";

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    private List<Sector> sectores = new ArrayList<>();

    @OneToOne(mappedBy = "hogar")
    private User usuario;

    public Hogar() {
        // Constructor por defecto
    }

    public Hogar(int miembros, String localidad, List<Sector> sectores) {
        this.miembros = miembros;
        this.localidad = localidad;
        this.sectores = sectores;
    }

    public Hogar(int miembros, String localidad) {
        this.miembros = miembros;
        this.localidad = localidad;
    }

    public void mostarProyeccionConsumoMensual() {
        System.out.println("Proyección de Consumo A fin de mes del Hogar:");

        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1);

        int diaHoy = hoy.getDayOfMonth();
        int diasRestantes = 30 - diaHoy; //Asumiendo un mes de 30 días

        int proyeccionHogar = 0;

        for (Sector sector : sectores) {
            int totalConsumoSector = sector.consumoTotalPorFecha(inicioMes, hoy);
            int promedioConsumoSector = totalConsumoSector / diaHoy;
            int proyeccionConsumoSector = totalConsumoSector + diasRestantes * promedioConsumoSector;

            System.out.println("  Sector: " + sector.getNombre());
            System.out.println("   -Categoria: " + sector.getCategoria());
            System.out.println("   -Total Consumo: " + totalConsumoSector);
            System.out.println("   -Promedio Consumo Diario: " + promedioConsumoSector);
            System.out.println("   -Proyección Mensual: " + proyeccionConsumoSector);

            proyeccionHogar += proyeccionConsumoSector;
        }

        System.out.println(" -Total Proyección Mensual Hogar: " + proyeccionHogar);
    }

    public void mostrarConsumoTotalPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        System.out.println("Consumo Total del Hogar desde " + fechaInicio + " hasta " + fechaFin + ":");

        int totalConsumoHogar = 0;

        for (Sector sector : sectores) {
            int totalConsumoSector = sector.consumoTotalPorFecha(fechaInicio, fechaFin);
            totalConsumoHogar += totalConsumoSector;

            System.out.println("  Sector: " + sector.getNombre());
            System.out.println("   -Categoria: " + sector.getCategoria());
            System.out.println("   -Total Consumo: " + totalConsumoSector);
        }

        System.out.println(" -Total Consumo Hogar: " + totalConsumoHogar);
    }

    public void mostrarConsumoActualDiaro(){
        System.out.println("Consumo del Mes Actual del Hogar:");

        int totalConsumoHogar = 0;

        for (Sector sector : sectores) {
            int totalConsumoSector = sector.consumoActualDiaro();
            totalConsumoHogar += totalConsumoSector;

            System.out.println("  Sector: " + sector.getNombre());
            System.out.println("   -Categoria: " + sector.getCategoria());
            System.out.println("   -Total Consumo: " + totalConsumoSector);
        }

        System.out.println(" -Total Consumo Hogar: " + totalConsumoHogar);
    }

    public void mostrarReporteHogar(){

        System.out.println("Reporte del Hogar:");
        System.out.println(" -Miembros: " + miembros);
        System.out.println(" -Localidad: " + localidad);
        System.out.println(" -Sectores: " + sectores.size());

        int totalConsumoHogar = 0;

        for (Sector sector : sectores) {
            int totalConsumoSector = sector.totalConsumo();
            int promedioConsumoSector = sector.promedioConsumo();

            System.out.println("  Sector: " + sector.getNombre());
            System.out.println("   -Categoria: " + sector.getCategoria());
            System.out.println("   -Total Consumo: " + totalConsumoSector);
            System.out.println("   -Promedio Consumo: " + promedioConsumoSector);

            totalConsumoHogar += totalConsumoSector;
        }

        System.out.println(" -Total Consumo Hogar: " + totalConsumoHogar);
        System.out.println(" -Promedio Consumo x Sector: " + totalConsumoHogar / sectores.size());
    }

    public List<Sector> getSectores() {
        return sectores;
    }

    public void setSectores(List<Sector> sectores) {
        this.sectores = sectores;
    }

    public int getMiembros() {
        return miembros;
    }

    public void setMiembros(int miembros) {
        this.miembros = miembros;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public Long getId() {
        return id;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }
}
