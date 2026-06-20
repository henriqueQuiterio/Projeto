package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Estadia;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.model.Selecao;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PainelEquipas extends JPanel {

    private static final int LIMITE_JOGADORES = 26;

    private final MundialController controller;
    private final List<Selecao> selecoes;

    private Selecao selecaoAtual;

    private JTextField txtPesquisa;
    private JList<Selecao> lstSelecoes;
    private DefaultListModel<Selecao> modeloListaSelecoes;

    private JLabel lblRanking;
    private JLabel lblParticipacoes;
    private JLabel lblGrupo;

    private JTextField txtCentroTreino;
    private JTextField txtHotel;
    private JButton btnGuardarAlteracoes;

    private JTable tblJogadores;
    private DefaultTableModel modeloTabelaJogadores;
    private JButton btnAdicionarJogador;
    private JButton btnGuardarPlantel;

    public PainelEquipas() {
        this(new MundialController());
    }

    public PainelEquipas(MundialController controller) {
        this.controller = controller;
        this.selecoes = new ArrayList<>(controller.getSelecoesParticipantes());

        setLayout(new BorderLayout());

        criarInterface();
        configurarEventos();
        carregarListaSelecoes(selecoes);
    }

    private void criarInterface() {
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane splitDireita = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel pnlEsquerdo = criarPainelEsquerdo();
        JPanel pnlCentro = criarPainelCentro();
        JPanel pnlDireito = criarPainelDireito();

        splitDireita.setLeftComponent(pnlCentro);
        splitDireita.setRightComponent(pnlDireito);
        splitDireita.setResizeWeight(0.40);

        splitPrincipal.setLeftComponent(pnlEsquerdo);
        splitPrincipal.setRightComponent(splitDireita);
        splitPrincipal.setResizeWeight(0.25);

        add(splitPrincipal, BorderLayout.CENTER);
    }

    private JPanel criarPainelEsquerdo() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Seleções"));

        txtPesquisa = new JTextField();

        modeloListaSelecoes = new DefaultListModel<>();
        lstSelecoes = new JList<>(modeloListaSelecoes);
        lstSelecoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        lstSelecoes.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();

            if (value != null) {
                label.setText(value.getPais());
            }

            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            return label;
        });

        JPanel painelInfo = new JPanel(new GridLayout(3, 1, 5, 5));
        painelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblRanking = new JLabel("#-");
        lblParticipacoes = new JLabel("- participações");
        lblGrupo = new JLabel("Grupo: -");

        painelInfo.add(lblRanking);
        painelInfo.add(lblParticipacoes);
        painelInfo.add(lblGrupo);

        painel.add(txtPesquisa, BorderLayout.NORTH);
        painel.add(new JScrollPane(lstSelecoes), BorderLayout.CENTER);
        painel.add(painelInfo, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarPainelCentro() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Gestão de Logística"));

        JPanel campos = new JPanel(new GridLayout(5, 1, 8, 8));

        JLabel lblCentroTreino = new JLabel("Centro de Treino");
        txtCentroTreino = new JTextField();

        JLabel lblHotel = new JLabel("Hotel");
        txtHotel = new JTextField();

        btnGuardarAlteracoes = new JButton("Guardar Alterações");

        campos.add(lblCentroTreino);
        campos.add(txtCentroTreino);
        campos.add(lblHotel);
        campos.add(txtHotel);
        campos.add(btnGuardarAlteracoes);

        painel.add(campos, BorderLayout.NORTH);

        return painel;
    }

    private JPanel criarPainelDireito() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Gestão de Plantel"));

        btnAdicionarJogador = new JButton("+ ADICIONAR");
        btnGuardarPlantel = new JButton("Guardar Plantel");

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topo.add(btnAdicionarJogador);
        topo.add(btnGuardarPlantel);

        modeloTabelaJogadores = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Nº", "Nome do Jogador", "Posição", ""}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        tblJogadores = new JTable(modeloTabelaJogadores);
        tblJogadores.setRowHeight(28);

        JComboBox<String> comboPosicoes = new JComboBox<>(new String[]{"GR", "DEF", "MED", "AV"});
        tblJogadores.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboPosicoes));

        tblJogadores.getColumnModel().getColumn(3).setCellRenderer(new BotaoEliminarRenderer());
        tblJogadores.getColumnModel().getColumn(3).setCellEditor(new BotaoEliminarEditor());

        tblJogadores.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblJogadores.getColumnModel().getColumn(1).setPreferredWidth(220);
        tblJogadores.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblJogadores.getColumnModel().getColumn(3).setPreferredWidth(60);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(tblJogadores), BorderLayout.CENTER);

        return painel;
    }

    private void configurarEventos() {
        lstSelecoes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Selecao selecionada = lstSelecoes.getSelectedValue();

                if (selecionada != null) {
                    carregarSelecao(selecionada);
                }
            }
        });

        txtPesquisa.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                pesquisarSelecoes();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                pesquisarSelecoes();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                pesquisarSelecoes();
            }
        });

        btnGuardarAlteracoes.addActionListener(e -> guardarLogistica());

        btnAdicionarJogador.addActionListener(e -> adicionarJogador());

        btnGuardarPlantel.addActionListener(e -> guardarPlantel());
    }

    private void carregarListaSelecoes(List<Selecao> lista) {
        modeloListaSelecoes.clear();

        for (Selecao selecao : lista) {
            modeloListaSelecoes.addElement(selecao);
        }

        if (!modeloListaSelecoes.isEmpty()) {
            lstSelecoes.setSelectedIndex(0);
        }
    }

    private void pesquisarSelecoes() {
        String termo = txtPesquisa.getText();

        if (termo == null) {
            termo = "";
        }

        List<Selecao> resultado = controller.pesquisarSelecoes(selecoes, termo);
        carregarListaSelecoes(resultado);
    }

    private void carregarSelecao(Selecao selecao) {
        this.selecaoAtual = selecao;

        lblRanking.setText("#" + selecao.getRanking());
        lblParticipacoes.setText(selecao.getParticipacoes() + " participações");
        lblGrupo.setText("Grupo: " + selecao.getGrupo());

        Estadia estadia = selecao.getEstadia();

        if (estadia != null) {
            txtCentroTreino.setText(estadia.getCentroTreino());
            txtHotel.setText(estadia.getHotel());
        } else {
            txtCentroTreino.setText("");
            txtHotel.setText("");
        }

        carregarJogadores(selecao);
    }

    private void carregarJogadores(Selecao selecao) {
        modeloTabelaJogadores.setRowCount(0);

        for (Jogador jogador : selecao.getJogadores()) {
            modeloTabelaJogadores.addRow(new Object[]{
                    jogador.getNumero(),
                    jogador.getNome(),
                    jogador.getPosicao(),
                    "🗑"
            });
        }
    }

    private void guardarLogistica() {
        if (selecaoAtual == null) {
            JOptionPane.showMessageDialog(this, "Seleciona uma seleção primeiro.");
            return;
        }

        try {
            controller.atualizarEstadia(
                    selecaoAtual,
                    txtCentroTreino.getText(),
                    txtHotel.getText()
            );

            JOptionPane.showMessageDialog(this, "Alterações guardadas com sucesso.");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void adicionarJogador() {
        if (selecaoAtual == null) {
            JOptionPane.showMessageDialog(this, "Seleciona uma seleção primeiro.");
            return;
        }

        if (modeloTabelaJogadores.getRowCount() >= LIMITE_JOGADORES) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não é possível convocar mais de " + LIMITE_JOGADORES + " jogadores.",
                    "Limite atingido",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        modeloTabelaJogadores.addRow(new Object[]{
                calcularProximoNumero(),
                "Novo Jogador",
                "AV",
                "🗑"
        });
    }

    private void guardarPlantel() {
        if (selecaoAtual == null) {
            JOptionPane.showMessageDialog(this, "Seleciona uma seleção primeiro.");
            return;
        }

        if (tblJogadores.isEditing()) {
            tblJogadores.getCellEditor().stopCellEditing();
        }

        if (modeloTabelaJogadores.getRowCount() > LIMITE_JOGADORES) {
            JOptionPane.showMessageDialog(
                    this,
                    "Uma seleção só pode ter no máximo " + LIMITE_JOGADORES + " jogadores convocados.",
                    "Plantel inválido",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            List<Jogador> novosJogadores = new ArrayList<>();
            Set<Integer> numerosUsados = new HashSet<>();

            for (int i = 0; i < modeloTabelaJogadores.getRowCount(); i++) {
                Object valorNumero = modeloTabelaJogadores.getValueAt(i, 0);
                Object valorNome = modeloTabelaJogadores.getValueAt(i, 1);
                Object valorPosicao = modeloTabelaJogadores.getValueAt(i, 2);

                if (valorNome == null || valorNome.toString().isBlank()) {
                    continue;
                }

                int numero = Integer.parseInt(valorNumero.toString());
                String nome = valorNome.toString().trim();
                String posicao = valorPosicao == null ? "" : valorPosicao.toString().trim();

                if (numerosUsados.contains(numero)) {
                    throw new IllegalArgumentException("Número repetido no plantel: " + numero);
                }

                numerosUsados.add(numero);
                novosJogadores.add(new Jogador(numero, nome, posicao));
            }

            if (novosJogadores.size() > LIMITE_JOGADORES) {
                JOptionPane.showMessageDialog(
                        this,
                        "Uma seleção só pode ter no máximo " + LIMITE_JOGADORES + " jogadores convocados.",
                        "Plantel inválido",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            selecaoAtual.getJogadores().clear();

            for (Jogador jogador : novosJogadores) {
                controller.adicionarJogador(selecaoAtual, jogador);
            }

            carregarJogadores(selecaoAtual);

            JOptionPane.showMessageDialog(this, "Plantel guardado com sucesso.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "O número do jogador deve ser válido.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private int calcularProximoNumero() {
        int maior = 0;

        for (int i = 0; i < modeloTabelaJogadores.getRowCount(); i++) {
            Object valor = modeloTabelaJogadores.getValueAt(i, 0);

            try {
                int numero = Integer.parseInt(valor.toString());

                if (numero > maior) {
                    maior = numero;
                }

            } catch (Exception ignored) {
            }
        }

        return maior + 1;
    }

    private class BotaoEliminarRenderer extends JButton implements TableCellRenderer {

        public BotaoEliminarRenderer() {
            setText("🗑");
            setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            return this;
        }
    }

    private class BotaoEliminarEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button;
        private int linha;

        public BotaoEliminarEditor() {
            button = new JButton("🗑");
            button.setFocusable(false);

            button.addActionListener(e -> {
                if (linha >= 0 && linha < modeloTabelaJogadores.getRowCount()) {
                    int resposta = JOptionPane.showConfirmDialog(
                            PainelEquipas.this,
                            "Tens a certeza que queres eliminar este jogador?",
                            "Confirmar eliminação",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (resposta == JOptionPane.YES_OPTION) {
                        modeloTabelaJogadores.removeRow(linha);
                    }
                }

                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column
        ) {
            this.linha = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "🗑";
        }
    }
}
