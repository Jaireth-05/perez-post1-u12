package com.empresa.reembolsos.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class FlujoCompletoIntegrationTest {

    @Autowired
    private SolicitudReembolsoService servicio;

    private SolicitudReembolso nueva(String empleado, CategoriaGasto categoria,
                                     String monto, int dias, boolean documentos) {
        var s = new SolicitudReembolso();
        s.setEmpleadoId(empleado);
        s.setCategoria(categoria);
        s.setMontoSolicitado(new BigDecimal(monto));
        s.setDiasViaje(dias);
        s.setFechaGasto(LocalDate.of(2026, 3, 10));
        s.setDocumentosSoporte(documentos);
        return s;
    }

    private void assertMonto(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real));
    }

    @Test
    void viajeSeCreaPendienteConSuMontoAprobado() {
        var creada = servicio.crear(nueva("E-viaje", CategoriaGasto.VIAJE, "120000", 1, true));
        assertNotNull(creada.getId());
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        assertMonto("120000", creada.getMontoAprobado());
    }

    @Test
    void representacionReembolsaElMontoCompleto() {
        var creada = servicio.crear(nueva("E-repr", CategoriaGasto.REPRESENTACION, "1500000", 0, true));
        assertMonto("1500000", creada.getMontoAprobado());
    }

    @Test
    void capacitacionReembolsaElOchentaPorCiento() {
        var creada = servicio.crear(nueva("E-cap", CategoriaGasto.CAPACITACION, "1000000", 0, true));
        assertMonto("800000", creada.getMontoAprobado());
    }

    @Test
    void rechazaUnaSolicitudSinDocumentosConMensajeClaro() {
        var e = assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(nueva("E-sindocs", CategoriaGasto.VIAJE, "100000", 1, false)));
        assertTrue(e.getMessage().contains("documentos"));
    }

    @Test
    void rechazaUnDuplicadoPendienteDeLaMismaCategoriaYPeriodo() {
        servicio.crear(nueva("E-dup", CategoriaGasto.VIAJE, "100000", 1, true));
        assertThrows(SolicitudInvalidaException.class,
                () -> servicio.crear(nueva("E-dup", CategoriaGasto.VIAJE, "50000", 1, true)));
    }

    @Test
    void aprobarNotificaALosTresInteresados(CapturedOutput salida) {
        var creada = servicio.crear(nueva("E-aprob", CategoriaGasto.CAPACITACION, "1000000", 0, true));

        servicio.cambiarEstado(creada.getId(), EstadoSolicitud.APROBADA);

        assertTrue(salida.getOut().contains("Notificando a E-aprob"));
        assertTrue(salida.getOut().contains("Contabilidad: programar pago"));
        assertTrue(salida.getOut().contains("Auditoria: solicitud"));
    }

    @Test
    void rechazarNoActivaAContabilidadPeroSiALosDemas(CapturedOutput salida) {
        var creada = servicio.crear(nueva("E-rech", CategoriaGasto.CAPACITACION, "1000000", 0, true));

        servicio.cambiarEstado(creada.getId(), EstadoSolicitud.RECHAZADA);

        assertTrue(salida.getOut().contains("Notificando a E-rech"));
        assertTrue(salida.getOut().contains("Auditoria: solicitud"));
        assertFalse(salida.getOut().contains("Contabilidad: programar pago"));
    }
}