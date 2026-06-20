package pt.ipleiria.estg.dei.ei.esoft.model;

import java.io.Serializable;
import java.util.UUID;

public class Bilhete implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private Jogo jogo;
    private String setor;
    private String lugar;

    public Bilhete(Jogo jogo, String setor, String lugar) {
        this.codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.jogo = jogo;
        this.setor = setor;
        this.lugar = lugar != null ? lugar : "Lugar Livre";
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

    public String getLugar() { // <-- NOVO GETTER
        return lugar;
    }

    @Override
    public String toString() {
        return codigo + " - " + jogo.getSelecaoA() + " vs " + jogo.getSelecaoB() + " (" + setor + " - " + lugar + ")";
    }
}