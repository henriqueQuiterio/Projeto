package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PainelCalendario extends JPanel {
    private MundialController controller;
    private JComboBox<String> comboOrdemData;
    private JComboBox<String> comboFusoHorario;
    private JList<Jogo> listaCartoesJogos;
    private DefaultListModel<Jogo> modeloListaJogos;

    public PainelCalendario(MundialController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        painelFiltros.setBackground(new Color(245, 245, 245));

        comboOrdemData = new JComboBox<>(new String[]{"Crescente", "Decrescente"});
        comboFusoHorario = new JComboBox<>(new String[]{"UTC+0 (Lisboa)", "UTC+1 (Central Europe)", "UTC-5 (EST / NY)"});

        painelFiltros.add(new JLabel("Ordem de Data:"));
        painelFiltros.add(comboOrdemData);
        painelFiltros.add(new JLabel("Fuso Horário:"));
        painelFiltros.add(comboFusoHorario);

        modeloListaJogos = new DefaultListModel<>();
        listaCartoesJogos = new JList<>(modeloListaJogos);
        listaCartoesJogos.setBackground(new Color(245, 245, 245));
        listaCartoesJogos.setCellRenderer(new CartaoJogoRenderer(this));

        JScrollPane scrollJogos = new JScrollPane(listaCartoesJogos);
        scrollJogos.setBorder(null);

        add(painelFiltros, BorderLayout.NORTH);
        add(scrollJogos, BorderLayout.CENTER);

        comboOrdemData.addActionListener(e -> carregarEOrdenarCartoes());
        comboFusoHorario.addActionListener(e -> listaCartoesJogos.repaint());

        carregarEOrdenarCartoes();
    }

    public void carregarEOrdenarCartoes() {
        modeloListaJogos.clear();
        List<Jogo> listaJogos = new ArrayList<>(controller.getCalendarioJogos());

        if (listaJogos.isEmpty()) {
            // O erro está aqui, falta o argumento da capacidade (int)
            Jogo jogoAviso = new Jogo("AVISO", "", "", "", "", "", 0,"", "Não existem jogos agendados de momento.", "", "", "Por favor, volte a tentar mais tarde.", "", "");
            modeloListaJogos.addElement(jogoAviso);
            return;
        }

        java.time.format.DateTimeFormatter formatter = new java.time.format.DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("EEEE d MMMM yyyy")
                .toFormatter(new java.util.Locale("pt", "PT"));

        boolean crescente = "Crescente".equals(comboOrdemData.getSelectedItem());

        listaJogos.sort((j1, j2) -> {
            try {
                java.time.LocalDate d1 = java.time.LocalDate.parse(j1.getData(), formatter);
                java.time.LocalDate d2 = java.time.LocalDate.parse(j2.getData(), formatter);

                int comp = d1.compareTo(d2);
                if (comp == 0) {
                    comp = j1.getHora().compareTo(j2.getHora());
                }
                return crescente ? comp : -comp;
            } catch (Exception e) {
                return j1.getData().compareTo(j2.getData());
            }
        });

        for (Jogo j : listaJogos) {
            modeloListaJogos.addElement(j);
        }
    }

    public String obtenerHoraConvertida(Jogo jogo) {
        String fusoSelecionado = (String) comboFusoHorario.getSelectedItem();
        if (fusoSelecionado == null) return jogo.getHora();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime horaOriginal = LocalTime.parse(jogo.getHora(), formatter);

            if (fusoSelecionado.contains("UTC+1")) {
                return horaOriginal.plusHours(1).format(formatter);
            } else if (fusoSelecionado.contains("UTC-5")) {
                return horaOriginal.minusHours(5).format(formatter);
            }
        } catch (Exception e) {
            // Fallback seguro
        }
        return jogo.getHora();
    }

    // --- CLASSE INTERNA: CartaoJogoRenderer ---
    private static class CartaoJogoRenderer extends JPanel implements ListCellRenderer<Jogo> {
        private JLabel lblDataMeta, lblEquipas, lblDetalhes, lblEstado;
        private PainelCalendario painel;

        public CartaoJogoRenderer(PainelCalendario painel) {
            this.painel = painel;
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(8, 0, 8, 0),
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true)
            ));
            setBackground(Color.WHITE);

            JPanel painelInterno = new JPanel(new GridLayout(3, 1, 5, 5));
            painelInterno.setBackground(Color.WHITE);
            painelInterno.setBorder(new EmptyBorder(12, 16, 12, 16));

            lblDataMeta = new JLabel();
            lblDataMeta.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblDataMeta.setForeground(Color.GRAY);

            lblEquipas = new JLabel();
            lblEquipas.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblEquipas.setHorizontalAlignment(SwingConstants.CENTER);

            lblDetalhes = new JLabel();
            lblDetalhes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblDetalhes.setForeground(Color.LIGHT_GRAY);
            lblDetalhes.setHorizontalAlignment(SwingConstants.CENTER);

            painelInterno.add(lblDataMeta);
            painelInterno.add(lblEquipas);
            painelInterno.add(lblDetalhes);

            lblEstado = new JLabel(" Terminado ");
            lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblEstado.setForeground(Color.WHITE);
            lblEstado.setBackground(new Color(46, 125, 50));
            lblEstado.setOpaque(true);

            JPanel painelDireito = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            painelDireito.setBackground(Color.WHITE);
            painelDireito.add(lblEstado);

            add(painelInterno, BorderLayout.CENTER);
            add(painelDireito, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Jogo> list, Jogo jogo, int index, boolean isSelected, boolean cellHasFocus) {
            if ("AVISO".equals(jogo.getData())) {
                lblDataMeta.setText("SISTEMA");
                lblEquipas.setText("<html><font color='#b71c1c'><b>" + jogo.getSelecaoA() + "</b></font><br><font color='black'>" + jogo.getSelecaoB() + "</font></html>");
                lblDetalhes.setText("");
                lblEstado.setVisible(false);
                setBackground(Color.WHITE);
                ((JPanel)getComponent(0)).setBackground(Color.WHITE);
                getComponent(1).setBackground(Color.WHITE);
                return this;
            }

            lblDataMeta.setText(jogo.getData().toUpperCase());
            String horaExibicao = painel.obtenerHoraConvertida(jogo);

            String textoHtml = "<html><font color='#555555'>" + jogo.getSelecaoA() + "</font> <b><font color='#111111'>" + jogo.getSiglaA() + "</font></b>&nbsp;&nbsp;&nbsp;<font color='#000000'><b>" + horaExibicao + "</b></font>&nbsp;&nbsp;&nbsp;<b><font color='#111111'>" + jogo.getSiglaB() + "</font></b> <font color='#555555'>" + jogo.getSelecaoB() + "</font></html>";
            lblEquipas.setText(textoHtml);
            lblDetalhes.setText(jogo.getFase() + " · " + jogo.getGrupo() + " · " + jogo.getEstadio() + " (" + jogo.getCidade() + ")");

            boolean jogoTerminado = false;
            try {
                java.time.format.DateTimeFormatter dateFormatter = new java.time.format.DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("EEEE d MMMM yyyy").toFormatter(new java.util.Locale("pt", "PT"));
                java.time.LocalDate dataJogo = java.time.LocalDate.parse(jogo.getData(), dateFormatter);
                java.time.LocalDate hoje = java.time.LocalDate.now();
                if (dataJogo.isBefore(hoje)) {
                    jogoTerminado = true;
                } else if (dataJogo.isEqual(hoje)) {
                    java.time.LocalTime horaJogo = java.time.LocalTime.parse(jogo.getHora(), java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                    if (java.time.LocalTime.now().isAfter(horaJogo.plusHours(2))) jogoTerminado = true;
                }
            } catch (Exception e) {
                jogoTerminado = jogo.isConcluido();
            }

            lblEstado.setVisible(jogoTerminado);

            if (isSelected) {
                setBackground(new Color(232, 240, 254));
                ((JPanel)getComponent(0)).setBackground(new Color(232, 240, 254));
                getComponent(1).setBackground(new Color(232, 240, 254));
            } else {
                setBackground(Color.WHITE);
                ((JPanel)getComponent(0)).setBackground(Color.WHITE);
                getComponent(1).setBackground(Color.WHITE);
            }
            return this;
        }
    }
}