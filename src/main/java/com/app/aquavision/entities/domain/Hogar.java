package com.app.aquavision.entities.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Hogar")
public class Hogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column
    private int miembros = 1;

    @Column
    private String localidad = "";

    @Column
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int rachaDiaria = 0;

    @Column
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int puntos = 0;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<RecompensaHogar> recompensas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    private List<Sector> sectores = new ArrayList<>();

    public Hogar() {
        // Constructor por defecto
    }

    public Hogar(int miembros, String localidad, List<Sector> sectores) {
        this.miembros = miembros;
        this.localidad = localidad;
        this.sectores = sectores;
        this.puntos=0;
    }

    public Hogar(int miembros, String localidad) {
        this.miembros = miembros;
        this.localidad = localidad;
         this.puntos=0;
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

    public int consumoTotalHora(int hora){
        int consumoTotal = 0;
        for (Sector sector : sectores) {
            consumoTotal += sector.totalConsumo(hora);
        }
        return consumoTotal;
    }

    public void aumentarRachaDiaria() {
        this.rachaDiaria += 1;
    }

    public void resetearRachaDiaria() {
        this.rachaDiaria = 0;
    }

    public void reclamarRecompensa(Recompensa recompensa) {
        if (this.puedeCanjearRecompensa(recompensa)) {
            this.puntos -= recompensa.getPuntosNecesarios();
            RecompensaHogar recompensaHogar = new RecompensaHogar(recompensa, EstadoRecompensa.DISPONIBLE, LocalDateTime.now().toLocalDate());
            this.agregarRecompensa(recompensaHogar);
        } else {
            throw new IllegalArgumentException("No tienes suficientes puntos para canjear esta recompensa.");
        }
    }

    public boolean puedeCanjearRecompensa(Recompensa recompensa) {
        return this.puntos >= recompensa.getPuntosNecesarios();
    }

    public void agregarRecompensa(RecompensaHogar recompensa) {
        this.recompensas.add(recompensa);
    }

    public void sumarPuntos(int puntos) {
        this.puntos += puntos;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public List<RecompensaHogar> getRecompensas() {
        return recompensas;
    }

    public void setRecompensas(List<RecompensaHogar> recompensas) {
        this.recompensas = recompensas;
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

    public void setRachaDiaria(int rachaDiaria) {
        this.rachaDiaria = rachaDiaria;
    }

    public int getRachaDiaria() {
        return rachaDiaria;
    }

}
