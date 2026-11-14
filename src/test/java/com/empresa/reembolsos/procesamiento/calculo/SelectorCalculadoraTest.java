package com.empresa.reembolsos.procesamiento.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import java.util.List;
import org.junit.jupiter.api.Test;

class SelectorCalculadoraTest {

    @Test
    void todasLasCategoriasTienenCalculadora() {
        var selector = new SelectorCalculadora(List.of(
                new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion()));

        for (CategoriaGasto categoria : CategoriaGasto.values()) {
            assertEquals(categoria, selector.obtener(categoria).getCategoria());
        }
    }

    @Test
    void categoriaSinCalculadoraLanzaExcepcion() {
        var selector = new SelectorCalculadora(List.of(new CalculadoraViaje()));
        assertThrows(IllegalArgumentException.class,
                () -> selector.obtener(CategoriaGasto.REPRESENTACION));
    }

    @Test
    void dosCalculadorasParaLaMismaCategoriaFallanAlArrancar() {
        assertThrows(IllegalStateException.class,
                () -> new SelectorCalculadora(List.of(new CalculadoraViaje(), new CalculadoraViaje())));
    }
}