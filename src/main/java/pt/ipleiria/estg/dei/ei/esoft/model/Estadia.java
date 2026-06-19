package pt.ipleiria.estg.dei.ei.esoft.model;

public class Estadia {

    private String centroTreino;
    private String hotel;

    public Estadia(String centroTreino, String hotel) {
        this.centroTreino = centroTreino;
        this.hotel = hotel;
    }

    public String getCentroTreino() {
        return centroTreino;
    }

    public void setCentroTreino(String centroTreino) {
        this.centroTreino = centroTreino;
    }

    public String getHotel() {
        return hotel;
    }

    public void setHotel(String hotel) {
        this.hotel = hotel;
    }
}
