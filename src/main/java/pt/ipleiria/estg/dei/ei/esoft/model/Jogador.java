package pt.ipleiria.estg.dei.ei.esoft.model;

public class Jogador {

    private int numero;
    private String nome;
    private String posicao;
    private int golos;
    private int assistencias;

    public Jogador(int numero, String nome, String posicao) {
        this.numero = numero;
        this.nome = nome;
        this.posicao = posicao;
        this.golos = 0;
        this.assistencias = 0;
    }

    public int getNumero() {
        return numero;
    }

    public String getNome() {
        return nome;
    }

    public String getPosicao() {
        return posicao;
    }

    public int getGolos() {
        return golos;
    }

    public int getAssistencias() {
        return assistencias;
    }

    public void adicionarGolo() {
        golos++;
    }

    public void adicionarAssistencia() {
        assistencias++;
    }

    @Override
    public String toString() {
        return numero + " - " + nome + " (" + posicao + ")";
    }
}
