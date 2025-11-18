package com.app.aquavision.services;

import com.app.aquavision.dto.cuenta.FacturacionDTO;
import com.app.aquavision.dto.cuenta.HogarInfoDTO;
import com.app.aquavision.dto.cuenta.SensorDTO;
import com.app.aquavision.dto.cuenta.UsuarioDTO;
import com.app.aquavision.entities.User;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.entities.payments.Facturacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CuentaService {

    @Autowired
    HogarService hogarService;

    @Autowired
    UserService userService;


    public UsuarioDTO obtenerDatosPersonales(Long hogarId) {
        Hogar hogar = hogarService.findById(hogarId);
        User usuario = userService.findByHogarId(hogarId);
        if (hogar == null) {
            return null;
        }
        UsuarioDTO usuarioDTO = new UsuarioDTO()
                .setNombreUsuario(usuario.getUsername())
                .setApellido(usuario.getSurname())
                .setNombre(usuario.getName())
                .setCuentaActiva(true) // TODO: Ver si modificarlo
                .setCorreo(hogar.getEmail())
                .setCorreoVerificado(true) // TODO: Default si
                .setUltimoLogin(usuario.getLastLogin());

    return usuarioDTO;
    }

    public HogarInfoDTO obtenerInfoHogar(Long hogarId) {
        Hogar hogar = hogarService.findById(hogarId);
        if (hogar == null) {
            return null;
        }
        HogarInfoDTO hogarInfoDTO = new HogarInfoDTO()
                .setNombreHogar(hogar.getNombre())
                .setDireccion(hogar.getDireccion())
                .setCiudad(hogar.getLocalidad())
                .setTipoHogar(hogar.getTipoHogar())
                .setCantidadBanos(hogar.getAmbientes())// TODO: Poner ambientes solo
                .setCantidadIntegrantes(hogar.getMiembros())
                .setTienePatio(hogar.tienePatio())
                .setTienePileta(hogar.tienePileta());
        return hogarInfoDTO;
    }

    public FacturacionDTO obtenerFacturacion(Long hogarId) {
        Hogar hogar = hogarService.findById(hogarId);
        if (hogar == null) {
            return null;
        }
        Facturacion facturacionHogar = hogar.getFacturacion();
        FacturacionDTO facturacionDTO = new FacturacionDTO()
                .setPlanActual(facturacionHogar.getTipoPlan().getPlan())
                .setMonto(facturacionHogar.getTipoPlan().getCostoMensual())
                .setFechaProxVencimiento(LocalDate.now().plusMonths(1).withDayOfMonth(10))  //TODO: fecha real
                .setMetodoPago(facturacionHogar.getMedioDePago());
        return facturacionDTO;
    }

    public List<SensorDTO> obtenerSensores(Long hogarId) {
        Hogar hogar = hogarService.findById(hogarId);
        if (hogar == null) {
            return null;
        }
// ✅ Recorrer los sectores del hogar y obtener sus medidores
        List<SensorDTO> sensores = new ArrayList<>();
        for (Sector sector : hogar.getSectores()) {
            if (sector.getMedidor() != null) {
                SensorDTO sensorDTO = new SensorDTO()
                        .setNombreSensor(sector.getNombre())
                        .setEstadoActual(sector.getMedidor().getEstado())
                        .setUltimaMedicion(sector.getMediciones().stream().findFirst()
                                .map(Medicion::getTimestamp)
                                .orElse(null))
                        //.setConsumoActual(sector.getMediciones().stream().mapToInt(Medicion::getFlow).sum());
                        .setConsumoActual(sector.getMediciones().stream().mapToDouble(Medicion::getFlow).sum());
                sensores.add(sensorDTO);
            }
        }
        return sensores;
    }
}
