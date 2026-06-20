package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat; // Se usares java.util.Date
// OU se usares java.time.LocalDateTime:
import java.time.format.DateTimeFormatter;

public class PainelResultados extends JPanel {
    private MundialController controller;

    // Componentes injetados da JanelaMundial
    private JList<Jogo> listaJogos;
    private JTextField txtGolosA, txtGolosB, txtPosseA, txtPosseB, txtRematesA, txtRematesB,
            txtCantosA, txtCantosB, txtFaltasA, txtFaltasB;
    private JComboBox<String> cbMOTM;
    private JButton btnSubmeter, btnEditar, btnCancelar;
    private JTable tabelaEventos;
    private DefaultTableModel modeloTabelaEventos;
    private PainelCalendario painelCalendarioRef;
    private JButton btnAdicionarEvento;
    private JButton btnApagarEvento;

    public PainelResultados(MundialController controller, JList<Jogo> lista,
                            JTextField gA, JTextField gB, JTextField pA, JTextField pB,
                            JTextField rA, JTextField rB, JTextField cA, JTextField cB,
                            JTextField fA, JTextField fB, JComboBox<String> motm,
                            JButton sub, JButton edit, JButton canc, JTable tabEventos,
                            PainelCalendario painelCalendarioRef, JButton btnApagar) {

        // 1. Atribuição dos atributos e serviços base
        this.controller = controller;
        this.listaJogos = lista;
        this.painelCalendarioRef = painelCalendarioRef;
        this.btnApagarEvento = btnApagar;

        // 2. Componentes de texto das estatísticas
        this.txtGolosA = gA;   this.txtGolosB = gB;
        this.txtPosseA = pA;   this.txtPosseB = pB;
        this.txtRematesA = rA; this.txtRematesB = rB;
        this.txtCantosA = cA;  this.txtCantosB = cB;
        this.txtFaltasA = fA;  this.txtFaltasB = fB;
        this.cbMOTM = motm;

        // 3. Botões de controlo do formulário
        this.btnSubmeter = sub;
        this.btnEditar = edit;
        this.btnCancelar = canc;
        this.btnAdicionarEvento = null; // Capturado dinamicamente via SwingUtilities no configurarEventos()

        this.btnApagarEvento.addActionListener(e -> apagarEventoSelecionado());

        // 4. Inicialização e acoplamento do modelo seguro da Tabela de Eventos
        this.tabelaEventos = tabEventos;
        String[] colunasEventos = {"Minuto", "Tipo de Evento", "Jogador / Detalhes"};
        this.modeloTabelaEventos = new DefaultTableModel(colunasEventos, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tranca a edição direta de texto nas células por duplo clique
            }
        };
        this.tabelaEventos.setModel(this.modeloTabelaEventos);

