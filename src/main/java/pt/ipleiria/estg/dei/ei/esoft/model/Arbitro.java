package pt.ipleiria.estg.dei.ei.esoft.model;

public class Arbitro {
    private String nome;
    private String nacionalidade;
    private String funcao; // Principal, Assistente, VAR

    public Arbitro(String nome, String nacionalidade, String funcao) {
        this.nome = nome;
        this.nacionalidade = nacionalidade;
        this.funcao = funcao;
    }

    public String getNome() { return nome; }
    public String getNacionalidade() { return nacionalidade; }
    public String getFuncao() { return funcao; }

    @Override
    public String toString() {
        return nome + " (" + nacionalidade + " - " + funcao + ")";
    }
}
