package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Classificacao;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.model.Selecao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PainelClassificacao extends JPanel {

    private final MundialController controller;
    private JPanel painelConteudo;
    private JPanel painelDireitoRankings;
    private JButton btnAlternarVista;
    private boolean mostrarFaseFinal;

    public PainelClassificacao(MundialController controller) {
        this.controller = controller;
        this.mostrarFaseFinal = false;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        criarInterface();
        atualizarInterfaceDinamica();
    }

    private void criarInterface() {
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAlternarVista = new JButton("VER FASE FINAL");
        painelTopo.add(btnAlternarVista);

        painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));

        JScrollPane scrollConteudo = new JScrollPane(painelConteudo);
        scrollConteudo.setBorder(null);

        painelDireitoRankings = new JPanel();
        painelDireitoRankings.setLayout(new BoxLayout(painelDireitoRankings, BoxLayout.Y_AXIS));
        painelDireitoRankings.setPreferredSize(new Dimension(280, 0));

        add(painelTopo, BorderLayout.NORTH);
        add(scrollConteudo, BorderLayout.CENTER);
        add(painelDireitoRankings, BorderLayout.EAST);

        btnAlternarVista.addActionListener(e -> alternarVista());
    }

    public void atualizarInterfaceDinamica() {
        configurarPainelDireitoRankings();

        if (mostrarFaseFinal) {
            mostrarFaseFinal();
        } else {
            mostrarGrupos();
        }
    }

    private void alternarVista() {
        mostrarFaseFinal = !mostrarFaseFinal;
        atualizarInterfaceDinamica();
    }

    private void mostrarGrupos() {
        btnAlternarVista.setText("VER FASE FINAL");
        painelConteudo.removeAll();

        String[] grupos = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};

        for (String g : grupos) {
            Object[][] dadosDoGrupo = calcularDadosGrupoReal(g);
            painelConteudo.add(criarPainelGrupo("Grupo " + g, dadosDoGrupo));
            painelConteudo.add(Box.createVerticalStrut(12));
        }

        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private JPanel criarPainelGrupo(String titulo, Object[][] dados) {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));

        String[] colunas = {"Pos", "Seleção", "J", "V", "E", "D", "GM", "GS", "DG", "Pts"};

        DefaultTableModel modelo = new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(25);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        tabela.getColumnModel().getColumn(0).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(9).setPreferredWidth(40);

        int alturaTabela = tabela.getRowHeight() * 4 + tabela.getTableHeader().getPreferredSize().height + 4;
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setPreferredSize(new Dimension(0, alturaTabela));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);

        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    private Object[][] calcularDadosGrupoReal(String grupo) {
        List<Classificacao> linhas = new ArrayList<>();

        // 1. Filtrar as seleções pertencentes a este grupo
        for (Selecao s : controller.getSelecoes()) {
            if (s.getGrupo() != null && s.getGrupo().trim().equalsIgnoreCase(grupo)) {
                linhas.add(new Classificacao(s));
            }
        }

        // 2. Computar o estado dos jogos reais usando os métodos do teu modelo oficial
        for (Jogo j : controller.getCalendarioJogos()) {
            if (j != null && j.isConcluido() && j.getGrupo() != null && j.getGrupo().trim().equalsIgnoreCase(grupo)) {

                Classificacao equipaA = obterLinhaModeloReal(linhas, j.getSelecaoA());
                Classificacao equipaB = obterLinhaModeloReal(linhas, j.getSelecaoB());

                if (equipaA != null && equipaB != null) {
                    int gA = j.getGolosA();
                    int gB = j.getGolosB();

                    if (gA > gB) {
                        equipaA.registarVitoria(gA, gB);
                        equipaB.registarDerrota(gB, gA);
                    } else if (gA < gB) {
                        equipaB.registarVitoria(gB, gA);
                        equipaA.registarDerrota(gA, gB);
                    } else {
                        equipaA.registarEmpate(gA, gB);
                        equipaB.registarEmpate(gB, gA);
                    }
                }
            }
        }

        // 3. Ordenar pelos critérios oficiais da FIFA (Pts -> DG -> GM -> Nome do País)
        linhas.sort(Comparator.comparingInt(Classificacao::getPontos).reversed()
                .thenComparing(Comparator.comparingInt(Classificacao::getDiferencaGolos).reversed())
                .thenComparing(Comparator.comparingInt(Classificacao::getGolosMarcados).reversed())
                .thenComparing(c -> c.getSelecao().getPais()));

        // 4. Montar a matriz bidimensional de objetos para a JTable
        Object[][] matriz = new Object[linhas.size()][10];
        for (int i = 0; i < linhas.size(); i++) {
            Classificacao c = linhas.get(i);
            int dg = c.getDiferencaGolos();
            matriz[i] = new Object[]{
                    (i + 1),
                    c.getSelecao().getPais(),
                    c.getJogos(),
                    c.getVitorias(),
                    c.getEmpates(),
                    c.getDerrotas(),
                    c.getGolosMarcados(),
                    c.getGolosSofridos(),
                    (dg > 0 ? "+" + dg : dg),
                    c.getPontos()
            };
        }
        return matriz;
    }

    private Classificacao obterLinhaModeloReal(List<Classificacao> lista, String nomePais) {
        for (Classificacao c : lista) {
            if (c.getSelecao().getPais().trim().equalsIgnoreCase(nomePais.trim())) return c;
        }
        return null;
    }

    private void mostrarFaseFinal() {
        btnAlternarVista.setText("VER GRUPOS");
        painelConteudo.removeAll();

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel titulo = new JLabel("Fase Final - Árvore do Torneio");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel bracket = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 14, 8, 14);
        c.fill = GridBagConstraints.HORIZONTAL;

        adicionarCabecalhoBracket(bracket, c, 0, "Oitavos");
        adicionarCabecalhoBracket(bracket, c, 1, "Quartos");
        adicionarCabecalhoBracket(bracket, c, 2, "Meias-Finais");
        adicionarCabecalhoBracket(bracket, c, 3, "Final");

        injetarJogosFaseFinalBracket(bracket, c);

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(bracket, BorderLayout.CENTER);
        painelConteudo.add(painel);

        painelConteudo.revalidate();
        painelConteudo.repaint();
    }

    private void injetarJogosFaseFinalBracket(JPanel bracket, GridBagConstraints c) {
        adicionarJogoBracket(bracket, c, 0, 1, obterTextoJogoFaseReal("Oitavas de final", 0));
        adicionarJogoBracket(bracket, c, 0, 2, obterTextoJogoFaseReal("Oitavas de final", 1));
        adicionarJogoBracket(bracket, c, 0, 3, obterTextoJogoFaseReal("Oitavas de final", 2));
        adicionarJogoBracket(bracket, c, 0, 4, obterTextoJogoFaseReal("Oitavas de final", 3));

        adicionarJogoBracket(bracket, c, 1, 2, obterTextoJogoFaseReal("Quartas de final", 0));
        adicionarJogoBracket(bracket, c, 1, 4, obterTextoJogoFaseReal("Quartas de final", 1));

        adicionarJogoBracket(bracket, c, 2, 3, obterTextoJogoFaseReal("Semifinal", 0));

        JButton finalBtn = new JButton(obterTextoJogoFaseReal("Final", 0));
        finalBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridx = 3;
        c.gridy = 3;
        bracket.add(finalBtn, c);
    }

    private String obterTextoJogoFaseReal(String fase, int indice) {
        int conta = 0;
        for (Jogo j : controller.getCalendarioJogos()) {
            if (j.getFase() != null && j.getFase().trim().equalsIgnoreCase(fase)) {
                if (conta == indice) {
                    if (j.isConcluido()) {
                        return j.getSelecaoA() + " (" + j.getGolosA() + ") vs (" + j.getGolosB() + ") " + j.getSelecaoB();
                    }
                    return j.getSelecaoA() + " vs " + j.getSelecaoB();
                }
                conta++;
            }
        }
        return "TBD (A definir)";
    }

    private void adicionarCabecalhoBracket(JPanel painel, GridBagConstraints c, int coluna, String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

    private void configurarPainelDireitoRankings() {
        painelDireitoRankings.removeAll();

        // 1. Bloco de Marcadores Reais
        JPanel pnlMarcadores = new JPanel();
        pnlMarcadores.setLayout(new BoxLayout(pnlMarcadores, BoxLayout.Y_AXIS));
        pnlMarcadores.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        JLabel lblT1 = new JLabel("Melhores Marcadores");
        lblT1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlMarcadores.add(lblT1);
        pnlMarcadores.add(Box.createVerticalStrut(10));

        List<String[]> marcadoresReais = controller.getMelhoresMarcadoresPorEventos();
        if (marcadoresReais == null || marcadoresReais.isEmpty()) {
            pnlMarcadores.add(new JLabel("Sem golos registados."));
        } else {
            int posicao = 1;
            for (String[] m : marcadoresReais) {
                pnlMarcadores.add(criarLinhaRanking(posicao + ". " + m[0], m[1], m[2]));
                posicao++;
            }
        }

        // 2. Bloco de Assistências Reais
        JPanel pnlAssistencias = new JPanel();
        pnlAssistencias.setLayout(new BoxLayout(pnlAssistencias, BoxLayout.Y_AXIS));
        pnlAssistencias.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        JLabel lblT2 = new JLabel("Líderes de Assistências");
        lblT2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlAssistencias.add(lblT2);
        pnlAssistencias.add(Box.createVerticalStrut(10));

        List<String[]> assistenciasReais = controller.getLideresAssistenciasPorEventos();
        if (assistenciasReais == null || assistenciasReais.isEmpty()) {
            pnlAssistencias.add(new JLabel("Sem assistências."));
        } else {
            int posicao = 1;
            for (String[] a : assistenciasReais) {
                pnlAssistencias.add(criarLinhaRanking(posicao + ". " + a[0], a[1], a[2]));
                posicao++;
            }
        }

        painelDireitoRankings.add(pnlMarcadores);
        painelDireitoRankings.add(Box.createVerticalStrut(12));
        painelDireitoRankings.add(pnlAssistencias);

        painelDireitoRankings.revalidate();
        painelDireitoRankings.repaint();
    }

    private JPanel criarLinhaRanking(String nome, String selecao, String valor) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

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
}
