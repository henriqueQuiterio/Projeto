package pt.ipleiria.estg.dei.ei.esoft.model;

public class Classificacao {

    private Selecao selecao;
    private int jogos;
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golosMarcados;
    private int golosSofridos;
    private int pontos;

    public Classificacao(Selecao selecao) {
        this.selecao = selecao;
    }

    public void registarVitoria(int gm, int gs) {
        jogos++;
        vitorias++;
        golosMarcados += gm;
        golosSofridos += gs;
        pontos += 3;
    }

    public void registarEmpate(int gm, int gs) {
        jogos++;
        empates++;
        golosMarcados += gm;
        golosSofridos += gs;
        pontos += 1;
    }

    public void registarDerrota(int gm, int gs) {
        jogos++;
        derrotas++;
        golosMarcados += gm;
        golosSofridos += gs;
    }

    public int getDiferencaGolos() {
        return golosMarcados - golosSofridos;
    }

    public Selecao getSelecao() {
        return selecao;
    }

    public int getJogos() {
        return jogos;
    }

    public int getVitorias() {
        return vitorias;
    }

    public int getEmpates() {
        return empates;
    }

    public int getDerrotas() {
        return derrotas;
    }

    public int getGolosMarcados() {
        return golosMarcados;
    }

    public int getGolosSofridos() {
        return golosSofridos;
    }

    public int getPontos() {
        return pontos;
    }
}
