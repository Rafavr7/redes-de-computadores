package cliente;

public class RequestLicitacao extends Request {
    private int idLicitacao;
    private int valorLicitacao;
    
    public RequestLicitacao(char keyWord, int idLicitacao, int valorLicitacao) {
        super(keyWord);
        
        this.idLicitacao = idLicitacao;
        this.valorLicitacao = valorLicitacao;
    }

    public int getIdLicitacao() {
        return idLicitacao;
    }

    public int getValorLicitacao() {
        return valorLicitacao;
    }
    
    @Override
    public String toString() {
        return "{" + idLicitacao + ", " + valorLicitacao + "}";
    }
}
