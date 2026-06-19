package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;

import javax.swing.*;
import java.awt.*;

public class JanelaMundial extends JFrame {
    // --- COMPONENTES DA ÁRVORE DO .FORM (Mapeamento exato para evitar erros de Binding) ---
    private JPanel painelPrincipal;
    private JTabbedPane abasPrincipais;

    // Aba 1: Calendário
    private JPanel abaCalendario;
    private JComboBox<String> comboOrdemData;
    private JComboBox<String> comboFusoHorario;
    private JList<Jogo> listaCartoesJogos;
    private DefaultListModel<Jogo> modeloListaJogos;

    // Aba 2: Alocação (Hierarquia exata da imagem c1db63.png)
    private JPanel abaAlocacao;
    private JPanel painelEsquerdoGestaoArbitragem; // Vinculado ao form!
    private JCheckBox chkSemArbitros;
    private JList<Jogo> listaJogosAlocacao;
    private DefaultListModel<Jogo> modeloListaJogosGestaoArbitragem;

    private JPanel painelFormularioGestaoArbitragem; // Vinculado ao form!
    private JComboBox<Arbitro> comboPrincipal;
    private JComboBox<Arbitro> comboAssistente1;
    private JComboBox<Arbitro> comboAssistente2;
    private JComboBox<Arbitro> comboVar;
    private JButton btnConfirmarEquipa;
    private JLabel lblStatusValidacao;
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

    private JPanel abaResultados;
    private JPanel painelResultados;
    private JList listaJogosResultados;
    private JTextField txtGolosA;
    JLabel lblSiglaA;
    private JTextField txtGolosB;
    JLabel lblSiglaB;
    JButton btnAdicionarEvento;
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

    private MundialController controller;
    private boolean acabouDeAlocarComSucesso = false;
    private javax.swing.event.ListSelectionListener listenerJogos;

    public JanelaMundial(MundialController controller) {
        this.controller = controller;

        setTitle("Sistema de Gestão do Mundial 2026");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Inicialização do ContentPane do teu .form do IntelliJ
        setContentPane(painelPrincipal);
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null);

        // Estiliza a barra de cima
        estilizarLayoutGlobal();

        // 1. Instanciar os teus painéis limpos
        PainelCalendario abaCalendarioLimpa = new PainelCalendario(controller);
        PainelGestaoArbitragem abaArbitragemLimpa = new PainelGestaoArbitragem(controller, abaCalendarioLimpa);
        PainelEquipas abaEquipasLimpa = new PainelEquipas(controller);
        PainelResultados abaResultadosLogica = new PainelResultados(controller,
                listaJogosResultados, txtGolosA, txtGolosB, txtPosseA, txtPosseB,
                txtRematesA, txtRematesB, txtCantosA, txtCantosB, txtFaltasA, txtFaltasB,
                cbMOTM, btnSubmeter, btnEditar, btnCancelarAlteracoes, tabelaEventos, abaCalendarioLimpa);

        // 2. Acoplar ao TabbedPane do ecrã
        abasPrincipais.removeAll(); // Limpa as abas em branco padrão do editor gráfico
        abasPrincipais.addTab("Consultar Calendário", abaCalendarioLimpa);
        abasPrincipais.addTab("Gestão de Arbitragem", abaArbitragemLimpa);
        abasPrincipais.addTab("Equipas", abaEquipasLimpa);
        abasPrincipais.addTab("Resultados", abaResultados);

        // Listener controlado de fecho
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println("Janela a fechar... A salvaguardar dados...");
                controller.guardarDados();
                System.exit(0);
            }
        });
    }

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

        // 2. Arranque da Interface Gráfica
        java.awt.EventQueue.invokeLater(() -> {
            new JanelaMundial(controller).setVisible(true);
        });
    }
}