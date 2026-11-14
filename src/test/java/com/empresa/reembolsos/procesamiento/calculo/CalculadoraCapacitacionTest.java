package com.empresa.reembolsos.procesamiento.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculadoraCapacitacionTest {

    private final CalculadoraCapacitacion calculadora = new CalculadoraCapacitacion();

    private SolicitudReembolso solicitud(String monto) {
        var s = new SolicitudReembolso();
        s.setMontoSolicitado(new BigDecimal(monto));
        return s;
    }

    @Test
    void reembolsaElOchentaPorCiento() {
        var r = calculadora.calcular(solicitud("1000000"));
        assertEquals(0, new BigDecimal("800000").compareTo(r));
    }

    @Test
    void limitaAlTopeAnualCuandoElOchentaPorCientoLoSupera() {
        var r = calculadora.calcular(solicitud("5000000"));
        assertEquals(0, new BigDecimal("3000000").compareTo(r));
    }
}