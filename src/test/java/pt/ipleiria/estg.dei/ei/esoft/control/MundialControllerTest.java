package pt.ipleiria.estg.dei.ei.esoft.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.model.Selecao;



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



    @Test
    public void testConsultarInformacaoSelecaoPortugal() {
        List<Selecao> selecoes = controller.getSelecoesParticipantes();

        Selecao portugal = null;

        for (Selecao selecao : selecoes) {
            if (selecao.getPais().equalsIgnoreCase("Portugal")) {
                portugal = selecao;
                break;
            }
        }

        assertNotNull(portugal);
        assertEquals("Portugal", portugal.getPais());
        assertEquals("K", portugal.getGrupo());
        assertTrue(portugal.getRanking() > 0);
        assertTrue(portugal.getParticipacoes() > 0);
        assertFalse(portugal.getJogadores().isEmpty());
    }

    @Test
    public void testPesquisarSelecaoPorNome() {
        List<Selecao> resultado = controller.pesquisarSelecoes("Portugal");

        assertFalse(resultado.isEmpty());
        assertEquals("Portugal", resultado.get(0).getPais());
    }

    @Test
    public void testPesquisarSelecaoInexistenteRetornaListaVazia() {
        List<Selecao> resultado = controller.pesquisarSelecoes("Atlantida");

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void testAtualizarEstadiaDaSelecao() {
        Selecao portugal = obterSelecaoPorNome("Portugal");

        controller.atualizarEstadia(
                portugal,
                "Cidade do Futebol",
                "Hotel Lisboa"
        );

        assertNotNull(portugal.getEstadia());
        assertEquals("Cidade do Futebol", portugal.getEstadia().getCentroTreino());
        assertEquals("Hotel Lisboa", portugal.getEstadia().getHotel());
    }

    @Test
    public void testAtualizarEstadiaComCentroTreinoVazioLancaErro() {
        Selecao portugal = obterSelecaoPorNome("Portugal");

        assertThrows(IllegalArgumentException.class, () -> {
            controller.atualizarEstadia(
                    portugal,
                    "",
                    "Hotel Lisboa"
            );
        });
    }

    @Test
    public void testAdicionarJogadorAoPlantel() {
        Selecao portugal = obterSelecaoPorNome("Portugal");

        int totalAntes = portugal.getJogadores().size();

        Jogador jogador = new Jogador(99, "Jogador Teste", "AV");

        controller.adicionarJogador(portugal, jogador);

        assertEquals(totalAntes + 1, portugal.getJogadores().size());
        assertTrue(existeJogadorComNumero(portugal, 99));
    }

    @Test
    public void testAdicionarJogadorComNumeroRepetidoLancaErro() {
        Selecao portugal = obterSelecaoPorNome("Portugal");

        int numeroExistente = portugal.getJogadores().get(0).getNumero();

        Jogador jogadorRepetido = new Jogador(
                numeroExistente,
                "Jogador Repetido",
                "MED"
        );

        assertThrows(IllegalArgumentException.class, () -> {
            controller.adicionarJogador(portugal, jogadorRepetido);
        });
    }

    @Test
    public void testRemoverJogadorDoPlantel() {
        Selecao portugal = obterSelecaoPorNome("Portugal");

        Jogador jogador = new Jogador(98, "Jogador Para Remover", "DEF");
        controller.adicionarJogador(portugal, jogador);

        assertTrue(existeJogadorComNumero(portugal, 98));

        controller.removerJogador(portugal, 98);

        assertFalse(existeJogadorComNumero(portugal, 98));
    }
    private Selecao obterSelecaoPorNome(String nome) {
        for (Selecao selecao : controller.getSelecoesParticipantes()) {
            if (selecao.getPais().equalsIgnoreCase(nome)) {
                return selecao;
            }
        }

        fail("Seleção não encontrada: " + nome);
        return null;
    }

    private boolean existeJogadorComNumero(Selecao selecao, int numero) {
        for (Jogador jogador : selecao.getJogadores()) {
            if (jogador.getNumero() == numero) {
                return true;
            }
        }

        return false;
    }
}