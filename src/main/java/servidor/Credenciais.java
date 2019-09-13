package servidor;

import java.io.Serializable;

public class Credenciais implements Serializable {
    String username;
    String password;
    int plafondInicial;
    public Credenciais(String username, String password, int plafondInicial) {
        this.password = password;
        this.username = username;
        this.plafondInicial = plafondInicial;
    }
    public Credenciais(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getPlafondInicial() {
        return plafondInicial;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String toString() {
        return "username: " + username +" password: " + password;
    }
}
