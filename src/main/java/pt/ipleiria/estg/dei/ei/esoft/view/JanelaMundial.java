package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JanelaMundial extends JFrame {
    // --- COMPONENTES DA ÁRVORE DO .FORM (Mapeamento exato para evitar erros de Binding) ---
    private JPanel painelPrincipal;
    private JTabbedPane abasPrincipais;

    // Aba 1: Calendário
    private JPanel abaCalendario;
    private JComboBox<String> comboOrdemData;
    private JComboBox<String> comboFusoHorario;
    private JList<Jogo> listaCartoesJogos;
    private DefaultListModel<Jogo> modeloListaJogos;

    // Aba 2: Alocação (Hierarquia exata da imagem c1db63.png)
    private JPanel abaAlocacao;
    private JPanel painelEsquerdoGestaoArbitragem; // Vinculado ao form!
    private JCheckBox chkSemArbitros;
    private JList<Jogo> listaJogosAlocacao;
    private DefaultListModel<Jogo> modeloListaJogosGestaoArbitragem;

    private JPanel painelFormularioGestaoArbitragem; // Vinculado ao form!
    private JComboBox<Arbitro> comboPrincipal;
    private JComboBox<Arbitro> comboAssistente1;
    private JComboBox<Arbitro> comboAssistente2;
    private JComboBox<Arbitro> comboVar;
    private JButton btnConfirmarEquipa;
    private JLabel lblStatusValidacao;

    private MundialController controller;
    private boolean acabouDeAlocarComSucesso = false;
    private javax.swing.event.ListSelectionListener listenerJogos;

    public JanelaMundial(MundialController controller) {
        this.controller = controller;

        setTitle("Sistema de Gestão do Mundial 2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(painelPrincipal);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);

        estilizarLayoutGlobal();
        configurarAbaCalendario();
        configurarAbaGestaoArbitragem();
    }

    private void estilizarLayoutGlobal() {
        JPanel painelTopoAzul = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        painelTopoAzul.setBackground(new Color(25, 118, 210));

        JLabel lblTitulo = new JLabel("🏆 Sistema de Gestão do Mundial 2026");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        painelTopoAzul.add(lblTitulo);

        painelPrincipal.setLayout(new BorderLayout());
        painelPrincipal.add(painelTopoAzul, BorderLayout.NORTH);
        painelPrincipal.add(abasPrincipais, BorderLayout.CENTER);
    }

    private void configurarAbaCalendario() {
        abaCalendario.setLayout(new BorderLayout(15, 15));
        abaCalendario.setBorder(new EmptyBorder(15, 15, 15, 15));
        abaCalendario.setBackground(new Color(245, 245, 245));

        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        painelFiltros.setBackground(new Color(245, 245, 245));

        comboOrdemData.addItem("Crescente");
        comboOrdemData.addItem("Decrescente");

        comboFusoHorario.addItem("UTC+0 (Lisboa)");
        comboFusoHorario.addItem("UTC+1 (Central Europe)");
        comboFusoHorario.addItem("UTC-5 (EST / NY)");

        painelFiltros.add(new JLabel("Ordem de Data:"));
        painelFiltros.add(comboOrdemData);
        painelFiltros.add(new JLabel("Fuso Horário:"));
        painelFiltros.add(comboFusoHorario);

        modeloListaJogos = new DefaultListModel<>();
        listaCartoesJogos.setModel(modeloListaJogos);
        listaCartoesJogos.setBackground(new Color(245, 245, 245));
        listaCartoesJogos.setCellRenderer(new CartaoJogoRenderer(this));

        JScrollPane scrollJogos = new JScrollPane(listaCartoesJogos);
        scrollJogos.setBorder(null);

        abaCalendario.add(painelFiltros, BorderLayout.NORTH);
        abaCalendario.add(scrollJogos, BorderLayout.CENTER);

        comboOrdemData.addActionListener(e -> carregarEOrdenarCartoes());
        comboFusoHorario.addActionListener(e -> listaCartoesJogos.repaint());

        carregarEOrdenarCartoes();
    }

    private void carregarEOrdenarCartoes() {
        modeloListaJogos.clear();
        List<Jogo> listaJogos = new ArrayList<>(controller.getCalendarioJogos());

        java.time.format.DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("EEEE d MMMM yyyy")
                .toFormatter(new java.util.Locale("pt", "PT"));

        boolean crescente = "Crescente".equals(comboOrdemData.getSelectedItem());

        listaJogos.sort((j1, j2) -> {
            try {
                java.time.LocalDate d1 = java.time.LocalDate.parse(j1.getData(), formatter);
                java.time.LocalDate d2 = java.time.LocalDate.parse(j2.getData(), formatter);

                int comp = d1.compareTo(d2);
                if (comp == 0) {
                    comp = j1.getHora().compareTo(j2.getHora());
                }
                return crescente ? comp : -comp;
            } catch (Exception e) {
                return j1.getData().compareTo(j2.getData());
            }
        });

        for (Jogo j : listaJogos) {
            modeloListaJogos.addElement(j);
        }
    }

    public String obterHoraConvertida(Jogo jogo) {
        String fusoSelecionado = (String) comboFusoHorario.getSelectedItem();
        if (fusoSelecionado == null) return jogo.getHora();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime horaOriginal = LocalTime.parse(jogo.getHora(), formatter);

            if (fusoSelecionado.contains("UTC+1")) {
                return horaOriginal.plusHours(1).format(formatter);
            } else if (fusoSelecionado.contains("UTC-5")) {
                return horaOriginal.minusHours(5).format(formatter);
            }
        } catch (Exception e) {
            // Fallback seguro
        }
        return jogo.getHora();
    }

    private void configurarAbaGestaoArbitragem() {
        // Configurar a divisão geográfica limpa
        abaAlocacao.setLayout(new BorderLayout(15, 15));
        abaAlocacao.add(painelEsquerdoGestaoArbitragem, BorderLayout.WEST);
        abaAlocacao.add(painelFormularioGestaoArbitragem, BorderLayout.CENTER);

        // Dimensões e margens
        painelEsquerdoGestaoArbitragem.setPreferredSize(new Dimension(350, 0));
        // Isto dá o espaçamento correto para o formulário não ficar colado às bordas do ecrã
        painelFormularioGestaoArbitragem.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Ligar os modelos de dados
        modeloListaJogosGestaoArbitragem = new DefaultListModel<>();
        listaJogosAlocacao.setModel(modeloListaJogosGestaoArbitragem);
        listaJogosAlocacao.setCellRenderer(new JogoAlocacaoListRenderer());

        // Forçar o design do botão
        btnConfirmarEquipa.setBackground(new Color(25, 118, 210));
        btnConfirmarEquipa.setForeground(Color.WHITE);
        btnConfirmarEquipa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirmarEquipa.setFocusPainted(false);

        // Renderizador para os nomes dos árbitros
        ListCellRenderer<Object> arbitroRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Arbitro) {
                    Arbitro a = (Arbitro) value;
                    setText(a.getNome() + " (" + a.getFuncao() + " · " + a.getNacionalidade() + ")");
                }
                return this;
            }
        };

        comboPrincipal.setRenderer(arbitroRenderer);
        comboAssistente1.setRenderer(arbitroRenderer);
        comboAssistente2.setRenderer(arbitroRenderer);
        comboVar.setRenderer(arbitroRenderer);

        // Inicialização da label sem interferência de molas por código
        lblStatusValidacao.setText("<html><font color='black'>🏆 Selecione um jogo na lista lateral para iniciar.</font></html>");

        // Listeners
        chkSemArbitros.addActionListener(e -> atualizarListaJogosGestaoArbitragem());

        // TRUQUE VISUAL: Guardamos a referência do listener no atributo para podermos ligar/desligar
        this.listenerJogos = e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioJogoSelecionado();
            }
        };
        listaJogosAlocacao.addListSelectionListener(this.listenerJogos);

        btnConfirmarEquipa.addActionListener(e -> submeterEquipaArbitragem());

        atualizarListaJogosGestaoArbitragem();

    }

    private void carregarArbitrosNosCombos(Jogo jogo) {
        if (controller == null) return;

        if (jogo == null) {
            comboPrincipal.setModel(new DefaultComboBoxModel<>());
            comboAssistente1.setModel(new DefaultComboBoxModel<>());
            comboAssistente2.setModel(new DefaultComboBoxModel<>());
            comboVar.setModel(new DefaultComboBoxModel<>());
            return;
        }

        // Chamada correta para o Controller (que vamos ajustar no Passo 2)
        List<Arbitro> arbitrosDisponiveis = controller.getArbitrosDisponiveisParaData(jogo);

        DefaultComboBoxModel<Arbitro> mPrincipal = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Arbitro> mAssistente1 = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Arbitro> mAssistente2 = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Arbitro> mVar = new DefaultComboBoxModel<>();

        for (Arbitro a : arbitrosDisponiveis) {
            if ("Principal".equalsIgnoreCase(a.getFuncao())) {
                mPrincipal.addElement(a);
            } else if ("Assistente".equalsIgnoreCase(a.getFuncao())) {
                mAssistente1.addElement(a);
                mAssistente2.addElement(a);
            } else if ("VAR".equalsIgnoreCase(a.getFuncao())) {
                mVar.addElement(a);
            }
        }

        comboPrincipal.setModel(mPrincipal);
        comboAssistente1.setModel(mAssistente1);
        comboAssistente2.setModel(mAssistente2);
        comboVar.setModel(mVar);
    }

    private void atualizarListaJogosGestaoArbitragem() {
        modeloListaJogosGestaoArbitragem.clear();
        boolean filtrarSemArbitros = chkSemArbitros.isSelected();

        for (Jogo j : controller.getCalendarioJogos()) {
            if (filtrarSemArbitros && j.getEquipaArbitragem() != null && !j.getEquipaArbitragem().isEmpty()) {
                continue;
            }
            modeloListaJogosGestaoArbitragem.addElement(j);
        }
    }

    private void preencherFormularioJogoSelecionado() {
        Jogo jogo = listaJogosAlocacao.getSelectedValue();

        if (jogo == null) {
            lblStatusValidacao.setText("<html>🏆 Selecione um jogo na lista lateral para iniciar.</html>");
            return;
        }

        // Carrega SEMPRE os árbitros disponíveis para a data deste jogo específico
        carregarArbitrosNosCombos(jogo);

        // Se o jogo já tiver árbitros alocados, seleciona-os visualmente nos combos
        if (jogo.getEquipaArbitragem() != null && !jogo.getEquipaArbitragem().isEmpty()) {
            lblStatusValidacao.setText("<html><font color='#2e7d32'><b>✓ Equipa Válida</b><br>Os árbitros estão escalados corretamente para este jogo.</font></html>");
            if (jogo.getEquipaArbitragem().size() >= 4) {
                comboPrincipal.setSelectedItem(jogo.getEquipaArbitragem().get(0));
                comboAssistente1.setSelectedItem(jogo.getEquipaArbitragem().get(1));
                comboAssistente2.setSelectedItem(jogo.getEquipaArbitragem().get(2));
                comboVar.setSelectedItem(jogo.getEquipaArbitragem().get(3));
            }
        } else {
            // Se não tiver equipa, deixa os combos em branco para o utilizador poder escolher
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>⚠️ Sem Árbitros Atribuídos</b><br>Por favor, monte a equipa de arbitragem para este desafio.</font></html>");
            comboPrincipal.setSelectedIndex(-1);
            comboAssistente1.setSelectedIndex(-1);
            comboAssistente2.setSelectedIndex(-1);
            comboVar.setSelectedIndex(-1);
        }
    }

    private void submeterEquipaArbitragem() {
        Jogo jogoSelecionado = listaJogosAlocacao.getSelectedValue();
        if (jogoSelecionado == null) return;

        Arbitro principal = (Arbitro) comboPrincipal.getSelectedItem();
        Arbitro assistente1 = (Arbitro) comboAssistente1.getSelectedItem();
        Arbitro assistente2 = (Arbitro) comboAssistente2.getSelectedItem();
        Arbitro var = (Arbitro) comboVar.getSelectedItem();

        // 1. Validar preenchimento
        if (principal == null || assistente1 == null || assistente2 == null || var == null) {
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>Erro de Seleção:</b><br>Todos os quatro campos devem ser preenchidos.</font></html>");
            return;
        }

        // 2. Validar duplicados
        if (principal.getNome().equals(assistente1.getNome()) || principal.getNome().equals(assistente2.getNome()) || principal.getNome().equals(var.getNome()) ||
                assistente1.getNome().equals(assistente2.getNome()) || assistente1.getNome().equals(var.getNome()) ||
                assistente2.getNome().equals(var.getNome())) {
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>Erro de Duplicação:</b><br>O mesmo árbitro não pode ocupar duas funções.</font></html>");
            return;
        }

        try {
            List<Arbitro> equipa = new ArrayList<>();
            equipa.add(principal);
            equipa.add(assistente1);
            equipa.add(assistente2);
            equipa.add(var);

            // Envia para o controlador salvar no modelo de dados
            controller.alocarEquipaArbitragem(jogoSelecionado, equipa);

            // Se a checkbox "Sem Árbitros" estiver ativa, como o jogo agora TEM árbitros,
            // ele deve sumir da lista. Só nesse caso é que reconstruímos a lista toda!
            if (chkSemArbitros.isSelected()) {
                atualizarListaJogosGestaoArbitragem();
                comboPrincipal.setSelectedIndex(-1);
                comboAssistente1.setSelectedIndex(-1);
                comboAssistente2.setSelectedIndex(-1);
                comboVar.setSelectedIndex(-1);
            } else {
                // Se a checkbox NÃO está ativa, o jogo continua na lista.
                // Apenas forçamos a JList a redesenhar a linha para atualizar a bola/cor do renderizador
                listaJogosAlocacao.repaint();
            }

            // Atualiza a tabela/cartões da outra aba se necessário
            carregarEOrdenarCartoes();

            // Mostra a mensagem de sucesso sem que nada se tenha movido ou alterado sozinho!
            lblStatusValidacao.setText("<html><font color='#2e7d32'><b>✓ Equipa Válida:</b><br>Todos os árbitros foram atribuídos com sucesso!</font></html>");

        } catch (Exception ex) {
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>Erro de Validação:</b><br>" + ex.getMessage() + "</font></html>");
        }
    }

    // --- RENDERIZADORES DAS LISTAS (CLASSES INTERNAS) ---
    private static class JogoAlocacaoListRenderer extends JPanel implements ListCellRenderer<Jogo> {
        private JLabel lblInfo;
        private JLabel lblVisto;

        public JogoAlocacaoListRenderer() {
            setLayout(new BorderLayout(5, 5));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setBackground(Color.WHITE);

            lblInfo = new JLabel();
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            lblVisto = new JLabel("✓");
            lblVisto.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblVisto.setForeground(new Color(46, 125, 50));

            add(lblInfo, BorderLayout.CENTER);
            add(lblVisto, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Jogo> list, Jogo jogo, int index, boolean isSelected, boolean cellHasFocus) {
            if (jogo != null) {
                // CORREÇÃO AQUI: Substituímos a fase pela data (jogo.getData()) na linha de baixo, a cinzento
                lblInfo.setText("<html><b>" + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() +
                        "</b><br><font color='gray'>" + jogo.getHora() + " · " + jogo.getData() + "</font></html>");

                // Mostra ou esconde o visto verde se o jogo já tiver árbitros alocados
                lblVisto.setVisible(jogo.getEquipaArbitragem() != null && !jogo.getEquipaArbitragem().isEmpty());
            }

            // Trata do comportamento visual da seleção da linha (fundo azul ou branco)
            if (isSelected) {
                setBackground(new Color(232, 240, 254));
            } else {
                setBackground(Color.WHITE);
            }

            return this;
        }
    }

    private static class CartaoJogoRenderer extends JPanel implements ListCellRenderer<Jogo> {
        private JLabel lblDataMeta;
        private JLabel lblEquipas;
        private JLabel lblDetalhes;
        private JLabel lblEstado;
        private JanelaMundial janela;

        public CartaoJogoRenderer(JanelaMundial janela) {
            this.janela = janela;
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(8, 0, 8, 0),
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true)
            ));
            setBackground(Color.WHITE);

            JPanel painelInterno = new JPanel(new GridLayout(3, 1, 5, 5));
            painelInterno.setBackground(Color.WHITE);
            painelInterno.setBorder(new EmptyBorder(12, 16, 12, 16));

            lblDataMeta = new JLabel();
            lblDataMeta.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblDataMeta.setForeground(Color.GRAY);

            lblEquipas = new JLabel();
            lblEquipas.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblEquipas.setHorizontalAlignment(SwingConstants.CENTER);

            lblDetalhes = new JLabel();
            lblDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblDetalhes.setForeground(Color.LIGHT_GRAY);
            lblDetalhes.setHorizontalAlignment(SwingConstants.CENTER);

            painelInterno.add(lblDataMeta);
            painelInterno.add(lblEquipas);
            painelInterno.add(lblDetalhes);

            lblEstado = new JLabel(" Terminado ");
            lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblEstado.setForeground(Color.WHITE);
            lblEstado.setBackground(new Color(46, 125, 50));
            lblEstado.setOpaque(true);

            JPanel painelDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            painelDireito.setBackground(Color.WHITE);
            painelDireito.add(lblEstado);

            add(painelInterno, BorderLayout.CENTER);
            add(painelDireito, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Jogo> list, Jogo jogo, int index, boolean isSelected, boolean cellHasFocus) {
            lblDataMeta.setText(jogo.getData().toUpperCase());
            String horaExibicao = janela.obterHoraConvertida(jogo);

            String textoHtml = "<html>" +
                    "<font color='#555555'>" + jogo.getSelecaoA() + "</font> " +
                    "<b><font color='#111111'>" + jogo.getSiglaA() + "</font></b>" +
                    "&nbsp;&nbsp;&nbsp;<font color='#000000'><b>" + horaExibicao + "</b></font>&nbsp;&nbsp;&nbsp;" +
                    "<b><font color='#111111'>" + jogo.getSiglaB() + "</font></b> " +
                    "<font color='#555555'>" + jogo.getSelecaoB() + "</font>" +
                    "</html>";
            lblEquipas.setText(textoHtml);
            lblDetalhes.setText(jogo.getFase() + " · " + jogo.getGrupo() + " · " + jogo.getEstadio() + " (" + jogo.getCidade() + ")");

            boolean jogoTerminado = false;
            try {
                java.time.format.DateTimeFormatter dateFormatter = new java.time.format.DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("EEEE d MMMM yyyy")
                        .toFormatter(new java.util.Locale("pt", "PT"));

                java.time.LocalDate dataJogo = java.time.LocalDate.parse(jogo.getData(), dateFormatter);
                java.time.LocalDate hoje = java.time.LocalDate.now();

                if (dataJogo.isBefore(hoje)) {
                    jogoTerminado = true;
                } else if (dataJogo.isEqual(hoje)) {
                    java.time.LocalTime horaJogo = java.time.LocalTime.parse(jogo.getHora(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                    java.time.LocalTime agora = java.time.LocalTime.now();
                    if (agora.isAfter(horaJogo.plusHours(2))) {
                        jogoTerminado = true;
                    }
                }
            } catch (Exception e) {
                jogoTerminado = jogo.isConcluido();
            }

            lblEstado.setVisible(jogoTerminado);

            if (isSelected) {
                setBackground(new Color(232, 240, 254));
                ((JPanel)getComponent(0)).setBackground(new Color(232, 240, 254));
                getComponent(1).setBackground(new Color(232, 240, 254));
            } else {
                setBackground(Color.WHITE);
                ((JPanel)getComponent(0)).setBackground(Color.WHITE);
                getComponent(1).setBackground(Color.WHITE);
            }
            return this;
        }
    }

    public static void main(String[] args) {
        MundialController controller = new MundialController();

        // --- CALENDÁRIO COMPLETO ORIGINAL DA FIFA ---
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

        // --- ÁRBITROS BASE DO SISTEMA ---
        popularArbitrosOficiais(controller);

        SwingUtilities.invokeLater(() -> {
            new JanelaMundial(controller).setVisible(true);
        });
    }

    private static void popularArbitrosOficiais(MundialController controller) {
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
}