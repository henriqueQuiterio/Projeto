package pt.ipleiria.estg.dei.ei.esoft.model;

import java.io.Serializable;
import java.util.UUID;

public class Bilhete implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private Jogo jogo;
    private String setor;

    public Bilhete(Jogo jogo, String setor) {
        this.codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.jogo = jogo;
        this.setor = setor;
    }

    public String getCodigo() {
        return codigo;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public String getSetor() {
        return setor;
    }

    @Override
    public String toString() {
        return codigo + " - " + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + " (" + setor + ")";
    }
}