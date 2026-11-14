package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    private SolicitudReembolso solicitud(CategoriaGasto categoria, LocalDate fecha) {
        var s = new SolicitudReembolso();
        s.setCategoria(categoria);
        s.setFechaGasto(fecha);
        return s;
    }

    @Test
    void rechazaMismaCategoriaEnElMismoPeriodo() {
        var existente = solicitud(CategoriaGasto.VIAJE, LocalDate.of(2026, 3, 2));
        var nueva = solicitud(CategoriaGasto.VIAJE, LocalDate.of(2026, 3, 20));
        assertFalse(validador.validar(nueva, List.of(existente)).valida());
    }

    @Test
    void apruebaOtraCategoriaEnElMismoPeriodo() {
        var existente = solicitud(CategoriaGasto.VIAJE, LocalDate.of(2026, 3, 2));
        var nueva = solicitud(CategoriaGasto.CAPACITACION, LocalDate.of(2026, 3, 20));
        assertTrue(validador.validar(nueva, List.of(existente)).valida());
    }

    @Test
    void apruebaElMismoMesDeOtroAnio() {
        var existente = solicitud(CategoriaGasto.VIAJE, LocalDate.of(2025, 3, 2));
        var nueva = solicitud(CategoriaGasto.VIAJE, LocalDate.of(2026, 3, 20));
        assertTrue(validador.validar(nueva, List.of(existente)).valida());
    }
}