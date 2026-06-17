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

    public List<Arbitro> getArbitrosDisponiveisParaData(Jogo jogoAtual) {
        List<Arbitro> arbitrosDisponiveis = new ArrayList<>(getArbitrosDisponiveis());

        if (jogoAtual == null || jogoAtual.getData() == null) {
            return arbitrosDisponiveis;
        }

        try {
            java.time.format.DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("EEEE d MMMM yyyy")
                    .toFormatter(new java.util.Locale("pt", "PT"));

            java.time.LocalDate dataJogoAtual = java.time.LocalDate.parse(jogoAtual.getData(), formatter);

            for (Jogo outroJogo : getCalendarioJogos()) {
                // CORREÇÃO CRUCIAL: Se for o PRÓPRIO jogo que estamos a editar/consultar,
                // ignoramos a validação para que os árbitros já escalados continuem disponíveis nos combos!
                if (outroJogo.equals(jogoAtual)) {
                    continue;
                }

                if (outroJogo.getEquipaArbitragem() == null || outroJogo.getEquipaArbitragem().isEmpty()) {
                    continue;
                }

                java.time.LocalDate dataOutroJogo = java.time.LocalDate.parse(outroJogo.getData(), formatter);
                long diferencaDias = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(dataJogoAtual, dataOutroJogo));

                // Regra das 72 horas (menos de 3 dias de intervalo em OUTROS jogos)
                if (diferencaDias < 3) {
                    for (Arbitro arbitroOcupado : outroJogo.getEquipaArbitragem()) {
                        arbitrosDisponiveis.remove(arbitroOcupado);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao validar intervalo de 72h: " + e.getMessage());
        }

        return arbitrosDisponiveis;
    }
}
