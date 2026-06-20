package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.model.Selecao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
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

    // Aba 2: Alocação
    private JPanel abaAlocacao;
    private JPanel painelEsquerdoGestaoArbitragem;
    private JCheckBox chkSemArbitros;
    private JList<Jogo> listaJogosAlocacao;
    private DefaultListModel<Jogo> modeloListaJogosGestaoArbitragem;

    private JPanel painelFormularioGestaoArbitragem;
    private JComboBox<Arbitro> comboPrincipal;
    private JComboBox<Arbitro> comboAssistente1;
    private JComboBox<Arbitro> comboAssistente2;
    private JComboBox<Arbitro> comboVar;
    private JButton btnConfirmarEquipa;
    private JLabel lblStatusValidacao;

    // Aba Equipas
    private JPanel abaSelecoes;
    private JSplitPane splitPrincipal;
    private JPanel pnlEsquerdo;
    private JTextField txtPesquisa;
    private JList lstSelecoes;
    private JPanel pnlInfoSelecao;
    private JLabel lblRanking;
    private JLabel lblParticipacoes;
    private JLabel lblGrupo;
    private JSplitPane splitDireita;
    private JPanel pnlCentro;
    private JTextField txtCentroTreino;
    private JTextField txtHotel;
    private JButton btnGuardarAlteracoes;
    private JPanel pnlDireita;
    private JButton btnAdicionarJogador;
    private JTable tblJogadores;

    // Aba Resultados
    private JPanel abaResultados;
    private JPanel painelResultados;
    private JList listaJogosResultados;
    private JTextField txtGolosA;
    JLabel lblSiglaA;
    private JTextField txtGolosB;
    JLabel lblSiglaB;
    JButton btnAdicionarEvento;
    JButton btnApagarEvento;
    private JTable tabelaEventos;
    JLabel lblEstatisticaA;
    JLabel lblEstatisticaB;
    private JTextField txtPosseA;
    private JTextField txtPosseB;
    private JTextField txtRematesA;
    private JTextField txtRematesB;
    private JTextField txtCantosA;
    private JTextField txtCantosB;
    private JTextField txtFaltasA;
    private JTextField txtFaltasB;
    private JButton btnSubmeter;
    private JButton btnEditar;
    private JButton btnCancelarAlteracoes;
    private JComboBox cbMOTM;

    // Aba Classificação
    private JPanel abaClassificacao;
    private JSplitPane splitClassificacaoPrincipal;
    private JPanel pnlClassificacaoEsquerda;
    private JPanel pnlClassificacaoDireita;
    private JButton btnVerFaseFinal;
    private JPanel pnlMelhoresMarcadores;
    private JPanel pnlLideresAssistencias;

    private MundialController controller;
    private boolean acabouDeAlocarComSucesso = false;
    private boolean mostrarFaseFinalClassificacao = false;
    private javax.swing.event.ListSelectionListener listenerJogos;

    public JanelaMundial(MundialController controller) {
        this.controller = controller;

        setTitle("Sistema de Gestão do Mundial 2026");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setContentPane(painelPrincipal);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);

        estilizarLayoutGlobal();

        // 1. Instanciar os painéis limpos
        PainelCalendario abaCalendarioLimpa = new PainelCalendario(controller);
        PainelGestaoArbitragem abaArbitragemLimpa = new PainelGestaoArbitragem(controller, abaCalendarioLimpa);
        PainelEquipas abaEquipasLimpa = new PainelEquipas(controller);

        PainelResultados abaResultadosLogica = new PainelResultados(
                controller,
                listaJogosResultados,
                txtGolosA,
                txtGolosB,
                txtPosseA,
                txtPosseB,
                txtRematesA,
                txtRematesB,
                txtCantosA,
                txtCantosB,
                txtFaltasA,
                txtFaltasB,
                cbMOTM,
                btnSubmeter,
                btnEditar,
                btnCancelarAlteracoes,
                tabelaEventos,
                abaCalendarioLimpa,
                btnApagarEvento
        );

        configurarAbaClassificacao();

        // 2. Acoplar ao TabbedPane do ecrã
        abasPrincipais.removeAll();
        abasPrincipais.addTab("Consultar Calendário", abaCalendarioLimpa);
        abasPrincipais.addTab("Gestão de Arbitragem", abaArbitragemLimpa);
        abasPrincipais.addTab("Equipas", abaEquipasLimpa);
        abasPrincipais.addTab("Resultados", abaResultados);
        abasPrincipais.addTab("Classificação", abaClassificacao);

        abasPrincipais.addChangeListener(e -> {
            if (abasPrincipais.getSelectedComponent() == abaClassificacao) {
                atualizarAbaClassificacao();
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println("Janela a fechar... A salvaguardar dados...");
                controller.guardarDados();
                System.exit(0);
            }
        });
    }

    private void configurarAbaClassificacao() {
        mostrarFaseFinalClassificacao = false;

        abaClassificacao.removeAll();
        abaClassificacao.setLayout(new BorderLayout());

        splitClassificacaoPrincipal.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitClassificacaoPrincipal.setResizeWeight(0.72);
        splitClassificacaoPrincipal.setLeftComponent(pnlClassificacaoEsquerda);
        splitClassificacaoPrincipal.setRightComponent(pnlClassificacaoDireita);

        abaClassificacao.add(splitClassificacaoPrincipal, BorderLayout.CENTER);

        pnlClassificacaoEsquerda.setLayout(new BorderLayout(10, 10));
        pnlClassificacaoDireita.setLayout(new BoxLayout(pnlClassificacaoDireita, BoxLayout.Y_AXIS));
        pnlClassificacaoDireita.setPreferredSize(new Dimension(280, 0));

        btnVerFaseFinal.setText("VER FASE FINAL");

        for (java.awt.event.ActionListener listener : btnVerFaseFinal.getActionListeners()) {
            btnVerFaseFinal.removeActionListener(listener);
        }

        btnVerFaseFinal.addActionListener(e -> {
            if (mostrarFaseFinalClassificacao) {
                mostrarGruposClassificacao();
            } else {
                mostrarVistaFaseFinalClassificacao();
            }
        });

        configurarPainelDireitoClassificacao();
        mostrarGruposClassificacao();
    }


    public void atualizarAbaClassificacao() {
        if (abaClassificacao == null || pnlClassificacaoEsquerda == null || pnlClassificacaoDireita == null) {
            return;
        }

        configurarPainelDireitoClassificacao();

        if (mostrarFaseFinalClassificacao) {
            mostrarVistaFaseFinalClassificacao();
        } else {
            mostrarGruposClassificacao();
        }
    }

    private void mostrarGruposClassificacao() {
        mostrarFaseFinalClassificacao = false;
        btnVerFaseFinal.setText("VER FASE FINAL");

        pnlClassificacaoEsquerda.removeAll();

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(btnVerFaseFinal);

        JPanel painelGrupos = new JPanel();
        painelGrupos.setLayout(new BoxLayout(painelGrupos, BoxLayout.Y_AXIS));

        String[] grupos = {
                "A", "B", "C", "D",
                "E", "F", "G", "H",
                "I", "J", "K", "L"
        };

        for (String grupo : grupos) {
            painelGrupos.add(criarPainelGrupo("Grupo " + grupo, grupo));
            painelGrupos.add(Box.createVerticalStrut(12));
        }

        JScrollPane scroll = new JScrollPane(painelGrupos);
        scroll.setBorder(null);

        pnlClassificacaoEsquerda.add(topo, BorderLayout.NORTH);
        pnlClassificacaoEsquerda.add(scroll, BorderLayout.CENTER);

        pnlClassificacaoEsquerda.revalidate();
        pnlClassificacaoEsquerda.repaint();
    }


    private JPanel criarPainelGrupo(String titulo, String grupo) {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));

        String[] colunas = {"Pos", "Seleção", "J", "V", "E", "D", "GM", "GS", "DG", "Pts"};

        DefaultTableModel modelo = new DefaultTableModel(criarDadosGrupo(grupo), colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(25);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        tabela.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(9).setPreferredWidth(45);

        int alturaTabela = tabela.getRowHeight() * Math.max(1, modelo.getRowCount())
                + tabela.getTableHeader().getPreferredSize().height + 4;

        JScrollPane scrollTabela = new JScrollPane(tabela);
        scrollTabela.setPreferredSize(new Dimension(0, alturaTabela));
        scrollTabela.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollTabela.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTabela.setBorder(null);

        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(scrollTabela, BorderLayout.CENTER);

        return painel;
    }



    private Object[][] criarDadosGrupo(String grupo) {
        List<EstatisticaGrupo> estatisticas = new ArrayList<>();

        for (Selecao selecao : controller.getSelecoesParticipantes()) {
            if (selecao.getGrupo() != null && selecao.getGrupo().equalsIgnoreCase(grupo)) {
                estatisticas.add(new EstatisticaGrupo(selecao.getPais()));
            }
        }

        for (Jogo jogo : controller.getCalendarioJogos()) {
            if (jogo == null || !jogo.isConcluido()) {
                continue;
            }

            if (!normalizarGrupo(jogo.getGrupo()).equalsIgnoreCase(grupo)) {
                continue;
            }

            if (jogo.getFase() != null && !jogo.getFase().equalsIgnoreCase("Primeira fase")) {
                continue;
            }

            EstatisticaGrupo equipaA = procurarEstatistica(estatisticas, jogo.getSelecaoA());
            EstatisticaGrupo equipaB = procurarEstatistica(estatisticas, jogo.getSelecaoB());

            if (equipaA == null || equipaB == null) {
                continue;
            }

            atualizarEstatisticasJogo(equipaA, equipaB, jogo.getGolosA(), jogo.getGolosB());
        }

        estatisticas.sort(
                Comparator.comparingInt(EstatisticaGrupo::getPontos).reversed()
                        .thenComparing(Comparator.comparingInt(EstatisticaGrupo::getDiferencaGolos).reversed())
                        .thenComparing(Comparator.comparingInt(EstatisticaGrupo::getGolosMarcados).reversed())
                        .thenComparing(EstatisticaGrupo::getSelecao)
        );

        List<Object[]> linhas = new ArrayList<>();
        int posicao = 1;

        for (EstatisticaGrupo e : estatisticas) {
            linhas.add(new Object[]{
                    posicao,
                    e.getSelecao(),
                    e.getJogos(),
                    e.getVitorias(),
                    e.getEmpates(),
                    e.getDerrotas(),
                    e.getGolosMarcados(),
                    e.getGolosSofridos(),
                    formatarDiferencaGolos(e.getDiferencaGolos()),
                    e.getPontos()
            });

            posicao++;
        }

        return linhas.toArray(new Object[0][]);
    }

    private EstatisticaGrupo procurarEstatistica(List<EstatisticaGrupo> estatisticas, String nomeSelecao) {
        if (nomeSelecao == null) {
            return null;
        }

        String nomeNormalizado = normalizarNomeSelecao(nomeSelecao);

        for (EstatisticaGrupo estatistica : estatisticas) {
            if (normalizarNomeSelecao(estatistica.getSelecao()).equals(nomeNormalizado)) {
                return estatistica;
            }
        }

        return null;
    }


    private String normalizarGrupo(String grupo) {
        if (grupo == null) {
            return "";
        }

        String grupoNormalizado = grupo.trim();

        if (grupoNormalizado.toLowerCase().startsWith("grupo ")) {
            grupoNormalizado = grupoNormalizado.substring(6).trim();
        }

        return grupoNormalizado;
    }

    private String normalizarNomeSelecao(String nome) {
        if (nome == null) {
            return "";
        }

        String normalizado = removerAcentos(nome).toLowerCase().trim();

        if (normalizado.equals("republica da coreia")) {
            return "coreia do sul";
        }

        if (normalizado.equals("tchequia")) {
            return "chequia";
        }

        if (normalizado.equals("eua") || normalizado.equals("usa")) {
            return "estados unidos";
        }

        return normalizado;
    }

    private String removerAcentos(String texto) {
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "");
    }

    private void atualizarEstatisticasJogo(EstatisticaGrupo equipaA, EstatisticaGrupo equipaB, int golosA, int golosB) {
        equipaA.adicionarJogo(golosA, golosB);
        equipaB.adicionarJogo(golosB, golosA);

        if (golosA > golosB) {
            equipaA.adicionarVitoria();
            equipaB.adicionarDerrota();
        } else if (golosA < golosB) {
            equipaB.adicionarVitoria();
            equipaA.adicionarDerrota();
        } else {
            equipaA.adicionarEmpate();
            equipaB.adicionarEmpate();
        }
    }

    private String formatarDiferencaGolos(int diferenca) {
        if (diferenca > 0) {
            return "+" + diferenca;
        }

        return String.valueOf(diferenca);
    }




    private static class EstatisticaGrupo {
        private final String selecao;
        private int jogos;
        private int vitorias;
        private int empates;
        private int derrotas;
        private int golosMarcados;
        private int golosSofridos;
        private int pontos;

        public EstatisticaGrupo(String selecao) {
            this.selecao = selecao;
        }

        public void adicionarJogo(int golosMarcados, int golosSofridos) {
            this.jogos++;
            this.golosMarcados += golosMarcados;
            this.golosSofridos += golosSofridos;
        }

        public void adicionarVitoria() {
            this.vitorias++;
            this.pontos += 3;
        }

        public void adicionarEmpate() {
            this.empates++;
            this.pontos += 1;
        }

        public void adicionarDerrota() {
            this.derrotas++;
        }

        public String getSelecao() {
            return selecao;
        }

        public int getJogos() {
            return jogos;
        }

        public int getVitorias() {
            return vitorias;
        }

        public int getEmpates() {
            return empates;
        }

        public int getDerrotas() {
            return derrotas;
        }

        public int getGolosMarcados() {
            return golosMarcados;
        }

        public int getGolosSofridos() {
            return golosSofridos;
        }

        public int getDiferencaGolos() {
            return golosMarcados - golosSofridos;
        }

        public int getPontos() {
            return pontos;
        }
    }

    private void mostrarVistaFaseFinalClassificacao() {
        mostrarFaseFinalClassificacao = true;
        btnVerFaseFinal.setText("VER GRUPOS");

        pnlClassificacaoEsquerda.removeAll();

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(btnVerFaseFinal);

        JPanel painelFaseFinal = new JPanel(new BorderLayout(10, 10));
        painelFaseFinal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel titulo = new JLabel("Fase Final - Mundial 2026");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel bracket = new JPanel(new GridLayout(1, 6, 12, 12));

        bracket.add(criarColunaBracket("Dezasseis-avos", "Segundas de final"));
        bracket.add(criarColunaBracket("Oitavos", "Oitavas de final"));
        bracket.add(criarColunaBracket("Quartos", "Quartas de final"));
        bracket.add(criarColunaBracket("Meias-Finais", "Semifinal"));
        bracket.add(criarColunaBracket("3.º Lugar", "Decisão do 3º lugar"));
        bracket.add(criarColunaBracket("Final", "Final"));

        JScrollPane scrollBracket = new JScrollPane(bracket);
        scrollBracket.setBorder(null);

        painelFaseFinal.add(titulo, BorderLayout.NORTH);
        painelFaseFinal.add(scrollBracket, BorderLayout.CENTER);

        pnlClassificacaoEsquerda.add(topo, BorderLayout.NORTH);
        pnlClassificacaoEsquerda.add(painelFaseFinal, BorderLayout.CENTER);

        pnlClassificacaoEsquerda.revalidate();
        pnlClassificacaoEsquerda.repaint();
    }

    private JPanel criarColunaBracket(String titulo, String fase) {
        JPanel coluna = new JPanel();
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        coluna.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        coluna.add(lblTitulo);
        coluna.add(Box.createVerticalStrut(12));

        List<Jogo> jogosDaFase = obterJogosDaFase(fase);

        if (jogosDaFase.isEmpty()) {
            JLabel vazio = new JLabel("Sem jogos");
            vazio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            vazio.setAlignmentX(Component.CENTER_ALIGNMENT);
            coluna.add(vazio);
            return coluna;
        }

        for (Jogo jogo : jogosDaFase) {
            coluna.add(criarCartaoJogoBracket(jogo));
            coluna.add(Box.createVerticalStrut(10));
        }

        return coluna;
    }

    private List<Jogo> obterJogosDaFase(String fase) {
        List<Jogo> resultado = new ArrayList<>();

        for (Jogo jogo : controller.getCalendarioJogos()) {
            if (jogo.getFase() != null && jogo.getFase().equalsIgnoreCase(fase)) {
                resultado.add(jogo);
            }
        }

        return resultado;
    }

    private JPanel criarCartaoJogoBracket(Jogo jogo) {
        JPanel cartao = new JPanel(new BorderLayout(6, 6));
        cartao.setMaximumSize(new Dimension(190, 78));
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        String textoEquipas = jogo.getSelecaoA() + " vs " + jogo.getSelecaoB();

        JLabel lblEquipas = new JLabel("<html><b>" + textoEquipas + "</b></html>");
        lblEquipas.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel lblInfo;
        if (jogo.isConcluido()) {
            lblInfo = new JLabel(jogo.getGolosA() + " - " + jogo.getGolosB(), SwingConstants.CENTER);
        } else {
            lblInfo = new JLabel(jogo.getData() + " · " + jogo.getHora(), SwingConstants.CENTER);
        }

        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));

        cartao.add(lblEquipas, BorderLayout.CENTER);
        cartao.add(lblInfo, BorderLayout.SOUTH);

        return cartao;
    }


    private void adicionarCabecalhoBracket(JPanel painel, GridBagConstraints c, int coluna, String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        c.gridx = coluna;
        c.gridy = 0;

        painel.add(label, c);
    }

    private void adicionarJogoBracket(JPanel painel, GridBagConstraints c, int coluna, int linha, String texto) {
        JButton botao = new JButton(texto);
        botao.setFocusable(false);

        c.gridx = coluna;
        c.gridy = linha;

        painel.add(botao, c);
    }


    private void configurarPainelDireitoClassificacao() {
        pnlClassificacaoDireita.removeAll();

        pnlClassificacaoDireita.add(criarPainelRanking(
                "Melhores Marcadores",
                controller.getMelhoresMarcadoresPorEventos()
        ));

        pnlClassificacaoDireita.add(Box.createVerticalStrut(12));

        pnlClassificacaoDireita.add(criarPainelRanking(
                "Líderes de Assistências",
                controller.getLideresAssistenciasPorEventos()
        ));

        pnlClassificacaoDireita.revalidate();
        pnlClassificacaoDireita.repaint();
    }



    private JPanel criarPainelRanking(String titulo, List<String[]> dados) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        painel.add(lblTitulo);
        painel.add(Box.createVerticalStrut(12));

        if (dados == null || dados.isEmpty()) {
            JLabel vazio = new JLabel("Sem dados registados.");
            vazio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            painel.add(vazio);
            return painel;
        }

        for (String[] linha : dados) {
            if (linha != null && linha.length >= 3) {
                painel.add(criarLinhaRanking(linha[0], linha[1], linha[2]));
            }
        }

        return painel;
    }


    private JPanel criarLinhaRanking(String nome, String selecao, String valor) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblNome = new JLabel(nome);
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblSelecao = new JLabel(selecao);
        lblSelecao.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblValor.setHorizontalAlignment(SwingConstants.RIGHT);

        textos.add(lblNome);
        textos.add(lblSelecao);

        linha.add(textos, BorderLayout.CENTER);
        linha.add(lblValor, BorderLayout.EAST);

        return linha;
    }

    private void estilizarLayoutGlobal() {
        JPanel painelTopoAzul = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        painelTopoAzul.setBackground(new Color(25, 118, 210));

        JLabel lblTitulo = new JLabel("Sistema de Gestão do Mundial 2026");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        painelTopoAzul.add(lblTitulo);

        painelPrincipal.setLayout(new BorderLayout());
        painelPrincipal.add(painelTopoAzul, BorderLayout.NORTH);
        painelPrincipal.add(abasPrincipais, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        MundialController controller = new MundialController();
        java.io.File ficheiro = new java.io.File("dados_mundial.dat");

        if (ficheiro.exists()) {
            controller.carregarDados();
        } else {
            controller.inicializarSelecoes();
            controller.inicializarArbitros(controller);
            controller.inicializarJogos(controller);
            controller.guardarDados();
        }

        java.awt.EventQueue.invokeLater(() -> {
            new JanelaMundial(controller).setVisible(true);
        });
    }
}
