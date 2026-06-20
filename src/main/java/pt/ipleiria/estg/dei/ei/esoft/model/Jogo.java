package pt.ipleiria.estg.dei.ei.esoft.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Jogo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String data;
    private String hora;
    private String fusoHorario;
    private String fase;
    private String grupo;
    private String estadio;
    private String cidade;

    private String selecaoA;
    private String siglaA;
    private String nacionalidadeSelecaoA;

    private String selecaoB;
    private String siglaB;
    private String nacionalidadeSelecaoB;

    private int golosA;
    private int golosB;

    private int posseA, posseB;
    private int rematesA, rematesB;
    private int cantosA, cantosB;
    private int faltasA, faltasB;
    private int capacidade;

    private boolean concluido;
    private List<Arbitro> equipaArbitragem;
    private List<String> autoresGolos = new ArrayList<>();
    private List<String> autoresAssistencias = new ArrayList<>();
    private List<String[]> eventosDoJogo = new ArrayList<>();

    public Jogo(String data, String hora, String fusoHorario, String fase, String grupo,
                String estadio, int capacidade, String cidade,
                String selecaoA, String siglaA, String nacionalidadeSelecaoA,
                String selecaoB, String siglaB, String nacionalidadeSelecaoB) {

        this.data = data;
        this.hora = hora;
        this.fusoHorario = fusoHorario;
        this.fase = fase;
        this.grupo = grupo;
        this.estadio = estadio;
        this.cidade = cidade;
        this.capacidade = capacidade;

        this.selecaoA = selecaoA;
        this.siglaA = siglaA;
        this.nacionalidadeSelecaoA = nacionalidadeSelecaoA;

        this.selecaoB = selecaoB;
        this.siglaB = siglaB;
        this.nacionalidadeSelecaoB = nacionalidadeSelecaoB;

        this.golosA = 0;
        this.golosB = 0;

        this.equipaArbitragem = new ArrayList<>();
        this.concluido = false;
    }

    public String getData() {
        return data;
    }

    public String getHora() {
        return hora;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public String getFase() {
        return fase;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getEstadio() {
        return estadio;
    }

    public String getCidade() {
        return cidade;
    }

    public String getSelecaoA() {
        return selecaoA;
    }

    public String getSiglaA() {
        return siglaA;
    }

    public String getNacionalidadeSelecaoA() {
        return nacionalidadeSelecaoA;
    }

    public String getSelecaoB() {
        return selecaoB;
    }

    public String getSiglaB() {
        return siglaB;
    }

    public String getNacionalidadeSelecaoB() {
        return nacionalidadeSelecaoB;
    }

    public int getGolosA() {
        return golosA;
    }

    public int getGolosB() {
        return golosB;
    }

    public void definirResultado(int golosA, int golosB) {
        if (golosA < 0 || golosB < 0) {
            throw new IllegalArgumentException("Os golos não podem ser negativos.");
        }

        this.golosA = golosA;
        this.golosB = golosB;
    }

    public boolean isConcluido() {
        return concluido;
    }

    public void setConcluido(boolean concluido) {
        this.concluido = concluido;
    }

    // Alias para ficar compatível com o ClassificacaoService
    public boolean isTerminado() {
        return concluido;
    }

    // Alias para ficar compatível com o ClassificacaoService
    public void setTerminado(boolean terminado) {
        this.concluido = terminado;
    }

    public List<Arbitro> getEquipaArbitragem() {
        return equipaArbitragem;
    }

    public void setEquipaArbitragem(List<Arbitro> equipa) {
        this.equipaArbitragem = equipa;
    }

    @Override
    public String toString() {
        return selecaoA + " vs " + selecaoB + " - " + data + " " + hora;
    }

    // Dentro da classe Jogo.java
    private String motm; // Adiciona este campo

    public String getMotm() {
        return motm;
    }

    public void setMotm(String motm) {
        this.motm = motm;
    }

    public void registarGoloComAssistencia(String nomeMarcador, String nomeAssistente) {
        if (nomeMarcador != null) this.autoresGolos.add(nomeMarcador);
        if (nomeAssistente != null) this.autoresAssistencias.add(nomeAssistente);
    }

    public List<String> getAutoresGolos() { return autoresGolos; }
    public List<String> getAutoresAssistencias() { return autoresAssistencias; }

    public int getPosseA() { return posseA; }
    public void setPosseA(int posseA) { this.posseA = posseA; }
    public int getPosseB() { return posseB; }
    public void setPosseB(int posseB) { this.posseB = posseB; }

    public int getRematesA() { return rematesA; }
    public void setRematesA(int rematesA) { this.rematesA = rematesA; }
    public int getRematesB() { return rematesB; }
    public void setRematesB(int rematesB) { this.rematesB = rematesB; }

    public int getCantosA() { return cantosA; }
    public void setCantosA(int cantosA) { this.cantosA = cantosA; }
    public int getCantosB() { return cantosB; }
    public void setCantosB(int cantosB) { this.cantosB = cantosB; }

    public int getFaltasA() { return faltasA; }
    public void setFaltasA(int faltasA) { this.faltasA = faltasA; }
    public int getFaltasB() { return faltasB; }
    public void setFaltasB(int faltasB) { this.faltasB = faltasB; }

    public List<String[]> getEventosDoJogo() { return eventosDoJogo; }
    public void setEventosDoJogo(List<String[]> eventos) { this.eventosDoJogo = eventos; }

    public int getCapacidade() { return capacidade; }
}