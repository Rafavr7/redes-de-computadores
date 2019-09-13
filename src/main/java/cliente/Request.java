package cliente;

import java.io.Serializable;

public class Request implements Serializable  {
    char keyWord;

    public Request() { }

    public Request(char keyWord) {
        this.keyWord = keyWord;
    }

    public char getKeyWord() {
        return keyWord;
    }
}
