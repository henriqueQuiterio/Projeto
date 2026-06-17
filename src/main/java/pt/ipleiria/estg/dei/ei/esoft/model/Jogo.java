package pt.ipleiria.estg.dei.ei.esoft.model;

import java.util.ArrayList;
import java.util.List;

public class Jogo {
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

    private boolean concluido;
    private List<Arbitro> equipaArbitragem;

    public Jogo(String data, String hora, String fusoHorario, String fase, String grupo,
                String estadio, String cidade, String selecaoA, String siglaA, String nacionalidadeSelecaoA,
                String selecaoB, String siglaB, String nacionalidadeSelecaoB) {
        this.data = data;
        this.hora = hora;
        this.fusoHorario = fusoHorario;
        this.fase = fase;
        this.grupo = grupo;
        this.estadio = estadio;
        this.cidade = cidade;
        this.selecaoA = selecaoA;
        this.siglaA = siglaA;
        this.nacionalidadeSelecaoA = nacionalidadeSelecaoA;
        this.selecaoB = selecaoB;
        this.siglaB = siglaB;
        this.nacionalidadeSelecaoB = nacionalidadeSelecaoB;
        this.equipaArbitragem = new ArrayList<>();
        this.concluido = false;
    }

    public String getData() { return data; }
    public String getHora() { return hora; }
    public String getFusoHorario() { return fusoHorario; }
    public String getFase() { return fase; }
    public String getGrupo() { return grupo; }
    public String getEstadio() { return estadio; }
    public String getCidade() { return cidade; }
    public String getSelecaoA() { return selecaoA; }
    public String getSiglaA() { return siglaA; }
    public String getNacionalidadeSelecaoA() { return nacionalidadeSelecaoA; }
    public String getSelecaoB() { return selecaoB; }
    public String getSiglaB() { return siglaB; }
    public String getNacionalidadeSelecaoB() { return nacionalidadeSelecaoB; }
    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }
    public List<Arbitro> getEquipaArbitragem() { return equipaArbitragem; }
    public void setEquipaArbitragem(List<Arbitro> equipa) { this.equipaArbitragem = equipa; }

}