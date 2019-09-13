package licitador;

import java.io.Serializable;
import java.net.InetAddress;

public class Licitador implements Serializable {

    String username;
    String password;
    int plafond;
    InetAddress address;

    public Licitador(String username, String password, int plafond, InetAddress address) {
        this.username = username;
        this.password = password;
        this.plafond = plafond;
        this.address = address;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getPlafond() {
        return plafond;
    }
    public InetAddress getAddress() {
        return address;
    }
    
    public void setPlafond(int plafond) {
        this.plafond = plafond;
    }
    public void setAddress(InetAddress address) {
        this.address = address;
    }
    public void reporPLafond(int i) {
        plafond = plafond + i;
    }
    public void retirarPlafond(int i) {
        plafond = plafond - i;
    }
    public String toString() {
        return username + " " + password + " " + plafond;
    }

    public boolean equals(Licitador licitador) {
        return username.equals(licitador.getUsername()) &&
               password.equals(licitador.getPassword()) &&
               plafond == licitador.getPlafond();
    }
}
