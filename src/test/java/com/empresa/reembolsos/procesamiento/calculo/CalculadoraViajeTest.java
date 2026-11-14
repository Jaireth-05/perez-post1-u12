package com.empresa.reembolsos.procesamiento.calculo;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    private SolicitudReembolso solicitud(String monto, int dias) {
        var s = new SolicitudReembolso();
        s.setMontoSolicitado(new BigDecimal(monto));
        s.setDiasViaje(dias);
        return s;
    }

    @Test
    void reembolsaElMontoCompletoSiNoSuperaElTopeTotal() {
        var r = calculadora.calcular(solicitud("200000", 3));
        assertEquals(0, new BigDecimal("200000").compareTo(r));
    }

    @Test
    void limitaAlTopeDiarioPorLosDiasDelViaje() {
        var r = calculadora.calcular(solicitud("600000", 3));
        assertEquals(0, new BigDecimal("450000").compareTo(r));
    }
}