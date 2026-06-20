package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class PainelClassificacao extends JPanel {

    private final MundialController controller;

    private JPanel painelConteudo;
    private JButton btnAlternarVista;
    private boolean mostrarFaseFinal;

    public PainelClassificacao(MundialController controller) {
        this.controller = controller;
        this.mostrarFaseFinal = false;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        criarInterface();
        mostrarGrupos();
    }

    private void criarInterface() {
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAlternarVista = new JButton("VER FASE FINAL");
        painelTopo.add(btnAlternarVista);

        painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));

        JScrollPane scrollConteudo = new JScrollPane(painelConteudo);
        scrollConteudo.setBorder(null);

        JPanel painelDireito = criarPainelRankings();

        add(painelTopo, BorderLayout.NORTH);
        add(scrollConteudo, BorderLayout.CENTER);
        add(painelDireito, BorderLayout.EAST);

        btnAlternarVista.addActionListener(e -> alternarVista());
    }

    private void alternarVista() {
        if (mostrarFaseFinal) {
            mostrarGrupos();
        } else {
            mostrarFaseFinal();
        }
    }

    private void mostrarGrupos() {
        mostrarFaseFinal = false;
        btnAlternarVista.setText("VER FASE FINAL");

        painelConteudo.removeAll();

        painelConteudo.add(criarPainelGrupo(
                "Grupo A",
                new Object[][]{
                        {1, "PT  Portugal", 3, 2, 1, 0, 7, 2, "+5", 7},
                        {2, "GH  Gana", 3, 1, 2, 0, 5, 3, "+2", 5},
                        {3, "UY  Uruguai", 3, 1, 1, 1, 4, 4, "0", 4},
                        {4, "KR  Coreia do Sul", 3, 0, 0, 3, 2, 9, "-7", 0}
                }
        ));

        painelConteudo.add(Box.createVerticalStrut(12));

        painelConteudo.add(criarPainelGrupo(
                "Grupo B",
                new Object[][]{
                        {1, "BR  Brasil", 3, 3, 0, 0, 8, 1, "+7", 9},
                        {2, "CH  Suíça", 3, 2, 0, 1, 5, 4, "+1", 6},
                        {3, "CM  Camarões", 3, 1, 0, 2, 4, 5, "-1", 3},
                        {4, "RS  Sérvia", 3, 0, 0, 3, 2, 9, "-7", 0}
                }
        ));

        painelConteudo.add(Box.createVerticalStrut(12));

        painelConteudo.add(criarPainelGrupo(
                "Grupo C",
                new Object[][]{
                        {1, "FR  França", 3, 2, 0, 1, 6, 3, "+3", 6},
                        {2, "AR  Argentina", 3, 2, 0, 1, 5, 3, "+2", 6},
                        {3, "ES  Espanha", 3, 1, 1, 1, 4, 4, "0", 4},
                        {4, "MA  Marrocos", 3, 0, 1, 2, 2, 7, "-5", 1}
                }
        ));

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
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String[] colunas = {"Pos", "Seleção", "J", "V", "E", "D", "GM", "GS", "DG", "Pts"};

        DefaultTableModel modelo = new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);
        tabela.setRowHeight(26);
        tabela.getTableHeader().setReorderingAllowed(false);

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

        painel.add(lblTitulo, BorderLayout.NORTH);
        painel.add(tabela, BorderLayout.CENTER);

        return painel;
    }

    private void mostrarFaseFinal() {
        mostrarFaseFinal = true;
        btnAlternarVista.setText("VER GRUPOS");

        painelConteudo.removeAll();

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel titulo = new JLabel("Fase Final - Eliminatórias");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel bracket = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 14, 8, 14);
        c.fill = GridBagConstraints.HORIZONTAL;

        adicionarCabecalhoBracket(bracket, c, 0, "Oitavos");
        adicionarCabecalhoBracket(bracket, c, 1, "Quartos");
        adicionarCabecalhoBracket(bracket, c, 2, "Meias-Finais");
        adicionarCabecalhoBracket(bracket, c, 3, "Final");

        adicionarJogoBracket(bracket, c, 0, 1, "Portugal vs Suíça");
        adicionarJogoBracket(bracket, c, 0, 2, "Brasil vs Gana");
        adicionarJogoBracket(bracket, c, 0, 3, "França vs Argentina");
        adicionarJogoBracket(bracket, c, 0, 4, "Espanha vs Alemanha");

        adicionarJogoBracket(bracket, c, 1, 2, "Portugal vs Brasil");
        adicionarJogoBracket(bracket, c, 1, 4, "França vs Espanha");

        adicionarJogoBracket(bracket, c, 2, 3, "Portugal vs França");

        JButton finalBtn = new JButton("🏆 TBD");
        finalBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.gridx = 3;
        c.gridy = 3;
        bracket.add(finalBtn, c);

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(bracket, BorderLayout.CENTER);

        painelConteudo.add(painel);

        painelConteudo.revalidate();
        painelConteudo.repaint();
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

    private JPanel criarPainelRankings() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setPreferredSize(new Dimension(260, 0));

        painel.add(criarPainelMelhoresMarcadores());
        painel.add(Box.createVerticalStrut(12));
        painel.add(criarPainelAssistencias());

        return painel;
    }

    private JPanel criarPainelMelhoresMarcadores() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel titulo = new JLabel("Melhores Marcadores");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(12));

        painel.add(criarLinhaRanking("1. Cristiano Ronaldo", "Portugal", "5"));
        painel.add(criarLinhaRanking("2. Neymar Jr.", "Brasil", "4"));
        painel.add(criarLinhaRanking("3. Kylian Mbappé", "França", "4"));
        painel.add(criarLinhaRanking("4. Lionel Messi", "Argentina", "3"));
        painel.add(criarLinhaRanking("5. Harry Kane", "Inglaterra", "3"));

        return painel;
    }

    private JPanel criarPainelAssistencias() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel titulo = new JLabel("Líderes de Assistências");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(12));

        painel.add(criarLinhaRanking("1. Bruno Fernandes", "Portugal", "4"));
        painel.add(criarLinhaRanking("2. Kevin De Bruyne", "Bélgica", "3"));
        painel.add(criarLinhaRanking("3. Lionel Messi", "Argentina", "3"));
        painel.add(criarLinhaRanking("4. Luka Modrić", "Croácia", "2"));
        painel.add(criarLinhaRanking("5. Antoine Griezmann", "França", "2"));

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
}
