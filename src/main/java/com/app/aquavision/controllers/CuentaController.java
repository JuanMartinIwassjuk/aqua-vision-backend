package com.app.aquavision.controllers;

import com.app.aquavision.dto.cuenta.FacturacionDTO;
import com.app.aquavision.dto.cuenta.HogarInfoDTO;
import com.app.aquavision.dto.cuenta.SensorDTO;
import com.app.aquavision.dto.cuenta.UsuarioDTO;
import com.app.aquavision.services.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "Mi Cuenta",
        description = "Datos para mi perfil"
)
@RestController
@RequestMapping("/cuenta")
@CrossOrigin(origins = "*")
public class CuentaController {

    @Autowired
    CuentaService cuentaService;

    //Datos personales
    @Operation(
            summary = "Obtener datos personales del usuario asociado a un hogar",
            parameters = {
                    @Parameter(
                            name = "hogarId",
                            description = "ID del hogar asociado al usuario",
                            required = true,
                            example = "5"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos personales obtenidos correctamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UsuarioDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No se encontró un hogar con el ID especificado",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{hogarId}/usuario")
    public ResponseEntity<UsuarioDTO> getDatosPersonales(@PathVariable Long hogarId) {
        return ResponseEntity.ok(cuentaService.obtenerDatosPersonales(hogarId));
    }

    @Operation(
            summary = "Obtener información general del hogar",
            description = "Retorna la información del hogar, incluyendo su nombre, dirección, tipo, cantidad de ambientes e integrantes.",
            parameters = {
                    @Parameter(
                            name = "hogarId",
                            description = "ID del hogar del cual se desea obtener información",
                            required = true,
                            example = "5"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Información del hogar obtenida correctamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HogarInfoDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No se encontró un hogar con el ID especificado",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{hogarId}/hogar")
    public ResponseEntity<HogarInfoDTO> getInfoHogar(@PathVariable Long hogarId) {
        return ResponseEntity.ok(cuentaService.obtenerInfoHogar(hogarId));
    }


    @Operation(
            summary = "Obtener información de facturación del hogar",
            description = "Devuelve los datos de facturación del hogar, incluyendo plan actual, monto mensual, método de pago y fecha del próximo vencimiento.",
            parameters = {
                    @Parameter(
                            name = "hogarId",
                            description = "ID del hogar del cual se desea obtener la información de facturación",
                            required = true,
                            example = "5"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos de facturación obtenidos correctamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = FacturacionDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No se encontró un hogar con el ID especificado",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{hogarId}/facturacion")
    public ResponseEntity<FacturacionDTO> getFacturacion(@PathVariable Long hogarId) {
        return ResponseEntity.ok(cuentaService.obtenerFacturacion(hogarId));
    }


    @Operation(
            summary = "Obtener los sensores o medidores del hogar",
            description = "Retorna la lista de sensores asociados al hogar, incluyendo su nombre, estado actual, última medición y consumo total registrado.",
            parameters = {
                    @Parameter(
                            name = "hogarId",
                            description = "ID del hogar del cual se desean obtener los sensores",
                            required = true,
                            example = "5"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de sensores obtenida correctamente",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = SensorDTO.class)))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No se encontraron sensores asociados al hogar especificado",
                            content = @Content
                    )
            }
    )
    @GetMapping("/{hogarId}/sensores")
    public ResponseEntity<List<SensorDTO>> getSensores(@PathVariable Long hogarId) {
        return ResponseEntity.ok(cuentaService.obtenerSensores(hogarId));
    }


}
