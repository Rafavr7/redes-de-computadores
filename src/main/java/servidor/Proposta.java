package servidor;

import licitador.Licitador;

import java.io.Serializable;

public class Proposta implements Serializable {
    Licitador licitador;
    int valor;

    public Proposta(Licitador licitador, int valor) {
        this.licitador = licitador;
        this.valor = valor;
    }

    public Licitador getLicitador() {
        return licitador;
    }
    public int getValor() {
        return valor;
    }
}
