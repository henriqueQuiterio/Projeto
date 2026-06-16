package pt.ipleiria.estg.dei.ei.esoft.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MundialControllerTest {
    private MundialController controller;
    private Jogo jogoPortugalEspanha;

    @BeforeEach
    public void setUp() {
        controller = new MundialController();
        // Criar um jogo de teste: Portugal vs Espanha
        jogoPortugalEspanha = new Jogo(
                "15/06/2026", "20:00", "GMT+1", "Fase de Grupos", "Grupo A",
                "Estádio Alvalade", "Lisboa",
                "Portugal", "POR", "Portuguesa",
                "Espanha", "ESP", "Espanhola"
        );
    }

    @Test
    public void testAlocarEquipaComSucesso() {
        List<Arbitro> equipaValida = new ArrayList<>();
        equipaValida.add(new Arbitro("John Doe", "Inglesa", "Principal"));
        equipaValida.add(new Arbitro("Jane Doe", "Francesa", "Assistente"));
        equipaValida.add(new Arbitro("Pierre", "Francesa", "Assistente"));
        equipaValida.add(new Arbitro("Anssi", "Finlandesa", "VAR"));

        boolean resultado = controller.alocarEquipaArbitragem(jogoPortugalEspanha, equipaValida);

        assertTrue(resultado);
        assertEquals(4, jogoPortugalEspanha.getEquipaArbitragem().size());
    }

    @Test
    public void testInsucessoArbitroComMesmaNacionalidadeDumaSelecao() {
        List<Arbitro> equipaInvalida = new ArrayList<>();
        equipaInvalida.add(new Arbitro("Manuel Silva", "Portuguesa", "Principal")); // Inválido: mesma nacionalidade de Portugal
        equipaInvalida.add(new Arbitro("Jane Doe", "Francesa", "Assistente"));
        equipaInvalida.add(new Arbitro("Pierre", "Francesa", "Assistente"));
        equipaInvalida.add(new Arbitro("Anssi", "Finlandesa", "VAR"));

        assertThrows(IllegalArgumentException.class, () -> {
            controller.alocarEquipaArbitragem(jogoPortugalEspanha, equipaInvalida);
        });
    }

    @Test
    public void testInsucessoEquipaComMenosDeQuatroArbitros() {
        List<Arbitro> equipaIncompleta = new ArrayList<>();
        equipaIncompleta.add(new Arbitro("John Doe", "Inglesa", "Principal"));

        assertThrows(IllegalArgumentException.class, () -> {
            controller.alocarEquipaArbitragem(jogoPortugalEspanha, equipaIncompleta);
        });
    }
}