package pt.ipleiria.estg.dei.ei.esoft.control;

import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import java.util.ArrayList;
import java.util.List;

public class MundialController {
    private List<Jogo> calendarioJogos;
    private List<Arbitro> arbitrosDisponiveis;

    public MundialController() {
        this.calendarioJogos = new ArrayList<>();
        this.arbitrosDisponiveis = new ArrayList<>();
    }

    // Métodos para gerir dados de teste (Serão úteis para a View e para os Testes Unitários)
    public void adicionarJogo(Jogo jogo) {
        this.calendarioJogos.add(jogo);
    }

    public void adicionarArbitro(Arbitro arbitro) {
        this.arbitrosDisponiveis.add(arbitro);
    }

    public List<Jogo> getCalendarioJogos() {
        return new ArrayList<>(calendarioJogos);
    }

    public List<Arbitro> getArbitrosDisponiveis() {
        return new ArrayList<>(arbitrosDisponiveis);
    }

    /**
     * UC: Alocar Equipa de Arbitragem
     * Valida e aloca uma lista de exatamente 4 árbitros a um jogo específico.
     */
    public boolean alocarEquipaArbitragem(Jogo jogo, List<Arbitro> equipa) {
        // 1. Validação de Lotação: Devem ser exatamente 4 árbitros
        if (equipa == null || equipa.size() != 4) {
            throw new IllegalArgumentException("A equipa de arbitragem deve ter exatamente 4 membros.");
        }

        for (Arbitro arbitro : equipa) {
            // 2. Validação de Nacionalidade: Não pode ser da mesma nacionalidade das seleções do jogo
            if (arbitro.getNacionalidade().equalsIgnoreCase(jogo.getNacionalidadeSelecaoA()) ||
                    arbitro.getNacionalidade().equalsIgnoreCase(jogo.getNacionalidadeSelecaoB())) {
                throw new IllegalArgumentException("O árbitro " + arbitro.getNome() +
                        " não pode apitar este jogo porque tem a mesma nacionalidade de uma das seleções.");
            }
        }

        // Se passar as validações, aloca a equipa ao jogo
        jogo.setEquipaArbitragem(equipa);
        return true;
    }

    /**
     * UC: Consultar Calendário de Jogos
     * Permite filtrar os jogos por uma determinada fase (ex: "Fase de Grupos", "Quartos de Final")
     */
    public List<Jogo> consultarJogosPorFase(String fase) {
        List<Jogo> filtrados = new ArrayList<>();
        for (Jogo jogo : calendarioJogos) {
            if (jogo.getFase().equalsIgnoreCase(fase)) {
                filtrados.add(jogo);
            }
        }
        return filtrados;
    }
}
