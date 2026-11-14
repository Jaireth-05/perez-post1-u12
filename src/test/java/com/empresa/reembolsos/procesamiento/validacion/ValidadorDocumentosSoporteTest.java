package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    private SolicitudReembolso solicitud(boolean documentos) {
        var s = new SolicitudReembolso();
        s.setDocumentosSoporte(documentos);
        return s;
    }

    @Test
    void apruebaCuandoHayDocumentosSoporte() {
        assertTrue(validador.validar(solicitud(true), List.of()).valida());
    }

    @Test
    void rechazaCuandoFaltanDocumentosSoporte() {
        var r = validador.validar(solicitud(false), List.of());
        assertFalse(r.valida());
        assertEquals("Faltan documentos soporte", r.motivoRechazo());
    }
}