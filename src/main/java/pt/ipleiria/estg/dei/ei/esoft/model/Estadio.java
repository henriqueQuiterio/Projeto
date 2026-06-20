package pt.ipleiria.estg.dei.ei.esoft.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Estadio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private int capacidade;
    private ArrayList<String> setores;

    public Estadio(String nome, int capacidade) {
        this.nome = nome;
        this.capacidade = capacidade;
        this.setores = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public ArrayList<String> getSetores() {
        return setores;
    }

    public void setSetores(ArrayList<String> setores) {
        this.setores = setores;
    }

    public void adicionarSetor(String setor) {
        setores.add(setor);
    }

    @Override
    public String toString() {
        return nome + " (Cap: " + capacidade + ")";
    }
}