package com.empresa.reembolsos.procesamiento.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    private SolicitudReembolso solicitud(String monto) {
        var s = new SolicitudReembolso();
        s.setMontoSolicitado(new BigDecimal(monto));
        return s;
    }

    @Test
    void reembolsaElMontoCompletoBajoElTope() {
        var r = calculadora.calcular(solicitud("1500000"));
        assertEquals(0, new BigDecimal("1500000").compareTo(r));
    }

    @Test
    void limitaAlTopeMensual() {
        var r = calculadora.calcular(solicitud("2500000"));
        assertEquals(0, new BigDecimal("2000000").compareTo(r));
    }
}