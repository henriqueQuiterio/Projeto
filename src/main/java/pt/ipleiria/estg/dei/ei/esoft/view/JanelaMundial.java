package pt.ipleiria.estg.dei.ei.esoft.view;

import pt.ipleiria.estg.dei.ei.esoft.control.MundialController;
import pt.ipleiria.estg.dei.ei.esoft.model.Arbitro;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogo;
import pt.ipleiria.estg.dei.ei.esoft.model.Jogador;
import pt.ipleiria.estg.dei.ei.esoft.model.Selecao;

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
    private JLabel lblSiglaA;
    private JTextField txtGolosB;
    private JLabel lblSiglaB;
    private JButton btnAdicionarEvento;
    private JTable tabelaEventos;
    private JLabel lblEstatisticaA;
    private JLabel lblEstatisticaB;
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
                cbMOTM, btnSubmeter, btnEditar, btnCancelarAlteracoes, tabelaEventos);

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

        JLabel lblTitulo = new JLabel("🏆 Sistema de Gestão do Mundial 2026");
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
            // Se o ficheiro já existe, lê o que gravaste e IGNERA a lista padrão antiga!
            controller.carregarDados();
        } else {

            // --- CALENDÁRIO COMPLETO ORIGINAL DA FIFA ---
            controller.adicionarJogo(new Jogo("Quinta-Feira 11 Junho 2026", "16:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio da Cidade do México", "Cidade do México", "México", "MEX", "Mexicana", "África do Sul", "RSA", "Sul-africana"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 12 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Guadalajara", "Guadalajara", "República da Coreia", "KOR", "Sul-coreana", "Tchéquia", "CZE", "Checa"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 12 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio de Toronto", "Toronto", "Canadá", "CAN", "Canadiana", "Bósnia e Herzegovina", "BIH", "Bósnia"));
            controller.adicionarJogo(new Jogo("Sábado 13 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio de Los Angeles", "Los Angeles", "EUA", "USA", "Americana", "Paraguai", "PAR", "Paraguaia"));
            controller.adicionarJogo(new Jogo("Sábado 13 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Catar", "QAT", "Catarina", "Suíça", "SUI", "Suíça"));
            controller.adicionarJogo(new Jogo("Sábado 13 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Brasil", "BR", "Brasileira", "Marrocos", "MAR", "Marroquina"));
            controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Boston", "Boston", "Haiti", "HAI", "Haitiana", "Escócia", "SCO", "Escocesa"));
            controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo D", "BC Place de Vancouver", "Vancouver", "Austrália", "AUS", "Australiana", "Turquia", "TUR", "Turca"));
            controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Houston", "Houston", "Alemanha", "GER", "Alemã", "Curaçau", "CUW", "Curaçalense"));
            controller.adicionarJogo(new Jogo("Domingo 14 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Dallas", "Dallas", "Holanda", "NED", "Holandesa", "Japão", "JPN", "Japonesa"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Filadélfia", "Filadélfia", "Costa do Marfim", "CIV", "Malandresa", "Equador", "ECU", "Equatoriana"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Monterrey", "Monterrey", "Suécia", "SWE", "Sueca", "Tunísia", "TUN", "Tunisina"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Atlanta", "Atlanta", "Espanha", "ESP", "Espanhola", "Cabo Verde", "CPV", "Cabo-verdiana"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Seattle", "Seattle", "Bélgica", "BEL", "Belga", "Egito", "EGY", "Egípcia"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 15 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Miami", "Miami", "Arábia Saudita", "KSA", "Saudita", "Uruguai", "URU", "Uruguaia"));
            controller.adicionarJogo(new Jogo("Terça-Feira 16 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Los Angeles", "Los Angeles", "RI do Irã", "IRN", "Iraniana", "Nova Zelândia", "NZL", "Neo-zelandesa"));
            controller.adicionarJogo(new Jogo("Terça-Feira 16 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "França", "FRA", "Francesa", "Senegal", "SEN", "Senegalesa"));
            controller.adicionarJogo(new Jogo("Terça-Feira 16 Junho 2026", "15:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Boston", "Boston", "Iraque", "IRQ", "Iraquiana", "Noruega", "NOR", "Norueguesa"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Kansas City", "Kansas City", "Argentina", "ARG", "Argentina", "Argélia", "ALG", "Argelina"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "05:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Áustria", "AUT", "Austríaca", "Jordânia", "JOR", "Jordana"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Houston", "Houston", "Portugal", "PT", "Portuguesa", "RD do Congo", "COD", "Congolesa"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 17 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Dallas", "Dallas", "Inglaterra", "ENG", "Inglesa", "Croácia", "CRO", "Croata"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Toronto", "Toronto", "Gana", "GHA", "Ganesa", "Panamá", "PAN", "Panamiana"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio da Cidade do México", "Cidade do México", "Uzbequistão", "UZB", "Usbeque", "Colômbia", "COL", "Colombiana"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "17:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Atlanta", "Atlanta", "Tchéquia", "CZE", "Checa", "África do Sul", "RSA", "Sul-africana"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio de Los Angeles", "Los Angeles", "Suíça", "SUI", "Suíça", "Bósnia e Herzegovina", "BIH", "Bósnia"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 18 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo B", "BC Place de Vancouver", "Vancouver", "Canadá", "CAN", "Canadiana", "Catar", "QAT", "Catarina"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 19 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Guadalajara", "Guadalajara", "México", "MEX", "Mexicana", "República da Coreia", "KOR", "Sul-coreana"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 19 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio de Seattle", "Seattle", "EUA", "USA", "Americana", "Austrália", "AUS", "Australiana"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 19 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Boston", "Boston", "Escócia", "SCO", "Escocesa", "Marrocos", "MAR", "Marroquina"));
            controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "01:30", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Filadélfia", "Filadélfia", "Brasil", "BR", "Brasileira", "Haiti", "HAI", "Haitiana"));
            controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Turquia", "TUR", "Turca", "Paraguai", "PAR", "Paraguaia"));
            controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Houston", "Houston", "Holanda", "NED", "Holandesa", "Suécia", "SWE", "Sueca"));
            controller.adicionarJogo(new Jogo("Sábado 20 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Toronto", "Toronto", "Alemanha", "GER", "Alemã", "Costa do Marfim", "CIV", "Malandresa"));
            controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Kansas City", "Kansas City", "Equador", "ECU", "Equatoriana", "Curaçau", "CUW", "Curaçalense"));
            controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "05:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Monterrey", "Monterrey", "Tunísia", "TUN", "Tunisina", "Japão", "JPN", "Japonesa"));
            controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "17:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Atlanta", "Atlanta", "Espanha", "ESP", "Espanhola", "Arábia Saudita", "KSA", "Saudita"));
            controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Los Angeles", "Los Angeles", "Bélgica", "BEL", "Belga", "RI do Irã", "IRN", "Iraniana"));
            controller.adicionarJogo(new Jogo("Domingo 21 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Miami", "Miami", "Uruguai", "URU", "Uruguaia", "Cabo Verde", "CPV", "Cabo-verdiana"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 22 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo G", "BC Place de Vancouver", "Vancouver", "Nova Zelândia", "NZL", "Neo-zelandesa", "Egito", "EGY", "Egípcia"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 22 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Dallas", "Dallas", "Argentina", "ARG", "Argentina", "Áustria", "AUT", "Austríaca"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 22 Junho 2026", "22:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Filadélfia", "Filadélfia", "França", "FRA", "Francesa", "Iraque", "IRQ", "Iraquiana"));
            controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Noruega", "NOR", "Norueguesa", "Senegal", "SEN", "Senegalesa"));
            controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Jordânia", "JOR", "Jordana", "Argélia", "ALG", "Argelina"));
            controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "18:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Houston", "Houston", "Portugal", "PT", "Portuguesa", "Uzbequistão", "UZB", "Usbeque"));
            controller.adicionarJogo(new Jogo("Terça-Feira 23 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Boston", "Boston", "Inglaterra", "ENG", "Inglesa", "Gana", "GHA", "Ganesa"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Toronto", "Toronto", "Panamá", "PAN", "Panamiana", "Croácia", "CRO", "Croata"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Guadalajara", "Guadalajara", "Colômbia", "COL", "Colombiana", "RD do Congo", "COD", "Congolesa"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo B", "BC Place de Vancouver", "Vancouver", "Suíça", "SUI", "Suíça", "Canadá", "CAN", "Canadiana"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo B", "Estádio de Seattle", "Seattle", "Bósnia e Herzegovina", "BIH", "Bósnia", "Catar", "QAT", "Catarina"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Miami", "Miami", "Escócia", "SCO", "Escocesa", "Brasil", "BR", "Brasileira"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 24 Junho 2026", "23:00", "GMT+0", "Primeira fase", "Grupo C", "Estádio de Atlanta", "Atlanta", "Marrocos", "MAR", "Marroquina", "Haiti", "HAI", "Haitiana"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio da Cidade do México", "Cidade do México", "Tchéquia", "CZE", "Checa", "México", "MEX", "Mexicana"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "02:00", "GMT+0", "Primeira fase", "Grupo A", "Estádio de Monterrey", "Monterrey", "África do Sul", "RSA", "Sul-africana", "República da Coreia", "KOR", "Sul-coreana"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Filadélfia", "Filadélfia", "Curaçau", "CUW", "Curaçalense", "Costa do Marfim", "CIV", "Malandresa"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 25 Junho 2026", "21:00", "GMT+0", "Primeira fase", "Grupo E", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Equador", "ECU", "Equatoriana", "Alemanha", "GER", "Alemã"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Dallas", "Dallas", "Japão", "JPN", "Japonesa", "Suécia", "SWE", "Sueca"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "00:00", "GMT+0", "Primeira fase", "Grupo F", "Estádio de Kansas City", "Kansas City", "Tunísia", "TUN", "Tunisina", "Holanda", "NED", "Holandesa"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio de Los Angeles", "Los Angeles", "Turquia", "TUR", "Turca", "EUA", "USA", "Americana"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo D", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "Paraguai", "PAR", "Paraguaia", "Austrália", "AUS", "Australiana"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Boston", "Boston", "Noruega", "NOR", "Norueguesa", "França", "FRA", "Francesa"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 26 Junho 2026", "20:00", "GMT+0", "Primeira fase", "Grupo I", "Estádio de Toronto", "Toronto", "Senegal", "SEN", "Senegalesa", "Iraque", "IRQ", "Iraquiana"));
            controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Houston", "Houston", "Cabo Verde", "CPV", "Cabo-verdiana", "Arábia Saudita", "KSA", "Saudita"));
            controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "01:00", "GMT+0", "Primeira fase", "Grupo H", "Estádio de Guadalajara", "Guadalajara", "Uruguai", "URU", "Uruguaia", "Espanha", "ESP", "Espanhola"));
            controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo G", "Estádio de Seattle", "Seattle", "Egito", "EGY", "Egípcia", "RI do Irã", "IRN", "Iraniana"));
            controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "04:00", "GMT+0", "Primeira fase", "Grupo G", "BC Place de Vancouver", "Vancouver", "Nova Zelândia", "NZL", "Neo-zelandesa", "Bélgica", "BEL", "Belga"));
            controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "22:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "Panamá", "PAN", "Panamiana", "Inglaterra", "ENG", "Inglesa"));
            controller.adicionarJogo(new Jogo("Sábado 27 Junho 2026", "22:00", "GMT+0", "Primeira fase", "Grupo L", "Estádio de Filadélfia", "Filadélfia", "Croácia", "CRO", "Croata", "Gana", "GHA", "Ganesa"));
            controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "00:30", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Miami", "Miami", "Colômbia", "COL", "Colombiana", "Portugal", "PT", "Portuguesa"));
            controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "00:30", "GMT+0", "Primeira fase", "Grupo K", "Estádio de Atlanta", "Atlanta", "RD do Congo", "COD", "Congolesa", "Uzbequistão", "UZB", "Usbeque"));
            controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Kansas City", "Kansas City", "Argélia", "ALG", "Argelina", "Áustria", "AUT", "Austríaca"));
            controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "03:00", "GMT+0", "Primeira fase", "Grupo J", "Estádio de Dallas", "Dallas", "Jordânia", "JOR", "Jordana", "Argentina", "ARG", "Argentina"));

            // Segundas de final
            controller.adicionarJogo(new Jogo("Domingo 28 Junho 2026", "20:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Los Angeles", "Los Angeles", "2A", "2A", "Apurada", "2B", "2B", "Apurada"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 29 Junho 2026", "18:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Houston", "Houston", "1C", "1C", "Apurada", "2F", "2F", "Apurada"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 29 Junho 2026", "21:30", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Boston", "Boston", "1E", "1E", "Apurada", "3ABCDF", "3AB", "Apurada"));
            controller.adicionarJogo(new Jogo("Terça-Feira 30 Junho 2026", "02:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Monterrey", "Monterrey", "1F", "1F", "Apurada", "2C", "2C", "Apurada"));
            controller.adicionarJogo(new Jogo("Terça-Feira 30 Junho 2026", "18:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Dallas", "Dallas", "2E", "2E", "Apurada", "2I", "2I", "Apurada"));
            controller.adicionarJogo(new Jogo("Terça-Feira 30 Junho 2026", "22:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "1I", "1I", "Apurada", "3CDFGH", "3CD", "Apurada"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 01 Julho 2026", "02:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio da Cidade do México", "Cidade do México", "1A", "1A", "Apurada", "3CEFHI", "3CE", "Apurada"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 01 Julho 2026", "17:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Atlanta", "Atlanta", "1L", "1L", "Apurada", "3EHIJK", "3EH", "Apurada"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 01 Julho 2026", "21:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Seattle", "Seattle", "1G", "1G", "Apurada", "3AEHIJ", "3AE", "Apurada"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 02 Julho 2026", "01:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio da Baía de São Francisco", "Área da baía de São Francisco", "1D", "1D", "Apurada", "3BEFIJ", "3BE", "Apurada"));
            controller.adicionarJogo(new Jogo("Quinta-Feira 02 Julho 2026", "20:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Los Angeles", "Los Angeles", "1H", "1H", "Apurada", "2J", "2J", "Apurada"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "00:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Toronto", "Toronto", "2K", "2K", "Apurada", "2L", "2L", "Apurada"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "04:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "BC Place de Vancouver", "Vancouver", "1B", "1B", "Apurada", "3EFGIJ", "3EF", "Apurada"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "19:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Dallas", "Dallas", "2D", "2D", "Apurada", "2G", "2G", "Apurada"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 03 Julho 2026", "23:00", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Miami", "Miami", "1J", "1J", "Apurada", "2H", "2H", "Apurada"));
            controller.adicionarJogo(new Jogo("Sábado 04 Julho 2026", "02:30", "GMT+0", "Segundas de final", "Dezasseis-avos", "Estádio de Kansas City", "Kansas City", "1K", "1K", "Apurada", "3DEIJL", "3DE", "Apurada"));

            // Oitavas de final
            controller.adicionarJogo(new Jogo("Sábado 04 Julho 2026", "18:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Houston", "Houston", "W73", "W73", "Vencedor", "W75", "W75", "Vencedor"));
            controller.adicionarJogo(new Jogo("Sábado 04 Julho 2026", "22:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Filadélfia", "Filadélfia", "W74", "W74", "Vencedor", "W77", "W77", "Vencedor"));
            controller.adicionarJogo(new Jogo("Domingo 05 Julho 2026", "21:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "W76", "W76", "Vencedor", "W78", "W78", "Vencedor"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 06 Julho 2026", "01:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio da Cidade do México", "Cidade do México", "W79", "W79", "Vencedor", "W80", "W80", "Vencedor"));
            controller.adicionarJogo(new Jogo("Segunda-Feira 06 Julho 2026", "20:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Dallas", "Dallas", "W83", "W83", "Vencedor", "W84", "W84", "Vencedor"));
            controller.adicionarJogo(new Jogo("Terça-Feira 07 Julho 2026", "01:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Seattle", "Seattle", "W81", "W81", "Vencedor", "W82", "W82", "Vencedor"));
            controller.adicionarJogo(new Jogo("Terça-Feira 07 Julho 2026", "17:00", "GMT+0", "Oitavas de final", "Oitavos", "Estádio de Atlanta", "Atlanta", "W86", "W86", "Vencedor", "W88", "W88", "Vencedor"));
            controller.adicionarJogo(new Jogo("Terça-Feira 07 Julho 2026", "21:00", "GMT+0", "Oitavas de final", "Oitavos", "BC Place de Vancouver", "Vancouver", "W85", "W85", "Vencedor", "W87", "W87", "Vencedor"));

            // Quartas de final
            controller.adicionarJogo(new Jogo("Quinta-Feira 09 Julho 2026", "21:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Boston", "Boston", "W89", "W89", "Vencedor", "W90", "W90", "Vencedor"));
            controller.adicionarJogo(new Jogo("Sexta-Feira 10 Julho 2026", "20:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Los Angeles", "Los Angeles", "W93", "W93", "Vencedor", "W94", "W94", "Vencedor"));
            controller.adicionarJogo(new Jogo("Sábado 11 Julho 2026", "22:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Miami", "Miami", "W91", "W91", "Vencedor", "W92", "W92", "Vencedor"));
            controller.adicionarJogo(new Jogo("Domingo 12 Julho 2026", "02:00", "GMT+0", "Quartas de final", "Quartos", "Estádio de Kansas City", "Kansas City", "W95", "W95", "Vencedor", "W96", "W96", "Vencedor"));

            // Semifinal
            controller.adicionarJogo(new Jogo("Terça-Feira 14 Julho 2026", "20:00", "GMT+0", "Semifinal", "Semi", "Estádio de Dallas", "Dallas", "W97", "W97", "Vencedor", "W98", "W98", "Vencedor"));
            controller.adicionarJogo(new Jogo("Quarta-Feira 15 Julho 2026", "20:00", "GMT+0", "Semifinal", "Semi", "Estádio de Atlanta", "Atlanta", "W99", "W99", "Vencedor", "W100", "W100", "Vencedor"));

            // Decisão do 3º lugar
            controller.adicionarJogo(new Jogo("Sábado 18 Julho 2026", "22:00", "GMT+0", "Decisão do 3º lugar", "3º Lugar", "Estádio de Miami", "Miami", "RU101", "RU1", "Derrotado", "RU102", "RU2", "Derrotado"));

            // Final
            controller.adicionarJogo(new Jogo("Domingo 19 Julho 2026", "20:00", "GMT+0", "Final", "Finalíssima", "Estádio de Nova York/Nova Jersey", "Nova Jersey", "W101", "W101", "Campeão", "W102", "W102", "Campeão"));

            // --- ÁRBITROS BASE DO SISTEMA ---
            popularArbitrosOficiais(controller);
        }
        SwingUtilities.invokeLater(() -> {
            new JanelaMundial(controller).setVisible(true);
        });
    }

    private static void popularArbitrosOficiais(MundialController controller) {
        // --- 52 ÁRBITROS PRINCIPAIS (Referees) ---
        controller.adicionarArbitro(new Arbitro("Abdulrahman Al-Jassim", "Catarina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Khalid Al-Turais", "Saudita", "Principal"));
        controller.adicionarArbitro(new Arbitro("Yusuke Araki", "Japonesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Omar Abdulkadir Artan", "Somali", "Principal"));
        controller.adicionarArbitro(new Arbitro("Pierre Atcho", "Gabonesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Iván Barton", "Salvadorenha", "Principal"));
        controller.adicionarArbitro(new Arbitro("Dahane Beida", "Mauritana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Juan Gabriel Benítez", "Paraguaia", "Principal"));
        controller.adicionarArbitro(new Arbitro("Juan Calderón", "Costa-riquenha", "Principal"));
        controller.adicionarArbitro(new Arbitro("Raphael Claus", "Brasileira", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ismail Elfath", "Americana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Espen Eskås", "Norueguesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Alireza Faghani", "Australiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Yael Falcón Pérez", "Argentina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Drew Fischer", "Canadiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Cristian Garay", "Chilena", "Principal"));
        controller.adicionarArbitro(new Arbitro("Katia García", "Mexicana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Mustapha Ghorbal", "Argelina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Alejandro Hernández", "Espanhola", "Principal"));
        controller.adicionarArbitro(new Arbitro("Darío Herrera", "Argentina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Jalal Jayed", "Marroquina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Campbell-Kirk Kawana-Waugh", "Neo-zelandesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("István Kovács", "Romena", "Principal"));
        controller.adicionarArbitro(new Arbitro("François Letexier", "Francesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ma Ning", "Chinesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Adham Makhadmeh", "Jordana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Danny Makkelie", "Holandesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Szymon Marciniak", "Polaca", "Principal"));
        controller.adicionarArbitro(new Arbitro("Maurizio Mariani", "Italiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Héctor Said Martínez", "Hondurenha", "Principal"));
        controller.adicionarArbitro(new Arbitro("Amin Mohamed", "Egípcia", "Principal"));
        controller.adicionarArbitro(new Arbitro("Oshane Nation", "Jamaicana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Glenn Nyberg", "Sueca", "Principal"));
        controller.adicionarArbitro(new Arbitro("Michael Oliver", "Inglesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Omar Al Ali", "Emirense", "Principal"));
        controller.adicionarArbitro(new Arbitro("Kevin Ortega", "Peruana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Tori Penso", "Americana", "Principal"));
        controller.adicionarArbitro(new Arbitro("João Pinheiro", "Portuguesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ramon Abatti", "Brasileira", "Principal"));
        controller.adicionarArbitro(new Arbitro("César Ramos", "Mexicana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Andrés Rojas", "Colombiana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Sandro Schärer", "Suíça", "Principal"));
        controller.adicionarArbitro(new Arbitro("Ilgiz Tantashev", "Usbeque", "Principal"));
        controller.adicionarArbitro(new Arbitro("Anthony Taylor", "Inglesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Gustavo Tejera", "Uruguaia", "Principal"));
        controller.adicionarArbitro(new Arbitro("Facundo Tello", "Argentina", "Principal"));
        controller.adicionarArbitro(new Arbitro("Abongile Tom", "Sul-africana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Clément Turpin", "Francesa", "Principal"));
        controller.adicionarArbitro(new Arbitro("Jesús Valenzuela", "Venezuelana", "Principal"));
        controller.adicionarArbitro(new Arbitro("Slavko Vinčić", "Eslovena", "Principal"));
        controller.adicionarArbitro(new Arbitro("Wilton Sampaio", "Brasileira", "Principal"));
        controller.adicionarArbitro(new Arbitro("Felix Zwayer", "Alemã", "Principal"));

        // --- 88 ÁRBITROS ASSISTENTES (Assistant Referees) ---
        controller.adicionarArbitro(new Arbitro("Amos Abeigne", "Gabonesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("George Lakrindis", "Australiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mahmoud Abouelregal", "Egípcia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("James Lindsay", "Jamaicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mostafa Akarkad", "Marroquina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Tomasz Listkiewicz", "Polaca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mohammed Al Abakry", "Saudita", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Walter López", "Hondurenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mohamed Al Hammadi", "Emirense", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Luciano Maia", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mohammad Al Kalaf", "Jordana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("James Mainwaring", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Saoud Al Maqaleh", "Catarina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mihai Marica", "Romena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Taleb Al Marri", "Catarina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Brooke Mayo", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Ahmad Al Roalle", "Jordana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jun Mihara", "Japonesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Lyes Arfa", "Canadiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Juan Carlos Mora", "Costa-riquenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Kyle Atkins", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("David Morán", "Salvadorenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Carlos Barreiro", "Uruguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Tulio Moreno", "Venezuelana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Micheal Barwegen", "Canadiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Alberto Morín", "Mexicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Isaak Bashevkin", "Norueguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Cyril Mugnier", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Adam Kupsik", "Polaca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("José Enrique Naranjo Pérez", "Espanhola", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mahbod Beigi", "Sueca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Cristian Navarro", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Juan Pablo Belatti", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Kathryn Nesbitt", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Gary Beswick", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Elvis Noupue", "Camandresa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Daniele Bindoni", "Italiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Adam Nunn", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Marco Bisguerra", "Mexicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Michael Orue", "Peruana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Zakaria Brinsi", "Marroquina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Benjamin Pages", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Bruno Boschilia", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Corey Parker", "Americana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Bruno Pires", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Antonio Pupiro", "Salvadorenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Stuart Burt", "Inglesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Rafael Alves", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Eduardo Cardozo", "Paraguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mehdi Rahmouni", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Gabriel Chade", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Christian Ramírez", "Hondurenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Danilo Manis", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Sandra Ramírez", "Mexicana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Nicolas Danos", "Francesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("José Retamal", "Chilena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Stéphane De Almeida", "Suíça", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Miguel Rocha", "Portuguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jan de Vries", "Holandesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Facundo Rodríguez", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Maximiliano Del Yesso", "Argentina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Milcíades Saldívar", "Paraguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Christian Dietz", "Alemã", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Diego Sánchez", "Costa-riquenha", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Boris Ditsoga", "Gabonesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Zakhele Siwela", "Sul-africana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jan Erik Engan", "Norueguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Andreas Söderkvist", "Sueca", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Rodrigo Figueiredo", "Brasileira", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Hessel Steegstra", "Holandesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Timur Gaynullin", "Usbeque", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Nicolás Tarán", "Uruguaia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Mokrane Gourari", "Argelina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Alberto Tegoni", "Italiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Alexander Guzmán", "Colombiana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Isaac Trevis", "Neo-zelandesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Ahmed Hossam Taha", "Egípcia", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Andrey Tsapenko", "Usbeque", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jerson Santos", "Angolana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Ferencz Tunyogi", "Romena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Bruno Jesus", "Portuguesa", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Jorge Urrego", "Venezuelana", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Robert Kempter", "Alemã", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Caleb Wales", "Trindadense", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Tomaž Klančnik", "Eslovena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Abbes Akram Zerhouni", "Argelina", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Andraž Kovačič", "Eslovena", "Assistente"));
        controller.adicionarArbitro(new Arbitro("Zhou Fei", "Chinesa", "Assistente"));

        // --- 30 VIDEOÁRBITROS (Video Match Officials - VAR) ---
        controller.adicionarArbitro(new Arbitro("Khamis Al-Marri", "Catarina", "VAR"));
        controller.adicionarArbitro(new Arbitro("Abdullah Alshehri", "Saudita", "VAR"));
        controller.adicionarArbitro(new Arbitro("Mahmoud Ashour", "Egípcia", "VAR"));
        controller.adicionarArbitro(new Arbitro("Ivan Bebek", "Croata", "VAR"));
        controller.adicionarArbitro(new Arbitro("Jérôme Brisard", "Francesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Bastian Dankert", "Alemã", "VAR"));
        controller.adicionarArbitro(new Arbitro("Carlos del Cerro Grande", "Espanhola", "VAR"));
        controller.adicionarArbitro(new Arbitro("Willy Delajod", "Francesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Marco Di Bello", "Italiana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Joe Dickerson", "Americana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Hamza El Fariq", "Marroquina", "VAR"));
        controller.adicionarArbitro(new Arbitro("Shaun Evans", "Australiana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Fu Ming", "Chinesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Nicolás Gallo", "Colombiana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Antonio García", "Uruguaia", "VAR"));
        controller.adicionarArbitro(new Arbitro("Jarred Gillett", "Inglesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Leodán González", "Uruguaia", "VAR"));
        controller.adicionarArbitro(new Arbitro("Tatiana Guzmán", "Nicaraguense", "VAR"));
        controller.adicionarArbitro(new Arbitro("Dennis Higler", "Holandesa", "VAR"));
        controller.adicionarArbitro(new Arbitro("Tomasz Kwiatkowski", "Polaca", "VAR"));
        controller.adicionarArbitro(new Arbitro("Juan Lara", "Chilena", "VAR"));
        controller.adicionarArbitro(new Arbitro("Hernán Mastrángelo", "Argentina", "VAR"));
        controller.adicionarArbitro(new Arbitro("Erick Miranda", "Mexicana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Mohammed Obaid Khadim", "Emirense", "VAR"));
        controller.adicionarArbitro(new Arbitro("Guillermo Pacheco", "Mexicana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Fedayi San", "Suíça", "VAR"));
        controller.adicionarArbitro(new Arbitro("Juan Soto", "Venezuelana", "VAR"));
        controller.adicionarArbitro(new Arbitro("Rodolpho Toski", "Brasileira", "VAR"));
        controller.adicionarArbitro(new Arbitro("Bram Van Driessche", "Belga", "VAR"));
        controller.adicionarArbitro(new Arbitro("Armando Villarreal", "Americana", "VAR"));
    }

}