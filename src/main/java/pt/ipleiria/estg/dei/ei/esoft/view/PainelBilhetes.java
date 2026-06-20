package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Bilhete;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelBilhetes extends JPanel {
    private MundialController controller;

    private JList<Jogo> listaJogos;
    private JSpinner spnQuantidade;
    private JComboBox<String> cbSetor;
    private JComboBox<String> cbLugar;
    private JButton btnGerarBilhete;
    private JTable tableBilhetes;
    private JLabel labelEstadio;
    private JLabel labelInfos;

    private DefaultTableModel modeloTabelaBilhetes;

    public PainelBilhetes(MundialController controller, JList<Jogo> listaJogos, JSpinner spnQuantidade,
                          JComboBox<String> cbSetor, JComboBox<String> cbLugar, JButton btnGerarBilhete,
                          JTable tableBilhetes, JLabel labelEstadio, JLabel labelInfos) {

        this.controller = controller;
        this.listaJogos = listaJogos;
        this.spnQuantidade = spnQuantidade;
        this.cbSetor = cbSetor;
        this.cbLugar = cbLugar;
        this.btnGerarBilhete = btnGerarBilhete;
        this.tableBilhetes = tableBilhetes;
        this.labelEstadio = labelEstadio;
        this.labelInfos = labelInfos;

        String[] colunas = {"Código", "Seleções", "Setor"};
        this.modeloTabelaBilhetes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        this.tableBilhetes.setModel(this.modeloTabelaBilhetes);

        this.spnQuantidade.setModel(new SpinnerNumberModel(1, 1, 10, 1));

        this.cbSetor.removeAllItems();
        this.cbSetor.addItem("Bancada Central");
        this.cbSetor.addItem("Bancada Sul");
        this.cbSetor.addItem("Bancada Norte");
        this.cbSetor.addItem("Zona VIP");

        if(this.cbLugar != null) {
            this.cbLugar.removeAllItems();
            this.cbLugar.addItem("Lugar Livre");
            this.cbLugar.setEnabled(false);
        }

        configurarEventos();
        carregarJogos();
        limparInterface();
    }

    private void carregarJogos() {
        DefaultListModel<Jogo> modelo = new DefaultListModel<>();
        for (Jogo j : controller.getCalendarioJogos()) {
            modelo.addElement(j);
        }
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
                    String textoHtml = "<html><b>" + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + "</b><br>"
                            + "<font color='#777777'>" + jogo.getData() + " &middot; " + jogo.getHora() + "</font></html>";
                    label.setText(textoHtml);
                    label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                }
                return label;
            }
        });

        listaJogos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaJogos.getSelectedValue() != null) {
                atualizarInterface(listaJogos.getSelectedValue());
            }
        });

        btnGerarBilhete.addActionListener(e -> emitirBilhetes());
    }

    private void atualizarInterface(Jogo jogo) {
        if (jogo == null) {
            limparInterface();
            return;
        }

        labelEstadio.setText(jogo.getEstadio());
        labelInfos.setText(jogo.getCidade() + " | " + jogo.getFase());

        atualizarTabelaBilhetes(jogo);

        boolean jogoIndisponivel = false;

        try {
            java.time.format.DateTimeFormatter dateFormatter = new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("EEEE d MMMM yyyy")
                    .toFormatter(new java.util.Locale("pt", "PT"));

            java.time.LocalDate dataJogo = java.time.LocalDate.parse(jogo.getData(), dateFormatter);
            java.time.LocalDate hoje = java.time.LocalDate.now();

            if (dataJogo.isBefore(hoje) || jogo.isConcluido()) {
                jogoIndisponivel = true;
            }
        } catch (Exception e) {
            jogoIndisponivel = jogo.isConcluido();
        }

        if (jogoIndisponivel) {
            spnQuantidade.setEnabled(false);
            cbSetor.setEnabled(false);
            btnGerarBilhete.setEnabled(false);
            btnGerarBilhete.setText("INDISPONÍVEL");
            labelInfos.setText(jogo.getCidade() + " | " + jogo.getFase() + " (JÁ REALIZADO)");
        } else {
            spnQuantidade.setEnabled(true);
            cbSetor.setEnabled(true);
            btnGerarBilhete.setEnabled(true);
            btnGerarBilhete.setText("GERAR BILHETES");
        }
    }

    private void atualizarTabelaBilhetes(Jogo jogo) {
        modeloTabelaBilhetes.setRowCount(0);
        List<Bilhete> bilhetesDesteJogo = controller.getBilhetesPorJogo(jogo);

        for (Bilhete b : bilhetesDesteJogo) {
            modeloTabelaBilhetes.addRow(new Object[]{
                    b.getCodigo(),
                    b.getJogo().getSiglaA() + " vs " + b.getJogo().getSiglaB(),
                    b.getSetor()
            });
        }
    }

    private void emitirBilhetes() {
        Jogo jogoSelecionado = listaJogos.getSelectedValue();
        if (jogoSelecionado == null) return;

        try {
            String setorSelecionado = (String) cbSetor.getSelectedItem();
            int quantidade = (Integer) spnQuantidade.getValue();

            controller.emitirBilhete(jogoSelecionado, setorSelecionado, quantidade);
            controller.guardarDados();
            atualizarTabelaBilhetes(jogoSelecionado);

            JOptionPane.showMessageDialog(this,
                    "✓ Sucesso! " + quantidade + " bilhete(s) gerado(s) para " + setorSelecionado + ".",
                    "Emissão Concluída", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro de Emissão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparInterface() {
        labelEstadio.setText("Selecione um jogo na lista");
        labelInfos.setText("---");

        spnQuantidade.setEnabled(false);
        cbSetor.setEnabled(false);
        btnGerarBilhete.setEnabled(false);
        btnGerarBilhete.setText("GERAR BILHETES");

        modeloTabelaBilhetes.setRowCount(0);
    }
}