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

    // Subtítulo dinâmico do jogo selecionado
    private JLabel lblNomeacaoJogo;

    private javax.swing.event.ListSelectionListener listenerJogos;
    private PainelCalendario painelCalendarioRef;

    public PainelGestaoArbitragem(MundialController controller, PainelCalendario painelCalendarioRef) {
        this.controller = controller;
        this.painelCalendarioRef = painelCalendarioRef;

        setLayout(new BorderLayout(15, 15));

        // =====================================================================
        // 1. PAINEL ESQUERDO (Lista de Jogos)
        // =====================================================================
        JPanel painelEsquerdo = new JPanel(new BorderLayout(5, 5));
        painelEsquerdo.setPreferredSize(new Dimension(350, 0));
        chkSemArbitros = new JCheckBox("Sem Árbitros Atribuídos");
        modeloListaJogosGestaoArbitragem = new DefaultListModel<>();
        listaJogosAlocacao = new JList<>(modeloListaJogosGestaoArbitragem);
        listaJogosAlocacao.setCellRenderer(new JogoAlocacaoListRenderer());
        painelEsquerdo.add(chkSemArbitros, BorderLayout.NORTH);
        painelEsquerdo.add(new JScrollPane(listaJogosAlocacao), BorderLayout.CENTER);

        // =====================================================================
        // 2. PAINEL DIREITO EM COLUNA TOTAL (Fiel ao Protótipo)
        // =====================================================================
        JPanel painelDireitoConteudo = new JPanel();
        painelDireitoConteudo.setLayout(new BoxLayout(painelDireitoConteudo, BoxLayout.Y_AXIS));
        painelDireitoConteudo.setBorder(new EmptyBorder(25, 25, 25, 25));

        // 2.1 Cabeçalho Superior
        JLabel lblTituloSuperior = new JLabel("Nomeação da Equipa Técnica");
        lblTituloSuperior.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloSuperior.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblNomeacaoJogo = new JLabel("Selecione um jogo na lista lateral para iniciar.");
        lblNomeacaoJogo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNomeacaoJogo.setForeground(new Color(120, 120, 120));
        lblNomeacaoJogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelDireitoConteudo.add(lblTituloSuperior);
        painelDireitoConteudo.add(Box.createVerticalStrut(4));
        painelDireitoConteudo.add(lblNomeacaoJogo);
        painelDireitoConteudo.add(Box.createVerticalStrut(20));

        // 2.2 Inicialização dos componentes do formulário
        comboPrincipal = new JComboBox<>();
        comboAssistente1 = new JComboBox<>();
        comboAssistente2 = new JComboBox<>();
        comboVar = new JComboBox<>();
        btnConfirmarEquipa = new JButton("CONFIRMAR EQUIPA");
        lblStatusValidacao = new JLabel("<html><font color='gray'>Aguardando seleção de jogo...</font></html>");

        // Alinhamentos estritos à esquerda
        comboPrincipal.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboAssistente1.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboAssistente2.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboVar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConfirmarEquipa.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblStatusValidacao.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnConfirmarEquipa.setBackground(new Color(25, 118, 210));
        btnConfirmarEquipa.setForeground(Color.WHITE);
        btnConfirmarEquipa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirmarEquipa.setMaximumSize(new Dimension(450, 40)); // Acompanha a largura das caixas

        // Configuração do Renderizador dos Árbitros nas caixas
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

        // 2.3 Empilhar os pares utilizando o método auxiliar
        addCampoFormulario(painelDireitoConteudo, "Árbitro Principal", comboPrincipal);
        addCampoFormulario(painelDireitoConteudo, "Árbitro Assistente 1", comboAssistente1);
        addCampoFormulario(painelDireitoConteudo, "Árbitro Assistente 2", comboAssistente2);
        addCampoFormulario(painelDireitoConteudo, "Quarto Árbitro / VAR", comboVar);

        painelDireitoConteudo.add(Box.createVerticalStrut(15));
        painelDireitoConteudo.add(btnConfirmarEquipa);
        painelDireitoConteudo.add(Box.createVerticalStrut(15));
        painelDireitoConteudo.add(lblStatusValidacao);

        // 🌟 ALTERAÇÃO AQUI: Removemos o limite de 500px para o painel ocupar a largura toda da janela
        painelDireitoConteudo.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // =====================================================================
        // 3. MONTAGEM FINAL DA ABA (Direto no CENTER para esticar)
        // =====================================================================
        add(painelEsquerdo, BorderLayout.WEST);
        add(painelDireitoConteudo, BorderLayout.CENTER); // Adiciona diretamente o conteúdo expandido!

        // Registo de Listeners e Ações
        chkSemArbitros.addActionListener(e -> atualizarListaJogosGestaoArbitragem());
        this.listenerJogos = e -> { if (!e.getValueIsAdjusting()) preencherFormularioJogoSelecionado(); };
        listaJogosAlocacao.addListSelectionListener(this.listenerJogos);
        btnConfirmarEquipa.addActionListener(e -> submeterEquipaArbitragem());

        // Carga inicial
        atualizarListaJogosGestaoArbitragem();
        btnConfirmarEquipa.setEnabled(false);
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
        if (jogo == null) {
            lblNomeacaoJogo.setText("Selecione um jogo na lista lateral para iniciar.");
            lblStatusValidacao.setText("<html><font color='gray'>Aguardando seleção de jogo...</font></html>");
            btnConfirmarEquipa.setEnabled(false);
            carregarArbitrosNosCombos(null);
            return;
        }

        // Atualiza o cabeçalho dinâmico com o formato do protótipo
        lblNomeacaoJogo.setText(jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + "   ·   " + JogoAlocacaoListRenderer.obterDataFormatada(jogo.getData()));
        btnConfirmarEquipa.setEnabled(true);

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
                lblNomeacaoJogo.setText("Selecione um jogo na lista lateral para iniciar.");
                btnConfirmarEquipa.setEnabled(false);
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
            setLayout(new BorderLayout(5, 5));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setBackground(Color.WHITE);

            lblInfo = new JLabel();
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            // 🌟 ALTERAÇÃO AQUI: Usar o código Unicode estável do visto
            lblVisto = new JLabel("✓");
            lblVisto.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblVisto.setForeground(new Color(46, 125, 50)); // Verde escuro elegante

            add(lblInfo, BorderLayout.CENTER);
            add(lblVisto, BorderLayout.EAST);
        }

        public static String obterDataFormatada(String dataOriginal) {
            String dataNumerica = dataOriginal;
            if (dataOriginal != null && dataOriginal.contains(" ")) {
                String[] partes = dataOriginal.split(" ");
                if (partes.length >= 4) {
                    String dia = partes[1];
                    String mesTexto = partes[2].toLowerCase();
                    String ano = partes[3];
                    String mesNum = "01";
                    if (mesTexto.startsWith("jan")) mesNum = "01";
                    else if (mesTexto.startsWith("fev")) mesNum = "02";
                    else if (mesTexto.startsWith("mar")) mesNum = "03";
                    else if (mesTexto.startsWith("abr")) mesNum = "04";
                    else if (mesTexto.startsWith("mai")) mesNum = "05";
                    else if (mesTexto.startsWith("jun")) mesNum = "06";
                    else if (mesTexto.startsWith("jul")) mesNum = "07";
                    else if (mesTexto.startsWith("ago")) mesNum = "08";
                    else if (mesTexto.startsWith("set")) mesNum = "09";
                    else if (mesTexto.startsWith("out")) mesNum = "10";
                    else if (mesTexto.startsWith("nov")) mesNum = "11";
                    else if (mesTexto.startsWith("dez")) mesNum = "12";
                    if (dia.length() == 1) dia = "0" + dia;
                    dataNumerica = dia + "-" + mesNum + "-" + ano;
                }
            }
            return dataNumerica;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Jogo> list, Jogo jogo, int index, boolean isSelected, boolean cellHasFocus) {
            if (jogo != null) {
                lblInfo.setText("<html><b>" + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + "</b><br><font color='gray'>" + obterDataFormatada(jogo.getData()) + " · " + jogo.getHora() + "</font></html>");
                lblVisto.setVisible(jogo.getEquipaArbitragem() != null && !jogo.getEquipaArbitragem().isEmpty());
            }
            setBackground(isSelected ? new Color(232, 240, 254) : Color.WHITE);
            return this;
        }
    }

    private void addCampoFormulario(JPanel painel, String labelTexto, JComboBox<?> combo) {
        JLabel lbl = new JLabel(labelTexto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 🌟 ALTERAÇÃO AQUI: Mudar de 450 para Integer.MAX_VALUE para acompanhar a janela toda
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        painel.add(lbl);
        painel.add(Box.createVerticalStrut(4));
        painel.add(combo);
        painel.add(Box.createVerticalStrut(12));
    }
}
