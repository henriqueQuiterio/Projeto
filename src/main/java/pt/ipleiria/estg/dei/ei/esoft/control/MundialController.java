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
import java.util.List;

public class MundialController {
    private List<Jogo> calendarioJogos;
    private List<Arbitro> arbitrosDisponiveis;
    private List<Selecao> selecoesParticipantes;

    public MundialController() {
        this.calendarioJogos = new ArrayList<>();
        this.arbitrosDisponiveis = new ArrayList<>();
        this.selecoesParticipantes = criarSelecoesMundial2026();
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
        adicionarJogador(cze, 1, "Matej Kovar", "GR");
        adicionarJogador(cze, 2, "David Zima", "DEF");
        adicionarJogador(cze, 3, "Tomas Holes", "DEF");
        adicionarJogador(cze, 4, "Robin Hranac", "DEF");
        adicionarJogador(cze, 5, "Vladimir Coufal", "DEF");
        adicionarJogador(cze, 6, "Stepan Chaloupek", "DEF");
        adicionarJogador(cze, 7, "Ladislav Krejci", "DEF");
        adicionarJogador(cze, 8, "Vladimir Darida", "MED");
        adicionarJogador(cze, 9, "Adam Hlozek", "AV");
        adicionarJogador(cze, 10, "Patrik Schick", "AV");
        adicionarJogador(cze, 11, "Jan Kuchta", "AV");
        adicionarJogador(cze, 12, "Lukas Cerv", "MED");
        adicionarJogador(cze, 13, "Mojmir Chytil", "AV");
        adicionarJogador(cze, 14, "David Jurasek", "DEF");
        adicionarJogador(cze, 15, "Pavel Sulc", "AV");
        adicionarJogador(cze, 16, "Jindrich Stanek", "GR");
        adicionarJogador(cze, 17, "Lukas Provod", "MED");
        adicionarJogador(cze, 18, "Michal Sadilek", "MED");
        adicionarJogador(cze, 19, "Tomas Chory", "AV");
        adicionarJogador(cze, 20, "Jaroslav Zeleny", "DEF");
        adicionarJogador(cze, 21, "David Doudera", "DEF");
        adicionarJogador(cze, 22, "Tomas Soucek", "MED");
        adicionarJogador(cze, 23, "Lukas Hornicek", "GR");
        adicionarJogador(cze, 24, "Alexandr Sojka", "MED");
        adicionarJogador(cze, 25, "Hugo Sochurek", "MED");
        adicionarJogador(cze, 26, "Denis Visinsky", "AV");
        selecoes.add(cze);

        Selecao kor = criarSelecao("Coreia do Sul", 23, 12, "A");
        adicionarJogador(kor, 1, "Seunggyu Kim", "GR");
        adicionarJogador(kor, 2, "Hanbeom Lee", "DEF");
        adicionarJogador(kor, 3, "Gihyuk Lee", "MED");
        adicionarJogador(kor, 4, "Minjae Kim", "DEF");
        adicionarJogador(kor, 5, "Taehyeon Kim", "DEF");
        adicionarJogador(kor, 6, "Inbeom Hwang", "MED");
        adicionarJogador(kor, 7, "Heungmin Son", "AV");
        adicionarJogador(kor, 8, "Seungho Paik", "MED");
        adicionarJogador(kor, 9, "Guesung Cho", "AV");
        adicionarJogador(kor, 10, "Jaesung Lee", "MED");
        adicionarJogador(kor, 11, "Heechan Hwang", "MED");
        adicionarJogador(kor, 12, "Bumkeun Song", "GR");
        adicionarJogador(kor, 13, "Taeseok Lee", "DEF");
        adicionarJogador(kor, 14, "Wije Cho", "DEF");
        adicionarJogador(kor, 15, "Moonhwan Kim", "DEF");
        adicionarJogador(kor, 16, "Jinseob Park", "DEF");
        adicionarJogador(kor, 17, "Junho Bae", "MED");
        adicionarJogador(kor, 18, "Hyeongyu Oh", "AV");
        adicionarJogador(kor, 19, "Kangin Lee", "MED");
        adicionarJogador(kor, 20, "Hyunjun Yang", "MED");
        adicionarJogador(kor, 21, "Hyeonwoo Jo", "GR");
        adicionarJogador(kor, 22, "Youngwoo Seol", "DEF");
        adicionarJogador(kor, 23, "Jens Castrop", "DEF");
        adicionarJogador(kor, 24, "Jingyu Kim", "MED");
        adicionarJogador(kor, 25, "Jisung Eom", "MED");
        adicionarJogador(kor, 26, "Donggyeong Lee", "MED");
        selecoes.add(kor);

        Selecao mex = criarSelecao("México", 15, 18, "A");
        adicionarJogador(mex, 1, "Raul Rangel", "GR");
        adicionarJogador(mex, 2, "Jorge Sanchez", "DEF");
        adicionarJogador(mex, 3, "Cesar Montes", "DEF");
        adicionarJogador(mex, 4, "Edson Alvarez", "DEF");
        adicionarJogador(mex, 5, "Johan Vasquez", "DEF");
        adicionarJogador(mex, 6, "Erik Lira", "MED");
        adicionarJogador(mex, 7, "Luis Romo", "MED");
        adicionarJogador(mex, 8, "Alvaro Fidalgo", "MED");
        adicionarJogador(mex, 9, "Raul Jimenez", "AV");
        adicionarJogador(mex, 10, "Alexis Vega", "AV");
        adicionarJogador(mex, 11, "Santiago Gimenez", "AV");
        adicionarJogador(mex, 12, "Carlos Acevedo", "GR");
        adicionarJogador(mex, 13, "Guillermo Ochoa", "GR");
        adicionarJogador(mex, 14, "Armando Gonzalez", "AV");
        adicionarJogador(mex, 15, "Israel Reyes", "DEF");
        adicionarJogador(mex, 16, "Julian Quinones", "AV");
        adicionarJogador(mex, 17, "Orbelin Pineda", "MED");
        adicionarJogador(mex, 18, "Obed Vargas", "MED");
        adicionarJogador(mex, 19, "Gilberto Mora", "MED");
        adicionarJogador(mex, 20, "Mateo Chavez", "DEF");
        adicionarJogador(mex, 21, "Cesar Huerta", "AV");
        adicionarJogador(mex, 22, "Guillermo Martinez", "AV");
        adicionarJogador(mex, 23, "Jesus Gallardo", "DEF");
        adicionarJogador(mex, 24, "Luis Chavez", "MED");
        adicionarJogador(mex, 25, "Roberto Alvarado", "AV");
        adicionarJogador(mex, 26, "Brian Gutierrez", "MED");
        selecoes.add(mex);

        Selecao rsa = criarSelecao("África do Sul", 59, 4, "A");
        adicionarJogador(rsa, 1, "Ronwen Williams", "GR");
        adicionarJogador(rsa, 2, "Thabang Matuludi", "DEF");
        adicionarJogador(rsa, 3, "Khulumani Ndamane", "DEF");
        adicionarJogador(rsa, 4, "Teboho Mokoena", "MED");
        adicionarJogador(rsa, 5, "Thalente Mbatha", "MED");
        adicionarJogador(rsa, 6, "Aubrey Modiba", "DEF");
        adicionarJogador(rsa, 7, "Oswin Appollis", "AV");
        adicionarJogador(rsa, 8, "Tshepang Moremi", "AV");
        adicionarJogador(rsa, 9, "Lyle Foster", "AV");
        adicionarJogador(rsa, 10, "Relebohile Mofokeng", "AV");
        adicionarJogador(rsa, 11, "Themba Zwane", "MED");
        adicionarJogador(rsa, 12, "Thapelo Maseko", "AV");
        adicionarJogador(rsa, 13, "Sphephelo Sithole", "MED");
        adicionarJogador(rsa, 14, "Mbekezeli Mbokazi", "DEF");
        adicionarJogador(rsa, 15, "Iqraam Rayners", "AV");
        adicionarJogador(rsa, 16, "Sipho Chaine", "GR");
        adicionarJogador(rsa, 17, "Evidence Makgopa", "AV");
        adicionarJogador(rsa, 18, "Samukele Kabini", "DEF");
        adicionarJogador(rsa, 19, "Nkosinathi Sibisi", "DEF");
        adicionarJogador(rsa, 20, "Khuliso Mudau", "DEF");
        adicionarJogador(rsa, 21, "Ime Okon", "DEF");
        adicionarJogador(rsa, 22, "Ricardo Goss", "GR");
        adicionarJogador(rsa, 23, "Jayden Adams", "MED");
        adicionarJogador(rsa, 24, "Olwethu Makhanya", "DEF");
        adicionarJogador(rsa, 25, "Kamogelo Sebelebele", "AV");
        adicionarJogador(rsa, 26, "Bradley Cross", "DEF");
        selecoes.add(rsa);

        Selecao bih = criarSelecao("Bósnia e Herzegovina", 69, 2, "B");
        adicionarJogador(bih, 1, "Nikola Vasilj", "GR");
        adicionarJogador(bih, 2, "Nihad Mujakic", "DEF");
        adicionarJogador(bih, 3, "Dennis Hadzikadunic", "DEF");
        adicionarJogador(bih, 4, "Tarik Muharemovic", "DEF");
        adicionarJogador(bih, 5, "Sead Kolasinac", "DEF");
        adicionarJogador(bih, 6, "Benjamin Tahirovic", "MED");
        adicionarJogador(bih, 7, "Amar Dedic", "DEF");
        adicionarJogador(bih, 8, "Armin Gigovic", "MED");
        adicionarJogador(bih, 9, "Samed Bazdar", "AV");
        adicionarJogador(bih, 10, "Ermedin Demirovic", "AV");
        adicionarJogador(bih, 11, "Edin Dzeko", "AV");
        adicionarJogador(bih, 12, "Mladen Jurkas", "GR");
        adicionarJogador(bih, 13, "Ivan Basic", "MED");
        adicionarJogador(bih, 14, "Ivan Sunjic", "MED");
        adicionarJogador(bih, 15, "Amar Memic", "MED");
        adicionarJogador(bih, 16, "Amir Hadziahmetovic", "MED");
        adicionarJogador(bih, 17, "Dzenis Burnic", "MED");
        adicionarJogador(bih, 18, "Nikola Katic", "DEF");
        adicionarJogador(bih, 19, "Kerim Alajbegovic", "AV");
        adicionarJogador(bih, 20, "Esmir Bajraktarevic", "AV");
        adicionarJogador(bih, 21, "Stjepan Radeljic", "DEF");
        adicionarJogador(bih, 22, "Martin Zlomislic", "GR");
        adicionarJogador(bih, 23, "Haris Tabakovic", "AV");
        adicionarJogador(bih, 24, "Arjan Malic", "DEF");
        adicionarJogador(bih, 25, "Jovo Lukic", "AV");
        adicionarJogador(bih, 26, "Ermin Mahmic", "MED");
        selecoes.add(bih);

        Selecao can = criarSelecao("Canadá", 48, 3, "B");
        adicionarJogador(can, 1, "Dayne St. Clair", "GR");
        adicionarJogador(can, 2, "Alistair Johnston", "DEF");
        adicionarJogador(can, 3, "Al E Jones", "DEF");
        adicionarJogador(can, 4, "Luc De Fougerolles", "DEF");
        adicionarJogador(can, 5, "Joel Waterman", "DEF");
        adicionarJogador(can, 6, "Mathieu Choiniere", "MED");
        adicionarJogador(can, 7, "Stephen Eustaquio", "MED");
        adicionarJogador(can, 8, "Ismael Kone", "MED");
        adicionarJogador(can, 9, "Cyle Larin", "AV");
        adicionarJogador(can, 10, "Jonathan David", "AV");
        adicionarJogador(can, 11, "Liam Millar", "MED");
        adicionarJogador(can, 12, "Tani Oluwaseyi", "AV");
        adicionarJogador(can, 13, "Derek Cornelius", "DEF");
        adicionarJogador(can, 14, "Jacob Shaffelburg", "MED");
        adicionarJogador(can, 15, "Moise Bombito", "DEF");
        adicionarJogador(can, 16, "Maxime Crepeau", "GR");
        adicionarJogador(can, 17, "Tajon Buchanan", "AV");
        adicionarJogador(can, 18, "Owen Goodman", "GR");
        adicionarJogador(can, 19, "Alphonso Davies", "DEF");
        adicionarJogador(can, 20, "Ali Ahmed", "AV");
        adicionarJogador(can, 21, "Jonathan Osorio", "MED");
        adicionarJogador(can, 22, "Richie Laryea", "DEF");
        adicionarJogador(can, 23, "Niko Sigur", "DEF");
        adicionarJogador(can, 24, "Promise David", "AV");
        adicionarJogador(can, 25, "Nathan Saliba", "MED");
        adicionarJogador(can, 26, "Jayden Nelson", "AV");
        selecoes.add(can);

        Selecao qat = criarSelecao("Catar", 53, 2, "B");
        adicionarJogador(qat, 1, "Mahmoud Abunada", "GR");
        adicionarJogador(qat, 2, "Pedro Miguel", "DEF");
        adicionarJogador(qat, 3, "Lucas Mendes", "DEF");
        adicionarJogador(qat, 4, "Issa Laye", "DEF");
        adicionarJogador(qat, 5, "Jassem Gaber", "DEF");
        adicionarJogador(qat, 6, "Abdulaziz Hatem", "MED");
        adicionarJogador(qat, 7, "Ahmed Alaaeldin", "AV");
        adicionarJogador(qat, 8, "Edmilson Junior", "AV");
        adicionarJogador(qat, 9, "Mohammed Muntari", "AV");
        adicionarJogador(qat, 10, "Hassan Alhaydos", "AV");
        adicionarJogador(qat, 11, "Akram Afif", "AV");
        adicionarJogador(qat, 12, "Karim Boudiaf", "MED");
        adicionarJogador(qat, 13, "Ayoub Aloui", "DEF");
        adicionarJogador(qat, 14, "Homam Ahmed", "DEF");
        adicionarJogador(qat, 15, "Yusuf Abdurisag", "AV");
        adicionarJogador(qat, 16, "Boualem Khoukhi", "DEF");
        adicionarJogador(qat, 17, "Ahmed Alganehi", "MED");
        adicionarJogador(qat, 18, "Sultan Albrake", "DEF");
        adicionarJogador(qat, 19, "Almoez Ali", "AV");
        adicionarJogador(qat, 20, "Ahmed Fathy", "MED");
        adicionarJogador(qat, 21, "Salah Zakaria", "GR");
        adicionarJogador(qat, 22, "Meshaal Barsham", "GR");
        adicionarJogador(qat, 23, "Assim Madibo", "MED");
        adicionarJogador(qat, 24, "Tahsin Mohammed", "AV");
        adicionarJogador(qat, 25, "Alhashmi Alhussein", "DEF");
        adicionarJogador(qat, 26, "Mohamed Manai", "AV");
        selecoes.add(qat);

        Selecao sui = criarSelecao("Suíça", 19, 13, "B");
        adicionarJogador(sui, 1, "Gregor Kobel", "GR");
        adicionarJogador(sui, 2, "Miro Muheim", "DEF");
        adicionarJogador(sui, 3, "Silvan Widmer", "DEF");
        adicionarJogador(sui, 4, "Nico Elvedi", "DEF");
        adicionarJogador(sui, 5, "Manuel Akanji", "DEF");
        adicionarJogador(sui, 6, "Denis Zakaria", "MED");
        adicionarJogador(sui, 7, "Breel Embolo", "AV");
        adicionarJogador(sui, 8, "Remo Freuler", "MED");
        adicionarJogador(sui, 9, "Johan Manzambi", "MED");
        adicionarJogador(sui, 10, "Granit Xhaka", "MED");
        adicionarJogador(sui, 11, "Dan Ndoye", "AV");
        adicionarJogador(sui, 12, "Yvon Mvogo", "GR");
        adicionarJogador(sui, 13, "Ricardo Rodriguez", "DEF");
        adicionarJogador(sui, 14, "Ardon Jashari", "MED");
        adicionarJogador(sui, 15, "Djibril Sow", "MED");
        adicionarJogador(sui, 16, "Christian Fassnacht", "AV");
        adicionarJogador(sui, 17, "Ruben Vargas", "AV");
        adicionarJogador(sui, 18, "Eray Coemert", "DEF");
        adicionarJogador(sui, 19, "Noah Okafor", "AV");
        adicionarJogador(sui, 20, "Michel Aebischer", "MED");
        adicionarJogador(sui, 21, "Marvin Keller", "GR");
        adicionarJogador(sui, 22, "Fabian Rieder", "MED");
        adicionarJogador(sui, 23, "Zeki Amdouni", "AV");
        adicionarJogador(sui, 24, "Aurele Amenda", "DEF");
        adicionarJogador(sui, 25, "Luca Jaquez", "DEF");
        adicionarJogador(sui, 26, "Cedric Itten", "AV");
        selecoes.add(sui);

        Selecao bra = criarSelecao("Brasil", 5, 23, "C");
        adicionarJogador(bra, 1, "Alisson", "GR");
        adicionarJogador(bra, 2, "Ederson Silva", "MED");
        adicionarJogador(bra, 3, "Gabriel Magalhaes", "DEF");
        adicionarJogador(bra, 4, "Marquinhos", "DEF");
        adicionarJogador(bra, 5, "Casemiro", "MED");
        adicionarJogador(bra, 6, "Alex Sandro", "DEF");
        adicionarJogador(bra, 7, "Vinicius Junior", "AV");
        adicionarJogador(bra, 8, "Bruno Guimaraes", "MED");
        adicionarJogador(bra, 9, "Matheus Cunha", "AV");
        adicionarJogador(bra, 10, "Neymar Jr", "AV");
        adicionarJogador(bra, 11, "Raphinha", "AV");
        adicionarJogador(bra, 12, "Weverton", "GR");
        adicionarJogador(bra, 13, "Danilo", "DEF");
        adicionarJogador(bra, 14, "Bremer", "DEF");
        adicionarJogador(bra, 15, "Leo Pereira", "DEF");
        adicionarJogador(bra, 16, "Douglas Santos", "DEF");
        adicionarJogador(bra, 17, "Fabinho", "MED");
        adicionarJogador(bra, 18, "Danilo Santos", "MED");
        adicionarJogador(bra, 19, "Endrick", "AV");
        adicionarJogador(bra, 20, "Lucas Paqueta", "MED");
        adicionarJogador(bra, 21, "Luiz Henrique", "AV");
        adicionarJogador(bra, 22, "Gabriel Martinelli", "AV");
        adicionarJogador(bra, 23, "Ederson", "GR");
        adicionarJogador(bra, 24, "Roger Ibanez", "DEF");
        adicionarJogador(bra, 25, "Igor Thiago", "AV");
        adicionarJogador(bra, 26, "Rayan", "AV");
        selecoes.add(bra);

        Selecao sco = criarSelecao("Escócia", 45, 9, "C");
        adicionarJogador(sco, 1, "Angus Gunn", "GR");
        adicionarJogador(sco, 2, "Aaron Hickey", "DEF");
        adicionarJogador(sco, 3, "Andy Robertson", "DEF");
        adicionarJogador(sco, 4, "Scott McTominay", "MED");
        adicionarJogador(sco, 5, "Grant Hanley", "DEF");
        adicionarJogador(sco, 6, "Kieran Tierney", "DEF");
        adicionarJogador(sco, 7, "John McGinn", "MED");
        adicionarJogador(sco, 8, "Tyler Fletcher", "MED");
        adicionarJogador(sco, 9, "Lyndon Dykes", "AV");
        adicionarJogador(sco, 10, "Che Adams", "AV");
        adicionarJogador(sco, 11, "Ryan Christie", "MED");
        adicionarJogador(sco, 12, "Liam Kelly", "GR");
        adicionarJogador(sco, 13, "Jack Hendry", "DEF");
        adicionarJogador(sco, 14, "Ross Stewart", "AV");
        adicionarJogador(sco, 15, "John Souttar", "DEF");
        adicionarJogador(sco, 16, "Dominic Hyam", "DEF");
        adicionarJogador(sco, 17, "Ben Gannon-Doak", "AV");
        adicionarJogador(sco, 18, "George Hirst", "AV");
        adicionarJogador(sco, 19, "Lewis Ferguson", "MED");
        adicionarJogador(sco, 20, "Lawrence Shankland", "AV");
        adicionarJogador(sco, 21, "Craig Gordon", "GR");
        adicionarJogador(sco, 22, "Nathan Patterson", "DEF");
        adicionarJogador(sco, 23, "Kenny McLean", "MED");
        adicionarJogador(sco, 24, "Anthony Ralston", "DEF");
        adicionarJogador(sco, 25, "Findlay Curtis", "AV");
        adicionarJogador(sco, 26, "Scott McKenna", "DEF");
        selecoes.add(sco);

        Selecao hai = criarSelecao("Haiti", 83, 3, "C");
        adicionarJogador(hai, 1, "Johny Placide", "GR");
        adicionarJogador(hai, 2, "Carlens Arcus", "DEF");
        adicionarJogador(hai, 3, "Keeto Thermoncy", "DEF");
        adicionarJogador(hai, 4, "Ricardo Ade", "DEF");
        adicionarJogador(hai, 5, "Hannes Delcroix", "DEF");
        adicionarJogador(hai, 6, "Carl Sainte", "MED");
        adicionarJogador(hai, 7, "Derrick Etienne", "AV");
        adicionarJogador(hai, 8, "Martin Experience", "DEF");
        adicionarJogador(hai, 9, "Duckens Nazon", "AV");
        adicionarJogador(hai, 10, "Jean-Ricner Bellegarde", "MED");
        adicionarJogador(hai, 11, "Louicius Deedson", "AV");
        adicionarJogador(hai, 12, "Alexandre Pierre", "GR");
        adicionarJogador(hai, 13, "Markhus Lacroix", "DEF");
        adicionarJogador(hai, 14, "Garven Metusala", "DEF");
        adicionarJogador(hai, 15, "Ruben Providence", "AV");
        adicionarJogador(hai, 16, "Lenny Joseph", "AV");
        adicionarJogador(hai, 17, "Danley Jean Jacques", "MED");
        adicionarJogador(hai, 18, "Wilson Isidor", "AV");
        adicionarJogador(hai, 19, "Yassin Fortune", "AV");
        adicionarJogador(hai, 20, "Frantzdy Pierrot", "AV");
        adicionarJogador(hai, 21, "Josue Casimir", "AV");
        adicionarJogador(hai, 22, "Jean-Kevin Duverne", "DEF");
        adicionarJogador(hai, 23, "Josue Duverger", "GR");
        adicionarJogador(hai, 24, "Wilguens Paugain", "DEF");
        adicionarJogador(hai, 25, "Dominique Simon", "MED");
        adicionarJogador(hai, 26, "Woodensky Pierre", "MED");
        selecoes.add(hai);

        Selecao mar = criarSelecao("Marrocos", 12, 7, "C");
        adicionarJogador(mar, 1, "Yassine Bounou", "GR");
        adicionarJogador(mar, 2, "Achraf Hakimi", "DEF");
        adicionarJogador(mar, 3, "Noussair Mazraoui", "DEF");
        adicionarJogador(mar, 4, "Sofyan Amrabat", "MED");
        adicionarJogador(mar, 5, "Marwane Saadane", "DEF");
        adicionarJogador(mar, 6, "Ayyoub Bouaddi", "MED");
        adicionarJogador(mar, 7, "Chemsdine Talbi", "MED");
        adicionarJogador(mar, 8, "Azzedine Ounahi", "MED");
        adicionarJogador(mar, 9, "Sou Ane Rahimi", "AV");
        adicionarJogador(mar, 10, "Brahim Diaz", "AV");
        adicionarJogador(mar, 11, "Ismael Saibari", "MED");
        adicionarJogador(mar, 12, "Munir El Kajoui", "GR");
        adicionarJogador(mar, 13, "Zakaria El Ouahdi", "DEF");
        adicionarJogador(mar, 14, "Issa Diop", "DEF");
        adicionarJogador(mar, 15, "Samir El Mourabet", "MED");
        adicionarJogador(mar, 16, "Gessime Yassine", "MED");
        adicionarJogador(mar, 17, "Amine Sbai", "AV");
        adicionarJogador(mar, 18, "Chadi Riad", "DEF");
        adicionarJogador(mar, 19, "Youssef Belammari", "DEF");
        adicionarJogador(mar, 20, "Ayoub El Kaabi", "AV");
        adicionarJogador(mar, 21, "Ayoube Amaimouni", "AV");
        adicionarJogador(mar, 22, "Ahmed Reda Tagnaouti", "GR");
        adicionarJogador(mar, 23, "Bilal El Khannouss", "MED");
        adicionarJogador(mar, 24, "Neil El Aynaoui", "MED");
        adicionarJogador(mar, 25, "Redouane Halhal", "DEF");
        adicionarJogador(mar, 26, "Anass Salah Eddine", "DEF");
        selecoes.add(mar);

        Selecao aus = criarSelecao("Austrália", 24, 7, "D");
        adicionarJogador(aus, 1, "Mathew Ryan", "GR");
        adicionarJogador(aus, 2, "Milos Degenek", "DEF");
        adicionarJogador(aus, 3, "Alessandro Circati", "DEF");
        adicionarJogador(aus, 4, "Jacob Italiano", "DEF");
        adicionarJogador(aus, 5, "Jordan Bos", "DEF");
        adicionarJogador(aus, 6, "Jason Geria", "DEF");
        adicionarJogador(aus, 7, "Mathew Leckie", "AV");
        adicionarJogador(aus, 8, "Connor Metcalfe", "MED");
        adicionarJogador(aus, 9, "Mohamed Toure", "AV");
        adicionarJogador(aus, 10, "Ajdin Hrustic", "AV");
        adicionarJogador(aus, 11, "Awer Mabil", "AV");
        adicionarJogador(aus, 12, "Paul Izzo", "GR");
        adicionarJogador(aus, 13, "Aiden Oneill", "MED");
        adicionarJogador(aus, 14, "Cameron Devlin", "MED");
        adicionarJogador(aus, 15, "Kai Trewin", "DEF");
        adicionarJogador(aus, 16, "Aziz Behich", "DEF");
        adicionarJogador(aus, 17, "Nestory Irankunda", "AV");
        adicionarJogador(aus, 18, "Patrick Beach", "GR");
        adicionarJogador(aus, 19, "Harry Souttar", "DEF");
        adicionarJogador(aus, 20, "Cristian Volpato", "AV");
        adicionarJogador(aus, 21, "Cameron Burgess", "DEF");
        adicionarJogador(aus, 22, "Jackson Irvine", "MED");
        adicionarJogador(aus, 23, "Nishan Velupillay", "AV");
        adicionarJogador(aus, 24, "Paul Okon-Engstler", "MED");
        adicionarJogador(aus, 25, "Lucas Herrington", "DEF");
        adicionarJogador(aus, 26, "Tete Yengi", "AV");
        selecoes.add(aus);

        Selecao usa = criarSelecao("Estados Unidos", 16, 12, "D");
        adicionarJogador(usa, 1, "Matt Turner", "GR");
        adicionarJogador(usa, 2, "Sergino Dest", "DEF");
        adicionarJogador(usa, 3, "Chris Richards", "DEF");
        adicionarJogador(usa, 4, "Tyler Adams", "MED");
        adicionarJogador(usa, 5, "Antonee Robinson", "DEF");
        adicionarJogador(usa, 6, "Auston Trusty", "DEF");
        adicionarJogador(usa, 7, "Giovanni Reyna", "MED");
        adicionarJogador(usa, 8, "Weston McKennie", "MED");
        adicionarJogador(usa, 9, "Ricardo Pepi", "AV");
        adicionarJogador(usa, 10, "Christian Pulisic", "AV");
        adicionarJogador(usa, 11, "Brenden Aaronson", "AV");
        adicionarJogador(usa, 12, "Miles Robinson", "DEF");
        adicionarJogador(usa, 13, "Tim Ream", "DEF");
        adicionarJogador(usa, 14, "Sebastian Berhalter", "MED");
        adicionarJogador(usa, 15, "Cristian Roldan", "MED");
        adicionarJogador(usa, 16, "Alex Freeman", "DEF");
        adicionarJogador(usa, 17, "Malik Tillman", "MED");
        adicionarJogador(usa, 18, "Max Arfsten", "DEF");
        adicionarJogador(usa, 19, "Haji Wright", "AV");
        adicionarJogador(usa, 20, "Folarin Balogun", "AV");
        adicionarJogador(usa, 21, "Timothy Weah", "AV");
        adicionarJogador(usa, 22, "Mark McKenzie", "DEF");
        adicionarJogador(usa, 23, "Joe Scally", "DEF");
        adicionarJogador(usa, 24, "Matt Freese", "GR");
        adicionarJogador(usa, 25, "Chris Brady", "GR");
        adicionarJogador(usa, 26, "Alex Zendejas", "AV");
        selecoes.add(usa);

        Selecao par = criarSelecao("Paraguai", 56, 9, "D");
        adicionarJogador(par, 1, "Gatito Fernandez", "GR");
        adicionarJogador(par, 2, "Gustavo Velazquez", "DEF");
        adicionarJogador(par, 3, "Omar Alderete", "DEF");
        adicionarJogador(par, 4, "Juan Jose Caceres", "DEF");
        adicionarJogador(par, 5, "Fabian Balbuena", "DEF");
        adicionarJogador(par, 6, "Junior Alonso", "DEF");
        adicionarJogador(par, 7, "Ramon Sosa", "MED");
        adicionarJogador(par, 8, "Diego Gomez", "MED");
        adicionarJogador(par, 9, "Antonio Sanabria", "AV");
        adicionarJogador(par, 10, "Miguel Almiron", "MED");
        adicionarJogador(par, 11, "Mauricio", "MED");
        adicionarJogador(par, 12, "Orlando Gill", "GR");
        adicionarJogador(par, 13, "Jose Canale", "DEF");
        adicionarJogador(par, 14, "Andres Cubas", "MED");
        adicionarJogador(par, 15, "Gustavo Gomez", "DEF");
        adicionarJogador(par, 16, "Damian Bobadilla", "MED");
        adicionarJogador(par, 17, "Alejandro Romero Gamarra", "AV");
        adicionarJogador(par, 18, "Alex Arce", "AV");
        adicionarJogador(par, 19, "Julio Enciso", "AV");
        adicionarJogador(par, 20, "Braian Ojeda", "MED");
        adicionarJogador(par, 21, "Gabriel Avalos", "AV");
        adicionarJogador(par, 22, "Gaston Olveira", "GR");
        adicionarJogador(par, 23, "Matias Galarza", "MED");
        adicionarJogador(par, 24, "Gustavo Caballero", "MED");
        adicionarJogador(par, 25, "Isidro Pitta", "AV");
        adicionarJogador(par, 26, "Alexandro Maidana", "DEF");
        selecoes.add(par);

        Selecao tur = criarSelecao("Turquia", 26, 3, "D");
        adicionarJogador(tur, 1, "Mert Gunok", "GR");
        adicionarJogador(tur, 2, "Zeki Celik", "DEF");
        adicionarJogador(tur, 3, "Merih Demiral", "DEF");
        adicionarJogador(tur, 4, "Caglar Soyuncu", "DEF");
        adicionarJogador(tur, 5, "Salih Ozcan", "MED");
        adicionarJogador(tur, 6, "Orkun Kokcu", "MED");
        adicionarJogador(tur, 7, "Kerem Akturkoglu", "AV");
        adicionarJogador(tur, 8, "Arda Guler", "AV");
        adicionarJogador(tur, 9, "Deniz Gul", "AV");
        adicionarJogador(tur, 10, "Hakan Calhanoglu", "MED");
        adicionarJogador(tur, 11, "Kenan Yildiz", "AV");
        adicionarJogador(tur, 12, "Altay Bayindir", "GR");
        adicionarJogador(tur, 13, "Eren Elmali", "DEF");
        adicionarJogador(tur, 14, "Abdulkerim Bardakci", "DEF");
        adicionarJogador(tur, 15, "Ozan Kabak", "DEF");
        adicionarJogador(tur, 16, "Ismail Yuksek", "MED");
        adicionarJogador(tur, 17, "Irfan Can Kahveci", "AV");
        adicionarJogador(tur, 18, "Mert Muldur", "DEF");
        adicionarJogador(tur, 19, "Yunus Akgun", "AV");
        adicionarJogador(tur, 20, "Ferdi Kadioglu", "DEF");
        adicionarJogador(tur, 21, "Baris Alper Yilmaz", "AV");
        adicionarJogador(tur, 22, "Kaan Ayhan", "MED");
        adicionarJogador(tur, 23, "Ugurcan Cakir", "GR");
        adicionarJogador(tur, 24, "Oguz Aydin", "AV");
        adicionarJogador(tur, 25, "Samet Akaydin", "DEF");
        adicionarJogador(tur, 26, "Can Uzun", "AV");
        selecoes.add(tur);

        Selecao ger = criarSelecao("Alemanha", 9, 21, "E");
        adicionarJogador(ger, 1, "Manuel Neuer", "GR");
        adicionarJogador(ger, 2, "Antonio Ruediger", "DEF");
        adicionarJogador(ger, 3, "Waldemar Anton", "DEF");
        adicionarJogador(ger, 4, "Jonathan Tah", "DEF");
        adicionarJogador(ger, 5, "Aleksandar Pavlovic", "MED");
        adicionarJogador(ger, 6, "Joshua Kimmich", "DEF");
        adicionarJogador(ger, 7, "Kai Havertz", "AV");
        adicionarJogador(ger, 8, "Leon Goretzka", "MED");
        adicionarJogador(ger, 9, "Jamie Leweling", "MED");
        adicionarJogador(ger, 10, "Jamal Musiala", "MED");
        adicionarJogador(ger, 11, "Nick Woltemade", "AV");
        adicionarJogador(ger, 12, "Oliver Baumann", "GR");
        adicionarJogador(ger, 13, "Pascal Gross", "MED");
        adicionarJogador(ger, 14, "Maximilian Beier", "AV");
        adicionarJogador(ger, 15, "Nico Schlotterbeck", "DEF");
        adicionarJogador(ger, 16, "Angelo Stiller", "MED");
        adicionarJogador(ger, 17, "Florian Wirtz", "MED");
        adicionarJogador(ger, 18, "Nathaniel Brown", "DEF");
        adicionarJogador(ger, 19, "Leroy Sane", "MED");
        adicionarJogador(ger, 20, "Nadiem Amiri", "MED");
        adicionarJogador(ger, 21, "Alexander Nuebel", "GR");
        adicionarJogador(ger, 22, "David Raum", "DEF");
        adicionarJogador(ger, 23, "Felix Nmecha", "MED");
        adicionarJogador(ger, 24, "Malick Thiaw", "DEF");
        adicionarJogador(ger, 25, "Assan Ouedraogo", "MED");
        adicionarJogador(ger, 26, "Deniz Undav", "AV");
        selecoes.add(ger);

        Selecao civ = criarSelecao("Costa do Marfim", 46, 5, "E");
        adicionarJogador(civ, 1, "Yahia Fofana", "GR");
        adicionarJogador(civ, 2, "Ousmane Diomande", "DEF");
        adicionarJogador(civ, 3, "Ghislain Konan", "DEF");
        adicionarJogador(civ, 4, "Jean Michael Seri", "MED");
        adicionarJogador(civ, 5, "Wilfried Singo", "DEF");
        adicionarJogador(civ, 6, "Seko Fofana", "MED");
        adicionarJogador(civ, 7, "Odilon Kossounou", "DEF");
        adicionarJogador(civ, 8, "Franck Kessie", "MED");
        adicionarJogador(civ, 9, "Ange-Yoan Bonny", "AV");
        adicionarJogador(civ, 10, "Simon Adingra", "AV");
        adicionarJogador(civ, 11, "Yan Diomande", "AV");
        adicionarJogador(civ, 12, "Elye Wahi", "AV");
        adicionarJogador(civ, 13, "Christopher Operi", "DEF");
        adicionarJogador(civ, 14, "Oumar Diakite", "AV");
        adicionarJogador(civ, 15, "Amad Diallo", "AV");
        adicionarJogador(civ, 16, "Mohamed Kone", "GR");
        adicionarJogador(civ, 17, "Guela Doue", "DEF");
        adicionarJogador(civ, 18, "Ibrahim Sangare", "MED");
        adicionarJogador(civ, 19, "Nicolas Pepe", "AV");
        adicionarJogador(civ, 20, "Emmanuel Agbadou", "DEF");
        adicionarJogador(civ, 21, "Evan Ndicka", "DEF");
        adicionarJogador(civ, 22, "Evann Guessand", "AV");
        adicionarJogador(civ, 23, "Alban Lafont", "GR");
        adicionarJogador(civ, 24, "Bazoumana Toure", "AV");
        adicionarJogador(civ, 25, "Parfait Guiagon", "MED");
        adicionarJogador(civ, 26, "Christ Inao Oulai", "MED");
        selecoes.add(civ);

        Selecao cuw = criarSelecao("Curaçao", 88, 1, "E");
        adicionarJogador(cuw, 1, "Eloy Room", "GR");
        adicionarJogador(cuw, 2, "Shurandy Sambo", "DEF");
        adicionarJogador(cuw, 3, "Jurien Gaari", "DEF");
        adicionarJogador(cuw, 4, "Roshon Van Eijma", "DEF");
        adicionarJogador(cuw, 5, "Sherel Floranus", "DEF");
        adicionarJogador(cuw, 6, "Godfried Roemeratoe", "MED");
        adicionarJogador(cuw, 7, "Juninho Bacuna", "MED");
        adicionarJogador(cuw, 8, "Livano Comenencia", "MED");
        adicionarJogador(cuw, 9, "Juergen Locadia", "AV");
        adicionarJogador(cuw, 10, "Leandro Bacuna", "MED");
        adicionarJogador(cuw, 11, "Jeremy Antonisse", "AV");
        adicionarJogador(cuw, 12, "Sontje Hansen", "AV");
        adicionarJogador(cuw, 13, "Tyrese Noslin", "AV");
        adicionarJogador(cuw, 14, "Kenji Gorre", "AV");
        adicionarJogador(cuw, 15, "Arjany Martha", "MED");
        adicionarJogador(cuw, 16, "Jearl Margaritha", "AV");
        adicionarJogador(cuw, 17, "Brandley Kuwas", "AV");
        adicionarJogador(cuw, 18, "Armando Obispo", "DEF");
        adicionarJogador(cuw, 19, "Gervane Kastaneer", "AV");
        adicionarJogador(cuw, 20, "Joshua Brenet", "DEF");
        adicionarJogador(cuw, 21, "Tahith Chong", "MED");
        adicionarJogador(cuw, 22, "Kevin Felida", "MED");
        adicionarJogador(cuw, 23, "Riechedly Bazoer", "DEF");
        adicionarJogador(cuw, 24, "Deveron Fonville", "DEF");
        adicionarJogador(cuw, 25, "Tyrick Bodak", "GR");
        adicionarJogador(cuw, 26, "Trevor Doornbusch", "GR");
        selecoes.add(cuw);

        Selecao ecu = criarSelecao("Equador", 31, 5, "E");
        adicionarJogador(ecu, 1, "Hernan Galindez", "GR");
        adicionarJogador(ecu, 2, "Felix Torres", "DEF");
        adicionarJogador(ecu, 3, "Piero Hincapie", "DEF");
        adicionarJogador(ecu, 4, "Joel Ordonez", "DEF");
        adicionarJogador(ecu, 5, "Jordy Alcivar", "MED");
        adicionarJogador(ecu, 6, "Willian Pacho", "DEF");
        adicionarJogador(ecu, 7, "Pervis Estupinan", "DEF");
        adicionarJogador(ecu, 8, "Anthony Valencia", "MED");
        adicionarJogador(ecu, 9, "John Yeboah", "AV");
        adicionarJogador(ecu, 10, "Kendry Paez", "MED");
        adicionarJogador(ecu, 11, "Kevin Rodriguez", "AV");
        adicionarJogador(ecu, 12, "Moises Ramirez", "GR");
        adicionarJogador(ecu, 13, "Enner Valencia", "AV");
        adicionarJogador(ecu, 14, "Alan Minda", "MED");
        adicionarJogador(ecu, 15, "Pedro Vite", "MED");
        adicionarJogador(ecu, 16, "Jordy Caicedo", "AV");
        adicionarJogador(ecu, 17, "Angelo Preciado", "DEF");
        adicionarJogador(ecu, 18, "Denil Castillo", "MED");
        adicionarJogador(ecu, 19, "Gonzalo Plata", "AV");
        adicionarJogador(ecu, 20, "Nilson Angulo", "AV");
        adicionarJogador(ecu, 21, "Alan Franco", "MED");
        adicionarJogador(ecu, 22, "Gonzalo Valle", "GR");
        adicionarJogador(ecu, 23, "Moises Caicedo", "MED");
        adicionarJogador(ecu, 24, "Jeremy Arevalo", "AV");
        adicionarJogador(ecu, 25, "Jackson Porozo", "DEF");
        adicionarJogador(ecu, 26, "Yaimar Medina", "DEF");
        selecoes.add(ecu);

        Selecao jpn = criarSelecao("Japão", 18, 8, "F");
        adicionarJogador(jpn, 1, "Zion Suzuki", "GR");
        adicionarJogador(jpn, 2, "Yukinari Sugawara", "DEF");
        adicionarJogador(jpn, 3, "Shogo Taniguchi", "DEF");
        adicionarJogador(jpn, 4, "Kou Itakura", "DEF");
        adicionarJogador(jpn, 5, "Yuto Nagatomo", "DEF");
        adicionarJogador(jpn, 6, "Shuto Machino", "AV");
        adicionarJogador(jpn, 7, "Ao Tanaka", "MED");
        adicionarJogador(jpn, 8, "Takefusa Kubo", "MED");
        adicionarJogador(jpn, 9, "Keisuke Goto", "AV");
        adicionarJogador(jpn, 10, "Ritsu Doan", "MED");
        adicionarJogador(jpn, 11, "Daizen Maeda", "MED");
        adicionarJogador(jpn, 12, "Keisuke Osako", "GR");
        adicionarJogador(jpn, 13, "Keito Nakamura", "MED");
        adicionarJogador(jpn, 14, "Junya Ito", "MED");
        adicionarJogador(jpn, 15, "Daichi Kamada", "MED");
        adicionarJogador(jpn, 16, "Tsuyoshi Watanabe", "DEF");
        adicionarJogador(jpn, 17, "Yuito Suzuki", "MED");
        adicionarJogador(jpn, 18, "Ayase Ueda", "AV");
        adicionarJogador(jpn, 19, "Koki Ogawa", "AV");
        adicionarJogador(jpn, 20, "Ayumu Seko", "DEF");
        adicionarJogador(jpn, 21, "Hiroki Ito", "DEF");
        adicionarJogador(jpn, 22, "Takehiro Tomiyasu", "DEF");
        adicionarJogador(jpn, 23, "Tomoki Hayakawa", "GR");
        adicionarJogador(jpn, 24, "Kaishu Sano", "MED");
        adicionarJogador(jpn, 25, "Junnosuke Suzuki", "DEF");
        adicionarJogador(jpn, 26, "Kento Shiogai", "AV");
        selecoes.add(jpn);

        Selecao ned = criarSelecao("Países Baixos", 7, 12, "F");
        adicionarJogador(ned, 1, "Bart Verbruggen", "GR");
        adicionarJogador(ned, 2, "Lutsharel Geertruida", "DEF");
        adicionarJogador(ned, 3, "Marten De Roon", "MED");
        adicionarJogador(ned, 4, "Virgil Van Dijk", "DEF");
        adicionarJogador(ned, 5, "Nathan Ake", "DEF");
        adicionarJogador(ned, 6, "Jan Paul Van Hecke", "DEF");
        adicionarJogador(ned, 7, "Justin Kluivert", "MED");
        adicionarJogador(ned, 8, "Ryan Gravenberch", "MED");
        adicionarJogador(ned, 9, "Wout Weghorst", "AV");
        adicionarJogador(ned, 10, "Memphis Depay", "AV");
        adicionarJogador(ned, 11, "Cody Gakpo", "AV");
        adicionarJogador(ned, 12, "Mats Wieffer", "DEF");
        adicionarJogador(ned, 13, "Robin Roefs", "GR");
        adicionarJogador(ned, 14, "Tijjani Reijnders", "MED");
        adicionarJogador(ned, 15, "Micky Van De Ven", "DEF");
        adicionarJogador(ned, 16, "Guus Til", "MED");
        adicionarJogador(ned, 17, "Noa Lang", "AV");
        adicionarJogador(ned, 18, "Donyell Malen", "AV");
        adicionarJogador(ned, 19, "Brian Brobbey", "AV");
        adicionarJogador(ned, 20, "Teun Koopmeiners", "MED");
        adicionarJogador(ned, 21, "Frenkie De Jong", "MED");
        adicionarJogador(ned, 22, "Denzel Dumfries", "DEF");
        adicionarJogador(ned, 23, "Mark Flekken", "GR");
        adicionarJogador(ned, 24, "Crysencio Summerville", "AV");
        adicionarJogador(ned, 25, "Jorrel Hato", "DEF");
        adicionarJogador(ned, 26, "Quinten Timber", "MED");
        selecoes.add(ned);

        Selecao swe = criarSelecao("Suécia", 28, 13, "F");
        adicionarJogador(swe, 1, "Jacob Widell Zetterstrom", "GR");
        adicionarJogador(swe, 2, "Gustaf Lagerbielke", "DEF");
        adicionarJogador(swe, 3, "Victor Lindelof", "DEF");
        adicionarJogador(swe, 4, "Isak Hien", "DEF");
        adicionarJogador(swe, 5, "Gabriel Gudmundsson", "DEF");
        adicionarJogador(swe, 6, "Herman Johansson", "DEF");
        adicionarJogador(swe, 7, "Lucas Bergvall", "MED");
        adicionarJogador(swe, 8, "Daniel Svensson", "DEF");
        adicionarJogador(swe, 9, "Alexander Isak", "AV");
        adicionarJogador(swe, 10, "Benjamin Nygren", "MED");
        adicionarJogador(swe, 11, "Anthony Elanga", "AV");
        adicionarJogador(swe, 12, "Viktor Johansson", "GR");
        adicionarJogador(swe, 13, "Ken Sema", "MED");
        adicionarJogador(swe, 14, "Hjalmar Ekdal", "DEF");
        adicionarJogador(swe, 15, "Carl Starfelt", "DEF");
        adicionarJogador(swe, 16, "Jesper Karlstrom", "MED");
        adicionarJogador(swe, 17, "Viktor Gyokeres", "AV");
        adicionarJogador(swe, 18, "Yasin Ayari", "MED");
        adicionarJogador(swe, 19, "Mattias Svanberg", "MED");
        adicionarJogador(swe, 20, "Eric Smith", "DEF");
        adicionarJogador(swe, 21, "Alexander Bernhardsson", "DEF");
        adicionarJogador(swe, 22, "Besfort Zeneli", "MED");
        adicionarJogador(swe, 23, "Kristoffer Nordfeldt", "GR");
        adicionarJogador(swe, 24, "Elliot Stroud", "DEF");
        adicionarJogador(swe, 25, "Gustaf Nilsson", "AV");
        adicionarJogador(swe, 26, "Taha Ali", "AV");
        selecoes.add(swe);

        Selecao tun = criarSelecao("Tunísia", 49, 7, "F");
        adicionarJogador(tun, 1, "Mouhib Chamakh", "GR");
        adicionarJogador(tun, 2, "Ali Abdi", "DEF");
        adicionarJogador(tun, 3, "Montassar Talbi", "DEF");
        adicionarJogador(tun, 4, "Omar Rekik", "DEF");
        adicionarJogador(tun, 5, "Adam Arous", "DEF");
        adicionarJogador(tun, 6, "Dylan Bronn", "DEF");
        adicionarJogador(tun, 7, "Elias Achouri", "AV");
        adicionarJogador(tun, 8, "Elias Saad", "AV");
        adicionarJogador(tun, 9, "Hazem Mastouri", "AV");
        adicionarJogador(tun, 10, "Hannibal Mejbri", "MED");
        adicionarJogador(tun, 11, "Ismael Gharbi", "MED");
        adicionarJogador(tun, 12, "Mortadha Ben Ouanes", "DEF");
        adicionarJogador(tun, 13, "Rani Khedira", "MED");
        adicionarJogador(tun, 14, "Khalil Ayari", "MED");
        adicionarJogador(tun, 15, "Mohamed Hadj Mahmoud", "MED");
        adicionarJogador(tun, 16, "Aymen Dahmen", "GR");
        adicionarJogador(tun, 17, "Ellyes Skhiri", "MED");
        adicionarJogador(tun, 18, "Rayan Elloumi", "AV");
        adicionarJogador(tun, 19, "Firas Chaouat", "AV");
        adicionarJogador(tun, 20, "Yan Valery", "DEF");
        adicionarJogador(tun, 21, "Mohamed Amine Ben Hmida", "DEF");
        adicionarJogador(tun, 22, "Sabri Ben Hessen", "GR");
        adicionarJogador(tun, 23, "Moutaz Neffati", "DEF");
        adicionarJogador(tun, 24, "Raed Chikhaoui", "DEF");
        adicionarJogador(tun, 25, "Anis Slimane", "MED");
        adicionarJogador(tun, 26, "Sebastian Tounekti", "MED");
        selecoes.add(tun);

        Selecao bel = criarSelecao("Bélgica", 3, 15, "G");
        adicionarJogador(bel, 1, "Thibaut Courtois", "GR");
        adicionarJogador(bel, 2, "Zeno Debast", "DEF");
        adicionarJogador(bel, 3, "Arthur Theate", "DEF");
        adicionarJogador(bel, 4, "Brandon Mechele", "DEF");
        adicionarJogador(bel, 5, "Maxim De Cuyper", "DEF");
        adicionarJogador(bel, 6, "Axel Witsel", "MED");
        adicionarJogador(bel, 7, "Kevin De Bruyne", "MED");
        adicionarJogador(bel, 8, "Youri Tielemans", "MED");
        adicionarJogador(bel, 9, "Romelu Lukaku", "AV");
        adicionarJogador(bel, 10, "Leandro Trossard", "AV");
        adicionarJogador(bel, 11, "Jeremy Doku", "AV");
        adicionarJogador(bel, 12, "Senne Lammens", "GR");
        adicionarJogador(bel, 13, "Mike Penders", "GR");
        adicionarJogador(bel, 14, "Dodi Lukebakio", "AV");
        adicionarJogador(bel, 15, "Thomas Meunier", "DEF");
        adicionarJogador(bel, 16, "Koni De Winter", "DEF");
        adicionarJogador(bel, 17, "Charles De Ketelaere", "AV");
        adicionarJogador(bel, 18, "Joaquin Seys", "DEF");
        adicionarJogador(bel, 19, "Diego Moreira", "MED");
        adicionarJogador(bel, 20, "Hans Vanaken", "MED");
        adicionarJogador(bel, 21, "Timothy Castagne", "DEF");
        adicionarJogador(bel, 22, "Alexis Saelemaekers", "MED");
        adicionarJogador(bel, 23, "Nicolas Raskin", "MED");
        adicionarJogador(bel, 24, "Amadou Onana", "MED");
        adicionarJogador(bel, 25, "Nathan Ngoy", "DEF");
        adicionarJogador(bel, 26, "Matias Fernandez-Pardo", "AV");
        selecoes.add(bel);

        Selecao egy = criarSelecao("Egito", 34, 4, "G");
        adicionarJogador(egy, 1, "Mohamed Elshenawy", "GR");
        adicionarJogador(egy, 2, "Yasser Ibrahim", "DEF");
        adicionarJogador(egy, 3, "Mohamed Hany", "DEF");
        adicionarJogador(egy, 4, "Hossam Abdelmaguid", "DEF");
        adicionarJogador(egy, 5, "Ramy Rabia", "DEF");
        adicionarJogador(egy, 6, "Mohamed Abdelmoneim", "DEF");
        adicionarJogador(egy, 7, "Trezeguet", "AV");
        adicionarJogador(egy, 8, "Emam Ashour", "MED");
        adicionarJogador(egy, 9, "Hamza Abdelkarim", "AV");
        adicionarJogador(egy, 10, "Mohamed Salah", "AV");
        adicionarJogador(egy, 11, "Mostafa Zico", "MED");
        adicionarJogador(egy, 12, "Haissem Hassan", "AV");
        adicionarJogador(egy, 13, "Ahmed Fatouh", "DEF");
        adicionarJogador(egy, 14, "Hamdy Fathy", "MED");
        adicionarJogador(egy, 15, "Karim Hafez", "DEF");
        adicionarJogador(egy, 16, "Mahdy Soliman", "GR");
        adicionarJogador(egy, 17, "Mohanad Lashin", "MED");
        adicionarJogador(egy, 18, "Nabil Donga", "MED");
        adicionarJogador(egy, 19, "Marawan Attia", "MED");
        adicionarJogador(egy, 20, "Ibrahim Adel", "AV");
        adicionarJogador(egy, 21, "Mahmoud Saber", "MED");
        adicionarJogador(egy, 22, "Omar Marmoush", "AV");
        adicionarJogador(egy, 23, "Mostafa Shoubir", "GR");
        adicionarJogador(egy, 24, "Tarek Alaa", "DEF");
        adicionarJogador(egy, 25, "Zizo", "AV");
        adicionarJogador(egy, 26, "Mohamed Alaa", "GR");
        selecoes.add(egy);

        Selecao irn = criarSelecao("Irão", 20, 7, "G");
        adicionarJogador(irn, 1, "Alireza Beiranvand", "GR");
        adicionarJogador(irn, 2, "Saleh Hardani", "DEF");
        adicionarJogador(irn, 3, "Ehsan Hajisafi", "DEF");
        adicionarJogador(irn, 4, "Shoja Khalilzadeh", "DEF");
        adicionarJogador(irn, 5, "Milad Mohammadi", "DEF");
        adicionarJogador(irn, 6, "Saeid Ezatolahi", "MED");
        adicionarJogador(irn, 7, "Alireza Jahanbakhsh", "MED");
        adicionarJogador(irn, 8, "Mohammad Mohebbi", "MED");
        adicionarJogador(irn, 9, "Mehdi Taremi", "AV");
        adicionarJogador(irn, 10, "Mehdi Ghayedi", "AV");
        adicionarJogador(irn, 11, "Ali Alipour", "AV");
        adicionarJogador(irn, 12, "Payam Niazmand", "GR");
        adicionarJogador(irn, 13, "Hossein Kanani", "DEF");
        adicionarJogador(irn, 14, "Saman Ghoddos", "MED");
        adicionarJogador(irn, 15, "Roozbeh Cheshmi", "MED");
        adicionarJogador(irn, 16, "Mehdi Torabi", "MED");
        adicionarJogador(irn, 17, "Arya Yousefi", "DEF");
        adicionarJogador(irn, 18, "Amirhossein Hosseinzadeh", "AV");
        adicionarJogador(irn, 19, "Ali Nemati", "DEF");
        adicionarJogador(irn, 20, "Shahriyar Moghanloo", "AV");
        adicionarJogador(irn, 21, "Mohammad Ghorbani", "MED");
        adicionarJogador(irn, 22, "Hossein Hosseini", "GR");
        adicionarJogador(irn, 23, "Ramin Rezaeian", "DEF");
        adicionarJogador(irn, 24, "Dennis Dargahi", "AV");
        adicionarJogador(irn, 25, "Danial Iri", "DEF");
        adicionarJogador(irn, 26, "Amirmohammad Razaghinia", "MED");
        selecoes.add(irn);

        Selecao nzl = criarSelecao("Nova Zelândia", 103, 4, "G");
        adicionarJogador(nzl, 1, "Max Crocombe", "GR");
        adicionarJogador(nzl, 2, "Tim Payne", "DEF");
        adicionarJogador(nzl, 3, "Francis De Vries", "DEF");
        adicionarJogador(nzl, 4, "Tyler Bindon", "DEF");
        adicionarJogador(nzl, 5, "Michael Boxall", "DEF");
        adicionarJogador(nzl, 6, "Joe Bell", "MED");
        adicionarJogador(nzl, 7, "Logan Rogerson", "AV");
        adicionarJogador(nzl, 8, "Marko Stamenic", "MED");
        adicionarJogador(nzl, 9, "Chris Wood", "AV");
        adicionarJogador(nzl, 10, "Sarpreet Singh", "MED");
        adicionarJogador(nzl, 11, "Elijah Just", "MED");
        adicionarJogador(nzl, 12, "Alex Paulsen", "GR");
        adicionarJogador(nzl, 13, "Liberato Cacace", "DEF");
        adicionarJogador(nzl, 14, "Alex Rufer", "MED");
        adicionarJogador(nzl, 15, "Nando Pijnaker", "DEF");
        adicionarJogador(nzl, 16, "Finn Surman", "DEF");
        adicionarJogador(nzl, 17, "Kosta Barbarouses", "AV");
        adicionarJogador(nzl, 18, "Ben Waine", "AV");
        adicionarJogador(nzl, 19, "Ben Old", "MED");
        adicionarJogador(nzl, 20, "Callum McCowatt", "MED");
        adicionarJogador(nzl, 21, "Jesse Randall", "AV");
        adicionarJogador(nzl, 22, "Michael Woud", "GR");
        adicionarJogador(nzl, 23, "Ryan Thomas", "MED");
        adicionarJogador(nzl, 24, "Callan Elliot", "DEF");
        adicionarJogador(nzl, 25, "Lachlan Bayliss", "MED");
        adicionarJogador(nzl, 26, "Tommy Smith", "DEF");
        selecoes.add(nzl);

        Selecao ksa = criarSelecao("Arábia Saudita", 58, 7, "H");
        adicionarJogador(ksa, 1, "Nawaf Alaqidi", "GR");
        adicionarJogador(ksa, 2, "Ali Majrashi", "DEF");
        adicionarJogador(ksa, 3, "Ali Lajami", "DEF");
        adicionarJogador(ksa, 4, "Abdulelah Alamri", "DEF");
        adicionarJogador(ksa, 5, "Hassan Altambakti", "DEF");
        adicionarJogador(ksa, 6, "Nasser Aldawsari", "MED");
        adicionarJogador(ksa, 7, "Musab Aljuwayr", "MED");
        adicionarJogador(ksa, 8, "Aiman Yahya", "AV");
        adicionarJogador(ksa, 9, "Feras Albrikan", "AV");
        adicionarJogador(ksa, 10, "Salem Aldawsari", "AV");
        adicionarJogador(ksa, 11, "Saleh Alshehri", "AV");
        adicionarJogador(ksa, 12, "Saud Abdulhamid", "DEF");
        adicionarJogador(ksa, 13, "Nawaf Bu Washl", "DEF");
        adicionarJogador(ksa, 14, "Hassan Kadish", "DEF");
        adicionarJogador(ksa, 15, "Abdullah Alkhaibari", "MED");
        adicionarJogador(ksa, 16, "Ziyad Aljohani", "MED");
        adicionarJogador(ksa, 17, "Khalid Alghannam", "AV");
        adicionarJogador(ksa, 18, "Ala Alhajji", "MED");
        adicionarJogador(ksa, 19, "Abdullah Alhamddan", "AV");
        adicionarJogador(ksa, 20, "Sultan Mandash", "AV");
        adicionarJogador(ksa, 21, "Mohammed Alowais", "GR");
        adicionarJogador(ksa, 22, "Ahmed Alkassar", "GR");
        adicionarJogador(ksa, 23, "Mohamed Kanno", "MED");
        adicionarJogador(ksa, 24, "Moteb Alharbi", "DEF");
        adicionarJogador(ksa, 25, "Jehad Thikri", "DEF");
        adicionarJogador(ksa, 26, "Mohammed Abu Alshamat", "DEF");
        selecoes.add(ksa);

        Selecao cpv = criarSelecao("Cabo Verde", 65, 1, "H");
        adicionarJogador(cpv, 1, "Vozinha", "GR");
        adicionarJogador(cpv, 2, "Stopira", "DEF");
        adicionarJogador(cpv, 3, "Diney Borges", "DEF");
        adicionarJogador(cpv, 4, "Pico Lopes", "DEF");
        adicionarJogador(cpv, 5, "Logan Costa", "DEF");
        adicionarJogador(cpv, 6, "Kevin Pina", "MED");
        adicionarJogador(cpv, 7, "Jovane Cabral", "MED");
        adicionarJogador(cpv, 8, "Joao Paulo", "MED");
        adicionarJogador(cpv, 9, "Gilson Benchimol", "AV");
        adicionarJogador(cpv, 10, "Jamiro Monteiro", "MED");
        adicionarJogador(cpv, 11, "Garry Rodrigues", "MED");
        adicionarJogador(cpv, 12, "Marcio Rosa", "GR");
        adicionarJogador(cpv, 13, "Sidny Lopes Cabral", "DEF");
        adicionarJogador(cpv, 14, "Deroy Duarte", "MED");
        adicionarJogador(cpv, 15, "Laros Duarte", "MED");
        adicionarJogador(cpv, 16, "Yannick Semedo", "MED");
        adicionarJogador(cpv, 17, "Willy Semedo", "MED");
        adicionarJogador(cpv, 18, "Telmo Arcanjo", "MED");
        adicionarJogador(cpv, 19, "Dailon Livramento", "AV");
        adicionarJogador(cpv, 20, "Ryan Mendes", "AV");
        adicionarJogador(cpv, 21, "Nuno Da Costa", "MED");
        adicionarJogador(cpv, 22, "Steven Moreira", "DEF");
        adicionarJogador(cpv, 23, "Cj Dos Santos", "GR");
        adicionarJogador(cpv, 24, "Wagner Pina", "DEF");
        adicionarJogador(cpv, 25, "Kelvin Pires", "DEF");
        adicionarJogador(cpv, 26, "Helio Varela", "MED");
        selecoes.add(cpv);

        Selecao esp = criarSelecao("Espanha", 8, 17, "H");
        adicionarJogador(esp, 1, "David Raya", "GR");
        adicionarJogador(esp, 2, "Marc Pubill", "DEF");
        adicionarJogador(esp, 3, "Alex Grimaldo", "DEF");
        adicionarJogador(esp, 4, "Eric Garcia", "DEF");
        adicionarJogador(esp, 5, "Marcos Llorente", "DEF");
        adicionarJogador(esp, 6, "Mikel Merino", "MED");
        adicionarJogador(esp, 7, "Ferran Torres", "AV");
        adicionarJogador(esp, 8, "Fabian Ruiz", "MED");
        adicionarJogador(esp, 9, "Gavi", "MED");
        adicionarJogador(esp, 10, "Dani Olmo", "AV");
        adicionarJogador(esp, 11, "Yeremy Pino", "AV");
        adicionarJogador(esp, 12, "Pedro Porro", "DEF");
        adicionarJogador(esp, 13, "Joan Garcia", "GR");
        adicionarJogador(esp, 14, "Aymeric Laporte", "DEF");
        adicionarJogador(esp, 15, "Alex Baena", "MED");
        adicionarJogador(esp, 16, "Rodri", "MED");
        adicionarJogador(esp, 17, "Nico Williams", "AV");
        adicionarJogador(esp, 18, "Martin Zubimendi", "MED");
        adicionarJogador(esp, 19, "Lamine Yamal", "AV");
        adicionarJogador(esp, 20, "Pedri", "MED");
        adicionarJogador(esp, 21, "Mikel Oyarzabal", "AV");
        adicionarJogador(esp, 22, "Pau Cubarsi", "DEF");
        adicionarJogador(esp, 23, "Unai Simon", "GR");
        adicionarJogador(esp, 24, "Marc Cucurella", "DEF");
        adicionarJogador(esp, 25, "Victor Munoz", "AV");
        adicionarJogador(esp, 26, "Borja Iglesias", "AV");
        selecoes.add(esp);

        Selecao uru = criarSelecao("Uruguai", 11, 15, "H");
        adicionarJogador(uru, 1, "Sergio Rochet", "GR");
        adicionarJogador(uru, 2, "Jose Maria Gimenez", "DEF");
        adicionarJogador(uru, 3, "Sebastian Caceres", "DEF");
        adicionarJogador(uru, 4, "Ronald Araujo", "DEF");
        adicionarJogador(uru, 5, "Manuel Ugarte", "MED");
        adicionarJogador(uru, 6, "Rodrigo Bentancur", "MED");
        adicionarJogador(uru, 7, "Nicolas De La Cruz", "MED");
        adicionarJogador(uru, 8, "Federico Valverde", "MED");
        adicionarJogador(uru, 9, "Darwin Nunez", "AV");
        adicionarJogador(uru, 10, "Giorgian De Arrascaeta", "MED");
        adicionarJogador(uru, 11, "Facundo Pellistri", "AV");
        adicionarJogador(uru, 12, "Santiago Mele", "GR");
        adicionarJogador(uru, 13, "Guillermo Varela", "DEF");
        adicionarJogador(uru, 14, "Agustin Canobbio", "MED");
        adicionarJogador(uru, 15, "Emiliano Martinez", "MED");
        adicionarJogador(uru, 16, "Mathias Olivera", "DEF");
        adicionarJogador(uru, 17, "Matias Vina", "DEF");
        adicionarJogador(uru, 18, "Brian Rodriguez", "AV");
        adicionarJogador(uru, 19, "Rodrigo Aguirre", "AV");
        adicionarJogador(uru, 20, "Maxi Araujo", "MED");
        adicionarJogador(uru, 21, "Federico Vinas", "AV");
        adicionarJogador(uru, 22, "Joaquin Piquerez", "MED");
        adicionarJogador(uru, 23, "Fernando Muslera", "GR");
        adicionarJogador(uru, 24, "Santiago Bueno", "DEF");
        adicionarJogador(uru, 25, "Juan Manuel Sanabria", "MED");
        adicionarJogador(uru, 26, "Rodrigo Zalazar", "MED");
        selecoes.add(uru);

        Selecao fra = criarSelecao("França", 2, 17, "I");
        adicionarJogador(fra, 1, "Brice Samba", "GR");
        adicionarJogador(fra, 2, "Malo Gusto", "DEF");
        adicionarJogador(fra, 3, "Lucas Digne", "DEF");
        adicionarJogador(fra, 4, "Dayot Upamecano", "DEF");
        adicionarJogador(fra, 5, "Jules Kounde", "DEF");
        adicionarJogador(fra, 6, "Manu Kone", "MED");
        adicionarJogador(fra, 7, "Ousmane Dembele", "AV");
        adicionarJogador(fra, 8, "Aurelien Tchouameni", "MED");
        adicionarJogador(fra, 9, "Marcus Thuram", "AV");
        adicionarJogador(fra, 10, "Kylian Mbappe", "AV");
        adicionarJogador(fra, 11, "Michael Olise", "AV");
        adicionarJogador(fra, 12, "Bradley Barcola", "AV");
        adicionarJogador(fra, 13, "Ngolo Kante", "MED");
        adicionarJogador(fra, 14, "Adrien Rabiot", "MED");
        adicionarJogador(fra, 15, "Ibrahima Konate", "DEF");
        adicionarJogador(fra, 16, "Mike Maignan", "GR");
        adicionarJogador(fra, 17, "William Saliba", "DEF");
        adicionarJogador(fra, 18, "Warren Zaire-Emery", "MED");
        adicionarJogador(fra, 19, "Theo Hernandez", "DEF");
        adicionarJogador(fra, 20, "Desire Doue", "AV");
        adicionarJogador(fra, 21, "Lucas Hernandez", "DEF");
        adicionarJogador(fra, 22, "Jean-Philippe Mateta", "AV");
        adicionarJogador(fra, 23, "Robin Risser", "GR");
        adicionarJogador(fra, 24, "Rayan Cherki", "MED");
        adicionarJogador(fra, 25, "Maghnes Akliouche", "MED");
        adicionarJogador(fra, 26, "Maxence Lacroix", "DEF");
        selecoes.add(fra);

        Selecao irq = criarSelecao("Iraque", 55, 2, "I");
        adicionarJogador(irq, 1, "Fahad Talib", "GR");
        adicionarJogador(irq, 2, "Rebin Sulaka", "DEF");
        adicionarJogador(irq, 3, "Hussein Ali", "DEF");
        adicionarJogador(irq, 4, "Zaid Tahseen", "DEF");
        adicionarJogador(irq, 5, "Akam Hashim", "DEF");
        adicionarJogador(irq, 6, "Munaf Younus", "DEF");
        adicionarJogador(irq, 7, "Youssef Amyn", "MED");
        adicionarJogador(irq, 8, "Ibrahim Bayesh", "MED");
        adicionarJogador(irq, 9, "Ali Alhamadi", "AV");
        adicionarJogador(irq, 10, "Mohanad Ali", "AV");
        adicionarJogador(irq, 11, "Ahmed Qasem", "AV");
        adicionarJogador(irq, 12, "Jalal Hassan", "GR");
        adicionarJogador(irq, 13, "Ali Yousif", "AV");
        adicionarJogador(irq, 14, "Zidane Iqbal", "MED");
        adicionarJogador(irq, 15, "Ahmed Maknazi", "DEF");
        adicionarJogador(irq, 16, "Amir Alammari", "MED");
        adicionarJogador(irq, 17, "Ali Jasim", "AV");
        adicionarJogador(irq, 18, "Aymen Hussein", "AV");
        adicionarJogador(irq, 19, "Kevin Yakob", "MED");
        adicionarJogador(irq, 20, "Aimar Sher", "MED");
        adicionarJogador(irq, 21, "Marko Farji", "AV");
        adicionarJogador(irq, 22, "Ahmed Basil", "GR");
        adicionarJogador(irq, 23, "Merchas Doski", "DEF");
        adicionarJogador(irq, 24, "Zaid Ismael", "MED");
        adicionarJogador(irq, 25, "Mustafa Saadoon", "DEF");
        adicionarJogador(irq, 26, "Frans Putros", "DEF");
        selecoes.add(irq);

        Selecao nor = criarSelecao("Noruega", 44, 4, "I");
        adicionarJogador(nor, 1, "Orjan Nyland", "GR");
        adicionarJogador(nor, 2, "Morten Thorsby", "MED");
        adicionarJogador(nor, 3, "Kristoffer Ajer", "DEF");
        adicionarJogador(nor, 4, "Leo Ostigard", "DEF");
        adicionarJogador(nor, 5, "David Moller Wolfe", "DEF");
        adicionarJogador(nor, 6, "Patrick Berg", "MED");
        adicionarJogador(nor, 7, "Alexander Sorloth", "AV");
        adicionarJogador(nor, 8, "Sander Berge", "MED");
        adicionarJogador(nor, 9, "Erling Haaland", "AV");
        adicionarJogador(nor, 10, "Martin Odegaard", "MED");
        adicionarJogador(nor, 11, "Jorgen Strand Larsen", "AV");
        adicionarJogador(nor, 12, "Sander Tangvik", "GR");
        adicionarJogador(nor, 13, "Egil Selvik", "GR");
        adicionarJogador(nor, 14, "Fredrik Aursnes", "MED");
        adicionarJogador(nor, 15, "Fredrik Andre Bjorkan", "DEF");
        adicionarJogador(nor, 16, "Marcus Holmgren Pedersen", "DEF");
        adicionarJogador(nor, 17, "Torbjorn Heggem", "DEF");
        adicionarJogador(nor, 18, "Kristian Thorstvedt", "MED");
        adicionarJogador(nor, 19, "Thelo Aasgaard", "MED");
        adicionarJogador(nor, 20, "Antonio Nusa", "AV");
        adicionarJogador(nor, 21, "Andreas Schjelderup", "MED");
        adicionarJogador(nor, 22, "Oscar Bobb", "MED");
        adicionarJogador(nor, 23, "Jens Petter Hauge", "MED");
        adicionarJogador(nor, 24, "Sondre Langas", "DEF");
        adicionarJogador(nor, 25, "Henrik Falchener", "DEF");
        adicionarJogador(nor, 26, "Julian Ryerson", "AV");
        selecoes.add(nor);

        Selecao sen = criarSelecao("Senegal", 17, 4, "I");
        adicionarJogador(sen, 1, "Yehvann Diouf", "GR");
        adicionarJogador(sen, 2, "Mamadou Sarr", "DEF");
        adicionarJogador(sen, 3, "Kalidou Koulibaly", "DEF");
        adicionarJogador(sen, 4, "Abdoulaye Seck", "DEF");
        adicionarJogador(sen, 5, "Idrissa Gana Gueye", "MED");
        adicionarJogador(sen, 6, "Pathe Ciss", "MED");
        adicionarJogador(sen, 7, "Assane Diao", "AV");
        adicionarJogador(sen, 8, "Lamine Camara", "MED");
        adicionarJogador(sen, 9, "Bamba Dieng", "AV");
        adicionarJogador(sen, 10, "Sadio Mane", "AV");
        adicionarJogador(sen, 11, "Nicolas Jackson", "AV");
        adicionarJogador(sen, 12, "Cherif Ndiaye", "AV");
        adicionarJogador(sen, 13, "Iliman Ndiaye", "AV");
        adicionarJogador(sen, 14, "Ismail Jakobs", "DEF");
        adicionarJogador(sen, 15, "Krepin Diatta", "DEF");
        adicionarJogador(sen, 16, "Edouard Mendy", "GR");
        adicionarJogador(sen, 17, "Pape Matar Sarr", "MED");
        adicionarJogador(sen, 18, "Ismaila Sarr", "AV");
        adicionarJogador(sen, 19, "Moussa Niakhate", "DEF");
        adicionarJogador(sen, 20, "Ibrahim Mbaye", "AV");
        adicionarJogador(sen, 21, "Habib Diarra", "MED");
        adicionarJogador(sen, 22, "Bara Sapoko Ndiaye", "MED");
        adicionarJogador(sen, 23, "Mory Diaw", "GR");
        adicionarJogador(sen, 24, "Antoine Mendy", "DEF");
        adicionarJogador(sen, 25, "El Hadji Malick Diouf", "DEF");
        adicionarJogador(sen, 26, "Pape Gueye", "MED");
        selecoes.add(sen);

        Selecao arg = criarSelecao("Argentina", 1, 19, "J");
        adicionarJogador(arg, 1, "Juan Musso", "GR");
        adicionarJogador(arg, 2, "Marcos Senesi", "DEF");
        adicionarJogador(arg, 3, "Nicolas Tagliafico", "DEF");
        adicionarJogador(arg, 4, "Gonzalo Montiel", "DEF");
        adicionarJogador(arg, 5, "Leandro Paredes", "MED");
        adicionarJogador(arg, 6, "Lisandro Martinez", "DEF");
        adicionarJogador(arg, 7, "Rodrigo De Paul", "MED");
        adicionarJogador(arg, 8, "Valentin Barco", "MED");
        adicionarJogador(arg, 9, "Julian Alvarez", "AV");
        adicionarJogador(arg, 10, "Lionel Messi", "AV");
        adicionarJogador(arg, 11, "Giovani Lo Celso", "MED");
        adicionarJogador(arg, 12, "Geronimo Rulli", "GR");
        adicionarJogador(arg, 13, "Cristian Romero", "DEF");
        adicionarJogador(arg, 14, "Exequiel Palacios", "MED");
        adicionarJogador(arg, 15, "Nico Gonzalez", "MED");
        adicionarJogador(arg, 16, "Thiago Almada", "AV");
        adicionarJogador(arg, 17, "Giuliano Simeone", "AV");
        adicionarJogador(arg, 18, "Nico Paz", "AV");
        adicionarJogador(arg, 19, "Nicolas Otamendi", "DEF");
        adicionarJogador(arg, 20, "Alexis Mac Allister", "MED");
        adicionarJogador(arg, 21, "Jose Manuel Lopez", "AV");
        adicionarJogador(arg, 22, "Lautaro Martinez", "AV");
        adicionarJogador(arg, 23, "Emiliano Martinez", "GR");
        adicionarJogador(arg, 24, "Enzo Fernandez", "MED");
        adicionarJogador(arg, 25, "Facundo Medina", "DEF");
        adicionarJogador(arg, 26, "Nahuel Molina", "DEF");
        selecoes.add(arg);

        Selecao alg = criarSelecao("Argélia", 37, 5, "J");
        adicionarJogador(alg, 1, "Melvin Mastil", "GR");
        adicionarJogador(alg, 2, "Aissa Mandi", "DEF");
        adicionarJogador(alg, 3, "Achref Abada", "DEF");
        adicionarJogador(alg, 4, "Mohamed Amine Tougai", "DEF");
        adicionarJogador(alg, 5, "Zineddine Belaid", "DEF");
        adicionarJogador(alg, 6, "Ramiz Zerrouki", "MED");
        adicionarJogador(alg, 7, "Riyad Mahrez", "AV");
        adicionarJogador(alg, 8, "Houssem Aouar", "MED");
        adicionarJogador(alg, 9, "Amine Gouiri", "AV");
        adicionarJogador(alg, 10, "Fares Chaibi", "MED");
        adicionarJogador(alg, 11, "Anis Hadj Moussa", "AV");
        adicionarJogador(alg, 12, "Nadhir Benbouali", "AV");
        adicionarJogador(alg, 13, "Jaouen Hadjam", "DEF");
        adicionarJogador(alg, 14, "Hicham Boudaoui", "MED");
        adicionarJogador(alg, 15, "Rayan Ait-Nouri", "DEF");
        adicionarJogador(alg, 16, "Oussama Benbot", "GR");
        adicionarJogador(alg, 17, "Ra K Belghali", "DEF");
        adicionarJogador(alg, 18, "Mohamed Amoura", "AV");
        adicionarJogador(alg, 19, "Nabil Bentaleb", "MED");
        adicionarJogador(alg, 20, "Adil Boulbina", "AV");
        adicionarJogador(alg, 21, "Ramy Bensebaini", "DEF");
        adicionarJogador(alg, 22, "Ibrahim Maza", "MED");
        adicionarJogador(alg, 23, "Luca Zidane", "GR");
        adicionarJogador(alg, 24, "Yassine Titraoui", "MED");
        adicionarJogador(alg, 25, "Fares Ghedjemis", "AV");
        adicionarJogador(alg, 26, "Samir Chergui", "DEF");
        selecoes.add(alg);

        Selecao jor = criarSelecao("Jordânia", 68, 1, "J");
        adicionarJogador(jor, 1, "Yazeed Abulaila", "GR");
        adicionarJogador(jor, 2, "Mohammad Abuhasheesh", "DEF");
        adicionarJogador(jor, 3, "Abdallah Nasib", "DEF");
        adicionarJogador(jor, 4, "Husam Abudahab", "DEF");
        adicionarJogador(jor, 5, "Yazan Alarab", "DEF");
        adicionarJogador(jor, 6, "Amer Jamous", "MED");
        adicionarJogador(jor, 7, "Mohammad Abuzraiq", "AV");
        adicionarJogador(jor, 8, "Noor Alrawabdeh", "MED");
        adicionarJogador(jor, 9, "Ali Olwan", "AV");
        adicionarJogador(jor, 10, "Mousa Altamari", "AV");
        adicionarJogador(jor, 11, "Odeh Fakhoury", "AV");
        adicionarJogador(jor, 12, "Nour Baniateyah", "GR");
        adicionarJogador(jor, 13, "Mahmoud Almardi", "AV");
        adicionarJogador(jor, 14, "Rajaei Ayed", "MED");
        adicionarJogador(jor, 15, "Ibrahim Sadeh", "MED");
        adicionarJogador(jor, 16, "Mohammad Abualnadi", "DEF");
        adicionarJogador(jor, 17, "Saleem Obaid", "DEF");
        adicionarJogador(jor, 18, "Mohammad Abughoush", "MED");
        adicionarJogador(jor, 19, "Saed Alrosan", "DEF");
        adicionarJogador(jor, 20, "Mohannad Abutaha", "MED");
        adicionarJogador(jor, 21, "Nizar Alrashdan", "MED");
        adicionarJogador(jor, 22, "Abdallah Alfakhori", "GR");
        adicionarJogador(jor, 23, "Ehsan Haddad", "DEF");
        adicionarJogador(jor, 24, "Ali Azaizeh", "AV");
        adicionarJogador(jor, 25, "Mohammad Aldaoud", "MED");
        adicionarJogador(jor, 26, "Anas Badawi", "DEF");
        selecoes.add(jor);

        Selecao aut = criarSelecao("Áustria", 25, 8, "J");
        adicionarJogador(aut, 1, "Alexander Schlager", "GR");
        adicionarJogador(aut, 2, "David Affengruber", "DEF");
        adicionarJogador(aut, 3, "Kevin Danso", "DEF");
        adicionarJogador(aut, 4, "Xaver Schlager", "MED");
        adicionarJogador(aut, 5, "Stefan Posch", "DEF");
        adicionarJogador(aut, 6, "Nicolas Seiwald", "MED");
        adicionarJogador(aut, 7, "Marko Arnautovic", "AV");
        adicionarJogador(aut, 8, "David Alaba", "DEF");
        adicionarJogador(aut, 9, "Marcel Sabitzer", "MED");
        adicionarJogador(aut, 10, "Florian Grillitsch", "MED");
        adicionarJogador(aut, 11, "Michael Gregoritsch", "AV");
        adicionarJogador(aut, 12, "Florian Wiegele", "GR");
        adicionarJogador(aut, 13, "Patrick Pentz", "GR");
        adicionarJogador(aut, 14, "Sasa Kalajdzic", "AV");
        adicionarJogador(aut, 15, "Philipp Lienhart", "DEF");
        adicionarJogador(aut, 16, "Phillip Mwene", "DEF");
        adicionarJogador(aut, 17, "Carney Chukwuemeka", "MED");
        adicionarJogador(aut, 18, "Romano Schmid", "MED");
        adicionarJogador(aut, 19, "Dejan Ljubicic", "MED");
        adicionarJogador(aut, 20, "Konrad Laimer", "MED");
        adicionarJogador(aut, 21, "Patrick Wimmer", "AV");
        adicionarJogador(aut, 22, "Alexander Prass", "MED");
        adicionarJogador(aut, 23, "Marco Friedl", "DEF");
        adicionarJogador(aut, 24, "Paul Wanner", "MED");
        adicionarJogador(aut, 25, "Michael Svoboda", "DEF");
        adicionarJogador(aut, 26, "Alessandro Schoepf", "MED");
        selecoes.add(aut);

        Selecao col = criarSelecao("Colômbia", 13, 7, "K");
        adicionarJogador(col, 1, "David Ospina", "GR");
        adicionarJogador(col, 2, "Daniel Munoz", "DEF");
        adicionarJogador(col, 3, "Jhon Lucumi", "DEF");
        adicionarJogador(col, 4, "Santiago Arias", "DEF");
        adicionarJogador(col, 5, "Kevin Castano", "MED");
        adicionarJogador(col, 6, "Richard Rios", "MED");
        adicionarJogador(col, 7, "Luis Diaz", "AV");
        adicionarJogador(col, 8, "Jorge Carrascal", "MED");
        adicionarJogador(col, 9, "Jhon Cordoba", "AV");
        adicionarJogador(col, 10, "James Rodriguez", "MED");
        adicionarJogador(col, 11, "Jhon Arias", "MED");
        adicionarJogador(col, 12, "Camilo Vargas", "GR");
        adicionarJogador(col, 13, "Yerry Mina", "DEF");
        adicionarJogador(col, 14, "Gustavo Puerta", "DEF");
        adicionarJogador(col, 15, "Juan Portilla", "MED");
        adicionarJogador(col, 16, "Jefferson Lerma", "MED");
        adicionarJogador(col, 17, "Johan Mojica", "DEF");
        adicionarJogador(col, 18, "Willer Ditta", "DEF");
        adicionarJogador(col, 19, "Cucho Hernandez", "AV");
        adicionarJogador(col, 20, "Juan Quintero", "MED");
        adicionarJogador(col, 21, "Jaminton Campaz", "AV");
        adicionarJogador(col, 22, "Deiver Machado", "DEF");
        adicionarJogador(col, 23, "Davinson Sanchez", "DEF");
        adicionarJogador(col, 24, "Alvaro Montero", "GR");
        adicionarJogador(col, 25, "Luis Suarez", "AV");
        adicionarJogador(col, 26, "Andres Gomez", "AV");
        selecoes.add(col);

        Selecao por = criarSelecao("Portugal", 6, 9, "K");
        adicionarJogador(por, 1, "Diogo Costa", "GR");
        adicionarJogador(por, 2, "Nelson Semedo", "DEF");
        adicionarJogador(por, 3, "Ruben Dias", "DEF");
        adicionarJogador(por, 4, "Tomas Araujo", "DEF");
        adicionarJogador(por, 5, "Diogo Dalot", "DEF");
        adicionarJogador(por, 6, "Matheus Nunes", "MED");
        adicionarJogador(por, 7, "Cristiano Ronaldo", "AV");
        adicionarJogador(por, 8, "Bruno Fernandes", "MED");
        adicionarJogador(por, 9, "Goncalo Ramos", "AV");
        adicionarJogador(por, 10, "Bernardo Silva", "MED");
        adicionarJogador(por, 11, "Joao Felix", "AV");
        adicionarJogador(por, 12, "Jose Sa", "GR");
        adicionarJogador(por, 13, "Renato Veiga", "DEF");
        adicionarJogador(por, 14, "Goncalo Inacio", "DEF");
        adicionarJogador(por, 15, "Joao Neves", "MED");
        adicionarJogador(por, 16, "Francisco Trincao", "AV");
        adicionarJogador(por, 17, "Rafael Leao", "AV");
        adicionarJogador(por, 18, "Pedro Neto", "AV");
        adicionarJogador(por, 19, "Goncalo Guedes", "AV");
        adicionarJogador(por, 20, "Joao Cancelo", "DEF");
        adicionarJogador(por, 21, "Ruben Neves", "MED");
        adicionarJogador(por, 22, "Rui Silva", "GR");
        adicionarJogador(por, 23, "Vitinha", "MED");
        adicionarJogador(por, 24, "Samu Costa", "DEF");
        adicionarJogador(por, 25, "Nuno Mendes", "DEF");
        adicionarJogador(por, 26, "Francisco Conceicao", "AV");
        selecoes.add(por);

        Selecao cod = criarSelecao("RD Congo", 61, 2, "K");
        adicionarJogador(cod, 1, "Lionel Mpasi", "GR");
        adicionarJogador(cod, 2, "Aaron Wan-Bissaka", "DEF");
        adicionarJogador(cod, 3, "Steve Kapuadi", "DEF");
        adicionarJogador(cod, 4, "Axel Tuanzebe", "DEF");
        adicionarJogador(cod, 5, "Dylan Batubinsika", "DEF");
        adicionarJogador(cod, 6, "Ngalayel Mukau", "MED");
        adicionarJogador(cod, 7, "Nathanael Mbuku", "MED");
        adicionarJogador(cod, 8, "Samuel Moutoussamy", "MED");
        adicionarJogador(cod, 9, "Brian Cipenga", "AV");
        adicionarJogador(cod, 10, "Theo Bongonda", "MED");
        adicionarJogador(cod, 11, "Gael Kakuta", "AV");
        adicionarJogador(cod, 12, "Joris Kayembe", "DEF");
        adicionarJogador(cod, 13, "Meschack Elia", "AV");
        adicionarJogador(cod, 14, "Noah Sadiki", "MED");
        adicionarJogador(cod, 15, "Aaron Tshibola", "MED");
        adicionarJogador(cod, 16, "Timothy Fayulu", "GR");
        adicionarJogador(cod, 17, "Cedric Bakambu", "AV");
        adicionarJogador(cod, 18, "Charles Pickel", "MED");
        adicionarJogador(cod, 19, "Fiston Mayele", "AV");
        adicionarJogador(cod, 20, "Yoane Wissa", "AV");
        adicionarJogador(cod, 21, "Matthieu Epolo", "GR");
        adicionarJogador(cod, 22, "Chancel Mbemba", "DEF");
        adicionarJogador(cod, 23, "Simon Banza", "AV");
        adicionarJogador(cod, 24, "Gedeon Kalulu", "DEF");
        adicionarJogador(cod, 25, "Edo Kayembe", "MED");
        adicionarJogador(cod, 26, "Arthur Masuaku", "DEF");
        selecoes.add(cod);

        Selecao uzb = criarSelecao("Uzbequistão", 57, 1, "K");
        adicionarJogador(uzb, 1, "Utkir Yusupov", "GR");
        adicionarJogador(uzb, 2, "Abdukodir Khusanov", "DEF");
        adicionarJogador(uzb, 3, "Khojiakbar Alijonov", "DEF");
        adicionarJogador(uzb, 4, "Farrukh Sayfiev", "DEF");
        adicionarJogador(uzb, 5, "Rustam Ashurmatov", "DEF");
        adicionarJogador(uzb, 6, "Akmal Mozgovoy", "MED");
        adicionarJogador(uzb, 7, "Otabek Shukurov", "MED");
        adicionarJogador(uzb, 8, "Jamshid Iskanderov", "MED");
        adicionarJogador(uzb, 9, "Odiljon Xamrobekov", "MED");
        adicionarJogador(uzb, 10, "Ruslanbek Jiyanov", "MED");
        adicionarJogador(uzb, 11, "Oston Urunov", "MED");
        adicionarJogador(uzb, 12, "Abduvohid Nematov", "GR");
        adicionarJogador(uzb, 13, "Sherzod Nasrullaev", "DEF");
        adicionarJogador(uzb, 14, "Eldor Shomurodov", "AV");
        adicionarJogador(uzb, 15, "Umar Eshmurodov", "DEF");
        adicionarJogador(uzb, 16, "Botirali Ergashev", "GR");
        adicionarJogador(uzb, 17, "Dostonbek Khamdamov", "MED");
        adicionarJogador(uzb, 18, "Abdulla Abdullaev", "DEF");
        adicionarJogador(uzb, 19, "Azizjon Ganiev", "MED");
        adicionarJogador(uzb, 20, "Azizbek Amonov", "AV");
        adicionarJogador(uzb, 21, "Igor Sergeev", "AV");
        adicionarJogador(uzb, 22, "Abbosbek Fayzullaev", "MED");
        adicionarJogador(uzb, 23, "Sherzod Esanov", "MED");
        adicionarJogador(uzb, 24, "Behruzjon Karimov", "DEF");
        adicionarJogador(uzb, 25, "Avazbek Ulmasaliyev", "DEF");
        adicionarJogador(uzb, 26, "Jakhongir Urozov", "DEF");
        selecoes.add(uzb);

        Selecao cro = criarSelecao("Croácia", 10, 7, "L");
        adicionarJogador(cro, 1, "Dominik Livakovic", "GR");
        adicionarJogador(cro, 2, "Josip Stanisic", "DEF");
        adicionarJogador(cro, 3, "Marin Pongracic", "DEF");
        adicionarJogador(cro, 4, "Josko Gvardiol", "DEF");
        adicionarJogador(cro, 5, "Duje Caleta-Car", "DEF");
        adicionarJogador(cro, 6, "Josip Sutalo", "DEF");
        adicionarJogador(cro, 7, "Nikola Moro", "MED");
        adicionarJogador(cro, 8, "Mateo Kovacic", "MED");
        adicionarJogador(cro, 9, "Andrej Kramaric", "AV");
        adicionarJogador(cro, 10, "Luka Modric", "MED");
        adicionarJogador(cro, 11, "Ante Budimir", "AV");
        adicionarJogador(cro, 12, "Ivor Pandur", "GR");
        adicionarJogador(cro, 13, "Nikola Vlasic", "MED");
        adicionarJogador(cro, 14, "Ivan Perisic", "AV");
        adicionarJogador(cro, 15, "Mario Pasalic", "MED");
        adicionarJogador(cro, 16, "Martin Baturina", "MED");
        adicionarJogador(cro, 17, "Petar Sucic", "MED");
        adicionarJogador(cro, 18, "Kristijan Jakic", "DEF");
        adicionarJogador(cro, 19, "Toni Fruk", "MED");
        adicionarJogador(cro, 20, "Igor Matanovic", "AV");
        adicionarJogador(cro, 21, "Luka Sucic", "MED");
        adicionarJogador(cro, 22, "Luka Vuskovic", "DEF");
        adicionarJogador(cro, 23, "Dominik Kotarski", "GR");
        adicionarJogador(cro, 24, "Marco Pasalic", "AV");
        adicionarJogador(cro, 25, "Martin Erlic", "DEF");
        adicionarJogador(cro, 26, "Petar Musa", "AV");
        selecoes.add(cro);

        Selecao gha = criarSelecao("Gana", 62, 5, "L");
        adicionarJogador(gha, 1, "Lawrence Ati Zigi", "GR");
        adicionarJogador(gha, 2, "Alidu Seidu", "DEF");
        adicionarJogador(gha, 3, "Caleb Yirenkyi", "MED");
        adicionarJogador(gha, 4, "Jonas Adjetey", "DEF");
        adicionarJogador(gha, 5, "Thomas Partey", "MED");
        adicionarJogador(gha, 6, "Abdul Mumin", "DEF");
        adicionarJogador(gha, 7, "Abdul Fatawu", "AV");
        adicionarJogador(gha, 8, "Kwasi Sibo", "MED");
        adicionarJogador(gha, 9, "Jordan Ayew", "AV");
        adicionarJogador(gha, 10, "Brandon Thomas-Asante", "AV");
        adicionarJogador(gha, 11, "Antoine Semenyo", "MED");
        adicionarJogador(gha, 12, "Joseph Anang", "GR");
        adicionarJogador(gha, 13, "Christopher Bonsu Baah", "AV");
        adicionarJogador(gha, 14, "Gideon Mensah", "DEF");
        adicionarJogador(gha, 15, "Elisha Owusu", "MED");
        adicionarJogador(gha, 16, "Benjamin Asare", "GR");
        adicionarJogador(gha, 17, "Baba Rahman", "DEF");
        adicionarJogador(gha, 18, "Jerome Opoku", "DEF");
        adicionarJogador(gha, 19, "Inaki Williams", "AV");
        adicionarJogador(gha, 20, "Augustine Boakye", "MED");
        adicionarJogador(gha, 21, "Kojo Peprah Oppong", "DEF");
        adicionarJogador(gha, 22, "Kamaldeen Sulemana", "AV");
        adicionarJogador(gha, 23, "Derrick Luckassen", "DEF");
        adicionarJogador(gha, 24, "Ernest Nuamah", "AV");
        adicionarJogador(gha, 25, "Prince Adu", "AV");
        adicionarJogador(gha, 26, "Marvin Senaya", "DEF");
        selecoes.add(gha);

        Selecao eng = criarSelecao("Inglaterra", 4, 17, "L");
        adicionarJogador(eng, 1, "Jordan Pickford", "GR");
        adicionarJogador(eng, 2, "Ezri Konsa", "DEF");
        adicionarJogador(eng, 3, "Nico Oreilly", "DEF");
        adicionarJogador(eng, 4, "Declan Rice", "MED");
        adicionarJogador(eng, 5, "John Stones", "DEF");
        adicionarJogador(eng, 6, "Marc Guehi", "DEF");
        adicionarJogador(eng, 7, "Bukayo Saka", "AV");
        adicionarJogador(eng, 8, "Elliot Anderson", "MED");
        adicionarJogador(eng, 9, "Harry Kane", "AV");
        adicionarJogador(eng, 10, "Jude Bellingham", "MED");
        adicionarJogador(eng, 11, "Marcus Rashford", "AV");
        adicionarJogador(eng, 12, "Trevoh Chalobah", "DEF");
        adicionarJogador(eng, 13, "Dean Henderson", "GR");
        adicionarJogador(eng, 14, "Jordan Henderson", "MED");
        adicionarJogador(eng, 15, "Dan Burn", "DEF");
        adicionarJogador(eng, 16, "Kobbie Mainoo", "MED");
        adicionarJogador(eng, 17, "Morgan Rogers", "MED");
        adicionarJogador(eng, 18, "Anthony Gordon", "AV");
        adicionarJogador(eng, 19, "Ollie Watkins", "AV");
        adicionarJogador(eng, 20, "Noni Madueke", "AV");
        adicionarJogador(eng, 21, "Eberechi Eze", "MED");
        adicionarJogador(eng, 22, "Ivan Toney", "AV");
        adicionarJogador(eng, 23, "James Trafford", "GR");
        adicionarJogador(eng, 24, "Reece James", "DEF");
        adicionarJogador(eng, 25, "Djed Spence", "DEF");
        adicionarJogador(eng, 26, "Jarell Quansah", "DEF");
        selecoes.add(eng);

        Selecao pan = criarSelecao("Panamá", 35, 2, "L");
        adicionarJogador(pan, 1, "Luis Mejia", "GR");
        adicionarJogador(pan, 2, "Cesar Blackman", "DEF");
        adicionarJogador(pan, 3, "Jose Cordoba", "DEF");
        adicionarJogador(pan, 4, "Fidel Escobar", "DEF");
        adicionarJogador(pan, 5, "Edgardo Farina", "DEF");
        adicionarJogador(pan, 6, "Cristian Martinez", "MED");
        adicionarJogador(pan, 7, "Jose Luis Rodriguez", "MED");
        adicionarJogador(pan, 8, "Adalberto Carrasquilla", "MED");
        adicionarJogador(pan, 9, "Tomas Rodriguez", "AV");
        adicionarJogador(pan, 10, "Ismael Diaz", "MED");
        adicionarJogador(pan, 11, "Edgar Yoel Barcenas", "MED");
        adicionarJogador(pan, 12, "Cesar Samudio", "GR");
        adicionarJogador(pan, 13, "Jiovany Ramos", "DEF");
        adicionarJogador(pan, 14, "Carlos Harvey", "DEF");
        adicionarJogador(pan, 15, "Eric Davis", "DEF");
        adicionarJogador(pan, 16, "Andres Andrade", "DEF");
        adicionarJogador(pan, 17, "Jose Fajardo", "AV");
        adicionarJogador(pan, 18, "Cecilio Waterman", "AV");
        adicionarJogador(pan, 19, "Alberto Quintero", "MED");
        adicionarJogador(pan, 20, "Anibal Godoy", "MED");
        adicionarJogador(pan, 21, "Cesar Yanis", "MED");
        adicionarJogador(pan, 22, "Orlando Mosquera", "GR");
        adicionarJogador(pan, 23, "Amir Murillo", "DEF");
        adicionarJogador(pan, 24, "Azarias Londono", "AV");
        adicionarJogador(pan, 25, "Roderick Miller", "DEF");
        adicionarJogador(pan, 26, "Jorge Gutierrez", "DEF");
        selecoes.add(pan);

        return selecoes;
    }

    private static Selecao criarSelecao(String pais, int ranking, int participacoes, String grupo) {
        Selecao selecao = new Selecao(pais, ranking, participacoes, grupo);
        selecao.setEstadia(new Estadia("", ""));
        return selecao;
    }

    private static void adicionarJogador(Selecao selecao, int numero, String nome, String posicao) {
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
}
