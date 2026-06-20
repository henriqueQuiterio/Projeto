package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Bilhete;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;


public class PainelBilhetes extends JPanel {
    private MundialController controller;
    private JList<Jogo> listaJogos;
    private JSpinner spnQuantidade;
    private JComboBox<String> cbSetor;
    private JList<String> listaLugares;
    private JButton btnGerarBilhete;
    private JTable tableBilhetes;
    private JLabel labelEstadio;
    private JLabel labelInfos;
    private DefaultTableModel modeloTabelaBilhetes;

    public PainelBilhetes(MundialController controller, JList<Jogo> listaJogos, JSpinner spnQuantidade,
                          JComboBox<String> cbSetor, JList<String> listaLugares, JButton btnGerarBilhete,
                          JTable tableBilhetes, JLabel labelEstadio, JLabel labelInfos) {
        this.controller = controller;
        this.listaJogos = listaJogos;
        this.spnQuantidade = spnQuantidade;
        this.cbSetor = cbSetor;
        this.listaLugares = listaLugares;
        this.btnGerarBilhete = btnGerarBilhete;
        this.tableBilhetes = tableBilhetes;
        this.labelEstadio = labelEstadio;
        this.labelInfos = labelInfos;

        // Adicionei a coluna "Lugar" à tabela
        String[] colunas = {"Código", "Seleções", "Setor", "Lugar"};
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

        configurarEventos();
        carregarJogos();
        limparInterface();
    }

    private void carregarJogos() {
        DefaultListModel<Jogo> modelo = new DefaultListModel<>();
        for (Jogo j : controller.getCalendarioJogos()) modelo.addElement(j);
        listaJogos.setModel(modelo);
    }