        // 5. Configuração do Sorter para garantir a ordenação crescente por minutos
        javax.swing.table.TableRowSorter<DefaultTableModel> ordenador = new javax.swing.table.TableRowSorter<>(this.modeloTabelaEventos);
        ordenador.setComparator(0, new java.util.Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    int m1 = Integer.parseInt(o1.replace("'", "").trim());
                    int m2 = Integer.parseInt(o2.replace("'", "").trim());
                    return Integer.compare(m1, m2);
                } catch (NumberFormatException e) {
                    return o1.compareTo(o2);
                }
            }
        });
        this.tabelaEventos.setRowSorter(ordenador);

        ordenador.toggleSortOrder(0);

        // 6. Arranque lógico dos dados e da interface gráfica
        configurarEventos();
        carregarJogos();
        limparInterfaceGenerica(); // Garante o ecrã limpo sem equipas fantasma até selecionar um jogo
    }

    private void carregarJogos() {
        DefaultListModel<Jogo> modelo = new DefaultListModel<>();
        for (Jogo j : controller.getCalendarioJogos()) modelo.addElement(j);
        listaJogos.setModel(modelo);
    }

    private void configurarEventos() {
        listaJogos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Jogo) {
                    Jogo jogo = (Jogo) value;

                    // Converter "Quinta-Feira 11 Junho 2026" -> "11-06-2026"
                    String dataOriginal = jogo.getData();
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

                            if (dia.length() == 1) {
                                dia = "0" + dia;
                            }

                            dataNumerica = dia + "-" + mesNum + "-" + ano;
                        }
                    }

                    // Texto em HTML para fazer a quebra de linha (<br>) e deixar a data cinzenta mais elegante
                    String textoHtml = "<html>"
                            + "<b>" + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + "</b>"
                            + "<br>"
                            + "<font color='#777777' size='3'>" + dataNumerica + " &middot; " + jogo.getHora() + "</font>"
                            + "</html>";

                    label.setText(textoHtml);

                    // Margem interna para o espaçamento vertical respirar bem
                    label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                }

                return label;
            }
        });
        // =========================================================================

        // O teu código antigo continua exatamente igual aqui para baixo:
        listaJogos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaJogos.getSelectedValue() != null) {
                atualizarInterface(listaJogos.getSelectedValue());
            }
        });

        btnSubmeter.addActionListener(e -> submeterDados());
        btnEditar.addActionListener(e -> modoEdicao(true));
        btnCancelar.addActionListener(e -> {
            atualizarInterface(listaJogos.getSelectedValue());
            modoEdicao(false);
        });

        txtPosseA.addActionListener(e -> calcularPosse());

        SwingUtilities.invokeLater(() -> {
            Component topo = SwingUtilities.getWindowAncestor(listaJogos);
            if (topo instanceof JanelaMundial) {
                JanelaMundial janela = (JanelaMundial) topo;
                this.btnAdicionarEvento = janela.btnAdicionarEvento;

                if (this.btnAdicionarEvento != null) {
                    for (java.awt.event.ActionListener al : this.btnAdicionarEvento.getActionListeners()) {
                        this.btnAdicionarEvento.removeActionListener(al);
                    }

                    this.btnAdicionarEvento.addActionListener(evt -> {
                        Jogo jogoSelecionado = listaJogos.getSelectedValue();
                        if (jogoSelecionado == null) {
                            JOptionPane.showMessageDialog(this, "Por favor, seleciona um jogo primeiro.");
                            return;
                        }

                        String minuto = JOptionPane.showInputDialog(this, "Minuto do Evento (1'-90'):");
                        if (minuto == null || minuto.isBlank()) return;

                        String[] tipos = {"Golo", "Cartão Amarelo", "Cartão Vermelho", "Substituição"};
                        String tipoSelecionado = (String) JOptionPane.showInputDialog(this,
                                "Selecione o Tipo de Evento:", "Evento",
                                JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
                        if (tipoSelecionado == null) return;

                        ArrayList<String> jogadoresDisponiveis = new ArrayList<>();
                        for (Selecao s : controller.getSelecoes()) {
                            if (s.getPais().equals(jogoSelecionado.getSelecaoA()) || s.getPais().equals(jogoSelecionado.getSelecaoB())) {
                                for (Jogador j : s.getJogadores()) {
                                    jogadoresDisponiveis.add(j.getNome() + " (" + s.getPais() + ")");
                                }
                            }
                        }

                        if (jogadoresDisponiveis.isEmpty()) {
                            JOptionPane.showMessageDialog(this, "Não existem jogadores registados para estas seleções.");
                            return;
                        }

                        if (tipoSelecionado.equals("Substituição")) {
                            // 1. Jogador que SAI
                            String sai = (String) JOptionPane.showInputDialog(this, "Selecione o jogador que SAI:", "Substituição (SAI)",
                                    JOptionPane.QUESTION_MESSAGE, null, jogadoresDisponiveis.toArray(), null);

                            // 2. Jogador que ENTRA
                            String entra = (String) JOptionPane.showInputDialog(this, "Selecione o jogador que ENTRA:", "Substituição (ENTRA)",
                                    JOptionPane.QUESTION_MESSAGE, null, jogadoresDisponiveis.toArray(), null);

                            if (sai != null && entra != null) {
                                if (sai.equals(entra)) {
                                    JOptionPane.showMessageDialog(this, "O jogador que sai não pode ser o mesmo que entra!");
                                } else {
                                    modeloTabelaEventos.addRow(new Object[]{minuto + "'", "Substituição", sai + " > " + entra});
                                }
                            }
                        } else if (tipoSelecionado.equals("Golo")) {
                            String marcador = (String) JOptionPane.showInputDialog(
                                    this,
                                    "Selecione o marcador do golo:",
                                    "Marcador",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    jogadoresDisponiveis.toArray(new String[0]),
                                    jogadoresDisponiveis.get(0)
                            );

                            if (marcador == null) {
                                return;
                            }

                            ArrayList<String> opcoesAssistencia = new ArrayList<>();
                            opcoesAssistencia.add("Sem assistência");
                            opcoesAssistencia.addAll(jogadoresDisponiveis);

                            String assistente = (String) JOptionPane.showInputDialog(
                                    this,
                                    "Selecione o assistente:",
                                    "Assistência",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    opcoesAssistencia.toArray(new String[0]),
                                    opcoesAssistencia.get(0)
                            );

                            String detalhe;

                            if (assistente == null || assistente.equals("Sem assistência")) {
                                detalhe = marcador;
                            } else {
                                if (assistente.equals(marcador)) {
                                    JOptionPane.showMessageDialog(this, "O assistente não pode ser o próprio marcador.");
                                    return;
                                }

                                detalhe = marcador + " | Assist: " + assistente;
                            }

                            modeloTabelaEventos.addRow(new Object[]{minuto + "'", "Golo", detalhe});

                        } else {
                            String jogadorSelecionado = (String) JOptionPane.showInputDialog(
                                    this,
                                    "Selecione o Jogador envolvido:",
                                    "Jogador",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    jogadoresDisponiveis.toArray(new String[0]),
                                    jogadoresDisponiveis.get(0)
                            );

                            if (jogadorSelecionado != null) {
                                modeloTabelaEventos.addRow(new Object[]{minuto + "'", tipoSelecionado, jogadorSelecionado});
                            }
                        }
                    });
                }

                JButton btnApagar = janela.btnApagarEvento;

                if (btnApagar != null) {
                    // Limpa listeners antigos para não duplicar
                    for (java.awt.event.ActionListener al : btnApagar.getActionListeners()) {
                        btnApagar.removeActionListener(al);
                    }
                    // Adiciona a nova lógica
                    btnApagar.addActionListener(e -> apagarEventoSelecionado());
                }

            }
        });
    }

    // Adiciona este método dentro da classe PainelResultados
    private void apagarEventoSelecionado() {
        int linhaSelecionada = tabelaEventos.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma linha na tabela para apagar.");
            return;
        }

        // Converter o índice da vista para o índice do modelo (importante se tiveres sorting na tabela)
        int modeloIndex = tabelaEventos.convertRowIndexToModel(linhaSelecionada);

        int resposta = JOptionPane.showConfirmDialog(this,
                "Tem a certeza que deseja apagar este evento?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION);

        if (resposta == JOptionPane.YES_OPTION) {
            modeloTabelaEventos.removeRow(modeloIndex);
        }
    }

    private void submeterDados() {
        Jogo jogo = listaJogos.getSelectedValue();
        if (jogo == null) return;

        try {
            int pA = Integer.parseInt(txtPosseA.getText());
            int pB = Integer.parseInt(txtPosseB.getText());

            // Validação simples:
            if ((pA + pB) != 100) {
                JOptionPane.showMessageDialog(this, "Erro: A soma da posse de bola tem de ser exatamente 100%.");
                return; // Interrompe o processo e não guarda nada
            }

            // Se passar na validação, guarda:
            jogo.setPosseA(pA);
            jogo.setPosseB(pB);

            // 1. Gravar golos e MOTM
            jogo.definirResultado(Integer.parseInt(txtGolosA.getText()), Integer.parseInt(txtGolosB.getText()));
            jogo.setMotm((String) cbMOTM.getSelectedItem());

            // 2. Gravar Estatísticas Técnicas (Novo!)
            jogo.setPosseA(Integer.parseInt(txtPosseA.getText().isBlank() ? "0" : txtPosseA.getText()));
            jogo.setPosseB(Integer.parseInt(txtPosseB.getText().isBlank() ? "0" : txtPosseB.getText()));
            jogo.setRematesA(Integer.parseInt(txtRematesA.getText().isBlank() ? "0" : txtRematesA.getText()));
            jogo.setRematesB(Integer.parseInt(txtRematesB.getText().isBlank() ? "0" : txtRematesB.getText()));
            jogo.setCantosA(Integer.parseInt(txtCantosA.getText().isBlank() ? "0" : txtCantosA.getText()));
            jogo.setCantosB(Integer.parseInt(txtCantosB.getText().isBlank() ? "0" : txtCantosB.getText()));
            jogo.setFaltasA(Integer.parseInt(txtFaltasA.getText().isBlank() ? "0" : txtFaltasA.getText()));
            jogo.setFaltasB(Integer.parseInt(txtFaltasB.getText().isBlank() ? "0" : txtFaltasB.getText()));

            // 3. Recolher as linhas da tabela e guardar no jogo (Novo!)
            List<String[]> listaEventosMemoria = new ArrayList<>();
            for (int i = 0; i < modeloTabelaEventos.getRowCount(); i++) {
                String min = modeloTabelaEventos.getValueAt(i, 0).toString();
                String tipo = modeloTabelaEventos.getValueAt(i, 1).toString();
                String det = modeloTabelaEventos.getValueAt(i, 2).toString();
                listaEventosMemoria.add(new String[]{min, tipo, det});
            }
            jogo.setEventosDoJogo(listaEventosMemoria);

            // Concluir e Persistir no Ficheiro .dat
            jogo.setConcluido(true);
            controller.guardarDados();

            if (painelCalendarioRef != null) {
                painelCalendarioRef.carregarEOrdenarCartoes();
            }

            Component topo = SwingUtilities.getWindowAncestor(listaJogos);
            if (topo instanceof JanelaMundial) {
                ((JanelaMundial) topo).atualizarAbaClassificacao();
            }

            modoEdicao(false);
            JOptionPane.showMessageDialog(this, "Todos os dados do jogo foram persistidos com sucesso!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: Certifique-se de que preencheu todos os campos técnicos com números.");
        }
    }

    private void modoEdicao(boolean editar) {
        txtGolosA.setEnabled(editar);
        txtGolosB.setEnabled(editar);
        cbMOTM.setEnabled(editar);

        // Controla a visibilidade dos botões de ação
        btnSubmeter.setVisible(editar);
        btnCancelar.setVisible(editar);

        // O TRUQUE ESTÁ AQUI: O botão Editar deve aparecer SEMPRE que NÃO estivermos a editar!
        btnEditar.setVisible(!editar);

        if (btnAdicionarEvento != null) {
            btnAdicionarEvento.setEnabled(editar);
        }
    }

    private void atualizarInterface(Jogo jogo) {
        // 1. Se não houver nenhum jogo selecionado, limpa o painel e para aqui
        if (jogo == null) {
            limparInterfaceGenerica();
            return;
        }

        // 2. Limpa os eventos do ecrã anterior
        modeloTabelaEventos.setRowCount(0);

        // 3. Carregar golos e detalhes principais
        txtGolosA.setText(String.valueOf(jogo.getGolosA()));
        txtGolosB.setText(String.valueOf(jogo.getGolosB()));

        // 4. Popular os campos das estatísticas técnicas guardadas
        txtPosseA.setText(String.valueOf(jogo.getPosseA()));
        txtPosseB.setText(String.valueOf(jogo.getPosseB()));
        txtRematesA.setText(String.valueOf(jogo.getRematesA()));
        txtRematesB.setText(String.valueOf(jogo.getRematesB()));
        txtCantosA.setText(String.valueOf(jogo.getCantosA()));
        txtCantosB.setText(String.valueOf(jogo.getCantosB()));
        txtFaltasA.setText(String.valueOf(jogo.getFaltasA()));
        txtFaltasB.setText(String.valueOf(jogo.getFaltasB()));

        // 5. Restaurar os eventos guardados de volta para a JTable
        if (jogo.getEventosDoJogo() != null) {
            for (String[] evento : jogo.getEventosDoJogo()) {
                modeloTabelaEventos.addRow(evento);
            }
        }

        if (tabelaEventos.getRowSorter() instanceof javax.swing.DefaultRowSorter) {
            ((javax.swing.DefaultRowSorter<?, ?>) tabelaEventos.getRowSorter()).sort();
        }
        // 6. Sincronizar as Labels de nomes e siglas da JanelaMundial
        Component topo = SwingUtilities.getWindowAncestor(listaJogos);
        if (topo instanceof JanelaMundial) {
            JanelaMundial janela = (JanelaMundial) topo;

            // Atualiza as siglas junto aos golos do topo
            janela.lblSiglaA.setText(jogo.getSiglaA());
            janela.lblSiglaB.setText(jogo.getSiglaB());

            // Atualiza os títulos das estatísticas com o nome do país real
            janela.lblEstatisticaA.setText(jogo.getSelecaoA());
            janela.lblEstatisticaB.setText(jogo.getSelecaoB());
        }

        // 7. Recarregar o Combo Box do Man of the Match com os jogadores das duas equipas
        cbMOTM.removeAllItems();
        for (Selecao s : controller.getSelecoes()) {
            if (s.getPais().equals(jogo.getSelecaoA()) || s.getPais().equals(jogo.getSelecaoB())) {
                for (Jogador j : s.getJogadores()) {
                    cbMOTM.addItem(j.getNome());
                }
            }
        }
        cbMOTM.setSelectedItem(jogo.getMotm());

        // 8. Controlar o estado de edição dos botões e campos
        modoEdicao(!jogo.isConcluido());
    }

    private void calcularPosse() {
        try {
            int pA = Integer.parseInt(txtPosseA.getText());
            txtPosseB.setText(String.valueOf(100 - pA));
        } catch (Exception ex) {
            txtPosseB.setText("0");
        }
    }

    private void limparInterfaceGenerica() {
        // 1. Limpar caixas de texto
        txtGolosA.setText(""); txtGolosB.setText("");
        txtPosseA.setText(""); txtPosseB.setText("");
        txtRematesA.setText(""); txtRematesB.setText("");
        txtCantosA.setText(""); txtCantosB.setText("");
        txtFaltasA.setText(""); txtFaltasB.setText("");

        // 2. Limpar a tabela de eventos e o combo box
        if (modeloTabelaEventos != null) {
            modeloTabelaEventos.setRowCount(0);
        }
        cbMOTM.removeAllItems();

        // 3. Colocar rótulos genéricos por segurança através da JanelaMundial
        SwingUtilities.invokeLater(() -> {
            Component topo = SwingUtilities.getWindowAncestor(listaJogos);
            if (topo instanceof JanelaMundial) {
                JanelaMundial janela = (JanelaMundial) topo;

                // Repor siglas do topo
                janela.lblSiglaA.setText("SEL A");
                janela.lblSiglaB.setText("SEL B");

                // Repor títulos das estatísticas técnicas cá em baixo
                janela.lblEstatisticaA.setText("Estatísticas - Seleção A");
                janela.lblEstatisticaB.setText("Estatísticas - Seleção B");
            }
        });

        // 4. Forçar o estado trancado (esconde botões Submeter/Cancelar e o Adicionar Evento)
        modoEdicao(false);
        btnEditar.setVisible(false); // Esconde também o Editar porque não há nenhum jogo selecionado!
    }
}