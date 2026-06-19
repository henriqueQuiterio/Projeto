package pt.ipleiria.estg.dei.ei.esoft.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Selecao implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pais;
    private int ranking;
    private int participacoes;
    private String grupo;
    private Estadia estadia;
    private List<Jogador> jogadores;

    public Selecao(String pais, int ranking, int participacoes, String grupo) {
        this.pais = pais;
        this.ranking = ranking;
        this.participacoes = participacoes;
        this.grupo = grupo;
        this.estadia = new Estadia("", "");
        this.jogadores = new ArrayList<>();
    }

    public String getPais() {
        return pais;
    }

    public int getRanking() {
        return ranking;
    }

    public int getParticipacoes() {
        return participacoes;
    }

    public String getGrupo() {
        return grupo;
    }

    public Estadia getEstadia() {
        return estadia;
    }

    public void setEstadia(Estadia estadia) {
        this.estadia = estadia;
    }

    public List<Jogador> getJogadores() {
        return jogadores;
    }

    public void adicionarJogador(Jogador jogador) {
        jogadores.add(jogador);
    }

    public void removerJogador(Jogador jogador) {
        jogadores.remove(jogador);
    }

    @Override
    public String toString() {
        return pais;
    }
}