    private void configurarEventos() {
        listaJogos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Jogo) {
                    Jogo jogo = (Jogo) value;
                    label.setText("<html><b>" + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + "</b><br><font color='#777777'>" + jogo.getData() + " &middot; " + jogo.getHora() + "</font></html>");
                    label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                }
                return label;
            }
        });

        listaLugares.addListSelectionListener(e -> {
            int quantidadePedida = (Integer) spnQuantidade.getValue();
            List<String> selecionados = listaLugares.getSelectedValuesList();

            if (selecionados.size() > quantidadePedida) {
                // Se selecionou a mais, removemos a seleção excessiva
                JOptionPane.showMessageDialog(this, "Só podes selecionar " + quantidadePedida + " lugares.");
                listaLugares.removeSelectionInterval(listaLugares.getSelectedIndex(), listaLugares.getSelectedIndex());
            }
        });

        listaJogos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaJogos.getSelectedValue() != null) {
                atualizarInterface(listaJogos.getSelectedValue());
            }
        });

        cbSetor.addActionListener(e -> atualizarLugares());

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

        boolean jogoIndisponivel = jogo.isConcluido();
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("EEEE d MMMM yyyy HH:mm").toFormatter(new Locale("pt", "PT"));
            LocalDateTime dataHoraJogo = LocalDateTime.parse(jogo.getData() + " " + jogo.getHora(), formatter);
            if (dataHoraJogo.isBefore(LocalDateTime.now())) jogoIndisponivel = true;
        } catch (Exception ignored) {}

        if (jogoIndisponivel) {
            spnQuantidade.setEnabled(false);
            cbSetor.setEnabled(false);
            listaLugares.setEnabled(false);
            btnGerarBilhete.setEnabled(false);
            btnGerarBilhete.setText("INDISPONÍVEL");
            labelInfos.setText(jogo.getCidade() + " | " + jogo.getFase() + " (JÁ REALIZADO)");
        } else {
            cbSetor.setEnabled(true);
            btnGerarBilhete.setText("GERAR BILHETES");
            atualizarLugares(); // Verifica as 72h e preenche os lugares
        }
    }

    private void atualizarLugares() {
        Jogo jogo = listaJogos.getSelectedValue();
        String setor = (String) cbSetor.getSelectedItem();
        if (jogo == null || setor == null) return;

        // Lógica das 72h...
        boolean aMenosDe72h = false;
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("EEEE d MMMM yyyy HH:mm")
                    .toFormatter(new Locale("pt", "PT"));

            LocalDateTime dataHoraJogo = LocalDateTime.parse(jogo.getData() + " " + jogo.getHora(), formatter);
            long horasParaJogo = ChronoUnit.HOURS.between(LocalDateTime.now(), dataHoraJogo);

            // Só ativa o modo de escolher lugar se estiver entre 0 e 72 horas para o jogo
            aMenosDe72h = (horasParaJogo >= 0 && horasParaJogo <= 72);
        } catch (Exception e) {
            System.out.println("Erro ao calcular data: " + e.getMessage());
        }

        // CRIAÇÃO DO MODELO - Isto é a "lista" que a JList vai mostrar
        DefaultListModel<String> model = new DefaultListModel<>();

        if (aMenosDe72h) {
            spnQuantidade.setValue(1);
            spnQuantidade.setEnabled(false);
            listaLugares.setEnabled(true);

            List<String> ocupados = controller.getLugaresOcupados(jogo, setor);
            for (int i = 1; i <= 20; i++) {
                String nomeLugar = "Lugar " + i;
                if (!ocupados.contains(nomeLugar)) {
                    model.addElement(nomeLugar); // USAR model.addElement, NÃO listaLugares.addItem
                }
            }

            if (model.isEmpty()) {
                model.addElement("Esgotado");
                listaLugares.setEnabled(false);
                btnGerarBilhete.setEnabled(false);
            } else {
                btnGerarBilhete.setEnabled(true);
            }
        } else {
            model.addElement("Lugar Livre");
            listaLugares.setEnabled(false);
            spnQuantidade.setEnabled(true);
            btnGerarBilhete.setEnabled(true);
        }

        // APLICAR O MODELO À LISTA (Isto resolve o erro de "Cannot resolve method")
        listaLugares.setModel(model);
    }

    private void atualizarTabelaBilhetes(Jogo jogo) {
        modeloTabelaBilhetes.setRowCount(0);
        for (Bilhete b : controller.getBilhetesPorJogo(jogo)) {
            modeloTabelaBilhetes.addRow(new Object[]{
                    b.getCodigo(),
                    b.getJogo().getSiglaA() + " vs " + b.getJogo().getSiglaB(),
                    b.getSetor(),
                    b.getLugar() != null ? b.getLugar() : "Livre"
            });
        }
    }

    private void emitirBilhetes() {
        Jogo jogoSelecionado = listaJogos.getSelectedValue();
        if (jogoSelecionado == null) return;

        try {
            String setor = (String) cbSetor.getSelectedItem();
            List<String> lugaresSelecionados = listaLugares.getSelectedValuesList();
            int quantidade = (Integer) spnQuantidade.getValue();

            // Se o utilizador selecionou lugares (período de 72h), usa a emissão múltipla
            if (!listaLugares.isEnabled() || lugaresSelecionados.contains("Lugar Livre")) {
                // Modo normal (lugar livre)
                controller.emitirBilhete(jogoSelecionado, setor, "Lugar Livre", quantidade);
            } else {
                // Modo 72h (lugares específicos)
                controller.emitirBilhetesMultiplos(jogoSelecionado, setor, lugaresSelecionados);
            }

            controller.guardarDados();

            atualizarTabelaBilhetes(jogoSelecionado);
            atualizarLugares(); // Atualiza a combobox para remover o lugar que acabou de ser comprado

            JOptionPane.showMessageDialog(this, "✓ Sucesso! Bilhete(s) gerado(s).", "Emissão Concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro de Emissão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparInterface() {
        labelEstadio.setText("Selecione um jogo na lista");
        labelInfos.setText("---");
        spnQuantidade.setEnabled(false);
        cbSetor.setEnabled(false);
        if(listaLugares != null) listaLugares.setEnabled(false);
        btnGerarBilhete.setEnabled(false);
        btnGerarBilhete.setText("GERAR BILHETES");
        modeloTabelaBilhetes.setRowCount(0);
    }
}