package pt.ipleiria.estg.dei.ei.esoft.control;

import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Estadia;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.model.Selecao;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MundialController {
    private List<Jogo> calendarioJogos;
    private List<Arbitro> arbitrosDisponiveis;
    private List<Selecao> selecoesParticipantes;

    public MundialController() {
        this.calendarioJogos = new ArrayList<>();
        this.arbitrosDisponiveis = new ArrayList<>();
        this.selecoesParticipantes = new ArrayList<>();
    }

    // =========================
    // Gestão de Seleções
    // =========================

    public List<Selecao> getSelecoesParticipantes() {
        return new ArrayList<>(selecoesParticipantes);
    }

    public List<Selecao> getSelecoes() {
        return getSelecoesParticipantes();
    }

    public List<Selecao> pesquisarSelecoes(String termo) {
        return pesquisarSelecoes(this.selecoesParticipantes, termo);
    }

    public List<Selecao> pesquisarSelecoes(List<Selecao> selecoes, String termo) {
        if (selecoes == null) {
            throw new IllegalArgumentException("A lista de seleções não pode ser nula.");
        }

        if (termo == null || termo.isBlank() || termo.equalsIgnoreCase("Procurar País...")) {
            return new ArrayList<>(selecoes);
        }

        List<Selecao> resultado = new ArrayList<>();
        String termoNormalizado = termo.toLowerCase();

        for (Selecao selecao : selecoes) {
            if (selecao.getPais().toLowerCase().contains(termoNormalizado)) {
                resultado.add(selecao);
            }
        }

        return resultado;
    }

    public void atualizarEstadia(Selecao selecao, String centroTreino, String hotel) {
        validarSelecao(selecao);

        if (centroTreino == null || centroTreino.isBlank()) {
            throw new IllegalArgumentException("O centro de treino é obrigatório.");
        }

        if (hotel == null || hotel.isBlank()) {
            throw new IllegalArgumentException("O hotel é obrigatório.");
        }

        Estadia estadia = selecao.getEstadia();

        if (estadia == null) {
            estadia = new Estadia(centroTreino, hotel);
            selecao.setEstadia(estadia);
        } else {
            estadia.setCentroTreino(centroTreino);
            estadia.setHotel(hotel);
        }
    }

    public void adicionarJogador(Selecao selecao, Jogador jogador) {
        validarSelecao(selecao);
        validarJogador(jogador);

        if (existeNumeroJogador(selecao, jogador.getNumero())) {
            throw new IllegalArgumentException("Já existe um jogador com esse número nesta seleção.");
        }

        selecao.getJogadores().add(jogador);
    }

    public void removerJogador(Selecao selecao, int numero) {
        validarSelecao(selecao);

        Jogador jogadorRemover = null;

        for (Jogador jogador : selecao.getJogadores()) {
            if (jogador.getNumero() == numero) {
                jogadorRemover = jogador;
                break;
            }
        }

        if (jogadorRemover == null) {
            throw new IllegalArgumentException("Não existe nenhum jogador com esse número.");
        }

        selecao.getJogadores().remove(jogadorRemover);
    }

    private boolean existeNumeroJogador(Selecao selecao, int numero) {
        for (Jogador jogador : selecao.getJogadores()) {
            if (jogador.getNumero() == numero) {
                return true;
            }
        }

        return false;
    }

    private void validarSelecao(Selecao selecao) {
        if (selecao == null) {
            throw new IllegalArgumentException("A seleção não pode ser nula.");
        }

        if (selecao.getJogadores() == null) {
            throw new IllegalArgumentException("A lista de jogadores da seleção não pode ser nula.");
        }
    }

    private void validarJogador(Jogador jogador) {
        if (jogador == null) {
            throw new IllegalArgumentException("O jogador não pode ser nulo.");
        }

        if (jogador.getNumero() <= 0) {
            throw new IllegalArgumentException("O número do jogador tem de ser positivo.");
        }

        if (jogador.getNome() == null || jogador.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do jogador é obrigatório.");
        }

        if (jogador.getPosicao() == null || jogador.getPosicao().isBlank()) {
            throw new IllegalArgumentException("A posição do jogador é obrigatória.");
        }
    }

    public List<Jogador> getTopMelhoresMarcadores() {
        List<Jogador> todosOsJogadores = new ArrayList<>();

        // Agrupar todos os jogadores de todas as seleções numa lista única
        for (Selecao s : selecoesParticipantes) {
            if (s.getJogadores() != null) {
                todosOsJogadores.addAll(s.getJogadores());
            }
        }

        // Ordenar por ordem decrescente de golos e colher os 5 melhores
        return todosOsJogadores.stream()
                .filter(j -> j.getGolos() > 0)
                .sorted((j1, j2) -> Integer.compare(j2.getGolos(), j1.getGolos()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
    }


    public List<String[]> getMelhoresMarcadoresPorEventos() {
        return obterRankingEventos(true);
    }

    public List<String[]> getLideresAssistenciasPorEventos() {
        return obterRankingEventos(false);
    }

    private List<String[]> obterRankingEventos(boolean contarGolos) {
        Map<String, Integer> contagem = new HashMap<>();

        for (Jogo jogo : calendarioJogos) {
            if (jogo == null) {
                continue;
            }

            // Fonte principal: eventos guardados na aba Resultados.
            if (jogo.getEventosDoJogo() != null) {
                for (String[] evento : jogo.getEventosDoJogo()) {
                    if (evento == null || evento.length < 3) {
                        continue;
                    }

                    String tipo = evento[1];
                    String detalhes = evento[2];

                    if (!"Golo".equalsIgnoreCase(tipo)) {
                        continue;
                    }

                    String jogador;

                    if (contarGolos) {
                        jogador = extrairMarcador(detalhes);
                    } else {
                        jogador = extrairAssistente(detalhes);
                    }

                    adicionarAoRanking(contagem, jogador);
                }
            }

            // Compatibilidade com a estrutura antiga do Jogo, caso algum dia seja usada.
            if (contarGolos && jogo.getAutoresGolos() != null) {
                for (String marcador : jogo.getAutoresGolos()) {
                    adicionarAoRanking(contagem, marcador);
                }
            }

            if (!contarGolos && jogo.getAutoresAssistencias() != null) {
                for (String assistente : jogo.getAutoresAssistencias()) {
                    adicionarAoRanking(contagem, assistente);
                }
            }
        }

        List<Map.Entry<String, Integer>> entradas = new ArrayList<>(contagem.entrySet());

        entradas.sort(
                Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(Map.Entry::getKey)
        );

        List<String[]> resultado = new ArrayList<>();
        int posicao = 1;

        for (Map.Entry<String, Integer> entrada : entradas) {
            if (posicao > 5) {
                break;
            }

            String jogadorCompleto = entrada.getKey();

            resultado.add(new String[]{
                    posicao + ". " + extrairNomeJogador(jogadorCompleto),
                    extrairSelecaoJogador(jogadorCompleto),
                    String.valueOf(entrada.getValue())
            });

            posicao++;
        }

        return resultado;
    }

    private void adicionarAoRanking(Map<String, Integer> contagem, String jogador) {
        if (jogador == null) {
            return;
        }

        jogador = jogador.trim();

        if (jogador.isBlank() || jogador.equalsIgnoreCase("Sem assistência")) {
            return;
        }

        contagem.put(jogador, contagem.getOrDefault(jogador, 0) + 1);
    }

    private String extrairMarcador(String detalhes) {
        if (detalhes == null || detalhes.isBlank()) {
            return "";
        }

        int indice = detalhes.indexOf("| Assist:");

        if (indice == -1) {
            return detalhes.trim();
        }

        return detalhes.substring(0, indice).trim();
    }

    private String extrairAssistente(String detalhes) {
        if (detalhes == null || detalhes.isBlank()) {
            return "";
        }

        String marcador = "| Assist:";
        int indice = detalhes.indexOf(marcador);

        if (indice == -1) {
            return "";
        }

        return detalhes.substring(indice + marcador.length()).trim();
    }

    private String extrairNomeJogador(String jogadorCompleto) {
        if (jogadorCompleto == null) {
            return "";
        }

        int indice = jogadorCompleto.lastIndexOf(" (");

        if (indice == -1) {
            return jogadorCompleto.trim();
        }

        return jogadorCompleto.substring(0, indice).trim();
    }

    private String extrairSelecaoJogador(String jogadorCompleto) {
        if (jogadorCompleto == null) {
            return "";
        }

        int inicio = jogadorCompleto.lastIndexOf("(");
        int fim = jogadorCompleto.lastIndexOf(")");

        if (inicio == -1 || fim == -1 || fim <= inicio) {
            return "";
        }

        return jogadorCompleto.substring(inicio + 1, fim).trim();
    }

    // =========================
    // Gestão de Calendário / Arbitragem
    // =========================

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

    public boolean alocarEquipaArbitragem(Jogo jogo, List<Arbitro> equipa) {
        if (equipa == null || equipa.size() != 4) {
            throw new IllegalArgumentException("A equipa de arbitragem deve ter exatamente 4 membros.");
        }

        for (Arbitro arbitro : equipa) {
            if (arbitro.getNacionalidade().equalsIgnoreCase(jogo.getNacionalidadeSelecaoA()) ||
                    arbitro.getNacionalidade().equalsIgnoreCase(jogo.getNacionalidadeSelecaoB())) {
                throw new IllegalArgumentException("O árbitro " + arbitro.getNome() +
                        " não pode apitar este jogo porque tem a mesma nacionalidade de uma das seleções.");
            }
        }

        jogo.setEquipaArbitragem(equipa);
        return true;
    }

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
                if (outroJogo.equals(jogoAtual)) {
                    continue;
                }

                if (outroJogo.getEquipaArbitragem() == null || outroJogo.getEquipaArbitragem().isEmpty()) {
                    continue;
                }

                java.time.LocalDate dataOutroJogo = java.time.LocalDate.parse(outroJogo.getData(), formatter);
                long diferencaDias = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(dataJogoAtual, dataOutroJogo));

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

    public void guardarDados() {
        System.out.println("A tentar gravar dados... Jogos em memória: " +
                (this.calendarioJogos != null ? this.calendarioJogos.size() : 0));

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("dados_mundial.dat"))) {
            oos.writeObject(this.calendarioJogos);
            oos.writeObject(this.arbitrosDisponiveis);
            oos.writeObject(this.selecoesParticipantes);
            System.out.println("Ficheiro dados_mundial.dat guardado fisicamente com sucesso!");
        } catch (IOException e) {
            System.out.println("ERRO CRÍTICO AO GRAVAR FICHEIRO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void carregarDados() {
        File ficheiro = new File("dados_mundial.dat");
        if (!ficheiro.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ficheiro))) {
            List<Jogo> jogosCarregados = (List<Jogo>) ois.readObject();
            List<Arbitro> arbitrosCarregados = (List<Arbitro>) ois.readObject();

            if (jogosCarregados != null && !jogosCarregados.isEmpty()) {
                this.calendarioJogos.clear();
                this.calendarioJogos.addAll(jogosCarregados);
                System.out.println("✓ SUCESSO: " + this.calendarioJogos.size() + " jogos restaurados na variável privada!");
            }

            if (arbitrosCarregados != null && !arbitrosCarregados.isEmpty()) {
                this.arbitrosDisponiveis.clear();
                this.arbitrosDisponiveis.addAll(arbitrosCarregados);
            }

            try {
                List<Selecao> selecoesCarregadas = (List<Selecao>) ois.readObject();
                if (selecoesCarregadas != null && !selecoesCarregadas.isEmpty()) {
                    this.selecoesParticipantes.clear();
                    this.selecoesParticipantes.addAll(selecoesCarregadas);
                }
            } catch (EOFException ignored) {
                // Compatibilidade com ficheiros antigos que só tinham jogos e árbitros.
            }

        } catch (Exception e) {
            System.out.println("Erro na leitura física: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Selecao> criarSelecoesMundial2026() {
        List<Selecao> selecoes = new ArrayList<>();

        Selecao cze = criarSelecao("Chéquia", 39, 10, "A");
        cadastrarJogadorInicial(cze, 1, "Matej Kovar", "GR");
        cadastrarJogadorInicial(cze, 2, "David Zima", "DEF");
        cadastrarJogadorInicial(cze, 3, "Tomas Holes", "DEF");
        cadastrarJogadorInicial(cze, 4, "Robin Hranac", "DEF");
        cadastrarJogadorInicial(cze, 5, "Vladimir Coufal", "DEF");
        cadastrarJogadorInicial(cze, 6, "Stepan Chaloupek", "DEF");
        cadastrarJogadorInicial(cze, 7, "Ladislav Krejci", "DEF");
        cadastrarJogadorInicial(cze, 8, "Vladimir Darida", "MED");
        cadastrarJogadorInicial(cze, 9, "Adam Hlozek", "AV");
        cadastrarJogadorInicial(cze, 10, "Patrik Schick", "AV");
        cadastrarJogadorInicial(cze, 11, "Jan Kuchta", "AV");
        cadastrarJogadorInicial(cze, 12, "Lukas Cerv", "MED");
        cadastrarJogadorInicial(cze, 13, "Mojmir Chytil", "AV");
        cadastrarJogadorInicial(cze, 14, "David Jurasek", "DEF");
        cadastrarJogadorInicial(cze, 15, "Pavel Sulc", "AV");
        cadastrarJogadorInicial(cze, 16, "Jindrich Stanek", "GR");
        cadastrarJogadorInicial(cze, 17, "Lukas Provod", "MED");
        cadastrarJogadorInicial(cze, 18, "Michal Sadilek", "MED");
        cadastrarJogadorInicial(cze, 19, "Tomas Chory", "AV");
        cadastrarJogadorInicial(cze, 20, "Jaroslav Zeleny", "DEF");
        cadastrarJogadorInicial(cze, 21, "David Doudera", "DEF");
        cadastrarJogadorInicial(cze, 22, "Tomas Soucek", "MED");
        cadastrarJogadorInicial(cze, 23, "Lukas Hornicek", "GR");
        cadastrarJogadorInicial(cze, 24, "Alexandr Sojka", "MED");
        cadastrarJogadorInicial(cze, 25, "Hugo Sochurek", "MED");
        cadastrarJogadorInicial(cze, 26, "Denis Visinsky", "AV");
        selecoes.add(cze);

        Selecao kor = criarSelecao("Coreia do Sul", 23, 12, "A");
        cadastrarJogadorInicial(kor, 1, "Seunggyu Kim", "GR");
        cadastrarJogadorInicial(kor, 2, "Hanbeom Lee", "DEF");
        cadastrarJogadorInicial(kor, 3, "Gihyuk Lee", "MED");
        cadastrarJogadorInicial(kor, 4, "Minjae Kim", "DEF");
        cadastrarJogadorInicial(kor, 5, "Taehyeon Kim", "DEF");
        cadastrarJogadorInicial(kor, 6, "Inbeom Hwang", "MED");
        cadastrarJogadorInicial(kor, 7, "Heungmin Son", "AV");
        cadastrarJogadorInicial(kor, 8, "Seungho Paik", "MED");
        cadastrarJogadorInicial(kor, 9, "Guesung Cho", "AV");
        cadastrarJogadorInicial(kor, 10, "Jaesung Lee", "MED");
        cadastrarJogadorInicial(kor, 11, "Heechan Hwang", "MED");
        cadastrarJogadorInicial(kor, 12, "Bumkeun Song", "GR");
        cadastrarJogadorInicial(kor, 13, "Taeseok Lee", "DEF");
        cadastrarJogadorInicial(kor, 14, "Wije Cho", "DEF");
        cadastrarJogadorInicial(kor, 15, "Moonhwan Kim", "DEF");
        cadastrarJogadorInicial(kor, 16, "Jinseob Park", "DEF");
        cadastrarJogadorInicial(kor, 17, "Junho Bae", "MED");
        cadastrarJogadorInicial(kor, 18, "Hyeongyu Oh", "AV");
        cadastrarJogadorInicial(kor, 19, "Kangin Lee", "MED");
        cadastrarJogadorInicial(kor, 20, "Hyunjun Yang", "MED");
        cadastrarJogadorInicial(kor, 21, "Hyeonwoo Jo", "GR");
        cadastrarJogadorInicial(kor, 22, "Youngwoo Seol", "DEF");
        cadastrarJogadorInicial(kor, 23, "Jens Castrop", "DEF");
        cadastrarJogadorInicial(kor, 24, "Jingyu Kim", "MED");
        cadastrarJogadorInicial(kor, 25, "Jisung Eom", "MED");
        cadastrarJogadorInicial(kor, 26, "Donggyeong Lee", "MED");
        selecoes.add(kor);

        Selecao mex = criarSelecao("México", 15, 18, "A");
        cadastrarJogadorInicial(mex, 1, "Raul Rangel", "GR");
        cadastrarJogadorInicial(mex, 2, "Jorge Sanchez", "DEF");
        cadastrarJogadorInicial(mex, 3, "Cesar Montes", "DEF");
        cadastrarJogadorInicial(mex, 4, "Edson Alvarez", "DEF");
        cadastrarJogadorInicial(mex, 5, "Johan Vasquez", "DEF");
        cadastrarJogadorInicial(mex, 6, "Erik Lira", "MED");
        cadastrarJogadorInicial(mex, 7, "Luis Romo", "MED");
        cadastrarJogadorInicial(mex, 8, "Alvaro Fidalgo", "MED");
        cadastrarJogadorInicial(mex, 9, "Raul Jimenez", "AV");
        cadastrarJogadorInicial(mex, 10, "Alexis Vega", "AV");
        cadastrarJogadorInicial(mex, 11, "Santiago Gimenez", "AV");
        cadastrarJogadorInicial(mex, 12, "Carlos Acevedo", "GR");
        cadastrarJogadorInicial(mex, 13, "Guillermo Ochoa", "GR");
        cadastrarJogadorInicial(mex, 14, "Armando Gonzalez", "AV");
        cadastrarJogadorInicial(mex, 15, "Israel Reyes", "DEF");
        cadastrarJogadorInicial(mex, 16, "Julian Quinones", "AV");
        cadastrarJogadorInicial(mex, 17, "Orbelin Pineda", "MED");
        cadastrarJogadorInicial(mex, 18, "Obed Vargas", "MED");
        cadastrarJogadorInicial(mex, 19, "Gilberto Mora", "MED");
        cadastrarJogadorInicial(mex, 20, "Mateo Chavez", "DEF");
        cadastrarJogadorInicial(mex, 21, "Cesar Huerta", "AV");
        cadastrarJogadorInicial(mex, 22, "Guillermo Martinez", "AV");
        cadastrarJogadorInicial(mex, 23, "Jesus Gallardo", "DEF");
        cadastrarJogadorInicial(mex, 24, "Luis Chavez", "MED");
        cadastrarJogadorInicial(mex, 25, "Roberto Alvarado", "AV");
        cadastrarJogadorInicial(mex, 26, "Brian Gutierrez", "MED");
        selecoes.add(mex);

        Selecao rsa = criarSelecao("África do Sul", 59, 4, "A");
        cadastrarJogadorInicial(rsa, 1, "Ronwen Williams", "GR");
        cadastrarJogadorInicial(rsa, 2, "Thabang Matuludi", "DEF");
        cadastrarJogadorInicial(rsa, 3, "Khulumani Ndamane", "DEF");
        cadastrarJogadorInicial(rsa, 4, "Teboho Mokoena", "MED");
        cadastrarJogadorInicial(rsa, 5, "Thalente Mbatha", "MED");
        cadastrarJogadorInicial(rsa, 6, "Aubrey Modiba", "DEF");
        cadastrarJogadorInicial(rsa, 7, "Oswin Appollis", "AV");
        cadastrarJogadorInicial(rsa, 8, "Tshepang Moremi", "AV");
        cadastrarJogadorInicial(rsa, 9, "Lyle Foster", "AV");
        cadastrarJogadorInicial(rsa, 10, "Relebohile Mofokeng", "AV");
        cadastrarJogadorInicial(rsa, 11, "Themba Zwane", "MED");
        cadastrarJogadorInicial(rsa, 12, "Thapelo Maseko", "AV");
        cadastrarJogadorInicial(rsa, 13, "Sphephelo Sithole", "MED");
        cadastrarJogadorInicial(rsa, 14, "Mbekezeli Mbokazi", "DEF");
        cadastrarJogadorInicial(rsa, 15, "Iqraam Rayners", "AV");
        cadastrarJogadorInicial(rsa, 16, "Sipho Chaine", "GR");
        cadastrarJogadorInicial(rsa, 17, "Evidence Makgopa", "AV");
        cadastrarJogadorInicial(rsa, 18, "Samukele Kabini", "DEF");
        cadastrarJogadorInicial(rsa, 19, "Nkosinathi Sibisi", "DEF");
        cadastrarJogadorInicial(rsa, 20, "Khuliso Mudau", "DEF");
        cadastrarJogadorInicial(rsa, 21, "Ime Okon", "DEF");
        cadastrarJogadorInicial(rsa, 22, "Ricardo Goss", "GR");
        cadastrarJogadorInicial(rsa, 23, "Jayden Adams", "MED");
        cadastrarJogadorInicial(rsa, 24, "Olwethu Makhanya", "DEF");
        cadastrarJogadorInicial(rsa, 25, "Kamogelo Sebelebele", "AV");
        cadastrarJogadorInicial(rsa, 26, "Bradley Cross", "DEF");
        selecoes.add(rsa);

        Selecao bih = criarSelecao("Bósnia e Herzegovina", 69, 2, "B");
        cadastrarJogadorInicial(bih, 1, "Nikola Vasilj", "GR");
        cadastrarJogadorInicial(bih, 2, "Nihad Mujakic", "DEF");
        cadastrarJogadorInicial(bih, 3, "Dennis Hadzikadunic", "DEF");
        cadastrarJogadorInicial(bih, 4, "Tarik Muharemovic", "DEF");
        cadastrarJogadorInicial(bih, 5, "Sead Kolasinac", "DEF");
        cadastrarJogadorInicial(bih, 6, "Benjamin Tahirovic", "MED");
        cadastrarJogadorInicial(bih, 7, "Amar Dedic", "DEF");
        cadastrarJogadorInicial(bih, 8, "Armin Gigovic", "MED");
        cadastrarJogadorInicial(bih, 9, "Samed Bazdar", "AV");
        cadastrarJogadorInicial(bih, 10, "Ermedin Demirovic", "AV");
        cadastrarJogadorInicial(bih, 11, "Edin Dzeko", "AV");
        cadastrarJogadorInicial(bih, 12, "Mladen Jurkas", "GR");
        cadastrarJogadorInicial(bih, 13, "Ivan Basic", "MED");
        cadastrarJogadorInicial(bih, 14, "Ivan Sunjic", "MED");
        cadastrarJogadorInicial(bih, 15, "Amar Memic", "MED");
        cadastrarJogadorInicial(bih, 16, "Amir Hadziahmetovic", "MED");
        cadastrarJogadorInicial(bih, 17, "Dzenis Burnic", "MED");
        cadastrarJogadorInicial(bih, 18, "Nikola Katic", "DEF");
        cadastrarJogadorInicial(bih, 19, "Kerim Alajbegovic", "AV");
        cadastrarJogadorInicial(bih, 20, "Esmir Bajraktarevic", "AV");
        cadastrarJogadorInicial(bih, 21, "Stjepan Radeljic", "DEF");
        cadastrarJogadorInicial(bih, 22, "Martin Zlomislic", "GR");
        cadastrarJogadorInicial(bih, 23, "Haris Tabakovic", "AV");
        cadastrarJogadorInicial(bih, 24, "Arjan Malic", "DEF");
        cadastrarJogadorInicial(bih, 25, "Jovo Lukic", "AV");
        cadastrarJogadorInicial(bih, 26, "Ermin Mahmic", "MED");
        selecoes.add(bih);

        Selecao can = criarSelecao("Canadá", 48, 3, "B");
        cadastrarJogadorInicial(can, 1, "Dayne St. Clair", "GR");
        cadastrarJogadorInicial(can, 2, "Alistair Johnston", "DEF");
        cadastrarJogadorInicial(can, 3, "Al E Jones", "DEF");
        cadastrarJogadorInicial(can, 4, "Luc De Fougerolles", "DEF");
        cadastrarJogadorInicial(can, 5, "Joel Waterman", "DEF");
        cadastrarJogadorInicial(can, 6, "Mathieu Choiniere", "MED");
        cadastrarJogadorInicial(can, 7, "Stephen Eustaquio", "MED");
        cadastrarJogadorInicial(can, 8, "Ismael Kone", "MED");
        cadastrarJogadorInicial(can, 9, "Cyle Larin", "AV");
        cadastrarJogadorInicial(can, 10, "Jonathan David", "AV");
        cadastrarJogadorInicial(can, 11, "Liam Millar", "MED");
        cadastrarJogadorInicial(can, 12, "Tani Oluwaseyi", "AV");
        cadastrarJogadorInicial(can, 13, "Derek Cornelius", "DEF");
        cadastrarJogadorInicial(can, 14, "Jacob Shaffelburg", "MED");
        cadastrarJogadorInicial(can, 15, "Moise Bombito", "DEF");
        cadastrarJogadorInicial(can, 16, "Maxime Crepeau", "GR");
        cadastrarJogadorInicial(can, 17, "Tajon Buchanan", "AV");
        cadastrarJogadorInicial(can, 18, "Owen Goodman", "GR");
        cadastrarJogadorInicial(can, 19, "Alphonso Davies", "DEF");
        cadastrarJogadorInicial(can, 20, "Ali Ahmed", "AV");
        cadastrarJogadorInicial(can, 21, "Jonathan Osorio", "MED");
        cadastrarJogadorInicial(can, 22, "Richie Laryea", "DEF");
        cadastrarJogadorInicial(can, 23, "Niko Sigur", "DEF");
        cadastrarJogadorInicial(can, 24, "Promise David", "AV");
        cadastrarJogadorInicial(can, 25, "Nathan Saliba", "MED");
        cadastrarJogadorInicial(can, 26, "Jayden Nelson", "AV");
        selecoes.add(can);

        Selecao qat = criarSelecao("Catar", 53, 2, "B");
        cadastrarJogadorInicial(qat, 1, "Mahmoud Abunada", "GR");
        cadastrarJogadorInicial(qat, 2, "Pedro Miguel", "DEF");
        cadastrarJogadorInicial(qat, 3, "Lucas Mendes", "DEF");
        cadastrarJogadorInicial(qat, 4, "Issa Laye", "DEF");
        cadastrarJogadorInicial(qat, 5, "Jassem Gaber", "DEF");
        cadastrarJogadorInicial(qat, 6, "Abdulaziz Hatem", "MED");
        cadastrarJogadorInicial(qat, 7, "Ahmed Alaaeldin", "AV");
        cadastrarJogadorInicial(qat, 8, "Edmilson Junior", "AV");
        cadastrarJogadorInicial(qat, 9, "Mohammed Muntari", "AV");
        cadastrarJogadorInicial(qat, 10, "Hassan Alhaydos", "AV");
        cadastrarJogadorInicial(qat, 11, "Akram Afif", "AV");
        cadastrarJogadorInicial(qat, 12, "Karim Boudiaf", "MED");
        cadastrarJogadorInicial(qat, 13, "Ayoub Aloui", "DEF");
        cadastrarJogadorInicial(qat, 14, "Homam Ahmed", "DEF");
        cadastrarJogadorInicial(qat, 15, "Yusuf Abdurisag", "AV");
        cadastrarJogadorInicial(qat, 16, "Boualem Khoukhi", "DEF");
        cadastrarJogadorInicial(qat, 17, "Ahmed Alganehi", "MED");
        cadastrarJogadorInicial(qat, 18, "Sultan Albrake", "DEF");
        cadastrarJogadorInicial(qat, 19, "Almoez Ali", "AV");
        cadastrarJogadorInicial(qat, 20, "Ahmed Fathy", "MED");
        cadastrarJogadorInicial(qat, 21, "Salah Zakaria", "GR");
        cadastrarJogadorInicial(qat, 22, "Meshaal Barsham", "GR");
        cadastrarJogadorInicial(qat, 23, "Assim Madibo", "MED");
        cadastrarJogadorInicial(qat, 24, "Tahsin Mohammed", "AV");
        cadastrarJogadorInicial(qat, 25, "Alhashmi Alhussein", "DEF");
        cadastrarJogadorInicial(qat, 26, "Mohamed Manai", "AV");
        selecoes.add(qat);

        Selecao sui = criarSelecao("Suíça", 19, 13, "B");
        cadastrarJogadorInicial(sui, 1, "Gregor Kobel", "GR");
        cadastrarJogadorInicial(sui, 2, "Miro Muheim", "DEF");
        cadastrarJogadorInicial(sui, 3, "Silvan Widmer", "DEF");
        cadastrarJogadorInicial(sui, 4, "Nico Elvedi", "DEF");
        cadastrarJogadorInicial(sui, 5, "Manuel Akanji", "DEF");
        cadastrarJogadorInicial(sui, 6, "Denis Zakaria", "MED");
        cadastrarJogadorInicial(sui, 7, "Breel Embolo", "AV");
        cadastrarJogadorInicial(sui, 8, "Remo Freuler", "MED");
        cadastrarJogadorInicial(sui, 9, "Johan Manzambi", "MED");
        cadastrarJogadorInicial(sui, 10, "Granit Xhaka", "MED");
        cadastrarJogadorInicial(sui, 11, "Dan Ndoye", "AV");
        cadastrarJogadorInicial(sui, 12, "Yvon Mvogo", "GR");
        cadastrarJogadorInicial(sui, 13, "Ricardo Rodriguez", "DEF");
        cadastrarJogadorInicial(sui, 14, "Ardon Jashari", "MED");
        cadastrarJogadorInicial(sui, 15, "Djibril Sow", "MED");
        cadastrarJogadorInicial(sui, 16, "Christian Fassnacht", "AV");
        cadastrarJogadorInicial(sui, 17, "Ruben Vargas", "AV");
        cadastrarJogadorInicial(sui, 18, "Eray Coemert", "DEF");
        cadastrarJogadorInicial(sui, 19, "Noah Okafor", "AV");
        cadastrarJogadorInicial(sui, 20, "Michel Aebischer", "MED");
        cadastrarJogadorInicial(sui, 21, "Marvin Keller", "GR");
        cadastrarJogadorInicial(sui, 22, "Fabian Rieder", "MED");
        cadastrarJogadorInicial(sui, 23, "Zeki Amdouni", "AV");
        cadastrarJogadorInicial(sui, 24, "Aurele Amenda", "DEF");
        cadastrarJogadorInicial(sui, 25, "Luca Jaquez", "DEF");
        cadastrarJogadorInicial(sui, 26, "Cedric Itten", "AV");
        selecoes.add(sui);

        Selecao bra = criarSelecao("Brasil", 5, 23, "C");
        cadastrarJogadorInicial(bra, 1, "Alisson", "GR");
        cadastrarJogadorInicial(bra, 2, "Ederson Silva", "MED");
        cadastrarJogadorInicial(bra, 3, "Gabriel Magalhaes", "DEF");
        cadastrarJogadorInicial(bra, 4, "Marquinhos", "DEF");
        cadastrarJogadorInicial(bra, 5, "Casemiro", "MED");
        cadastrarJogadorInicial(bra, 6, "Alex Sandro", "DEF");
        cadastrarJogadorInicial(bra, 7, "Vinicius Junior", "AV");
        cadastrarJogadorInicial(bra, 8, "Bruno Guimaraes", "MED");
        cadastrarJogadorInicial(bra, 9, "Matheus Cunha", "AV");
        cadastrarJogadorInicial(bra, 10, "Neymar Jr", "AV");
        cadastrarJogadorInicial(bra, 11, "Raphinha", "AV");
        cadastrarJogadorInicial(bra, 12, "Weverton", "GR");
        cadastrarJogadorInicial(bra, 13, "Danilo", "DEF");
        cadastrarJogadorInicial(bra, 14, "Bremer", "DEF");
        cadastrarJogadorInicial(bra, 15, "Leo Pereira", "DEF");
        cadastrarJogadorInicial(bra, 16, "Douglas Santos", "DEF");
        cadastrarJogadorInicial(bra, 17, "Fabinho", "MED");
        cadastrarJogadorInicial(bra, 18, "Danilo Santos", "MED");
        cadastrarJogadorInicial(bra, 19, "Endrick", "AV");
        cadastrarJogadorInicial(bra, 20, "Lucas Paqueta", "MED");
        cadastrarJogadorInicial(bra, 21, "Luiz Henrique", "AV");
        cadastrarJogadorInicial(bra, 22, "Gabriel Martinelli", "AV");
        cadastrarJogadorInicial(bra, 23, "Ederson", "GR");
        cadastrarJogadorInicial(bra, 24, "Roger Ibanez", "DEF");
        cadastrarJogadorInicial(bra, 25, "Igor Thiago", "AV");
        cadastrarJogadorInicial(bra, 26, "Rayan", "AV");
        selecoes.add(bra);

        Selecao sco = criarSelecao("Escócia", 45, 9, "C");
        cadastrarJogadorInicial(sco, 1, "Angus Gunn", "GR");
        cadastrarJogadorInicial(sco, 2, "Aaron Hickey", "DEF");
        cadastrarJogadorInicial(sco, 3, "Andy Robertson", "DEF");
        cadastrarJogadorInicial(sco, 4, "Scott McTominay", "MED");
        cadastrarJogadorInicial(sco, 5, "Grant Hanley", "DEF");
        cadastrarJogadorInicial(sco, 6, "Kieran Tierney", "DEF");
        cadastrarJogadorInicial(sco, 7, "John McGinn", "MED");
        cadastrarJogadorInicial(sco, 8, "Tyler Fletcher", "MED");
        cadastrarJogadorInicial(sco, 9, "Lyndon Dykes", "AV");
        cadastrarJogadorInicial(sco, 10, "Che Adams", "AV");
        cadastrarJogadorInicial(sco, 11, "Ryan Christie", "MED");
        cadastrarJogadorInicial(sco, 12, "Liam Kelly", "GR");
        cadastrarJogadorInicial(sco, 13, "Jack Hendry", "DEF");
        cadastrarJogadorInicial(sco, 14, "Ross Stewart", "AV");
        cadastrarJogadorInicial(sco, 15, "John Souttar", "DEF");
        cadastrarJogadorInicial(sco, 16, "Dominic Hyam", "DEF");
        cadastrarJogadorInicial(sco, 17, "Ben Gannon-Doak", "AV");
        cadastrarJogadorInicial(sco, 18, "George Hirst", "AV");
        cadastrarJogadorInicial(sco, 19, "Lewis Ferguson", "MED");
        cadastrarJogadorInicial(sco, 20, "Lawrence Shankland", "AV");
        cadastrarJogadorInicial(sco, 21, "Craig Gordon", "GR");
        cadastrarJogadorInicial(sco, 22, "Nathan Patterson", "DEF");
        cadastrarJogadorInicial(sco, 23, "Kenny McLean", "MED");
        cadastrarJogadorInicial(sco, 24, "Anthony Ralston", "DEF");
        cadastrarJogadorInicial(sco, 25, "Findlay Curtis", "AV");
        cadastrarJogadorInicial(sco, 26, "Scott McKenna", "DEF");
        selecoes.add(sco);

        Selecao hai = criarSelecao("Haiti", 83, 3, "C");
        cadastrarJogadorInicial(hai, 1, "Johny Placide", "GR");
        cadastrarJogadorInicial(hai, 2, "Carlens Arcus", "DEF");
        cadastrarJogadorInicial(hai, 3, "Keeto Thermoncy", "DEF");
        cadastrarJogadorInicial(hai, 4, "Ricardo Ade", "DEF");
        cadastrarJogadorInicial(hai, 5, "Hannes Delcroix", "DEF");
        cadastrarJogadorInicial(hai, 6, "Carl Sainte", "MED");
        cadastrarJogadorInicial(hai, 7, "Derrick Etienne", "AV");
        cadastrarJogadorInicial(hai, 8, "Martin Experience", "DEF");
        cadastrarJogadorInicial(hai, 9, "Duckens Nazon", "AV");
        cadastrarJogadorInicial(hai, 10, "Jean-Ricner Bellegarde", "MED");
        cadastrarJogadorInicial(hai, 11, "Louicius Deedson", "AV");
        cadastrarJogadorInicial(hai, 12, "Alexandre Pierre", "GR");
        cadastrarJogadorInicial(hai, 13, "Markhus Lacroix", "DEF");
        cadastrarJogadorInicial(hai, 14, "Garven Metusala", "DEF");
        cadastrarJogadorInicial(hai, 15, "Ruben Providence", "AV");
        cadastrarJogadorInicial(hai, 16, "Lenny Joseph", "AV");
        cadastrarJogadorInicial(hai, 17, "Danley Jean Jacques", "MED");
        cadastrarJogadorInicial(hai, 18, "Wilson Isidor", "AV");
        cadastrarJogadorInicial(hai, 19, "Yassin Fortune", "AV");
        cadastrarJogadorInicial(hai, 20, "Frantzdy Pierrot", "AV");
        cadastrarJogadorInicial(hai, 21, "Josue Casimir", "AV");
        cadastrarJogadorInicial(hai, 22, "Jean-Kevin Duverne", "DEF");
        cadastrarJogadorInicial(hai, 23, "Josue Duverger", "GR");
        cadastrarJogadorInicial(hai, 24, "Wilguens Paugain", "DEF");
        cadastrarJogadorInicial(hai, 25, "Dominique Simon", "MED");
        cadastrarJogadorInicial(hai, 26, "Woodensky Pierre", "MED");
        selecoes.add(hai);

        Selecao mar = criarSelecao("Marrocos", 12, 7, "C");
        cadastrarJogadorInicial(mar, 1, "Yassine Bounou", "GR");
        cadastrarJogadorInicial(mar, 2, "Achraf Hakimi", "DEF");
        cadastrarJogadorInicial(mar, 3, "Noussair Mazraoui", "DEF");
        cadastrarJogadorInicial(mar, 4, "Sofyan Amrabat", "MED");
        cadastrarJogadorInicial(mar, 5, "Marwane Saadane", "DEF");
        cadastrarJogadorInicial(mar, 6, "Ayyoub Bouaddi", "MED");
        cadastrarJogadorInicial(mar, 7, "Chemsdine Talbi", "MED");
        cadastrarJogadorInicial(mar, 8, "Azzedine Ounahi", "MED");
        cadastrarJogadorInicial(mar, 9, "Sou Ane Rahimi", "AV");
        cadastrarJogadorInicial(mar, 10, "Brahim Diaz", "AV");
        cadastrarJogadorInicial(mar, 11, "Ismael Saibari", "MED");
        cadastrarJogadorInicial(mar, 12, "Munir El Kajoui", "GR");
        cadastrarJogadorInicial(mar, 13, "Zakaria El Ouahdi", "DEF");
        cadastrarJogadorInicial(mar, 14, "Issa Diop", "DEF");
        cadastrarJogadorInicial(mar, 15, "Samir El Mourabet", "MED");
        cadastrarJogadorInicial(mar, 16, "Gessime Yassine", "MED");
        cadastrarJogadorInicial(mar, 17, "Amine Sbai", "AV");
        cadastrarJogadorInicial(mar, 18, "Chadi Riad", "DEF");
        cadastrarJogadorInicial(mar, 19, "Youssef Belammari", "DEF");
        cadastrarJogadorInicial(mar, 20, "Ayoub El Kaabi", "AV");
        cadastrarJogadorInicial(mar, 21, "Ayoube Amaimouni", "AV");
        cadastrarJogadorInicial(mar, 22, "Ahmed Reda Tagnaouti", "GR");
        cadastrarJogadorInicial(mar, 23, "Bilal El Khannouss", "MED");
        cadastrarJogadorInicial(mar, 24, "Neil El Aynaoui", "MED");
        cadastrarJogadorInicial(mar, 25, "Redouane Halhal", "DEF");
        cadastrarJogadorInicial(mar, 26, "Anass Salah Eddine", "DEF");
        selecoes.add(mar);

        Selecao aus = criarSelecao("Austrália", 24, 7, "D");
        cadastrarJogadorInicial(aus, 1, "Mathew Ryan", "GR");
        cadastrarJogadorInicial(aus, 2, "Milos Degenek", "DEF");
        cadastrarJogadorInicial(aus, 3, "Alessandro Circati", "DEF");
        cadastrarJogadorInicial(aus, 4, "Jacob Italiano", "DEF");
        cadastrarJogadorInicial(aus, 5, "Jordan Bos", "DEF");
        cadastrarJogadorInicial(aus, 6, "Jason Geria", "DEF");
        cadastrarJogadorInicial(aus, 7, "Mathew Leckie", "AV");
        cadastrarJogadorInicial(aus, 8, "Connor Metcalfe", "MED");
        cadastrarJogadorInicial(aus, 9, "Mohamed Toure", "AV");
        cadastrarJogadorInicial(aus, 10, "Ajdin Hrustic", "AV");
        cadastrarJogadorInicial(aus, 11, "Awer Mabil", "AV");
        cadastrarJogadorInicial(aus, 12, "Paul Izzo", "GR");
        cadastrarJogadorInicial(aus, 13, "Aiden Oneill", "MED");
        cadastrarJogadorInicial(aus, 14, "Cameron Devlin", "MED");
        cadastrarJogadorInicial(aus, 15, "Kai Trewin", "DEF");
        cadastrarJogadorInicial(aus, 16, "Aziz Behich", "DEF");
        cadastrarJogadorInicial(aus, 17, "Nestory Irankunda", "AV");
        cadastrarJogadorInicial(aus, 18, "Patrick Beach", "GR");
        cadastrarJogadorInicial(aus, 19, "Harry Souttar", "DEF");
        cadastrarJogadorInicial(aus, 20, "Cristian Volpato", "AV");
        cadastrarJogadorInicial(aus, 21, "Cameron Burgess", "DEF");
        cadastrarJogadorInicial(aus, 22, "Jackson Irvine", "MED");
        cadastrarJogadorInicial(aus, 23, "Nishan Velupillay", "AV");
        cadastrarJogadorInicial(aus, 24, "Paul Okon-Engstler", "MED");
        cadastrarJogadorInicial(aus, 25, "Lucas Herrington", "DEF");
        cadastrarJogadorInicial(aus, 26, "Tete Yengi", "AV");
        selecoes.add(aus);

        Selecao usa = criarSelecao("Estados Unidos", 16, 12, "D");
        cadastrarJogadorInicial(usa, 1, "Matt Turner", "GR");
        cadastrarJogadorInicial(usa, 2, "Sergino Dest", "DEF");
        cadastrarJogadorInicial(usa, 3, "Chris Richards", "DEF");
        cadastrarJogadorInicial(usa, 4, "Tyler Adams", "MED");
        cadastrarJogadorInicial(usa, 5, "Antonee Robinson", "DEF");
        cadastrarJogadorInicial(usa, 6, "Auston Trusty", "DEF");
        cadastrarJogadorInicial(usa, 7, "Giovanni Reyna", "MED");
        cadastrarJogadorInicial(usa, 8, "Weston McKennie", "MED");
        cadastrarJogadorInicial(usa, 9, "Ricardo Pepi", "AV");
        cadastrarJogadorInicial(usa, 10, "Christian Pulisic", "AV");
        cadastrarJogadorInicial(usa, 11, "Brenden Aaronson", "AV");
        cadastrarJogadorInicial(usa, 12, "Miles Robinson", "DEF");
        cadastrarJogadorInicial(usa, 13, "Tim Ream", "DEF");
        cadastrarJogadorInicial(usa, 14, "Sebastian Berhalter", "MED");
        cadastrarJogadorInicial(usa, 15, "Cristian Roldan", "MED");
        cadastrarJogadorInicial(usa, 16, "Alex Freeman", "DEF");
        cadastrarJogadorInicial(usa, 17, "Malik Tillman", "MED");
        cadastrarJogadorInicial(usa, 18, "Max Arfsten", "DEF");
        cadastrarJogadorInicial(usa, 19, "Haji Wright", "AV");
        cadastrarJogadorInicial(usa, 20, "Folarin Balogun", "AV");
        cadastrarJogadorInicial(usa, 21, "Timothy Weah", "AV");
        cadastrarJogadorInicial(usa, 22, "Mark McKenzie", "DEF");
        cadastrarJogadorInicial(usa, 23, "Joe Scally", "DEF");
        cadastrarJogadorInicial(usa, 24, "Matt Freese", "GR");
        cadastrarJogadorInicial(usa, 25, "Chris Brady", "GR");
        cadastrarJogadorInicial(usa, 26, "Alex Zendejas", "AV");
        selecoes.add(usa);

        Selecao par = criarSelecao("Paraguai", 56, 9, "D");
        cadastrarJogadorInicial(par, 1, "Gatito Fernandez", "GR");
        cadastrarJogadorInicial(par, 2, "Gustavo Velazquez", "DEF");
        cadastrarJogadorInicial(par, 3, "Omar Alderete", "DEF");
        cadastrarJogadorInicial(par, 4, "Juan Jose Caceres", "DEF");
        cadastrarJogadorInicial(par, 5, "Fabian Balbuena", "DEF");
        cadastrarJogadorInicial(par, 6, "Junior Alonso", "DEF");
        cadastrarJogadorInicial(par, 7, "Ramon Sosa", "MED");
        cadastrarJogadorInicial(par, 8, "Diego Gomez", "MED");
        cadastrarJogadorInicial(par, 9, "Antonio Sanabria", "AV");
        cadastrarJogadorInicial(par, 10, "Miguel Almiron", "MED");
        cadastrarJogadorInicial(par, 11, "Mauricio", "MED");
        cadastrarJogadorInicial(par, 12, "Orlando Gill", "GR");
        cadastrarJogadorInicial(par, 13, "Jose Canale", "DEF");
        cadastrarJogadorInicial(par, 14, "Andres Cubas", "MED");
        cadastrarJogadorInicial(par, 15, "Gustavo Gomez", "DEF");
        cadastrarJogadorInicial(par, 16, "Damian Bobadilla", "MED");
        cadastrarJogadorInicial(par, 17, "Alejandro Romero Gamarra", "AV");
        cadastrarJogadorInicial(par, 18, "Alex Arce", "AV");
        cadastrarJogadorInicial(par, 19, "Julio Enciso", "AV");
        cadastrarJogadorInicial(par, 20, "Braian Ojeda", "MED");
        cadastrarJogadorInicial(par, 21, "Gabriel Avalos", "AV");
        cadastrarJogadorInicial(par, 22, "Gaston Olveira", "GR");
        cadastrarJogadorInicial(par, 23, "Matias Galarza", "MED");
        cadastrarJogadorInicial(par, 24, "Gustavo Caballero", "MED");
        cadastrarJogadorInicial(par, 25, "Isidro Pitta", "AV");
        cadastrarJogadorInicial(par, 26, "Alexandro Maidana", "DEF");
        selecoes.add(par);

        Selecao tur = criarSelecao("Turquia", 26, 3, "D");
        cadastrarJogadorInicial(tur, 1, "Mert Gunok", "GR");
        cadastrarJogadorInicial(tur, 2, "Zeki Celik", "DEF");
        cadastrarJogadorInicial(tur, 3, "Merih Demiral", "DEF");
        cadastrarJogadorInicial(tur, 4, "Caglar Soyuncu", "DEF");
        cadastrarJogadorInicial(tur, 5, "Salih Ozcan", "MED");
        cadastrarJogadorInicial(tur, 6, "Orkun Kokcu", "MED");
        cadastrarJogadorInicial(tur, 7, "Kerem Akturkoglu", "AV");
        cadastrarJogadorInicial(tur, 8, "Arda Guler", "AV");
        cadastrarJogadorInicial(tur, 9, "Deniz Gul", "AV");
        cadastrarJogadorInicial(tur, 10, "Hakan Calhanoglu", "MED");
        cadastrarJogadorInicial(tur, 11, "Kenan Yildiz", "AV");
        cadastrarJogadorInicial(tur, 12, "Altay Bayindir", "GR");
        cadastrarJogadorInicial(tur, 13, "Eren Elmali", "DEF");
        cadastrarJogadorInicial(tur, 14, "Abdulkerim Bardakci", "DEF");
        cadastrarJogadorInicial(tur, 15, "Ozan Kabak", "DEF");
        cadastrarJogadorInicial(tur, 16, "Ismail Yuksek", "MED");
        cadastrarJogadorInicial(tur, 17, "Irfan Can Kahveci", "AV");
        cadastrarJogadorInicial(tur, 18, "Mert Muldur", "DEF");
        cadastrarJogadorInicial(tur, 19, "Yunus Akgun", "AV");
        cadastrarJogadorInicial(tur, 20, "Ferdi Kadioglu", "DEF");
        cadastrarJogadorInicial(tur, 21, "Baris Alper Yilmaz", "AV");
        cadastrarJogadorInicial(tur, 22, "Kaan Ayhan", "MED");
        cadastrarJogadorInicial(tur, 23, "Ugurcan Cakir", "GR");
        cadastrarJogadorInicial(tur, 24, "Oguz Aydin", "AV");
        cadastrarJogadorInicial(tur, 25, "Samet Akaydin", "DEF");
        cadastrarJogadorInicial(tur, 26, "Can Uzun", "AV");
        selecoes.add(tur);

        Selecao ger = criarSelecao("Alemanha", 9, 21, "E");
        cadastrarJogadorInicial(ger, 1, "Manuel Neuer", "GR");
        cadastrarJogadorInicial(ger, 2, "Antonio Ruediger", "DEF");
        cadastrarJogadorInicial(ger, 3, "Waldemar Anton", "DEF");
        cadastrarJogadorInicial(ger, 4, "Jonathan Tah", "DEF");
        cadastrarJogadorInicial(ger, 5, "Aleksandar Pavlovic", "MED");
        cadastrarJogadorInicial(ger, 6, "Joshua Kimmich", "DEF");
        cadastrarJogadorInicial(ger, 7, "Kai Havertz", "AV");
        cadastrarJogadorInicial(ger, 8, "Leon Goretzka", "MED");
        cadastrarJogadorInicial(ger, 9, "Jamie Leweling", "MED");
        cadastrarJogadorInicial(ger, 10, "Jamal Musiala", "MED");
        cadastrarJogadorInicial(ger, 11, "Nick Woltemade", "AV");
        cadastrarJogadorInicial(ger, 12, "Oliver Baumann", "GR");
        cadastrarJogadorInicial(ger, 13, "Pascal Gross", "MED");
        cadastrarJogadorInicial(ger, 14, "Maximilian Beier", "AV");
        cadastrarJogadorInicial(ger, 15, "Nico Schlotterbeck", "DEF");
        cadastrarJogadorInicial(ger, 16, "Angelo Stiller", "MED");
        cadastrarJogadorInicial(ger, 17, "Florian Wirtz", "MED");
        cadastrarJogadorInicial(ger, 18, "Nathaniel Brown", "DEF");
        cadastrarJogadorInicial(ger, 19, "Leroy Sane", "MED");
        cadastrarJogadorInicial(ger, 20, "Nadiem Amiri", "MED");
        cadastrarJogadorInicial(ger, 21, "Alexander Nuebel", "GR");
        cadastrarJogadorInicial(ger, 22, "David Raum", "DEF");
        cadastrarJogadorInicial(ger, 23, "Felix Nmecha", "MED");
        cadastrarJogadorInicial(ger, 24, "Malick Thiaw", "DEF");
        cadastrarJogadorInicial(ger, 25, "Assan Ouedraogo", "MED");
        cadastrarJogadorInicial(ger, 26, "Deniz Undav", "AV");
        selecoes.add(ger);

        Selecao civ = criarSelecao("Costa do Marfim", 46, 5, "E");
        cadastrarJogadorInicial(civ, 1, "Yahia Fofana", "GR");
        cadastrarJogadorInicial(civ, 2, "Ousmane Diomande", "DEF");
        cadastrarJogadorInicial(civ, 3, "Ghislain Konan", "DEF");
        cadastrarJogadorInicial(civ, 4, "Jean Michael Seri", "MED");
        cadastrarJogadorInicial(civ, 5, "Wilfried Singo", "DEF");
        cadastrarJogadorInicial(civ, 6, "Seko Fofana", "MED");
        cadastrarJogadorInicial(civ, 7, "Odilon Kossounou", "DEF");
        cadastrarJogadorInicial(civ, 8, "Franck Kessie", "MED");
        cadastrarJogadorInicial(civ, 9, "Ange-Yoan Bonny", "AV");
        cadastrarJogadorInicial(civ, 10, "Simon Adingra", "AV");
        cadastrarJogadorInicial(civ, 11, "Yan Diomande", "AV");
        cadastrarJogadorInicial(civ, 12, "Elye Wahi", "AV");
        cadastrarJogadorInicial(civ, 13, "Christopher Operi", "DEF");
        cadastrarJogadorInicial(civ, 14, "Oumar Diakite", "AV");
        cadastrarJogadorInicial(civ, 15, "Amad Diallo", "AV");
        cadastrarJogadorInicial(civ, 16, "Mohamed Kone", "GR");
        cadastrarJogadorInicial(civ, 17, "Guela Doue", "DEF");
        cadastrarJogadorInicial(civ, 18, "Ibrahim Sangare", "MED");
        cadastrarJogadorInicial(civ, 19, "Nicolas Pepe", "AV");
        cadastrarJogadorInicial(civ, 20, "Emmanuel Agbadou", "DEF");
        cadastrarJogadorInicial(civ, 21, "Evan Ndicka", "DEF");
        cadastrarJogadorInicial(civ, 22, "Evann Guessand", "AV");
        cadastrarJogadorInicial(civ, 23, "Alban Lafont", "GR");
        cadastrarJogadorInicial(civ, 24, "Bazoumana Toure", "AV");
        cadastrarJogadorInicial(civ, 25, "Parfait Guiagon", "MED");
        cadastrarJogadorInicial(civ, 26, "Christ Inao Oulai", "MED");
        selecoes.add(civ);

        Selecao cuw = criarSelecao("Curaçao", 88, 1, "E");
        cadastrarJogadorInicial(cuw, 1, "Eloy Room", "GR");
        cadastrarJogadorInicial(cuw, 2, "Shurandy Sambo", "DEF");
        cadastrarJogadorInicial(cuw, 3, "Jurien Gaari", "DEF");
        cadastrarJogadorInicial(cuw, 4, "Roshon Van Eijma", "DEF");
        cadastrarJogadorInicial(cuw, 5, "Sherel Floranus", "DEF");
        cadastrarJogadorInicial(cuw, 6, "Godfried Roemeratoe", "MED");
        cadastrarJogadorInicial(cuw, 7, "Juninho Bacuna", "MED");
        cadastrarJogadorInicial(cuw, 8, "Livano Comenencia", "MED");
        cadastrarJogadorInicial(cuw, 9, "Juergen Locadia", "AV");
        cadastrarJogadorInicial(cuw, 10, "Leandro Bacuna", "MED");
        cadastrarJogadorInicial(cuw, 11, "Jeremy Antonisse", "AV");
        cadastrarJogadorInicial(cuw, 12, "Sontje Hansen", "AV");
        cadastrarJogadorInicial(cuw, 13, "Tyrese Noslin", "AV");
        cadastrarJogadorInicial(cuw, 14, "Kenji Gorre", "AV");
        cadastrarJogadorInicial(cuw, 15, "Arjany Martha", "MED");
        cadastrarJogadorInicial(cuw, 16, "Jearl Margaritha", "AV");
        cadastrarJogadorInicial(cuw, 17, "Brandley Kuwas", "AV");
        cadastrarJogadorInicial(cuw, 18, "Armando Obispo", "DEF");
        cadastrarJogadorInicial(cuw, 19, "Gervane Kastaneer", "AV");
        cadastrarJogadorInicial(cuw, 20, "Joshua Brenet", "DEF");
        cadastrarJogadorInicial(cuw, 21, "Tahith Chong", "MED");
        cadastrarJogadorInicial(cuw, 22, "Kevin Felida", "MED");
        cadastrarJogadorInicial(cuw, 23, "Riechedly Bazoer", "DEF");
        cadastrarJogadorInicial(cuw, 24, "Deveron Fonville", "DEF");
        cadastrarJogadorInicial(cuw, 25, "Tyrick Bodak", "GR");
        cadastrarJogadorInicial(cuw, 26, "Trevor Doornbusch", "GR");
        selecoes.add(cuw);

        Selecao ecu = criarSelecao("Equador", 31, 5, "E");
        cadastrarJogadorInicial(ecu, 1, "Hernan Galindez", "GR");
        cadastrarJogadorInicial(ecu, 2, "Felix Torres", "DEF");
        cadastrarJogadorInicial(ecu, 3, "Piero Hincapie", "DEF");
        cadastrarJogadorInicial(ecu, 4, "Joel Ordonez", "DEF");
        cadastrarJogadorInicial(ecu, 5, "Jordy Alcivar", "MED");
        cadastrarJogadorInicial(ecu, 6, "Willian Pacho", "DEF");
        cadastrarJogadorInicial(ecu, 7, "Pervis Estupinan", "DEF");
        cadastrarJogadorInicial(ecu, 8, "Anthony Valencia", "MED");
        cadastrarJogadorInicial(ecu, 9, "John Yeboah", "AV");
        cadastrarJogadorInicial(ecu, 10, "Kendry Paez", "MED");
        cadastrarJogadorInicial(ecu, 11, "Kevin Rodriguez", "AV");
        cadastrarJogadorInicial(ecu, 12, "Moises Ramirez", "GR");
        cadastrarJogadorInicial(ecu, 13, "Enner Valencia", "AV");
        cadastrarJogadorInicial(ecu, 14, "Alan Minda", "MED");
        cadastrarJogadorInicial(ecu, 15, "Pedro Vite", "MED");
        cadastrarJogadorInicial(ecu, 16, "Jordy Caicedo", "AV");
        cadastrarJogadorInicial(ecu, 17, "Angelo Preciado", "DEF");
        cadastrarJogadorInicial(ecu, 18, "Denil Castillo", "MED");
        cadastrarJogadorInicial(ecu, 19, "Gonzalo Plata", "AV");
        cadastrarJogadorInicial(ecu, 20, "Nilson Angulo", "AV");
        cadastrarJogadorInicial(ecu, 21, "Alan Franco", "MED");
        cadastrarJogadorInicial(ecu, 22, "Gonzalo Valle", "GR");
        cadastrarJogadorInicial(ecu, 23, "Moises Caicedo", "MED");
        cadastrarJogadorInicial(ecu, 24, "Jeremy Arevalo", "AV");
        cadastrarJogadorInicial(ecu, 25, "Jackson Porozo", "DEF");
        cadastrarJogadorInicial(ecu, 26, "Yaimar Medina", "DEF");
        selecoes.add(ecu);

        Selecao jpn = criarSelecao("Japão", 18, 8, "F");
        cadastrarJogadorInicial(jpn, 1, "Zion Suzuki", "GR");
        cadastrarJogadorInicial(jpn, 2, "Yukinari Sugawara", "DEF");
        cadastrarJogadorInicial(jpn, 3, "Shogo Taniguchi", "DEF");
        cadastrarJogadorInicial(jpn, 4, "Kou Itakura", "DEF");
        cadastrarJogadorInicial(jpn, 5, "Yuto Nagatomo", "DEF");
        cadastrarJogadorInicial(jpn, 6, "Shuto Machino", "AV");
        cadastrarJogadorInicial(jpn, 7, "Ao Tanaka", "MED");
        cadastrarJogadorInicial(jpn, 8, "Takefusa Kubo", "MED");
        cadastrarJogadorInicial(jpn, 9, "Keisuke Goto", "AV");
        cadastrarJogadorInicial(jpn, 10, "Ritsu Doan", "MED");
        cadastrarJogadorInicial(jpn, 11, "Daizen Maeda", "MED");
        cadastrarJogadorInicial(jpn, 12, "Keisuke Osako", "GR");
        cadastrarJogadorInicial(jpn, 13, "Keito Nakamura", "MED");
        cadastrarJogadorInicial(jpn, 14, "Junya Ito", "MED");
        cadastrarJogadorInicial(jpn, 15, "Daichi Kamada", "MED");
        cadastrarJogadorInicial(jpn, 16, "Tsuyoshi Watanabe", "DEF");
        cadastrarJogadorInicial(jpn, 17, "Yuito Suzuki", "MED");
        cadastrarJogadorInicial(jpn, 18, "Ayase Ueda", "AV");
        cadastrarJogadorInicial(jpn, 19, "Koki Ogawa", "AV");
        cadastrarJogadorInicial(jpn, 20, "Ayumu Seko", "DEF");
        cadastrarJogadorInicial(jpn, 21, "Hiroki Ito", "DEF");
        cadastrarJogadorInicial(jpn, 22, "Takehiro Tomiyasu", "DEF");
        cadastrarJogadorInicial(jpn, 23, "Tomoki Hayakawa", "GR");
        cadastrarJogadorInicial(jpn, 24, "Kaishu Sano", "MED");
        cadastrarJogadorInicial(jpn, 25, "Junnosuke Suzuki", "DEF");
        cadastrarJogadorInicial(jpn, 26, "Kento Shiogai", "AV");
        selecoes.add(jpn);

        Selecao ned = criarSelecao("Países Baixos", 7, 12, "F");
        cadastrarJogadorInicial(ned, 1, "Bart Verbruggen", "GR");
        cadastrarJogadorInicial(ned, 2, "Lutsharel Geertruida", "DEF");
        cadastrarJogadorInicial(ned, 3, "Marten De Roon", "MED");
        cadastrarJogadorInicial(ned, 4, "Virgil Van Dijk", "DEF");
        cadastrarJogadorInicial(ned, 5, "Nathan Ake", "DEF");
        cadastrarJogadorInicial(ned, 6, "Jan Paul Van Hecke", "DEF");
        cadastrarJogadorInicial(ned, 7, "Justin Kluivert", "MED");
        cadastrarJogadorInicial(ned, 8, "Ryan Gravenberch", "MED");
        cadastrarJogadorInicial(ned, 9, "Wout Weghorst", "AV");
        cadastrarJogadorInicial(ned, 10, "Memphis Depay", "AV");
        cadastrarJogadorInicial(ned, 11, "Cody Gakpo", "AV");
        cadastrarJogadorInicial(ned, 12, "Mats Wieffer", "DEF");
        cadastrarJogadorInicial(ned, 13, "Robin Roefs", "GR");
        cadastrarJogadorInicial(ned, 14, "Tijjani Reijnders", "MED");
        cadastrarJogadorInicial(ned, 15, "Micky Van De Ven", "DEF");
        cadastrarJogadorInicial(ned, 16, "Guus Til", "MED");
        cadastrarJogadorInicial(ned, 17, "Noa Lang", "AV");
        cadastrarJogadorInicial(ned, 18, "Donyell Malen", "AV");
        cadastrarJogadorInicial(ned, 19, "Brian Brobbey", "AV");
        cadastrarJogadorInicial(ned, 20, "Teun Koopmeiners", "MED");
        cadastrarJogadorInicial(ned, 21, "Frenkie De Jong", "MED");
        cadastrarJogadorInicial(ned, 22, "Denzel Dumfries", "DEF");
        cadastrarJogadorInicial(ned, 23, "Mark Flekken", "GR");
        cadastrarJogadorInicial(ned, 24, "Crysencio Summerville", "AV");
        cadastrarJogadorInicial(ned, 25, "Jorrel Hato", "DEF");
        cadastrarJogadorInicial(ned, 26, "Quinten Timber", "MED");
        selecoes.add(ned);

        Selecao swe = criarSelecao("Suécia", 28, 13, "F");
        cadastrarJogadorInicial(swe, 1, "Jacob Widell Zetterstrom", "GR");
        cadastrarJogadorInicial(swe, 2, "Gustaf Lagerbielke", "DEF");
        cadastrarJogadorInicial(swe, 3, "Victor Lindelof", "DEF");
        cadastrarJogadorInicial(swe, 4, "Isak Hien", "DEF");
        cadastrarJogadorInicial(swe, 5, "Gabriel Gudmundsson", "DEF");
        cadastrarJogadorInicial(swe, 6, "Herman Johansson", "DEF");
        cadastrarJogadorInicial(swe, 7, "Lucas Bergvall", "MED");
        cadastrarJogadorInicial(swe, 8, "Daniel Svensson", "DEF");
        cadastrarJogadorInicial(swe, 9, "Alexander Isak", "AV");
        cadastrarJogadorInicial(swe, 10, "Benjamin Nygren", "MED");
        cadastrarJogadorInicial(swe, 11, "Anthony Elanga", "AV");
        cadastrarJogadorInicial(swe, 12, "Viktor Johansson", "GR");
        cadastrarJogadorInicial(swe, 13, "Ken Sema", "MED");
        cadastrarJogadorInicial(swe, 14, "Hjalmar Ekdal", "DEF");
        cadastrarJogadorInicial(swe, 15, "Carl Starfelt", "DEF");
        cadastrarJogadorInicial(swe, 16, "Jesper Karlstrom", "MED");
        cadastrarJogadorInicial(swe, 17, "Viktor Gyokeres", "AV");
        cadastrarJogadorInicial(swe, 18, "Yasin Ayari", "MED");
        cadastrarJogadorInicial(swe, 19, "Mattias Svanberg", "MED");
        cadastrarJogadorInicial(swe, 20, "Eric Smith", "DEF");
        cadastrarJogadorInicial(swe, 21, "Alexander Bernhardsson", "DEF");
        cadastrarJogadorInicial(swe, 22, "Besfort Zeneli", "MED");
        cadastrarJogadorInicial(swe, 23, "Kristoffer Nordfeldt", "GR");
        cadastrarJogadorInicial(swe, 24, "Elliot Stroud", "DEF");
        cadastrarJogadorInicial(swe, 25, "Gustaf Nilsson", "AV");
        cadastrarJogadorInicial(swe, 26, "Taha Ali", "AV");
        selecoes.add(swe);

        Selecao tun = criarSelecao("Tunísia", 49, 7, "F");
        cadastrarJogadorInicial(tun, 1, "Mouhib Chamakh", "GR");
        cadastrarJogadorInicial(tun, 2, "Ali Abdi", "DEF");
        cadastrarJogadorInicial(tun, 3, "Montassar Talbi", "DEF");
        cadastrarJogadorInicial(tun, 4, "Omar Rekik", "DEF");
        cadastrarJogadorInicial(tun, 5, "Adam Arous", "DEF");
        cadastrarJogadorInicial(tun, 6, "Dylan Bronn", "DEF");
        cadastrarJogadorInicial(tun, 7, "Elias Achouri", "AV");
        cadastrarJogadorInicial(tun, 8, "Elias Saad", "AV");
        cadastrarJogadorInicial(tun, 9, "Hazem Mastouri", "AV");
        cadastrarJogadorInicial(tun, 10, "Hannibal Mejbri", "MED");
        cadastrarJogadorInicial(tun, 11, "Ismael Gharbi", "MED");
        cadastrarJogadorInicial(tun, 12, "Mortadha Ben Ouanes", "DEF");
        cadastrarJogadorInicial(tun, 13, "Rani Khedira", "MED");
        cadastrarJogadorInicial(tun, 14, "Khalil Ayari", "MED");
        cadastrarJogadorInicial(tun, 15, "Mohamed Hadj Mahmoud", "MED");
        cadastrarJogadorInicial(tun, 16, "Aymen Dahmen", "GR");
        cadastrarJogadorInicial(tun, 17, "Ellyes Skhiri", "MED");
        cadastrarJogadorInicial(tun, 18, "Rayan Elloumi", "AV");
        cadastrarJogadorInicial(tun, 19, "Firas Chaouat", "AV");
        cadastrarJogadorInicial(tun, 20, "Yan Valery", "DEF");
        cadastrarJogadorInicial(tun, 21, "Mohamed Amine Ben Hmida", "DEF");
        cadastrarJogadorInicial(tun, 22, "Sabri Ben Hessen", "GR");
        cadastrarJogadorInicial(tun, 23, "Moutaz Neffati", "DEF");
        cadastrarJogadorInicial(tun, 24, "Raed Chikhaoui", "DEF");
        cadastrarJogadorInicial(tun, 25, "Anis Slimane", "MED");
        cadastrarJogadorInicial(tun, 26, "Sebastian Tounekti", "MED");
        selecoes.add(tun);

        Selecao bel = criarSelecao("Bélgica", 3, 15, "G");
        cadastrarJogadorInicial(bel, 1, "Thibaut Courtois", "GR");
        cadastrarJogadorInicial(bel, 2, "Zeno Debast", "DEF");
        cadastrarJogadorInicial(bel, 3, "Arthur Theate", "DEF");
        cadastrarJogadorInicial(bel, 4, "Brandon Mechele", "DEF");
        cadastrarJogadorInicial(bel, 5, "Maxim De Cuyper", "DEF");
        cadastrarJogadorInicial(bel, 6, "Axel Witsel", "MED");
        cadastrarJogadorInicial(bel, 7, "Kevin De Bruyne", "MED");
        cadastrarJogadorInicial(bel, 8, "Youri Tielemans", "MED");
        cadastrarJogadorInicial(bel, 9, "Romelu Lukaku", "AV");
        cadastrarJogadorInicial(bel, 10, "Leandro Trossard", "AV");
        cadastrarJogadorInicial(bel, 11, "Jeremy Doku", "AV");
        cadastrarJogadorInicial(bel, 12, "Senne Lammens", "GR");
        cadastrarJogadorInicial(bel, 13, "Mike Penders", "GR");
        cadastrarJogadorInicial(bel, 14, "Dodi Lukebakio", "AV");
        cadastrarJogadorInicial(bel, 15, "Thomas Meunier", "DEF");
        cadastrarJogadorInicial(bel, 16, "Koni De Winter", "DEF");
        cadastrarJogadorInicial(bel, 17, "Charles De Ketelaere", "AV");
        cadastrarJogadorInicial(bel, 18, "Joaquin Seys", "DEF");
        cadastrarJogadorInicial(bel, 19, "Diego Moreira", "MED");
        cadastrarJogadorInicial(bel, 20, "Hans Vanaken", "MED");
        cadastrarJogadorInicial(bel, 21, "Timothy Castagne", "DEF");
        cadastrarJogadorInicial(bel, 22, "Alexis Saelemaekers", "MED");
        cadastrarJogadorInicial(bel, 23, "Nicolas Raskin", "MED");
        cadastrarJogadorInicial(bel, 24, "Amadou Onana", "MED");
        cadastrarJogadorInicial(bel, 25, "Nathan Ngoy", "DEF");
        cadastrarJogadorInicial(bel, 26, "Matias Fernandez-Pardo", "AV");
        selecoes.add(bel);

        Selecao egy = criarSelecao("Egito", 34, 4, "G");
        cadastrarJogadorInicial(egy, 1, "Mohamed Elshenawy", "GR");
        cadastrarJogadorInicial(egy, 2, "Yasser Ibrahim", "DEF");
        cadastrarJogadorInicial(egy, 3, "Mohamed Hany", "DEF");
        cadastrarJogadorInicial(egy, 4, "Hossam Abdelmaguid", "DEF");
        cadastrarJogadorInicial(egy, 5, "Ramy Rabia", "DEF");
        cadastrarJogadorInicial(egy, 6, "Mohamed Abdelmoneim", "DEF");
        cadastrarJogadorInicial(egy, 7, "Trezeguet", "AV");
        cadastrarJogadorInicial(egy, 8, "Emam Ashour", "MED");
        cadastrarJogadorInicial(egy, 9, "Hamza Abdelkarim", "AV");
        cadastrarJogadorInicial(egy, 10, "Mohamed Salah", "AV");
        cadastrarJogadorInicial(egy, 11, "Mostafa Zico", "MED");
        cadastrarJogadorInicial(egy, 12, "Haissem Hassan", "AV");
        cadastrarJogadorInicial(egy, 13, "Ahmed Fatouh", "DEF");
        cadastrarJogadorInicial(egy, 14, "Hamdy Fathy", "MED");
        cadastrarJogadorInicial(egy, 15, "Karim Hafez", "DEF");
        cadastrarJogadorInicial(egy, 16, "Mahdy Soliman", "GR");
        cadastrarJogadorInicial(egy, 17, "Mohanad Lashin", "MED");
        cadastrarJogadorInicial(egy, 18, "Nabil Donga", "MED");
        cadastrarJogadorInicial(egy, 19, "Marawan Attia", "MED");
        cadastrarJogadorInicial(egy, 20, "Ibrahim Adel", "AV");
        cadastrarJogadorInicial(egy, 21, "Mahmoud Saber", "MED");
        cadastrarJogadorInicial(egy, 22, "Omar Marmoush", "AV");
        cadastrarJogadorInicial(egy, 23, "Mostafa Shoubir", "GR");
        cadastrarJogadorInicial(egy, 24, "Tarek Alaa", "DEF");
        cadastrarJogadorInicial(egy, 25, "Zizo", "AV");
        cadastrarJogadorInicial(egy, 26, "Mohamed Alaa", "GR");
        selecoes.add(egy);

        Selecao irn = criarSelecao("Irão", 20, 7, "G");
        cadastrarJogadorInicial(irn, 1, "Alireza Beiranvand", "GR");
        cadastrarJogadorInicial(irn, 2, "Saleh Hardani", "DEF");
        cadastrarJogadorInicial(irn, 3, "Ehsan Hajisafi", "DEF");
        cadastrarJogadorInicial(irn, 4, "Shoja Khalilzadeh", "DEF");
        cadastrarJogadorInicial(irn, 5, "Milad Mohammadi", "DEF");
        cadastrarJogadorInicial(irn, 6, "Saeid Ezatolahi", "MED");
        cadastrarJogadorInicial(irn, 7, "Alireza Jahanbakhsh", "MED");
        cadastrarJogadorInicial(irn, 8, "Mohammad Mohebbi", "MED");
        cadastrarJogadorInicial(irn, 9, "Mehdi Taremi", "AV");
        cadastrarJogadorInicial(irn, 10, "Mehdi Ghayedi", "AV");
        cadastrarJogadorInicial(irn, 11, "Ali Alipour", "AV");
        cadastrarJogadorInicial(irn, 12, "Payam Niazmand", "GR");
        cadastrarJogadorInicial(irn, 13, "Hossein Kanani", "DEF");
        cadastrarJogadorInicial(irn, 14, "Saman Ghoddos", "MED");
        cadastrarJogadorInicial(irn, 15, "Roozbeh Cheshmi", "MED");
        cadastrarJogadorInicial(irn, 16, "Mehdi Torabi", "MED");
        cadastrarJogadorInicial(irn, 17, "Arya Yousefi", "DEF");
        cadastrarJogadorInicial(irn, 18, "Amirhossein Hosseinzadeh", "AV");
        cadastrarJogadorInicial(irn, 19, "Ali Nemati", "DEF");
        cadastrarJogadorInicial(irn, 20, "Shahriyar Moghanloo", "AV");
        cadastrarJogadorInicial(irn, 21, "Mohammad Ghorbani", "MED");
        cadastrarJogadorInicial(irn, 22, "Hossein Hosseini", "GR");
        cadastrarJogadorInicial(irn, 23, "Ramin Rezaeian", "DEF");
        cadastrarJogadorInicial(irn, 24, "Dennis Dargahi", "AV");
        cadastrarJogadorInicial(irn, 25, "Danial Iri", "DEF");
        cadastrarJogadorInicial(irn, 26, "Amirmohammad Razaghinia", "MED");
        selecoes.add(irn);

        Selecao nzl = criarSelecao("Nova Zelândia", 103, 4, "G");
        cadastrarJogadorInicial(nzl, 1, "Max Crocombe", "GR");
        cadastrarJogadorInicial(nzl, 2, "Tim Payne", "DEF");
        cadastrarJogadorInicial(nzl, 3, "Francis De Vries", "DEF");
        cadastrarJogadorInicial(nzl, 4, "Tyler Bindon", "DEF");
        cadastrarJogadorInicial(nzl, 5, "Michael Boxall", "DEF");
        cadastrarJogadorInicial(nzl, 6, "Joe Bell", "MED");
        cadastrarJogadorInicial(nzl, 7, "Logan Rogerson", "AV");
        cadastrarJogadorInicial(nzl, 8, "Marko Stamenic", "MED");
        cadastrarJogadorInicial(nzl, 9, "Chris Wood", "AV");
        cadastrarJogadorInicial(nzl, 10, "Sarpreet Singh", "MED");
        cadastrarJogadorInicial(nzl, 11, "Elijah Just", "MED");
        cadastrarJogadorInicial(nzl, 12, "Alex Paulsen", "GR");
        cadastrarJogadorInicial(nzl, 13, "Liberato Cacace", "DEF");
        cadastrarJogadorInicial(nzl, 14, "Alex Rufer", "MED");
        cadastrarJogadorInicial(nzl, 15, "Nando Pijnaker", "DEF");
        cadastrarJogadorInicial(nzl, 16, "Finn Surman", "DEF");
        cadastrarJogadorInicial(nzl, 17, "Kosta Barbarouses", "AV");
        cadastrarJogadorInicial(nzl, 18, "Ben Waine", "AV");
        cadastrarJogadorInicial(nzl, 19, "Ben Old", "MED");
        cadastrarJogadorInicial(nzl, 20, "Callum McCowatt", "MED");
        cadastrarJogadorInicial(nzl, 21, "Jesse Randall", "AV");
        cadastrarJogadorInicial(nzl, 22, "Michael Woud", "GR");
        cadastrarJogadorInicial(nzl, 23, "Ryan Thomas", "MED");
        cadastrarJogadorInicial(nzl, 24, "Callan Elliot", "DEF");
        cadastrarJogadorInicial(nzl, 25, "Lachlan Bayliss", "MED");
        cadastrarJogadorInicial(nzl, 26, "Tommy Smith", "DEF");
        selecoes.add(nzl);

        Selecao ksa = criarSelecao("Arábia Saudita", 58, 7, "H");
        cadastrarJogadorInicial(ksa, 1, "Nawaf Alaqidi", "GR");
        cadastrarJogadorInicial(ksa, 2, "Ali Majrashi", "DEF");
        cadastrarJogadorInicial(ksa, 3, "Ali Lajami", "DEF");
        cadastrarJogadorInicial(ksa, 4, "Abdulelah Alamri", "DEF");
        cadastrarJogadorInicial(ksa, 5, "Hassan Altambakti", "DEF");
        cadastrarJogadorInicial(ksa, 6, "Nasser Aldawsari", "MED");
        cadastrarJogadorInicial(ksa, 7, "Musab Aljuwayr", "MED");
        cadastrarJogadorInicial(ksa, 8, "Aiman Yahya", "AV");
        cadastrarJogadorInicial(ksa, 9, "Feras Albrikan", "AV");
        cadastrarJogadorInicial(ksa, 10, "Salem Aldawsari", "AV");
        cadastrarJogadorInicial(ksa, 11, "Saleh Alshehri", "AV");
        cadastrarJogadorInicial(ksa, 12, "Saud Abdulhamid", "DEF");
        cadastrarJogadorInicial(ksa, 13, "Nawaf Bu Washl", "DEF");
        cadastrarJogadorInicial(ksa, 14, "Hassan Kadish", "DEF");
        cadastrarJogadorInicial(ksa, 15, "Abdullah Alkhaibari", "MED");
        cadastrarJogadorInicial(ksa, 16, "Ziyad Aljohani", "MED");
        cadastrarJogadorInicial(ksa, 17, "Khalid Alghannam", "AV");
        cadastrarJogadorInicial(ksa, 18, "Ala Alhajji", "MED");
        cadastrarJogadorInicial(ksa, 19, "Abdullah Alhamddan", "AV");
        cadastrarJogadorInicial(ksa, 20, "Sultan Mandash", "AV");
        cadastrarJogadorInicial(ksa, 21, "Mohammed Alowais", "GR");
        cadastrarJogadorInicial(ksa, 22, "Ahmed Alkassar", "GR");
        cadastrarJogadorInicial(ksa, 23, "Mohamed Kanno", "MED");
        cadastrarJogadorInicial(ksa, 24, "Moteb Alharbi", "DEF");
        cadastrarJogadorInicial(ksa, 25, "Jehad Thikri", "DEF");
        cadastrarJogadorInicial(ksa, 26, "Mohammed Abu Alshamat", "DEF");
        selecoes.add(ksa);

        Selecao cpv = criarSelecao("Cabo Verde", 65, 1, "H");
        cadastrarJogadorInicial(cpv, 1, "Vozinha", "GR");
        cadastrarJogadorInicial(cpv, 2, "Stopira", "DEF");
        cadastrarJogadorInicial(cpv, 3, "Diney Borges", "DEF");
        cadastrarJogadorInicial(cpv, 4, "Pico Lopes", "DEF");
        cadastrarJogadorInicial(cpv, 5, "Logan Costa", "DEF");
        cadastrarJogadorInicial(cpv, 6, "Kevin Pina", "MED");
        cadastrarJogadorInicial(cpv, 7, "Jovane Cabral", "MED");
        cadastrarJogadorInicial(cpv, 8, "Joao Paulo", "MED");
        cadastrarJogadorInicial(cpv, 9, "Gilson Benchimol", "AV");
        cadastrarJogadorInicial(cpv, 10, "Jamiro Monteiro", "MED");
        cadastrarJogadorInicial(cpv, 11, "Garry Rodrigues", "MED");
        cadastrarJogadorInicial(cpv, 12, "Marcio Rosa", "GR");
        cadastrarJogadorInicial(cpv, 13, "Sidny Lopes Cabral", "DEF");
        cadastrarJogadorInicial(cpv, 14, "Deroy Duarte", "MED");
        cadastrarJogadorInicial(cpv, 15, "Laros Duarte", "MED");
        cadastrarJogadorInicial(cpv, 16, "Yannick Semedo", "MED");
        cadastrarJogadorInicial(cpv, 17, "Willy Semedo", "MED");
        cadastrarJogadorInicial(cpv, 18, "Telmo Arcanjo", "MED");
        cadastrarJogadorInicial(cpv, 19, "Dailon Livramento", "AV");
        cadastrarJogadorInicial(cpv, 20, "Ryan Mendes", "AV");
        cadastrarJogadorInicial(cpv, 21, "Nuno Da Costa", "MED");
        cadastrarJogadorInicial(cpv, 22, "Steven Moreira", "DEF");
        cadastrarJogadorInicial(cpv, 23, "Cj Dos Santos", "GR");
        cadastrarJogadorInicial(cpv, 24, "Wagner Pina", "DEF");
        cadastrarJogadorInicial(cpv, 25, "Kelvin Pires", "DEF");
        cadastrarJogadorInicial(cpv, 26, "Helio Varela", "MED");
        selecoes.add(cpv);

        Selecao esp = criarSelecao("Espanha", 8, 17, "H");
        cadastrarJogadorInicial(esp, 1, "David Raya", "GR");
        cadastrarJogadorInicial(esp, 2, "Marc Pubill", "DEF");
        cadastrarJogadorInicial(esp, 3, "Alex Grimaldo", "DEF");
        cadastrarJogadorInicial(esp, 4, "Eric Garcia", "DEF");
        cadastrarJogadorInicial(esp, 5, "Marcos Llorente", "DEF");
        cadastrarJogadorInicial(esp, 6, "Mikel Merino", "MED");
        cadastrarJogadorInicial(esp, 7, "Ferran Torres", "AV");
        cadastrarJogadorInicial(esp, 8, "Fabian Ruiz", "MED");
        cadastrarJogadorInicial(esp, 9, "Gavi", "MED");
        cadastrarJogadorInicial(esp, 10, "Dani Olmo", "AV");
        cadastrarJogadorInicial(esp, 11, "Yeremy Pino", "AV");
        cadastrarJogadorInicial(esp, 12, "Pedro Porro", "DEF");
        cadastrarJogadorInicial(esp, 13, "Joan Garcia", "GR");
        cadastrarJogadorInicial(esp, 14, "Aymeric Laporte", "DEF");
        cadastrarJogadorInicial(esp, 15, "Alex Baena", "MED");
        cadastrarJogadorInicial(esp, 16, "Rodri", "MED");
        cadastrarJogadorInicial(esp, 17, "Nico Williams", "AV");
        cadastrarJogadorInicial(esp, 18, "Martin Zubimendi", "MED");
        cadastrarJogadorInicial(esp, 19, "Lamine Yamal", "AV");
        cadastrarJogadorInicial(esp, 20, "Pedri", "MED");
        cadastrarJogadorInicial(esp, 21, "Mikel Oyarzabal", "AV");
        cadastrarJogadorInicial(esp, 22, "Pau Cubarsi", "DEF");
        cadastrarJogadorInicial(esp, 23, "Unai Simon", "GR");
        cadastrarJogadorInicial(esp, 24, "Marc Cucurella", "DEF");
        cadastrarJogadorInicial(esp, 25, "Victor Munoz", "AV");
        cadastrarJogadorInicial(esp, 26, "Borja Iglesias", "AV");
        selecoes.add(esp);

        Selecao uru = criarSelecao("Uruguai", 11, 15, "H");
        cadastrarJogadorInicial(uru, 1, "Sergio Rochet", "GR");
        cadastrarJogadorInicial(uru, 2, "Jose Maria Gimenez", "DEF");
        cadastrarJogadorInicial(uru, 3, "Sebastian Caceres", "DEF");
        cadastrarJogadorInicial(uru, 4, "Ronald Araujo", "DEF");
        cadastrarJogadorInicial(uru, 5, "Manuel Ugarte", "MED");
        cadastrarJogadorInicial(uru, 6, "Rodrigo Bentancur", "MED");
        cadastrarJogadorInicial(uru, 7, "Nicolas De La Cruz", "MED");
        cadastrarJogadorInicial(uru, 8, "Federico Valverde", "MED");
        cadastrarJogadorInicial(uru, 9, "Darwin Nunez", "AV");
        cadastrarJogadorInicial(uru, 10, "Giorgian De Arrascaeta", "MED");
        cadastrarJogadorInicial(uru, 11, "Facundo Pellistri", "AV");
        cadastrarJogadorInicial(uru, 12, "Santiago Mele", "GR");
        cadastrarJogadorInicial(uru, 13, "Guillermo Varela", "DEF");
        cadastrarJogadorInicial(uru, 14, "Agustin Canobbio", "MED");
        cadastrarJogadorInicial(uru, 15, "Emiliano Martinez", "MED");
        cadastrarJogadorInicial(uru, 16, "Mathias Olivera", "DEF");
        cadastrarJogadorInicial(uru, 17, "Matias Vina", "DEF");
        cadastrarJogadorInicial(uru, 18, "Brian Rodriguez", "AV");
        cadastrarJogadorInicial(uru, 19, "Rodrigo Aguirre", "AV");
        cadastrarJogadorInicial(uru, 20, "Maxi Araujo", "MED");
        cadastrarJogadorInicial(uru, 21, "Federico Vinas", "AV");
        cadastrarJogadorInicial(uru, 22, "Joaquin Piquerez", "MED");
        cadastrarJogadorInicial(uru, 23, "Fernando Muslera", "GR");
        cadastrarJogadorInicial(uru, 24, "Santiago Bueno", "DEF");
        cadastrarJogadorInicial(uru, 25, "Juan Manuel Sanabria", "MED");
        cadastrarJogadorInicial(uru, 26, "Rodrigo Zalazar", "MED");
        selecoes.add(uru);

        Selecao fra = criarSelecao("França", 2, 17, "I");
        cadastrarJogadorInicial(fra, 1, "Brice Samba", "GR");
        cadastrarJogadorInicial(fra, 2, "Malo Gusto", "DEF");
        cadastrarJogadorInicial(fra, 3, "Lucas Digne", "DEF");
        cadastrarJogadorInicial(fra, 4, "Dayot Upamecano", "DEF");
        cadastrarJogadorInicial(fra, 5, "Jules Kounde", "DEF");
        cadastrarJogadorInicial(fra, 6, "Manu Kone", "MED");
        cadastrarJogadorInicial(fra, 7, "Ousmane Dembele", "AV");
        cadastrarJogadorInicial(fra, 8, "Aurelien Tchouameni", "MED");
        cadastrarJogadorInicial(fra, 9, "Marcus Thuram", "AV");
        cadastrarJogadorInicial(fra, 10, "Kylian Mbappe", "AV");
        cadastrarJogadorInicial(fra, 11, "Michael Olise", "AV");
        cadastrarJogadorInicial(fra, 12, "Bradley Barcola", "AV");
        cadastrarJogadorInicial(fra, 13, "Ngolo Kante", "MED");
        cadastrarJogadorInicial(fra, 14, "Adrien Rabiot", "MED");
        cadastrarJogadorInicial(fra, 15, "Ibrahima Konate", "DEF");
        cadastrarJogadorInicial(fra, 16, "Mike Maignan", "GR");
        cadastrarJogadorInicial(fra, 17, "William Saliba", "DEF");
        cadastrarJogadorInicial(fra, 18, "Warren Zaire-Emery", "MED");
        cadastrarJogadorInicial(fra, 19, "Theo Hernandez", "DEF");
        cadastrarJogadorInicial(fra, 20, "Desire Doue", "AV");
        cadastrarJogadorInicial(fra, 21, "Lucas Hernandez", "DEF");
        cadastrarJogadorInicial(fra, 22, "Jean-Philippe Mateta", "AV");
        cadastrarJogadorInicial(fra, 23, "Robin Risser", "GR");
        cadastrarJogadorInicial(fra, 24, "Rayan Cherki", "MED");
        cadastrarJogadorInicial(fra, 25, "Maghnes Akliouche", "MED");
        cadastrarJogadorInicial(fra, 26, "Maxence Lacroix", "DEF");
        selecoes.add(fra);

        Selecao irq = criarSelecao("Iraque", 55, 2, "I");
        cadastrarJogadorInicial(irq, 1, "Fahad Talib", "GR");
        cadastrarJogadorInicial(irq, 2, "Rebin Sulaka", "DEF");
        cadastrarJogadorInicial(irq, 3, "Hussein Ali", "DEF");
        cadastrarJogadorInicial(irq, 4, "Zaid Tahseen", "DEF");
        cadastrarJogadorInicial(irq, 5, "Akam Hashim", "DEF");
        cadastrarJogadorInicial(irq, 6, "Munaf Younus", "DEF");
        cadastrarJogadorInicial(irq, 7, "Youssef Amyn", "MED");
        cadastrarJogadorInicial(irq, 8, "Ibrahim Bayesh", "MED");
        cadastrarJogadorInicial(irq, 9, "Ali Alhamadi", "AV");
        cadastrarJogadorInicial(irq, 10, "Mohanad Ali", "AV");
        cadastrarJogadorInicial(irq, 11, "Ahmed Qasem", "AV");
        cadastrarJogadorInicial(irq, 12, "Jalal Hassan", "GR");
        cadastrarJogadorInicial(irq, 13, "Ali Yousif", "AV");
        cadastrarJogadorInicial(irq, 14, "Zidane Iqbal", "MED");
        cadastrarJogadorInicial(irq, 15, "Ahmed Maknazi", "DEF");
        cadastrarJogadorInicial(irq, 16, "Amir Alammari", "MED");
        cadastrarJogadorInicial(irq, 17, "Ali Jasim", "AV");
        cadastrarJogadorInicial(irq, 18, "Aymen Hussein", "AV");
        cadastrarJogadorInicial(irq, 19, "Kevin Yakob", "MED");
        cadastrarJogadorInicial(irq, 20, "Aimar Sher", "MED");
        cadastrarJogadorInicial(irq, 21, "Marko Farji", "AV");
        cadastrarJogadorInicial(irq, 22, "Ahmed Basil", "GR");
        cadastrarJogadorInicial(irq, 23, "Merchas Doski", "DEF");
        cadastrarJogadorInicial(irq, 24, "Zaid Ismael", "MED");
        cadastrarJogadorInicial(irq, 25, "Mustafa Saadoon", "DEF");
        cadastrarJogadorInicial(irq, 26, "Frans Putros", "DEF");
        selecoes.add(irq);

        Selecao nor = criarSelecao("Noruega", 44, 4, "I");
        cadastrarJogadorInicial(nor, 1, "Orjan Nyland", "GR");
        cadastrarJogadorInicial(nor, 2, "Morten Thorsby", "MED");
        cadastrarJogadorInicial(nor, 3, "Kristoffer Ajer", "DEF");
        cadastrarJogadorInicial(nor, 4, "Leo Ostigard", "DEF");
        cadastrarJogadorInicial(nor, 5, "David Moller Wolfe", "DEF");
        cadastrarJogadorInicial(nor, 6, "Patrick Berg", "MED");
        cadastrarJogadorInicial(nor, 7, "Alexander Sorloth", "AV");
        cadastrarJogadorInicial(nor, 8, "Sander Berge", "MED");
        cadastrarJogadorInicial(nor, 9, "Erling Haaland", "AV");
        cadastrarJogadorInicial(nor, 10, "Martin Odegaard", "MED");
        cadastrarJogadorInicial(nor, 11, "Jorgen Strand Larsen", "AV");
        cadastrarJogadorInicial(nor, 12, "Sander Tangvik", "GR");
        cadastrarJogadorInicial(nor, 13, "Egil Selvik", "GR");
        cadastrarJogadorInicial(nor, 14, "Fredrik Aursnes", "MED");
        cadastrarJogadorInicial(nor, 15, "Fredrik Andre Bjorkan", "DEF");
        cadastrarJogadorInicial(nor, 16, "Marcus Holmgren Pedersen", "DEF");
        cadastrarJogadorInicial(nor, 17, "Torbjorn Heggem", "DEF");
        cadastrarJogadorInicial(nor, 18, "Kristian Thorstvedt", "MED");
        cadastrarJogadorInicial(nor, 19, "Thelo Aasgaard", "MED");
        cadastrarJogadorInicial(nor, 20, "Antonio Nusa", "AV");
        cadastrarJogadorInicial(nor, 21, "Andreas Schjelderup", "MED");
        cadastrarJogadorInicial(nor, 22, "Oscar Bobb", "MED");
        cadastrarJogadorInicial(nor, 23, "Jens Petter Hauge", "MED");
        cadastrarJogadorInicial(nor, 24, "Sondre Langas", "DEF");
        cadastrarJogadorInicial(nor, 25, "Henrik Falchener", "DEF");
        cadastrarJogadorInicial(nor, 26, "Julian Ryerson", "AV");
        selecoes.add(nor);

        Selecao sen = criarSelecao("Senegal", 17, 4, "I");
        cadastrarJogadorInicial(sen, 1, "Yehvann Diouf", "GR");
        cadastrarJogadorInicial(sen, 2, "Mamadou Sarr", "DEF");
        cadastrarJogadorInicial(sen, 3, "Kalidou Koulibaly", "DEF");
        cadastrarJogadorInicial(sen, 4, "Abdoulaye Seck", "DEF");
        cadastrarJogadorInicial(sen, 5, "Idrissa Gana Gueye", "MED");
        cadastrarJogadorInicial(sen, 6, "Pathe Ciss", "MED");
        cadastrarJogadorInicial(sen, 7, "Assane Diao", "AV");
        cadastrarJogadorInicial(sen, 8, "Lamine Camara", "MED");
        cadastrarJogadorInicial(sen, 9, "Bamba Dieng", "AV");
        cadastrarJogadorInicial(sen, 10, "Sadio Mane", "AV");
        cadastrarJogadorInicial(sen, 11, "Nicolas Jackson", "AV");
        cadastrarJogadorInicial(sen, 12, "Cherif Ndiaye", "AV");
        cadastrarJogadorInicial(sen, 13, "Iliman Ndiaye", "AV");
        cadastrarJogadorInicial(sen, 14, "Ismail Jakobs", "DEF");
        cadastrarJogadorInicial(sen, 15, "Krepin Diatta", "DEF");
        cadastrarJogadorInicial(sen, 16, "Edouard Mendy", "GR");
        cadastrarJogadorInicial(sen, 17, "Pape Matar Sarr", "MED");
        cadastrarJogadorInicial(sen, 18, "Ismaila Sarr", "AV");
        cadastrarJogadorInicial(sen, 19, "Moussa Niakhate", "DEF");
        cadastrarJogadorInicial(sen, 20, "Ibrahim Mbaye", "AV");
        cadastrarJogadorInicial(sen, 21, "Habib Diarra", "MED");
        cadastrarJogadorInicial(sen, 22, "Bara Sapoko Ndiaye", "MED");
        cadastrarJogadorInicial(sen, 23, "Mory Diaw", "GR");
        cadastrarJogadorInicial(sen, 24, "Antoine Mendy", "DEF");
        cadastrarJogadorInicial(sen, 25, "El Hadji Malick Diouf", "DEF");
        cadastrarJogadorInicial(sen, 26, "Pape Gueye", "MED");
        selecoes.add(sen);

        Selecao arg = criarSelecao("Argentina", 1, 19, "J");
        cadastrarJogadorInicial(arg, 1, "Juan Musso", "GR");
        cadastrarJogadorInicial(arg, 2, "Marcos Senesi", "DEF");
        cadastrarJogadorInicial(arg, 3, "Nicolas Tagliafico", "DEF");
        cadastrarJogadorInicial(arg, 4, "Gonzalo Montiel", "DEF");
        cadastrarJogadorInicial(arg, 5, "Leandro Paredes", "MED");
        cadastrarJogadorInicial(arg, 6, "Lisandro Martinez", "DEF");
        cadastrarJogadorInicial(arg, 7, "Rodrigo De Paul", "MED");
        cadastrarJogadorInicial(arg, 8, "Valentin Barco", "MED");
        cadastrarJogadorInicial(arg, 9, "Julian Alvarez", "AV");
        cadastrarJogadorInicial(arg, 10, "Lionel Messi", "AV");
        cadastrarJogadorInicial(arg, 11, "Giovani Lo Celso", "MED");
        cadastrarJogadorInicial(arg, 12, "Geronimo Rulli", "GR");
        cadastrarJogadorInicial(arg, 13, "Cristian Romero", "DEF");
        cadastrarJogadorInicial(arg, 14, "Exequiel Palacios", "MED");
        cadastrarJogadorInicial(arg, 15, "Nico Gonzalez", "MED");
        cadastrarJogadorInicial(arg, 16, "Thiago Almada", "AV");
        cadastrarJogadorInicial(arg, 17, "Giuliano Simeone", "AV");
        cadastrarJogadorInicial(arg, 18, "Nico Paz", "AV");
        cadastrarJogadorInicial(arg, 19, "Nicolas Otamendi", "DEF");
        cadastrarJogadorInicial(arg, 20, "Alexis Mac Allister", "MED");
        cadastrarJogadorInicial(arg, 21, "Jose Manuel Lopez", "AV");
        cadastrarJogadorInicial(arg, 22, "Lautaro Martinez", "AV");
        cadastrarJogadorInicial(arg, 23, "Emiliano Martinez", "GR");
        cadastrarJogadorInicial(arg, 24, "Enzo Fernandez", "MED");
        cadastrarJogadorInicial(arg, 25, "Facundo Medina", "DEF");
        cadastrarJogadorInicial(arg, 26, "Nahuel Molina", "DEF");
        selecoes.add(arg);

        Selecao alg = criarSelecao("Argélia", 37, 5, "J");
        cadastrarJogadorInicial(alg, 1, "Melvin Mastil", "GR");
        cadastrarJogadorInicial(alg, 2, "Aissa Mandi", "DEF");
        cadastrarJogadorInicial(alg, 3, "Achref Abada", "DEF");
        cadastrarJogadorInicial(alg, 4, "Mohamed Amine Tougai", "DEF");
        cadastrarJogadorInicial(alg, 5, "Zineddine Belaid", "DEF");
        cadastrarJogadorInicial(alg, 6, "Ramiz Zerrouki", "MED");
        cadastrarJogadorInicial(alg, 7, "Riyad Mahrez", "AV");
        cadastrarJogadorInicial(alg, 8, "Houssem Aouar", "MED");
        cadastrarJogadorInicial(alg, 9, "Amine Gouiri", "AV");
        cadastrarJogadorInicial(alg, 10, "Fares Chaibi", "MED");
        cadastrarJogadorInicial(alg, 11, "Anis Hadj Moussa", "AV");
        cadastrarJogadorInicial(alg, 12, "Nadhir Benbouali", "AV");
        cadastrarJogadorInicial(alg, 13, "Jaouen Hadjam", "DEF");
        cadastrarJogadorInicial(alg, 14, "Hicham Boudaoui", "MED");
        cadastrarJogadorInicial(alg, 15, "Rayan Ait-Nouri", "DEF");
        cadastrarJogadorInicial(alg, 16, "Oussama Benbot", "GR");
        cadastrarJogadorInicial(alg, 17, "Ra K Belghali", "DEF");
        cadastrarJogadorInicial(alg, 18, "Mohamed Amoura", "AV");
        cadastrarJogadorInicial(alg, 19, "Nabil Bentaleb", "MED");
        cadastrarJogadorInicial(alg, 20, "Adil Boulbina", "AV");
        cadastrarJogadorInicial(alg, 21, "Ramy Bensebaini", "DEF");
        cadastrarJogadorInicial(alg, 22, "Ibrahim Maza", "MED");
        cadastrarJogadorInicial(alg, 23, "Luca Zidane", "GR");
        cadastrarJogadorInicial(alg, 24, "Yassine Titraoui", "MED");
        cadastrarJogadorInicial(alg, 25, "Fares Ghedjemis", "AV");
        cadastrarJogadorInicial(alg, 26, "Samir Chergui", "DEF");
        selecoes.add(alg);

        Selecao jor = criarSelecao("Jordânia", 68, 1, "J");
        cadastrarJogadorInicial(jor, 1, "Yazeed Abulaila", "GR");
        cadastrarJogadorInicial(jor, 2, "Mohammad Abuhasheesh", "DEF");
        cadastrarJogadorInicial(jor, 3, "Abdallah Nasib", "DEF");
        cadastrarJogadorInicial(jor, 4, "Husam Abudahab", "DEF");
        cadastrarJogadorInicial(jor, 5, "Yazan Alarab", "DEF");
        cadastrarJogadorInicial(jor, 6, "Amer Jamous", "MED");
        cadastrarJogadorInicial(jor, 7, "Mohammad Abuzraiq", "AV");
        cadastrarJogadorInicial(jor, 8, "Noor Alrawabdeh", "MED");
        cadastrarJogadorInicial(jor, 9, "Ali Olwan", "AV");
        cadastrarJogadorInicial(jor, 10, "Mousa Altamari", "AV");
        cadastrarJogadorInicial(jor, 11, "Odeh Fakhoury", "AV");
        cadastrarJogadorInicial(jor, 12, "Nour Baniateyah", "GR");
        cadastrarJogadorInicial(jor, 13, "Mahmoud Almardi", "AV");
        cadastrarJogadorInicial(jor, 14, "Rajaei Ayed", "MED");
        cadastrarJogadorInicial(jor, 15, "Ibrahim Sadeh", "MED");
        cadastrarJogadorInicial(jor, 16, "Mohammad Abualnadi", "DEF");
        cadastrarJogadorInicial(jor, 17, "Saleem Obaid", "DEF");
        cadastrarJogadorInicial(jor, 18, "Mohammad Abughoush", "MED");
        cadastrarJogadorInicial(jor, 19, "Saed Alrosan", "DEF");
        cadastrarJogadorInicial(jor, 20, "Mohannad Abutaha", "MED");
        cadastrarJogadorInicial(jor, 21, "Nizar Alrashdan", "MED");
        cadastrarJogadorInicial(jor, 22, "Abdallah Alfakhori", "GR");
        cadastrarJogadorInicial(jor, 23, "Ehsan Haddad", "DEF");
        cadastrarJogadorInicial(jor, 24, "Ali Azaizeh", "AV");
        cadastrarJogadorInicial(jor, 25, "Mohammad Aldaoud", "MED");
        cadastrarJogadorInicial(jor, 26, "Anas Badawi", "DEF");
        selecoes.add(jor);

        Selecao aut = criarSelecao("Áustria", 25, 8, "J");
        cadastrarJogadorInicial(aut, 1, "Alexander Schlager", "GR");
        cadastrarJogadorInicial(aut, 2, "David Affengruber", "DEF");
        cadastrarJogadorInicial(aut, 3, "Kevin Danso", "DEF");
        cadastrarJogadorInicial(aut, 4, "Xaver Schlager", "MED");
        cadastrarJogadorInicial(aut, 5, "Stefan Posch", "DEF");
        cadastrarJogadorInicial(aut, 6, "Nicolas Seiwald", "MED");
        cadastrarJogadorInicial(aut, 7, "Marko Arnautovic", "AV");
        cadastrarJogadorInicial(aut, 8, "David Alaba", "DEF");
        cadastrarJogadorInicial(aut, 9, "Marcel Sabitzer", "MED");
        cadastrarJogadorInicial(aut, 10, "Florian Grillitsch", "MED");
        cadastrarJogadorInicial(aut, 11, "Michael Gregoritsch", "AV");
        cadastrarJogadorInicial(aut, 12, "Florian Wiegele", "GR");
        cadastrarJogadorInicial(aut, 13, "Patrick Pentz", "GR");
        cadastrarJogadorInicial(aut, 14, "Sasa Kalajdzic", "AV");
        cadastrarJogadorInicial(aut, 15, "Philipp Lienhart", "DEF");
        cadastrarJogadorInicial(aut, 16, "Phillip Mwene", "DEF");
        cadastrarJogadorInicial(aut, 17, "Carney Chukwuemeka", "MED");
        cadastrarJogadorInicial(aut, 18, "Romano Schmid", "MED");
        cadastrarJogadorInicial(aut, 19, "Dejan Ljubicic", "MED");
        cadastrarJogadorInicial(aut, 20, "Konrad Laimer", "MED");
        cadastrarJogadorInicial(aut, 21, "Patrick Wimmer", "AV");
        cadastrarJogadorInicial(aut, 22, "Alexander Prass", "MED");
        cadastrarJogadorInicial(aut, 23, "Marco Friedl", "DEF");
        cadastrarJogadorInicial(aut, 24, "Paul Wanner", "MED");
        cadastrarJogadorInicial(aut, 25, "Michael Svoboda", "DEF");
        cadastrarJogadorInicial(aut, 26, "Alessandro Schoepf", "MED");
        selecoes.add(aut);

        Selecao col = criarSelecao("Colômbia", 13, 7, "K");
        cadastrarJogadorInicial(col, 1, "David Ospina", "GR");
        cadastrarJogadorInicial(col, 2, "Daniel Munoz", "DEF");
        cadastrarJogadorInicial(col, 3, "Jhon Lucumi", "DEF");
        cadastrarJogadorInicial(col, 4, "Santiago Arias", "DEF");
        cadastrarJogadorInicial(col, 5, "Kevin Castano", "MED");
        cadastrarJogadorInicial(col, 6, "Richard Rios", "MED");
        cadastrarJogadorInicial(col, 7, "Luis Diaz", "AV");
        cadastrarJogadorInicial(col, 8, "Jorge Carrascal", "MED");
        cadastrarJogadorInicial(col, 9, "Jhon Cordoba", "AV");
        cadastrarJogadorInicial(col, 10, "James Rodriguez", "MED");
        cadastrarJogadorInicial(col, 11, "Jhon Arias", "MED");
        cadastrarJogadorInicial(col, 12, "Camilo Vargas", "GR");
        cadastrarJogadorInicial(col, 13, "Yerry Mina", "DEF");
        cadastrarJogadorInicial(col, 14, "Gustavo Puerta", "DEF");
        cadastrarJogadorInicial(col, 15, "Juan Portilla", "MED");
        cadastrarJogadorInicial(col, 16, "Jefferson Lerma", "MED");
        cadastrarJogadorInicial(col, 17, "Johan Mojica", "DEF");
        cadastrarJogadorInicial(col, 18, "Willer Ditta", "DEF");
        cadastrarJogadorInicial(col, 19, "Cucho Hernandez", "AV");
        cadastrarJogadorInicial(col, 20, "Juan Quintero", "MED");
        cadastrarJogadorInicial(col, 21, "Jaminton Campaz", "AV");
        cadastrarJogadorInicial(col, 22, "Deiver Machado", "DEF");
        cadastrarJogadorInicial(col, 23, "Davinson Sanchez", "DEF");
        cadastrarJogadorInicial(col, 24, "Alvaro Montero", "GR");
        cadastrarJogadorInicial(col, 25, "Luis Suarez", "AV");
        cadastrarJogadorInicial(col, 26, "Andres Gomez", "AV");
        selecoes.add(col);

        Selecao por = criarSelecao("Portugal", 6, 9, "K");
        cadastrarJogadorInicial(por, 1, "Diogo Costa", "GR");
        cadastrarJogadorInicial(por, 2, "Nelson Semedo", "DEF");
        cadastrarJogadorInicial(por, 3, "Ruben Dias", "DEF");
        cadastrarJogadorInicial(por, 4, "Tomas Araujo", "DEF");
        cadastrarJogadorInicial(por, 5, "Diogo Dalot", "DEF");
        cadastrarJogadorInicial(por, 6, "Matheus Nunes", "MED");
        cadastrarJogadorInicial(por, 7, "Cristiano Ronaldo", "AV");
        cadastrarJogadorInicial(por, 8, "Bruno Fernandes", "MED");
        cadastrarJogadorInicial(por, 9, "Goncalo Ramos", "AV");
        cadastrarJogadorInicial(por, 10, "Bernardo Silva", "MED");
        cadastrarJogadorInicial(por, 11, "Joao Felix", "AV");
        cadastrarJogadorInicial(por, 12, "Jose Sa", "GR");
        cadastrarJogadorInicial(por, 13, "Renato Veiga", "DEF");
        cadastrarJogadorInicial(por, 14, "Goncalo Inacio", "DEF");
        cadastrarJogadorInicial(por, 15, "Joao Neves", "MED");
        cadastrarJogadorInicial(por, 16, "Francisco Trincao", "AV");
        cadastrarJogadorInicial(por, 17, "Rafael Leao", "AV");
        cadastrarJogadorInicial(por, 18, "Pedro Neto", "AV");
        cadastrarJogadorInicial(por, 19, "Goncalo Guedes", "AV");
        cadastrarJogadorInicial(por, 20, "Joao Cancelo", "DEF");
        cadastrarJogadorInicial(por, 21, "Ruben Neves", "MED");
        cadastrarJogadorInicial(por, 22, "Rui Silva", "GR");
        cadastrarJogadorInicial(por, 23, "Vitinha", "MED");
        cadastrarJogadorInicial(por, 24, "Samu Costa", "DEF");
        cadastrarJogadorInicial(por, 25, "Nuno Mendes", "DEF");
        cadastrarJogadorInicial(por, 26, "Francisco Conceicao", "AV");
        selecoes.add(por);

        Selecao cod = criarSelecao("RD Congo", 61, 2, "K");
        cadastrarJogadorInicial(cod, 1, "Lionel Mpasi", "GR");
        cadastrarJogadorInicial(cod, 2, "Aaron Wan-Bissaka", "DEF");
        cadastrarJogadorInicial(cod, 3, "Steve Kapuadi", "DEF");
        cadastrarJogadorInicial(cod, 4, "Axel Tuanzebe", "DEF");
        cadastrarJogadorInicial(cod, 5, "Dylan Batubinsika", "DEF");
        cadastrarJogadorInicial(cod, 6, "Ngalayel Mukau", "MED");
        cadastrarJogadorInicial(cod, 7, "Nathanael Mbuku", "MED");
        cadastrarJogadorInicial(cod, 8, "Samuel Moutoussamy", "MED");
        cadastrarJogadorInicial(cod, 9, "Brian Cipenga", "AV");
        cadastrarJogadorInicial(cod, 10, "Theo Bongonda", "MED");
        cadastrarJogadorInicial(cod, 11, "Gael Kakuta", "AV");
        cadastrarJogadorInicial(cod, 12, "Joris Kayembe", "DEF");
        cadastrarJogadorInicial(cod, 13, "Meschack Elia", "AV");
        cadastrarJogadorInicial(cod, 14, "Noah Sadiki", "MED");
        cadastrarJogadorInicial(cod, 15, "Aaron Tshibola", "MED");
        cadastrarJogadorInicial(cod, 16, "Timothy Fayulu", "GR");
        cadastrarJogadorInicial(cod, 17, "Cedric Bakambu", "AV");
        cadastrarJogadorInicial(cod, 18, "Charles Pickel", "MED");
        cadastrarJogadorInicial(cod, 19, "Fiston Mayele", "AV");
        cadastrarJogadorInicial(cod, 20, "Yoane Wissa", "AV");
        cadastrarJogadorInicial(cod, 21, "Matthieu Epolo", "GR");
        cadastrarJogadorInicial(cod, 22, "Chancel Mbemba", "DEF");
        cadastrarJogadorInicial(cod, 23, "Simon Banza", "AV");
        cadastrarJogadorInicial(cod, 24, "Gedeon Kalulu", "DEF");
        cadastrarJogadorInicial(cod, 25, "Edo Kayembe", "MED");
        cadastrarJogadorInicial(cod, 26, "Arthur Masuaku", "DEF");
        selecoes.add(cod);

        Selecao uzb = criarSelecao("Uzbequistão", 57, 1, "K");
        cadastrarJogadorInicial(uzb, 1, "Utkir Yusupov", "GR");
        cadastrarJogadorInicial(uzb, 2, "Abdukodir Khusanov", "DEF");
        cadastrarJogadorInicial(uzb, 3, "Khojiakbar Alijonov", "DEF");
        cadastrarJogadorInicial(uzb, 4, "Farrukh Sayfiev", "DEF");
        cadastrarJogadorInicial(uzb, 5, "Rustam Ashurmatov", "DEF");
        cadastrarJogadorInicial(uzb, 6, "Akmal Mozgovoy", "MED");
        cadastrarJogadorInicial(uzb, 7, "Otabek Shukurov", "MED");
        cadastrarJogadorInicial(uzb, 8, "Jamshid Iskanderov", "MED");
        cadastrarJogadorInicial(uzb, 9, "Odiljon Xamrobekov", "MED");
        cadastrarJogadorInicial(uzb, 10, "Ruslanbek Jiyanov", "MED");
        cadastrarJogadorInicial(uzb, 11, "Oston Urunov", "MED");
        cadastrarJogadorInicial(uzb, 12, "Abduvohid Nematov", "GR");
        cadastrarJogadorInicial(uzb, 13, "Sherzod Nasrullaev", "DEF");
        cadastrarJogadorInicial(uzb, 14, "Eldor Shomurodov", "AV");
        cadastrarJogadorInicial(uzb, 15, "Umar Eshmurodov", "DEF");
        cadastrarJogadorInicial(uzb, 16, "Botirali Ergashev", "GR");
        cadastrarJogadorInicial(uzb, 17, "Dostonbek Khamdamov", "MED");
        cadastrarJogadorInicial(uzb, 18, "Abdulla Abdullaev", "DEF");
        cadastrarJogadorInicial(uzb, 19, "Azizjon Ganiev", "MED");
        cadastrarJogadorInicial(uzb, 20, "Azizbek Amonov", "AV");
        cadastrarJogadorInicial(uzb, 21, "Igor Sergeev", "AV");
        cadastrarJogadorInicial(uzb, 22, "Abbosbek Fayzullaev", "MED");
        cadastrarJogadorInicial(uzb, 23, "Sherzod Esanov", "MED");
        cadastrarJogadorInicial(uzb, 24, "Behruzjon Karimov", "DEF");
        cadastrarJogadorInicial(uzb, 25, "Avazbek Ulmasaliyev", "DEF");
        cadastrarJogadorInicial(uzb, 26, "Jakhongir Urozov", "DEF");
        selecoes.add(uzb);

        Selecao cro = criarSelecao("Croácia", 10, 7, "L");
        cadastrarJogadorInicial(cro, 1, "Dominik Livakovic", "GR");
        cadastrarJogadorInicial(cro, 2, "Josip Stanisic", "DEF");
        cadastrarJogadorInicial(cro, 3, "Marin Pongracic", "DEF");
        cadastrarJogadorInicial(cro, 4, "Josko Gvardiol", "DEF");
        cadastrarJogadorInicial(cro, 5, "Duje Caleta-Car", "DEF");
        cadastrarJogadorInicial(cro, 6, "Josip Sutalo", "DEF");
        cadastrarJogadorInicial(cro, 7, "Nikola Moro", "MED");
        cadastrarJogadorInicial(cro, 8, "Mateo Kovacic", "MED");
        cadastrarJogadorInicial(cro, 9, "Andrej Kramaric", "AV");
        cadastrarJogadorInicial(cro, 10, "Luka Modric", "MED");
        cadastrarJogadorInicial(cro, 11, "Ante Budimir", "AV");
        cadastrarJogadorInicial(cro, 12, "Ivor Pandur", "GR");
        cadastrarJogadorInicial(cro, 13, "Nikola Vlasic", "MED");
        cadastrarJogadorInicial(cro, 14, "Ivan Perisic", "AV");
        cadastrarJogadorInicial(cro, 15, "Mario Pasalic", "MED");
        cadastrarJogadorInicial(cro, 16, "Martin Baturina", "MED");
        cadastrarJogadorInicial(cro, 17, "Petar Sucic", "MED");
        cadastrarJogadorInicial(cro, 18, "Kristijan Jakic", "DEF");
        cadastrarJogadorInicial(cro, 19, "Toni Fruk", "MED");
        cadastrarJogadorInicial(cro, 20, "Igor Matanovic", "AV");
        cadastrarJogadorInicial(cro, 21, "Luka Sucic", "MED");
        cadastrarJogadorInicial(cro, 22, "Luka Vuskovic", "DEF");
        cadastrarJogadorInicial(cro, 23, "Dominik Kotarski", "GR");
        cadastrarJogadorInicial(cro, 24, "Marco Pasalic", "AV");
        cadastrarJogadorInicial(cro, 25, "Martin Erlic", "DEF");
        cadastrarJogadorInicial(cro, 26, "Petar Musa", "AV");
        selecoes.add(cro);

        Selecao gha = criarSelecao("Gana", 62, 5, "L");
        cadastrarJogadorInicial(gha, 1, "Lawrence Ati Zigi", "GR");
        cadastrarJogadorInicial(gha, 2, "Alidu Seidu", "DEF");
        cadastrarJogadorInicial(gha, 3, "Caleb Yirenkyi", "MED");
        cadastrarJogadorInicial(gha, 4, "Jonas Adjetey", "DEF");
        cadastrarJogadorInicial(gha, 5, "Thomas Partey", "MED");
        cadastrarJogadorInicial(gha, 6, "Abdul Mumin", "DEF");
        cadastrarJogadorInicial(gha, 7, "Abdul Fatawu", "AV");
        cadastrarJogadorInicial(gha, 8, "Kwasi Sibo", "MED");
        cadastrarJogadorInicial(gha, 9, "Jordan Ayew", "AV");
        cadastrarJogadorInicial(gha, 10, "Brandon Thomas-Asante", "AV");
        cadastrarJogadorInicial(gha, 11, "Antoine Semenyo", "MED");
        cadastrarJogadorInicial(gha, 12, "Joseph Anang", "GR");
        cadastrarJogadorInicial(gha, 13, "Christopher Bonsu Baah", "AV");
        cadastrarJogadorInicial(gha, 14, "Gideon Mensah", "DEF");
        cadastrarJogadorInicial(gha, 15, "Elisha Owusu", "MED");
        cadastrarJogadorInicial(gha, 16, "Benjamin Asare", "GR");
        cadastrarJogadorInicial(gha, 17, "Baba Rahman", "DEF");
        cadastrarJogadorInicial(gha, 18, "Jerome Opoku", "DEF");
        cadastrarJogadorInicial(gha, 19, "Inaki Williams", "AV");
        cadastrarJogadorInicial(gha, 20, "Augustine Boakye", "MED");
        cadastrarJogadorInicial(gha, 21, "Kojo Peprah Oppong", "DEF");
        cadastrarJogadorInicial(gha, 22, "Kamaldeen Sulemana", "AV");
        cadastrarJogadorInicial(gha, 23, "Derrick Luckassen", "DEF");
        cadastrarJogadorInicial(gha, 24, "Ernest Nuamah", "AV");
        cadastrarJogadorInicial(gha, 25, "Prince Adu", "AV");
        cadastrarJogadorInicial(gha, 26, "Marvin Senaya", "DEF");
        selecoes.add(gha);

        Selecao eng = criarSelecao("Inglaterra", 4, 17, "L");
        cadastrarJogadorInicial(eng, 1, "Jordan Pickford", "GR");
        cadastrarJogadorInicial(eng, 2, "Ezri Konsa", "DEF");
        cadastrarJogadorInicial(eng, 3, "Nico Oreilly", "DEF");
        cadastrarJogadorInicial(eng, 4, "Declan Rice", "MED");
        cadastrarJogadorInicial(eng, 5, "John Stones", "DEF");
        cadastrarJogadorInicial(eng, 6, "Marc Guehi", "DEF");
        cadastrarJogadorInicial(eng, 7, "Bukayo Saka", "AV");
        cadastrarJogadorInicial(eng, 8, "Elliot Anderson", "MED");
        cadastrarJogadorInicial(eng, 9, "Harry Kane", "AV");
        cadastrarJogadorInicial(eng, 10, "Jude Bellingham", "MED");
        cadastrarJogadorInicial(eng, 11, "Marcus Rashford", "AV");
        cadastrarJogadorInicial(eng, 12, "Trevoh Chalobah", "DEF");
        cadastrarJogadorInicial(eng, 13, "Dean Henderson", "GR");
        cadastrarJogadorInicial(eng, 14, "Jordan Henderson", "MED");
        cadastrarJogadorInicial(eng, 15, "Dan Burn", "DEF");
        cadastrarJogadorInicial(eng, 16, "Kobbie Mainoo", "MED");
        cadastrarJogadorInicial(eng, 17, "Morgan Rogers", "MED");
        cadastrarJogadorInicial(eng, 18, "Anthony Gordon", "AV");
        cadastrarJogadorInicial(eng, 19, "Ollie Watkins", "AV");
        cadastrarJogadorInicial(eng, 20, "Noni Madueke", "AV");
        cadastrarJogadorInicial(eng, 21, "Eberechi Eze", "MED");
        cadastrarJogadorInicial(eng, 22, "Ivan Toney", "AV");
        cadastrarJogadorInicial(eng, 23, "James Trafford", "GR");
        cadastrarJogadorInicial(eng, 24, "Reece James", "DEF");
        cadastrarJogadorInicial(eng, 25, "Djed Spence", "DEF");
        cadastrarJogadorInicial(eng, 26, "Jarell Quansah", "DEF");
        selecoes.add(eng);

        Selecao pan = criarSelecao("Panamá", 35, 2, "L");
        cadastrarJogadorInicial(pan, 1, "Luis Mejia", "GR");
        cadastrarJogadorInicial(pan, 2, "Cesar Blackman", "DEF");
        cadastrarJogadorInicial(pan, 3, "Jose Cordoba", "DEF");
        cadastrarJogadorInicial(pan, 4, "Fidel Escobar", "DEF");
        cadastrarJogadorInicial(pan, 5, "Edgardo Farina", "DEF");
        cadastrarJogadorInicial(pan, 6, "Cristian Martinez", "MED");
        cadastrarJogadorInicial(pan, 7, "Jose Luis Rodriguez", "MED");
        cadastrarJogadorInicial(pan, 8, "Adalberto Carrasquilla", "MED");
        cadastrarJogadorInicial(pan, 9, "Tomas Rodriguez", "AV");
        cadastrarJogadorInicial(pan, 10, "Ismael Diaz", "MED");
        cadastrarJogadorInicial(pan, 11, "Edgar Yoel Barcenas", "MED");
        cadastrarJogadorInicial(pan, 12, "Cesar Samudio", "GR");
        cadastrarJogadorInicial(pan, 13, "Jiovany Ramos", "DEF");
        cadastrarJogadorInicial(pan, 14, "Carlos Harvey", "DEF");
        cadastrarJogadorInicial(pan, 15, "Eric Davis", "DEF");
        cadastrarJogadorInicial(pan, 16, "Andres Andrade", "DEF");
        cadastrarJogadorInicial(pan, 17, "Jose Fajardo", "AV");
        cadastrarJogadorInicial(pan, 18, "Cecilio Waterman", "AV");
        cadastrarJogadorInicial(pan, 19, "Alberto Quintero", "MED");
        cadastrarJogadorInicial(pan, 20, "Anibal Godoy", "MED");
        cadastrarJogadorInicial(pan, 21, "Cesar Yanis", "MED");
        cadastrarJogadorInicial(pan, 22, "Orlando Mosquera", "GR");
        cadastrarJogadorInicial(pan, 23, "Amir Murillo", "DEF");
        cadastrarJogadorInicial(pan, 24, "Azarias Londono", "AV");
        cadastrarJogadorInicial(pan, 25, "Roderick Miller", "DEF");
        cadastrarJogadorInicial(pan, 26, "Jorge Gutierrez", "DEF");
        selecoes.add(pan);

        return selecoes;
    }

    private static Selecao criarSelecao(String pais, int ranking, int participacoes, String grupo) {
        Selecao selecao = new Selecao(pais, ranking, participacoes, grupo);
        selecao.setEstadia(new Estadia("", ""));
        return selecao;
    }

    private static void cadastrarJogadorInicial(Selecao selecao, int numero, String nome, String posicao) {
        selecao.getJogadores().add(new Jogador(numero, formatarNomeJogador(nome), posicao));
    }

    private static String formatarNomeJogador(String nomeOriginal) {
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            return "";
        }

        String nome = nomeOriginal.trim().replaceAll("\\s+", " ");
        String[] partes = nome.split(" ");

        if (partes.length < 2) {
            return capitalizar(nome);
        }

        int indicePrimeiroNome = 0;

        while (indicePrimeiroNome < partes.length && estaEmMaiusculas(partes[indicePrimeiroNome])) {
            indicePrimeiroNome++;
        }

        if (indicePrimeiroNome == 0 || indicePrimeiroNome >= partes.length) {
            return capitalizarNomeComposto(nome);
        }

        StringBuilder nomesProprios = new StringBuilder();
        for (int i = indicePrimeiroNome; i < partes.length; i++) {
            if (nomesProprios.length() > 0) {
                nomesProprios.append(" ");
            }
            nomesProprios.append(capitalizar(partes[i]));
        }

        StringBuilder apelidos = new StringBuilder();
        for (int i = 0; i < indicePrimeiroNome; i++) {
            if (apelidos.length() > 0) {
                apelidos.append(" ");
            }
            apelidos.append(capitalizar(partes[i]));
        }

        return nomesProprios + " " + apelidos;
    }

    private static boolean estaEmMaiusculas(String texto) {
        return texto.equals(texto.toUpperCase()) && texto.matches(".*[A-ZÁÉÍÓÚÂÊÎÔÛÃÕÇÑ].*");
    }

    private static String capitalizarNomeComposto(String texto) {
        String[] partes = texto.split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String parte : partes) {
            if (resultado.length() > 0) {
                resultado.append(" ");
            }
            resultado.append(capitalizar(parte));
        }

        return resultado.toString();
    }

    private static String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }

        String[] partes = texto.toLowerCase().split("-");
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < partes.length; i++) {
            if (i > 0) {
                resultado.append("-");
            }

            String parte = partes[i];
            if (parte.isBlank()) {
                continue;
            }

            resultado.append(parte.substring(0, 1).toUpperCase());
            if (parte.length() > 1) {
                resultado.append(parte.substring(1));
            }
        }

        return resultado.toString();
    }

    public void inicializarSelecoes() {
        this.selecoesParticipantes = criarSelecoesMundial2026();
    }

    public void inicializarArbitros(MundialController controller) {
        // --- 52 ÁRBITROS PRINCIPAIS (Referees) ---
        controller.adicionarArbitro(new Arbitro("Abdulrahman Al-Jassim", "Catarina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Khalid Al-Turais", "Saudita", "Principal"));
        controller.adicionarArbitro(new Arbitro("Yusuke Araki", "Japonesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Omar Abdulkadir Artan", "Somali", "Principal"));
        controller.adicionarArbitro(new Arbitro("Pierre Atcho", "Gabonesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Iván Barton", "Salvadorenha", "Principal"));
        controller.adicionarArbitro(new Arbitro("Dahane Beida", "Mauritana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Juan Gabriel Benítez", "Paraguaia", "Principal"));
        controller.adicionarArbitro(new Arbitro("Juan Calderón", "Costa-riquenha", "Principal"));
        controller.adicionarArbitro(new Arbitro("Raphael Claus", "Brasileira", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ismail Elfath", "Americana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Espen Eskås", "Norueguesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Alireza Faghani", "Australiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Yael Falcón Pérez", "Argentina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Drew Fischer", "Canadiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Cristian Garay", "Chilena", "Principal"));
        controller.adicionarArbitro(new Arbitro("Katia García", "Mexicana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Mustapha Ghorbal", "Argelina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Alejandro Hernández", "Espanhola", "Principal"));
        controller.adicionarArbitro(new Arbitro("Darío Herrera", "Argentina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Jalal Jayed", "Marroquina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Campbell-Kirk Kawana-Waugh", "Neo-zelandesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("István Kovács", "Romena", "Principal"));
        controller.adicionarArbitro(new Arbitro("François Letexier", "Francesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ma Ning", "Chinesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Adham Makhadmeh", "Jordana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Danny Makkelie", "Holandesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Szymon Marciniak", "Polaca", "Principal"));
        controller.adicionarArbitro(new Arbitro("Maurizio Mariani", "Italiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Héctor Said Martínez", "Hondurenha", "Principal"));
        controller.adicionarArbitro(new Arbitro("Amin Mohamed", "Egípcia", "Principal"));
        controller.adicionarArbitro(new Arbitro("Oshane Nation", "Jamaicana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Glenn Nyberg", "Sueca", "Principal"));
        controller.adicionarArbitro(new Arbitro("Michael Oliver", "Inglesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Omar Al Ali", "Emirense", "Principal"));
        controller.adicionarArbitro(new Arbitro("Kevin Ortega", "Peruana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Tori Penso", "Americana", "Principal"));
        controller.adicionarArbitro(new Arbitro("João Pinheiro", "Portuguesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ramon Abatti", "Brasileira", "Principal"));
        controller.adicionarArbitro(new Arbitro("César Ramos", "Mexicana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Andrés Rojas", "Colombiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Sandro Schärer", "Suíça", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ilgiz Tantashev", "Usbeque", "Principal"));
        controller.adicionarArbitro(new Arbitro("Anthony Taylor", "Inglesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Gustavo Tejera", "Uruguaia", "Principal"));
        controller.adicionarArbitro(new Arbitro("Facundo Tello", "Argentina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Abongile Tom", "Sul-africana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Clément Turpin", "Francesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Jesús Valenzuela", "Venezuelana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Slavko Vinčić", "Eslovena", "Principal"));
        controller.adicionarArbitro(new Arbitro("Wilton Sampaio", "Brasileira", "Principal"));
        controller.adicionarArbitro(new Arbitro("Felix Zwayer", "Alemã", "Principal"));

        // --- 88 ÁRBITROS ASSISTENTES (Assistant Referees) ---
        controller.adicionarArbitro(new Arbitro("Amos Abeigne", "Gabonesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("George Lakrindis", "Australiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mahmoud Abouelregal", "Egípcia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("James Lindsay", "Jamaicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mostafa Akarkad", "Marroquina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Tomasz Listkiewicz", "Polaca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mohammed Al Abakry", "Saudita", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Walter López", "Hondurenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mohamed Al Hammadi", "Emirense", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Luciano Maia", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mohammad Al Kalaf", "Jordana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("James Mainwaring", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Saoud Al Maqaleh", "Catarina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mihai Marica", "Romena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Taleb Al Marri", "Catarina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Brooke Mayo", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Ahmad Al Roalle", "Jordana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jun Mihara", "Japonesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Lyes Arfa", "Canadiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Juan Carlos Mora", "Costa-riquenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Kyle Atkins", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("David Morán", "Salvadorenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Carlos Barreiro", "Uruguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Tulio Moreno", "Venezuelana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Micheal Barwegen", "Canadiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Alberto Morín", "Mexicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Isaak Bashevkin", "Norueguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Cyril Mugnier", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Adam Kupsik", "Polaca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("José Enrique Naranjo Pérez", "Espanhola", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mahbod Beigi", "Sueca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Cristian Navarro", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Juan Pablo Belatti", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Kathryn Nesbitt", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Gary Beswick", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Elvis Noupue", "Camandresa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Daniele Bindoni", "Italiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Adam Nunn", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Marco Bisguerra", "Mexicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Michael Orue", "Peruana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Zakaria Brinsi", "Marroquina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Benjamin Pages", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Bruno Boschilia", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Corey Parker", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Bruno Pires", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Antonio Pupiro", "Salvadorenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Stuart Burt", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Rafael Alves", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Eduardo Cardozo", "Paraguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mehdi Rahmouni", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Gabriel Chade", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Christian Ramírez", "Hondurenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Danilo Manis", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Sandra Ramírez", "Mexicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Nicolas Danos", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("José Retamal", "Chilena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Stéphane De Almeida", "Suíça", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Miguel Rocha", "Portuguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jan de Vries", "Holandesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Facundo Rodríguez", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Maximiliano Del Yesso", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Milcíades Saldívar", "Paraguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Christian Dietz", "Alemã", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Diego Sánchez", "Costa-riquenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Boris Ditsoga", "Gabonesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Zakhele Siwela", "Sul-africana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jan Erik Engan", "Norueguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Andreas Söderkvist", "Sueca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Rodrigo Figueiredo", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Hessel Steegstra", "Holandesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Timur Gaynullin", "Usbeque", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Nicolás Tarán", "Uruguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mokrane Gourari", "Argelina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Alberto Tegoni", "Italiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Alexander Guzmán", "Colombiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Isaac Trevis", "Neo-zelandesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Ahmed Hossam Taha", "Egípcia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Andrey Tsapenko", "Usbeque", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jerson Santos", "Angolana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Ferencz Tunyogi", "Romena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Bruno Jesus", "Portuguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jorge Urrego", "Venezuelana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Robert Kempter", "Alemã", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Caleb Wales", "Trindadense", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Tomaž Klančnik", "Eslovena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Abbes Akram Zerhouni", "Argelina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Andraž Kovačič", "Eslovena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Zhou Fei", "Chinesa", "Assistente"));

        // --- 30 VIDEOÁRBITROS (Video Match Officials - VAR) ---
        controller.adicionarArbitro(new Arbitro("Khamis Al-Marri", "Catarina", "VAR"));
        controller.adicionarArbitro(new Arbitro("Abdullah Alshehri", "Saudita", "VAR"));
        controller.adicionarArbitro(new Arbitro("Mahmoud Ashour", "Egípcia", "VAR"));
        controller.adicionarArbitro(new Arbitro("Ivan Bebek", "Croata", "VAR"));
        controller.adicionarArbitro(new Arbitro("Jérôme Brisard", "Francesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Bastian Dankert", "Alemã", "VAR"));
        controller.adicionarArbitro(new Arbitro("Carlos del Cerro Grande", "Espanhola", "VAR"));
        controller.adicionarArbitro(new Arbitro("Willy Delajod", "Francesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Marco Di Bello", "Italiana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Joe Dickerson", "Americana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Hamza El Fariq", "Marroquina", "VAR"));
        controller.adicionarArbitro(new Arbitro("Shaun Evans", "Australiana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Fu Ming", "Chinesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Nicolás Gallo", "Colombiana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Antonio García", "Uruguaia", "VAR"));
        controller.adicionarArbitro(new Arbitro("Jarred Gillett", "Inglesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Leodán González", "Uruguaia", "VAR"));
        controller.adicionarArbitro(new Arbitro("Tatiana Guzmán", "Nicaraguense", "VAR"));
        controller.adicionarArbitro(new Arbitro("Dennis Higler", "Holandesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Tomasz Kwiatkowski", "Polaca", "VAR"));
        controller.adicionarArbitro(new Arbitro("Juan Lara", "Chilena", "VAR"));
        controller.adicionarArbitro(new Arbitro("Hernán Mastrángelo", "Argentina", "VAR"));
        controller.adicionarArbitro(new Arbitro("Erick Miranda", "Mexicana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Mohammed Obaid Khadim", "Emirense", "VAR"));
        controller.adicionarArbitro(new Arbitro("Guillermo Pacheco", "Mexicana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Fedayi San", "Suíça", "VAR"));
        controller.adicionarArbitro(new Arbitro("Juan Soto", "Venezuelana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Rodolpho Toski", "Brasileira", "VAR"));
        controller.adicionarArbitro(new Arbitro("Bram Van Driessche", "Belga", "VAR"));
        controller.adicionarArbitro(new Arbitro("Armando Villarreal", "Americana", "VAR"));
    }

    public void inicializarJogos(MundialController controller){
        controller.adicionarJogo(new Jogo("Quinta-Feira 11 Junho 2026", "16:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio da Cidade do México", "Cidade do México", "México", "MEX", "Mexicana", "África do Sul", "RSA", "Sul-africana"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 12 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Guadalajara", "Guadalajara", "República da Coreia", "KOR", "Sul-coreana", "Tchéquia", "CZE", "Checa"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 12 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio de Toronto", "Toronto", "Canadá", "CAN", "Canadiana", "Bósnia e Herzegovina", "BIH", "Bósnia"));
        controller.adicionarJogo(new Jogo("Sábado 13 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio de Los Angeles", "Los Angeles", "EUA", "USA", "Americana", "Paraguai", "PAR", "Paraguaia"));
        controller.adicionarJogo(new Jogo("Sábado 13 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Catar", "QAT", "Catarina", "Suíça", "SUI", "Suíça"));
        controller.adicionarJogo(new Jogo("Sábado 13 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Brasil", "BR", "Brasileira", "Marrocos", "MAR", "Marroquina"));
        controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Boston", "Boston", "Haiti", "HAI", "Haitiana", "Escócia", "SCO", "Escocesa"));
        controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo D", "BC Place de Vancouver", "Vancouver", "Austrália", "AUS", "Australiana", "Turquia", "TUR", "Turca"));
        controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Houston", "Houston", "Alemanha", "GER", "Alemã", "Curaçau", "CUW", "Curaçalense"));
        controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Dallas", "Dallas", "Holanda", "NED", "Holandesa", "Japão", "JPN", "Japonesa"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Filadélfia", "Filadélfia", "Costa do Marfim", "CIV", "Malandresa", "Equador", "ECU", "Equatoriana"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Monterrey", "Monterrey", "Suécia", "SWE", "Sueca", "Tunísia", "TUN", "Tunisina"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Atlanta", "Atlanta", "Espanha", "ESP", "Espanhola", "Cabo Verde", "CPV", "Cabo-verdiana"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Seattle", "Seattle", "Bélgica", "BEL", "Belga", "Egito", "EGY", "Egípcia"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Miami", "Miami", "Arábia Saudita", "KSA", "Saudita", "Uruguai", "URU", "Uruguaia"));
        controller.adicionarJogo(new Jogo("Terça-Feira 16 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Los Angeles", "Los Angeles", "RI do Irã", "IRN", "Iraniana", "Nova Zelândia", "NZL", "Neo-zelandesa"));
        controller.adicionarJogo(new Jogo("Terça-Feira 16 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "França", "FRA", "Francesa", "Senegal", "SEN", "Senegalesa"));
        controller.adicionarJogo(new Jogo("Terça-Feira 16 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Boston", "Boston", "Iraque", "IRQ", "Iraquiana", "Noruega", "NOR", "Norueguesa"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Kansas City", "Kansas City", "Argentina", "ARG", "Argentina", "Argélia", "ALG", "Argelina"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "05:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Áustria", "AUT", "Austríaca", "Jordânia", "JOR", "Jordana"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Houston", "Houston", "Portugal", "PT", "Portuguesa", "RD do Congo", "COD", "Congolesa"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Dallas", "Dallas", "Inglaterra", "ENG", "Inglesa", "Croácia", "CRO", "Croata"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Toronto", "Toronto", "Gana", "GHA", "Ganesa", "Panamá", "PAN", "Panamiana"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio da Cidade do México", "Cidade do México", "Uzbequistão", "UZB", "Usbeque", "Colômbia", "COL", "Colombiana"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "17:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Atlanta", "Atlanta", "Tchéquia", "CZE", "Checa", "África do Sul", "RSA", "Sul-africana"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio de Los Angeles", "Los Angeles", "Suíça", "SUI", "Suíça", "Bósnia e Herzegovina", "BIH", "Bósnia"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo B", "BC Place de Vancouver", "Vancouver", "Canadá", "CAN", "Canadiana", "Catar", "QAT", "Catarina"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 19 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Guadalajara", "Guadalajara", "México", "MEX", "Mexicana", "República da Coreia", "KOR", "Sul-coreana"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 19 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio de Seattle", "Seattle", "EUA", "USA", "Americana", "Austrália", "AUS", "Australiana"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 19 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Boston", "Boston", "Escócia", "SCO", "Escocesa", "Marrocos", "MAR", "Marroquina"));
        controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "01:30", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Filadélfia", "Filadélfia", "Brasil", "BR", "Brasileira", "Haiti", "HAI", "Haitiana"));
        controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Turquia", "TUR", "Turca", "Paraguai", "PAR", "Paraguaia"));
        controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Houston", "Houston", "Holanda", "NED", "Holandesa", "Suécia", "SWE", "Sueca"));
        controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Toronto", "Toronto", "Alemanha", "GER", "Alemã", "Costa do Marfim", "CIV", "Malandresa"));
        controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Kansas City", "Kansas City", "Equador", "ECU", "Equatoriana", "Curaçau", "CUW", "Curaçalense"));
        controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "05:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Monterrey", "Monterrey", "Tunísia", "TUN", "Tunisina", "Japão", "JPN", "Japonesa"));
        controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "17:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Atlanta", "Atlanta", "Espanha", "ESP", "Espanhola", "Arábia Saudita", "KSA", "Saudita"));
        controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Los Angeles", "Los Angeles", "Bélgica", "BEL", "Belga", "RI do Irã", "IRN", "Iraniana"));
        controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Miami", "Miami", "Uruguai", "URU", "Uruguaia", "Cabo Verde", "CPV", "Cabo-verdiana"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 22 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo G", "BC Place de Vancouver", "Vancouver", "Nova Zelândia", "NZL", "Neo-zelandesa", "Egito", "EGY", "Egípcia"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 22 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Dallas", "Dallas", "Argentina", "ARG", "Argentina", "Áustria", "AUT", "Austríaca"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 22 Junho 2026", "22:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Filadélfia", "Filadélfia", "França", "FRA", "Francesa", "Iraque", "IRQ", "Iraquiana"));
        controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Noruega", "NOR", "Norueguesa", "Senegal", "SEN", "Senegalesa"));
        controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Jordânia", "JOR", "Jordana", "Argélia", "ALG", "Argelina"));
        controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Houston", "Houston", "Portugal", "PT", "Portuguesa", "Uzbequistão", "UZB", "Usbeque"));
        controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Boston", "Boston", "Inglaterra", "ENG", "Inglesa", "Gana", "GHA", "Ganesa"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Toronto", "Toronto", "Panamá", "PAN", "Panamiana", "Croácia", "CRO", "Croata"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Guadalajara", "Guadalajara", "Colômbia", "COL", "Colombiana", "RD do Congo", "COD", "Congolesa"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo B", "BC Place de Vancouver", "Vancouver", "Suíça", "SUI", "Suíça", "Canadá", "CAN", "Canadiana"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio de Seattle", "Seattle", "Bósnia e Herzegovina", "BIH", "Bósnia", "Catar", "QAT", "Catarina"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Miami", "Miami", "Escócia", "SCO", "Escocesa", "Brasil", "BR", "Brasileira"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Atlanta", "Atlanta", "Marrocos", "MAR", "Marroquina", "Haiti", "HAI", "Haitiana"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio da Cidade do México", "Cidade do México", "Tchéquia", "CZE", "Checa", "México", "MEX", "Mexicana"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Monterrey", "Monterrey", "África do Sul", "RSA", "Sul-africana", "República da Coreia", "KOR", "Sul-coreana"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Filadélfia", "Filadélfia", "Curaçau", "CUW", "Curaçalense", "Costa do Marfim", "CIV", "Malandresa"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Equador", "ECU", "Equatoriana", "Alemanha", "GER", "Alemã"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Dallas", "Dallas", "Japão", "JPN", "Japonesa", "Suécia", "SWE", "Sueca"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Kansas City", "Kansas City", "Tunísia", "TUN", "Tunisina", "Holanda", "NED", "Holandesa"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio de Los Angeles", "Los Angeles", "Turquia", "TUR", "Turca", "EUA", "USA", "Americana"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Paraguai", "PAR", "Paraguaia", "Austrália", "AUS", "Australiana"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Boston", "Boston", "Noruega", "NOR", "Norueguesa", "França", "FRA", "Francesa"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Toronto", "Toronto", "Senegal", "SEN", "Senegalesa", "Iraque", "IRQ", "Iraquiana"));
        controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Houston", "Houston", "Cabo Verde", "CPV", "Cabo-verdiana", "Arábia Saudita", "KSA", "Saudita"));
        controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Guadalajara", "Guadalajara", "Uruguai", "URU", "Uruguaia", "Espanha", "ESP", "Espanhola"));
        controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Seattle", "Seattle", "Egito", "EGY", "Egípcia", "RI do Irã", "IRN", "Iraniana"));
        controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo G", "BC Place de Vancouver", "Vancouver", "Nova Zelândia", "NZL", "Neo-zelandesa", "Bélgica", "BEL", "Belga"));
        controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "22:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Panamá", "PAN", "Panamiana", "Inglaterra", "ENG", "Inglesa"));
        controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "22:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Filadélfia", "Filadélfia", "Croácia", "CRO", "Croata", "Gana", "GHA", "Ganesa"));
        controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "00:30", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Miami", "Miami", "Colômbia", "COL", "Colombiana", "Portugal", "PT", "Portuguesa"));
        controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "00:30", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Atlanta", "Atlanta", "RD do Congo", "COD", "Congolesa", "Uzbequistão", "UZB", "Usbeque"));
        controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Kansas City", "Kansas City", "Argélia", "ALG", "Argelina", "Áustria", "AUT", "Austríaca"));
        controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Dallas", "Dallas", "Jordânia", "JOR", "Jordana", "Argentina", "ARG", "Argentina"));

        // Segundas de final
        controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "20:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Los Angeles", "Los Angeles", "2A", "2A", "Apurada", "2B", "2B", "Apurada"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 29 Junho 2026", "18:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Houston", "Houston", "1C", "1C", "Apurada", "2F", "2F", "Apurada"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 29 Junho 2026", "21:30", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Boston", "Boston", "1E", "1E", "Apurada", "3ABCDF", "3AB", "Apurada"));
        controller.adicionarJogo(new Jogo("Terça-Feira 30 Junho 2026", "02:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Monterrey", "Monterrey", "1F", "1F", "Apurada", "2C", "2C", "Apurada"));
        controller.adicionarJogo(new Jogo("Terça-Feira 30 Junho 2026", "18:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Dallas", "Dallas", "2E", "2E", "Apurada", "2I", "2I", "Apurada"));
        controller.adicionarJogo(new Jogo("Terça-Feira 30 Junho 2026", "22:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "1I", "1I", "Apurada", "3CDFGH", "3CD", "Apurada"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 01 Julho 2026", "02:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio da Cidade do México", "Cidade do México", "1A", "1A", "Apurada", "3CEFHI", "3CE", "Apurada"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 01 Julho 2026", "17:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Atlanta", "Atlanta", "1L", "1L", "Apurada", "3EHIJK", "3EH", "Apurada"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 01 Julho 2026", "21:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Seattle", "Seattle", "1G", "1G", "Apurada", "3AEHIJ", "3AE", "Apurada"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 02 Julho 2026", "01:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "1D", "1D", "Apurada", "3BEFIJ", "3BE", "Apurada"));
        controller.adicionarJogo(new Jogo("Quinta-Feira 02 Julho 2026", "20:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Los Angeles", "Los Angeles", "1H", "1H", "Apurada", "2J", "2J", "Apurada"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "00:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Toronto", "Toronto", "2K", "2K", "Apurada", "2L", "2L", "Apurada"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "04:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "BC Place de Vancouver", "Vancouver", "1B", "1B", "Apurada", "3EFGIJ", "3EF", "Apurada"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "19:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Dallas", "Dallas", "2D", "2D", "Apurada", "2G", "2G", "Apurada"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "23:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Miami", "Miami", "1J", "1J", "Apurada", "2H", "2H", "Apurada"));
        controller.adicionarJogo(new Jogo("Sábado 04 Julho 2026", "02:30", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Kansas City", "Kansas City", "1K", "1K", "Apurada", "3DEIJL", "3DE", "Apurada"));

        // Oitavas de final
        controller.adicionarJogo(new Jogo("Sábado 04 Julho 2026", "18:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Houston", "Houston", "W73", "W73", "Vencedor", "W75", "W75", "Vencedor"));
        controller.adicionarJogo(new Jogo("Sábado 04 Julho 2026", "22:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Filadélfia", "Filadélfia", "W74", "W74", "Vencedor", "W77", "W77", "Vencedor"));
        controller.adicionarJogo(new Jogo("Domingo 05 Julho 2026", "21:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "W76", "W76", "Vencedor", "W78", "W78", "Vencedor"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 06 Julho 2026", "01:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio da Cidade do México", "Cidade do México", "W79", "W79", "Vencedor", "W80", "W80", "Vencedor"));
        controller.adicionarJogo(new Jogo("Segunda-Feira 06 Julho 2026", "20:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Dallas", "Dallas", "W83", "W83", "Vencedor", "W84", "W84", "Vencedor"));
        controller.adicionarJogo(new Jogo("Terça-Feira 07 Julho 2026", "01:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Seattle", "Seattle", "W81", "W81", "Vencedor", "W82", "W82", "Vencedor"));
        controller.adicionarJogo(new Jogo("Terça-Feira 07 Julho 2026", "17:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Atlanta", "Atlanta", "W86", "W86", "Vencedor", "W88", "W88", "Vencedor"));
        controller.adicionarJogo(new Jogo("Terça-Feira 07 Julho 2026", "21:00", "GMT+0", "Oitavas de final", "Oitavos", "BC Place de Vancouver", "Vancouver", "W85", "W85", "Vencedor", "W87", "W87", "Vencedor"));

        // Quartas de final
        controller.adicionarJogo(new Jogo("Quinta-Feira 09 Julho 2026", "21:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Boston", "Boston", "W89", "W89", "Vencedor", "W90", "W90", "Vencedor"));
        controller.adicionarJogo(new Jogo("Sexta-Feira 10 Julho 2026", "20:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Los Angeles", "Los Angeles", "W93", "W93", "Vencedor", "W94", "W94", "Vencedor"));
        controller.adicionarJogo(new Jogo("Sábado 11 Julho 2026", "22:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Miami", "Miami", "W91", "W91", "Vencedor", "W92", "W92", "Vencedor"));
        controller.adicionarJogo(new Jogo("Domingo 12 Julho 2026", "02:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Kansas City", "Kansas City", "W95", "W95", "Vencedor", "W96", "W96", "Vencedor"));

        // Semifinal
        controller.adicionarJogo(new Jogo("Terça-Feira 14 Julho 2026", "20:00", "GMT+0", "Semifinal", "Semi", "Estádio de Dallas", "Dallas", "W97", "W97", "Vencedor", "W98", "W98", "Vencedor"));
        controller.adicionarJogo(new Jogo("Quarta-Feira 15 Julho 2026", "20:00", "GMT+0", "Semifinal", "Semi", "Estádio de Atlanta", "Atlanta", "W99", "W99", "Vencedor", "W100", "W100", "Vencedor"));

        // Decisão do 3º lugar
        controller.adicionarJogo(new Jogo("Sábado 18 Julho 2026", "22:00", "GMT+0", "Decisão do 3º lugar", "3º Lugar", "Estádio de Miami", "Miami", "RU101", "RU1", "Derrotado", "RU102", "RU2", "Derrotado"));

        // Final
        controller.adicionarJogo(new Jogo("Domingo 19 Julho 2026", "20:00", "GMT+0", "Final", "Finalíssima", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "W101", "W101", "Campeão", "W102", "W102", "Campeão"));

    }
}
