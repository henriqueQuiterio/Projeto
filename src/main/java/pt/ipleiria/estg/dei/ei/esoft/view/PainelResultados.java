package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

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

    public PainelResultados(MundialController controller, JList<Jogo> lista,
                            JTextField gA, JTextField gB, JTextField pA, JTextField pB,
                            JTextField rA, JTextField rB, JTextField cA, JTextField cB,
                            JTextField fA, JTextField fB, JComboBox<String> motm,
                            JButton sub, JButton edit, JButton canc, JTable tabEventos) {

        this.controller = controller;
        this.listaJogos = lista;
        this.txtGolosA = gA; this.txtGolosB = gB;
        this.txtPosseA = pA; this.txtPosseB = pB;
        this.txtRematesA = rA; this.txtRematesB = rB;
        this.txtCantosA = cA; this.txtCantosB = cB;
        this.txtFaltasA = fA; this.txtFaltasB = fB;
        this.cbMOTM = motm;
        this.btnSubmeter = sub;
        this.btnEditar = edit;
        this.btnCancelar = canc;
        this.tabelaEventos = tabEventos;

        this.modeloTabelaEventos = (DefaultTableModel) tabelaEventos.getModel();

        configurarEventos();
        carregarJogos();
    }

    private void carregarJogos() {
        DefaultListModel<Jogo> modelo = new DefaultListModel<>();
        for (Jogo j : controller.getCalendarioJogos()) modelo.addElement(j);
        listaJogos.setModel(modelo);
    }

    private void configurarEventos() {
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

        // Lógica de Posse de Bola Automática
        txtPosseA.addActionListener(e -> calcularPosse());
    }

    private void submeterDados() {
        Jogo jogo = listaJogos.getSelectedValue();
        if (jogo == null) return;
        try {
            jogo.definirResultado(Integer.parseInt(txtGolosA.getText()), Integer.parseInt(txtGolosB.getText()));
            jogo.setMotm((String) cbMOTM.getSelectedItem());
            jogo.setConcluido(true);
            controller.guardarDados();
            modoEdicao(false);
            JOptionPane.showMessageDialog(this, "Dados gravados com sucesso!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro: Verifique os campos numéricos.");
        }
    }

    private void modoEdicao(boolean editar) {
        boolean podeEditar = !editar || (listaJogos.getSelectedValue() != null && listaJogos.getSelectedValue().isConcluido());
        txtGolosA.setEnabled(podeEditar);
        txtGolosB.setEnabled(podeEditar);
        cbMOTM.setEnabled(podeEditar);
        btnSubmeter.setVisible(podeEditar);
        btnEditar.setVisible(!podeEditar);
        btnCancelar.setVisible(podeEditar);
    }

    private void atualizarInterface(Jogo jogo) {
        txtGolosA.setText(String.valueOf(jogo.getGolosA()));
        txtGolosB.setText(String.valueOf(jogo.getGolosB()));
        cbMOTM.removeAllItems();
        for (Selecao s : controller.getSelecoes()) {
            if (s.getPais().equals(jogo.getSelecaoA()) || s.getPais().equals(jogo.getSelecaoB())) {
                for (Jogador j : s.getJogadores()) cbMOTM.addItem(j.getNome());
            }
        }
        cbMOTM.setSelectedItem(jogo.getMotm());
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
}