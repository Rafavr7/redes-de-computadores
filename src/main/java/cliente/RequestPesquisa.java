package cliente;

public class RequestPesquisa extends Request {
    private String criterio;
    
    public RequestPesquisa(char keyword, String criterio) {
        super(keyword);
        
        this.criterio = criterio;
    }
    
    public String getCriterio() {
        return criterio;
    }
}
