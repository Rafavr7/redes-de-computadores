package cliente;

public class RequestLeilao extends Request{
    private int ano;
    private int mes;
    private int dia;
    private int hora;
    private int min;
    private String descricao;
    private int valor;
    
    public RequestLeilao(char keyWord, int ano, int mes, int dia, int hora, int min,
            String descricao, int valor) {
        
        super(keyWord);
        
        this.ano = ano;
        this.dia = dia;
        this.mes = mes;
        this.hora = hora;
        this.min = min;
        this.descricao = descricao;
        this.valor = valor;
    }

    public int getAno() {
        return ano;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getDia() {
        return dia;
    }

    public int getHora() {
        return hora;
    }

    public int getMes() {
        return mes;
    }

    public int getMin() {
        return min;
    }

    public int getValor() {
        return valor;
    }
    
    private String buildDataString() {
        StringBuilder data = new StringBuilder();
        data.append(dia)
                .append("/")
                .append(mes)
                .append("/")
                .append(ano)
                .append(" - ")
                .append(hora)
                .append(":")
                .append(min);
        
        return data.toString();
    }
    
    @Override
    public String toString() {
        return "{" + buildDataString() + ", " + descricao + ", "
                + valor + "}";
    }
}
