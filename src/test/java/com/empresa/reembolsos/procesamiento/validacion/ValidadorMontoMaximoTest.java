package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();

    private SolicitudReembolso solicitud(CategoriaGasto categoria, String monto) {
        var s = new SolicitudReembolso();
        s.setCategoria(categoria);
        s.setMontoSolicitado(new BigDecimal(monto));
        return s;
    }

    @Test
    void apruebaUnMontoIgualAlTopeDeLaCategoria() {
        assertTrue(validador.validar(solicitud(CategoriaGasto.REPRESENTACION, "2000000"), List.of()).valida());
    }

    @Test
    void rechazaUnMontoQueExcedeElTopeYNombraLaCategoria() {
        var r = validador.validar(solicitud(CategoriaGasto.REPRESENTACION, "2000001"), List.of());
        assertFalse(r.valida());
        assertTrue(r.motivoRechazo().contains("REPRESENTACION"));
    }

    @Test
    void cadaCategoriaAplicaSuPropioTope() {
        assertTrue(validador.validar(solicitud(CategoriaGasto.CAPACITACION, "3000000"), List.of()).valida());
        assertFalse(validador.validar(solicitud(CategoriaGasto.CAPACITACION, "3000001"), List.of()).valida());
    }
}