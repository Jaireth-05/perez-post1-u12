package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.*;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(
            new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados());

    private SolicitudReembolso solicitud(CategoriaGasto cat, String monto, LocalDate fecha, boolean docs) {
        var s = new SolicitudReembolso();
        s.setEmpleadoId("E1");
        s.setCategoria(cat);
        s.setMontoSolicitado(new BigDecimal(monto));
        s.setFechaGasto(fecha);
        s.setDocumentosSoporte(docs);
        return s;
    }

    @Test
    void solicitudCorrectaPasaLasTresValidaciones() {
        var s = solicitud(CategoriaGasto.VIAJE, "100000", LocalDate.of(2026, 3, 10), true);
        assertTrue(cadena.validar(s, List.of()).valida());
    }

    @Test
    void rechazaSiFaltanDocumentos() {
        var s = solicitud(CategoriaGasto.VIAJE, "100000", LocalDate.of(2026, 3, 10), false);
        var r = cadena.validar(s, List.of());
        assertFalse(r.valida());
        assertEquals("Faltan documentos soporte", r.motivoRechazo());
    }

    @Test
    void rechazaSiExcedeElTope() {
        var s = solicitud(CategoriaGasto.VIAJE, "150001", LocalDate.of(2026, 3, 10), true);
        assertFalse(cadena.validar(s, List.of()).valida());
    }

    @Test
    void rechazaDuplicadoDeLaMismaCategoriaYPeriodo() {
        var existente = solicitud(CategoriaGasto.VIAJE, "50000", LocalDate.of(2026, 3, 2), true);
        var nueva = solicitud(CategoriaGasto.VIAJE, "60000", LocalDate.of(2026, 3, 20), true);
        assertFalse(cadena.validar(nueva, List.of(existente)).valida());
    }

    @Test
    void mismoMesDeOtroAnioNoEsDuplicado() {
        var existente = solicitud(CategoriaGasto.VIAJE, "50000", LocalDate.of(2025, 3, 2), true);
        var nueva = solicitud(CategoriaGasto.VIAJE, "60000", LocalDate.of(2026, 3, 20), true);
        assertTrue(cadena.validar(nueva, List.of(existente)).valida());
    }

    @Test
    void laCadenaSeDetieneEnElPrimerRechazo() {
        var s = solicitud(CategoriaGasto.VIAJE, "999999", LocalDate.of(2026, 3, 10), false);
        assertEquals("Faltan documentos soporte", cadena.validar(s, List.of()).motivoRechazo());
    }
}