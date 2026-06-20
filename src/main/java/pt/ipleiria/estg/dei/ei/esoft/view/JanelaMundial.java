package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import javax.swing.*;
import java.awt.*;

public class JanelaMundial extends JFrame {
    // --- COMPONENTES DA ÁRVORE DO .FORM ---
    private JPanel painelPrincipal;
    private JTabbedPane abasPrincipais;

    // Aba 1: Calendário
    private JPanel abaCalendario;
    private JComboBox<String> comboOrdemData;
    private JComboBox<String> comboFusoHorario;
    private JList<Jogo> listaCartoesJogos;
    private DefaultListModel<Jogo> modeloListaJogos;

    // Aba 2: Alocação
    private JPanel abaAlocacao;
    private JPanel painelEsquerdoGestaoArbitragem;
    private JCheckBox chkSemArbitros;
    private JList<Jogo> listaJogosAlocacao;
    private DefaultListModel<Jogo> modeloListaJogosGestaoArbitragem;

    private JPanel painelFormularioGestaoArbitragem;
    private JComboBox<Arbitro> comboPrincipal;
    private JComboBox<Arbitro> comboAssistente1;
    private JComboBox<Arbitro> comboAssistente2;
    private JComboBox<Arbitro> comboVar;
    private JButton btnConfirmarEquipa;
    private JLabel lblStatusValidacao;

    // Aba Equipas
    private JPanel abaSelecoes;
    private JSplitPane splitPrincipal;
    private JPanel pnlEsquerdo;
    private JTextField txtPesquisa;
    private JList lstSelecoes;
    private JPanel pnlInfoSelecao;
    private JLabel lblRanking;
    private JLabel lblParticipacoes;
    private JLabel lblGrupo;
    private JSplitPane splitDireita;
    private JPanel pnlCentro;
    private JTextField txtCentroTreino;
    private JTextField txtHotel;
    private JButton btnGuardarAlteracoes;
    private JPanel pnlDireita;
    private JButton btnAdicionarJogador;
    private JTable tblJogadores;

    // Aba Resultados
    private JPanel abaResultados;
    private JPanel painelResultados;
    private JList listaJogosResultados;
    private JTextField txtGolosA;
    JLabel lblSiglaA;
    private JTextField txtGolosB;
    JLabel lblSiglaB;
    JButton btnAdicionarEvento;
    JButton btnApagarEvento;
    private JTable tabelaEventos;
    JLabel lblEstatisticaA;
    JLabel lblEstatisticaB;
    private JTextField txtPosseA;
    private JTextField txtPosseB;
    private JTextField txtRematesA;
    private JTextField txtRematesB;
    private JTextField txtCantosA;
    private JTextField txtCantosB;
    private JTextField txtFaltasA;
    private JTextField txtFaltasB;
    private JButton btnSubmeter;
    private JButton btnEditar;
    private JButton btnCancelarAlteracoes;
    private JComboBox cbMOTM;

    // Variáveis da classificação para manter o Binding do .form intacto
    private JPanel abaClassificacao;
    private JSplitPane splitClassificacaoPrincipal;
    private JPanel pnlClassificacaoEsquerda;
    private JPanel pnlClassificacaoDireita;
    private JButton btnVerFaseFinal;
    private JPanel pnlMelhoresMarcadores;
    private JPanel pnlLideresAssistencias;
    private JPanel abaBilhetes;
    private JList list1;
    private JSpinner spinner1;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JButton btnGerarBilhetes;
    private JTable table1;
    private JPanel painelBilhetesEmitidos;
    private JPanel painelFormularioBilhetes;
    private JPanel painelJogoEstadio;
    private JLabel lblEstadio;
    private JLabel lblInfos;

    private MundialController controller;
    private PainelClassificacao abaClassificacaoLimpa;

    public JanelaMundial(MundialController controller) {
        this.controller = controller;

        setTitle("Sistema de Gestão do Mundial 2026");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setContentPane(painelPrincipal);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);

        estilizarLayoutGlobal();

        // 1. Instanciar os painéis modulares
        PainelCalendario abaCalendarioLimpa = new PainelCalendario(controller);
        PainelGestaoArbitragem abaArbitragemLimpa = new PainelGestaoArbitragem(controller, abaCalendarioLimpa);
        PainelEquipas abaEquipasLimpa = new PainelEquipas(controller);

        // Injetar o novo PainelClassificacao dentro do contentor oficial para o manter visível
        abaClassificacao.setLayout(new BorderLayout());
        this.abaClassificacaoLimpa = new PainelClassificacao(controller);
        abaClassificacao.add(abaClassificacaoLimpa, BorderLayout.CENTER);

        PainelResultados abaResultadosLogica = new PainelResultados(
                controller,
                listaJogosResultados,
                txtGolosA,
                txtGolosB,
                txtPosseA,
                txtPosseB,
                txtRematesA,
                txtRematesB,
                txtCantosA,
                txtCantosB,
                txtFaltasA,
                txtFaltasB,
                cbMOTM,
                btnSubmeter,
                btnEditar,
                btnCancelarAlteracoes,
                tabelaEventos,
                abaCalendarioLimpa,
                btnApagarEvento
        );
        new PainelBilhetes(
                controller,
                list1,
                spinner1,
                comboBox1, // Setor
                comboBox2, // Lugar
                btnGerarBilhetes,
                table1,
                lblEstadio,
                lblInfos
        );

        // 2. Acoplar de forma limpa ao TabbedPane usando as abas geridas pelo .form
        abasPrincipais.removeAll();
        abasPrincipais.addTab("Calendário", abaCalendarioLimpa);
        abasPrincipais.addTab("Arbitragem", abaArbitragemLimpa);
        abasPrincipais.addTab("Equipas", abaEquipasLimpa);
        abasPrincipais.addTab("Resultados", abaResultados);
        abasPrincipais.addTab("Classificação", abaClassificacao);
        abasPrincipais.addTab("Bilhetes", abaBilhetes);

        // 3. Listener focado em atualizar os dados dinâmicos do novo painel
        abasPrincipais.addChangeListener(e -> {
            if (abasPrincipais.getSelectedComponent() == abaClassificacao) {
                abaClassificacaoLimpa.atualizarInterfaceDinamica();
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println("Janela a fechar... A salvaguardar dados...");
                controller.guardarDados();
                System.exit(0);
            }
        });
    }

    public void atualizarAbaClassificacao() {
        if (abaClassificacaoLimpa != null) {
            abaClassificacaoLimpa.atualizarInterfaceDinamica();
        }
    }

    // 🌟 CORRIGIDO: Removido o erro de digitação "voidGrid" para "void"
    private void estilizarLayoutGlobal() {
        JPanel painelTopoAzul = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        painelTopoAzul.setBackground(new Color(25, 118, 210));

        JLabel lblTitulo = new JLabel("Sistema de Gestão do Mundial 2026");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        painelTopoAzul.add(lblTitulo);

        painelPrincipal.setLayout(new BorderLayout());
        painelPrincipal.add(painelTopoAzul, BorderLayout.NORTH);
        painelPrincipal.add(abasPrincipais, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        MundialController controller = new MundialController();
        java.io.File ficheiro = new java.io.File("dados_mundial.dat");

        if (ficheiro.exists()) {
            controller.carregarDados();
        } else {
            controller.inicializarSelecoes();
            controller.inicializarArbitros(controller);
            controller.inicializarJogos(controller);
            controller.guardarDados();
        }

        java.awt.EventQueue.invokeLater(() -> {
            new JanelaMundial(controller).setVisible(true);
        });
    }
}
