package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PainelGestaoArbitragem extends JPanel {
    private MundialController controller;
    private JCheckBox chkSemArbitros;
    private JList<Jogo> listaJogosAlocacao;
    private DefaultListModel<Jogo> modeloListaJogosGestaoArbitragem;

    private JComboBox<Arbitro> comboPrincipal, comboAssistente1, comboAssistente2, comboVar;
    private JButton btnConfirmarEquipa;
    private JLabel lblStatusValidacao;
    private javax.swing.event.ListSelectionListener listenerJogos;
    private PainelCalendario painelCalendarioRef; // Referência para atualizar a outra aba ao gravar!

    public PainelGestaoArbitragem(MundialController controller, PainelCalendario painelCalendarioRef) {
        this.controller = controller;
        this.painelCalendarioRef = painelCalendarioRef;

        setLayout(new BorderLayout(15, 15));

        // Painel Esquerdo (Lista de Jogos)
        JPanel painelEsquerdo = new JPanel(new BorderLayout(5, 5));
        painelEsquerdo.setPreferredSize(new Dimension(350, 0));
        chkSemArbitros = new JCheckBox("Sem Árbitros Atribuídos");
        modeloListaJogosGestaoArbitragem = new DefaultListModel<>();
        listaJogosAlocacao = new JList<>(modeloListaJogosGestaoArbitragem);
        listaJogosAlocacao.setCellRenderer(new JogoAlocacaoListRenderer());
        painelEsquerdo.add(chkSemArbitros, BorderLayout.NORTH);
        painelEsquerdo.add(new JScrollPane(listaJogosAlocacao), BorderLayout.CENTER);

        // Painel Direito (Formulário)
        JPanel painelFormulario = new JPanel(new GridLayout(11, 1, 5, 5));
        painelFormulario.setBorder(new EmptyBorder(20, 20, 20, 20));

        comboPrincipal = new JComboBox<>();
        comboAssistente1 = new JComboBox<>();
        comboAssistente2 = new JComboBox<>();
        comboVar = new JComboBox<>();
        btnConfirmarEquipa = new JButton("CONFIRMAR EQUIPA");
        lblStatusValidacao = new JLabel("<html>🏆 Selecione um jogo na lista lateral para iniciar.</html>");

        btnConfirmarEquipa.setBackground(new Color(25, 118, 210));
        btnConfirmarEquipa.setForeground(Color.WHITE);
        btnConfirmarEquipa.setFont(new Font("Segoe UI", Font.BOLD, 13));

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

        painelFormulario.add(new JLabel("Árbitro Principal")); painelFormulario.add(comboPrincipal);
        painelFormulario.add(new JLabel("Árbitro Assistente 1")); painelFormulario.add(comboAssistente1);
        painelFormulario.add(new JLabel("Árbitro Assistente 2")); painelFormulario.add(comboAssistente2);
        painelFormulario.add(new JLabel("Quarto Árbitro / VAR")); painelFormulario.add(comboVar);
        painelFormulario.add(new JLabel("")); painelFormulario.add(btnConfirmarEquipa);
        painelFormulario.add(lblStatusValidacao);

        add(painelEsquerdo, BorderLayout.WEST);
        add(painelFormulario, BorderLayout.CENTER);

        chkSemArbitros.addActionListener(e -> atualizarListaJogosGestaoArbitragem());
        this.listenerJogos = e -> { if (!e.getValueIsAdjusting()) preencherFormularioJogoSelecionado(); };
        listaJogosAlocacao.addListSelectionListener(this.listenerJogos);
        btnConfirmarEquipa.addActionListener(e -> submeterEquipaArbitragem());

        atualizarListaJogosGestaoArbitragem();
    }

    private void carregarArbitrosNosCombos(Jogo jogo) {
        if (jogo == null) {
            comboPrincipal.setModel(new DefaultComboBoxModel<>());
            comboAssistente1.setModel(new DefaultComboBoxModel<>());
            comboAssistente2.setModel(new DefaultComboBoxModel<>());
            comboVar.setModel(new DefaultComboBoxModel<>());
            return;
        }

        List<Arbitro> arbitrosDisponiveis = controller.getArbitrosDisponiveisParaData(jogo);
        DefaultComboBoxModel<Arbitro> mPrincipal = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Arbitro> mAssistente1 = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Arbitro> mAssistente2 = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Arbitro> mVar = new DefaultComboBoxModel<>();

        for (Arbitro a : arbitrosDisponiveis) {
            if ("Principal".equalsIgnoreCase(a.getFuncao())) mPrincipal.addElement(a);
            else if ("Assistente".equalsIgnoreCase(a.getFuncao())) { mAssistente1.addElement(a); mAssistente2.addElement(a); }
            else if ("VAR".equalsIgnoreCase(a.getFuncao())) mVar.addElement(a);
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
            if (filtrarSemArbitros && j.getEquipaArbitragem() != null && !j.getEquipaArbitragem().isEmpty()) continue;
            modeloListaJogosGestaoArbitragem.addElement(j);
        }
    }

    private void preencherFormularioJogoSelecionado() {
        Jogo jogo = listaJogosAlocacao.getSelectedValue();
        if (jogo == null) return;

        carregarArbitrosNosCombos(jogo);

        if (jogo.getEquipaArbitragem() != null && !jogo.getEquipaArbitragem().isEmpty()) {
            lblStatusValidacao.setText("<html><font color='#2e7d32'><b>✓ Equipa Válida</b><br>Os árbitros estão escalados corretamente.</font></html>");
            if (jogo.getEquipaArbitragem().size() >= 4) {
                comboPrincipal.setSelectedItem(jogo.getEquipaArbitragem().get(0));
                comboAssistente1.setSelectedItem(jogo.getEquipaArbitragem().get(1));
                comboAssistente2.setSelectedItem(jogo.getEquipaArbitragem().get(2));
                comboVar.setSelectedItem(jogo.getEquipaArbitragem().get(3));
            }
        } else {
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>⚠️ Sem Árbitros Atribuídos</b><br>Monte a equipa para este desafio.</font></html>");
            comboPrincipal.setSelectedIndex(-1); comboAssistente1.setSelectedIndex(-1); comboAssistente2.setSelectedIndex(-1); comboVar.setSelectedIndex(-1);
        }
    }

    private void submeterEquipaArbitragem() {
        Jogo jogoSelecionado = listaJogosAlocacao.getSelectedValue();
        if (jogoSelecionado == null) return;

        Arbitro principal = (Arbitro) comboPrincipal.getSelectedItem();
        Arbitro assistente1 = (Arbitro) comboAssistente1.getSelectedItem();
        Arbitro assistente2 = (Arbitro) comboAssistente2.getSelectedItem();
        Arbitro var = (Arbitro) comboVar.getSelectedItem();

        if (principal == null || assistente1 == null || assistente2 == null || var == null) {
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>Erro:</b> Todos os quatro campos devem ser preenchidos.</font></html>");
            return;
        }

        if (principal.getNome().equals(assistente1.getNome()) || principal.getNome().equals(assistente2.getNome()) || principal.getNome().equals(var.getNome()) ||
                assistente1.getNome().equals(assistente2.getNome()) || assistente1.getNome().equals(var.getNome()) || assistente2.getNome().equals(var.getNome())) {
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>Erro:</b> O mesmo árbitro não pode duplicar funções.</font></html>");
            return;
        }

        try {
            List<Arbitro> equipa = new ArrayList<>();
            equipa.add(principal); equipa.add(assistente1); equipa.add(assistente2); equipa.add(var);

            controller.alocarEquipaArbitragem(jogoSelecionado, equipa);

            if (chkSemArbitros.isSelected()) {
                listaJogosAlocacao.removeListSelectionListener(this.listenerJogos);
                listaJogosAlocacao.clearSelection();
                atualizarListaJogosGestaoArbitragem();
                comboPrincipal.setSelectedIndex(-1); comboAssistente1.setSelectedIndex(-1); comboAssistente2.setSelectedIndex(-1); comboVar.setSelectedIndex(-1);
                lblStatusValidacao.setText("<html><font color='#2e7d32'><b>✓ Sucesso:</b> Equipa alocada!</font></html>");
                listaJogosAlocacao.addListSelectionListener(this.listenerJogos);
            } else {
                listaJogosAlocacao.repaint();
                lblStatusValidacao.setText("<html><font color='#2e7d32'><b>Equipa Válida:</b> Atribuída com sucesso!</font></html>");
            }

            if (painelCalendarioRef != null) painelCalendarioRef.carregarEOrdenarCartoes();
            controller.guardarDados();
        } catch (Exception ex) {
            lblStatusValidacao.setText("<html><font color='#b71c1c'><b>Erro:</b> " + ex.getMessage() + "</font></html>");
        }
    }

    private static class JogoAlocacaoListRenderer extends JPanel implements ListCellRenderer<Jogo> {
        private JLabel lblInfo, lblVisto;
        public JogoAlocacaoListRenderer() {
            setLayout(new BorderLayout(5, 5)); setBorder(new EmptyBorder(8, 12, 8, 12)); setBackground(Color.WHITE);
            lblInfo = new JLabel(); lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblVisto = new JLabel("✓"); lblVisto.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblVisto.setForeground(new Color(46, 125, 50));
            add(lblInfo, BorderLayout.CENTER); add(lblVisto, BorderLayout.EAST);
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends Jogo> list, Jogo jogo, int index, boolean isSelected, boolean cellHasFocus) {
            if (jogo != null) {
                lblInfo.setText("<html><b>" + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + "</b><br><font color='gray'>" + jogo.getHora() + " · " + jogo.getData() + "</font></html>");
                lblVisto.setVisible(jogo.getEquipaArbitragem() != null && !jogo.getEquipaArbitragem().isEmpty());
            }
            setBackground(isSelected ? new Color(232, 240, 254) : Color.WHITE);
            return this;
        }
    }
}
