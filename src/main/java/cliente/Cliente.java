package cliente;

import servidor.Credenciais;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Cliente{
    private String ip;
    private int porta;

    Socket clientSocket;
    ObjectOutputStream out;
    DatagramSocket socket;
    Scanner teclado;

    private int idLeilao;
    private int valorInicial;
    private int valorLicitacoes;
    private int nrPropostas;
    private String username;
    private String password;

    public Cliente(String ip, int porta) {
        this.ip = ip;
        this.porta = porta;
        teclado = new Scanner(System.in);
    }
    
    public Cliente(String ip, int porta, int idLeilao, int valorInicial, int valorLicitacoes, int nrPropostas, String user, String pass) {
        this.ip = ip;
        this.porta = porta;
        this.idLeilao = idLeilao;
        this.valorInicial = valorInicial;
        this.valorLicitacoes = valorLicitacoes;
        this.nrPropostas = nrPropostas;
        this.username = user;
        this.password = pass;
    }

    public void inicializar() throws IOException, NoSuchAlgorithmException {
        clientSocket = new Socket(ip, porta);
        out = new ObjectOutputStream((clientSocket.getOutputStream()));
        socket = new DatagramSocket(porta);
        fazerLogin();
        receberMensagens();
        mandarPedidos();
    }
    
    
    public void inicializarAutomatico() throws IOException {
        final Socket clientSocketA = new Socket(ip,porta);
        final ObjectOutputStream outA = new ObjectOutputStream(clientSocketA.getOutputStream());
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    Credenciais c = new Credenciais(username, password);
                    outA.writeObject(c);
                    int valor = valorInicial;
                    for(int i = 0; i < nrPropostas; i++) {
                        RequestLicitacao temp = new RequestLicitacao('b', idLeilao, valor);
                        outA.writeObject(temp);
                        valor += valorLicitacoes;
                    }
                    outA.close();
                    clientSocketA.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    
    
    private void receberMensagens() throws IOException {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    String mensagem;
                    while (true) {
                        byte[] buffer = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        mensagem = new String(packet.getData()).trim();
                        System.out.println(mensagem);
                        Log.write("User '" + ip + "': Recebeu mensagem do servidor:\n'"
                                + mensagem + "'");
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }
    
    
    public void fazerLogin() throws NoSuchAlgorithmException, IOException {
        boolean passErrada = true;
        
        while(passErrada) {
            System.out.println("Introduza o username");
            String user = teclado.nextLine();
            System.out.println("Introduza a password");
            String pass = teclado.nextLine();
            Credenciais credenciais = new Credenciais(user, pass);
            out.writeObject(credenciais);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String mensagem = new String(packet.getData()).trim();
            System.out.println(mensagem);
            if(!mensagem.equals("Credenciais Erradas, tente novamente")) {
                passErrada = false;
            }
            else {
                Log.write("User '" + ip + "': Falhou tentativa de login");
            }
        }
    }
    
    
    public void mandarPedidos() throws IOException {
        while (true) {
            String menuInicial = "Escolha o que quer fazer:\n"
                    + "a) Criar um Leil�o\n"
                    + "b) Fazer uma Licita��o\n"
                    + "c) Consultar Leil�es dispon�veis\n"
                    + "d) Consultar leil�es em que j� efetuou uma licita��o\n"
                    + "e) Pesquisar leil�es";
            System.out.println(menuInicial);
            String mensagem =  teclado.nextLine();
            char keyWord = Character.toLowerCase(mensagem.charAt(0));
            if(mensagem.length() > 1) {
                keyWord = 'z';
            }
            switch (keyWord) {
                case 'a' :
                    Log.write("User '" + ip + "': Pretende iniciar um leil�o");
                    criarPedidoLeilao();
                    break;
                case 'b':
                    Log.write("User '" + ip + "': Pretende fazer uma licita��o");
                    criarPedidoLicitacao();
                    break;
                case 'c':
                    Log.write("User '" + ip + "': Quer ver os leil�es dispon�veis");
                    consultarLeiloes();
                    break;
                case 'd':
                    Log.write("User '" + ip + "': Quer ver os leil�es em que j� licitou");
                    consultarLeiloesLicitados();
                    break;
                case 'e':
                    Log.write("User '" + ip + "': Quer pesquisar por leil�es");
                    pesquisarLeiloes();
                    break;
                default:
                    System.out.println("O comando que introduziu n�o � v�lido!\n");
                    Log.write("User '" + ip + "': Introduziu um comando inv�lido (" + String.valueOf(keyWord) + ")");
                    break;
            }
        }
    }
    
    
    public void criarPedidoLeilao() throws IOException {
        Log.write("User '" + ip + "': Vai preencher o formul�rio de cria��o de leil�o");
        
        System.out.println("Dever� introduzir a data de validade do leil�o:");
        System.out.print("Introduza o ano: ");
        Integer ano = Integer.parseInt(teclado.nextLine());
        
        System.out.print("Introduza o mes: ");
        Integer mes = Integer.parseInt(teclado.nextLine()) ;
        
        System.out.print("Introduza o dia: ");
        Integer dia = Integer.parseInt(teclado.nextLine()) ;
        
        System.out.print("Introduza a hora: ");
        Integer hora = Integer.parseInt(teclado.nextLine()) ;
        
        System.out.print("Introduza os minutos: ");
        Integer min = Integer.parseInt(teclado.nextLine()) ;
        
        System.out.print("Descreva o objeto do leil�o: ");
        String descricao = teclado.nextLine();
        
        System.out.print("Introduza um valor inicial para o leil�o: ");
        Integer valor = Integer.parseInt(teclado.nextLine()) ;
        
        RequestLeilao p = new RequestLeilao('a', ano, mes, dia, hora, min, descricao, valor);
        out.writeObject(p);
        Log.write("User '" + ip + "': pedido de leil�o feito\nDados do pedido: "
        + p);
    }
    
    
    public void criarPedidoLicitacao() throws IOException {
        Log.write("User '" + ip + "': Vai preencher o formul�rio de licita��o");
        
        System.out.print("Introduza o ID do leil�o no qual pretende fazer uma licita��o: ");
        Integer idPretendido = Integer.parseInt(teclado.nextLine());
        
        System.out.print("Introduza o valor da sua licita��o: ");
        Integer valorLicitacao = Integer.parseInt(teclado.nextLine());
        RequestLicitacao p = new RequestLicitacao('b', idPretendido, valorLicitacao);
        out.writeObject(p);
        
        Log.write("User '" + ip + "': pedido de licita��o feito\nDados do pedido: "
        + p);
    }
    
    
    public void consultarLeiloes() throws IOException {
        Request p = new Request('c');
        out.writeObject(p);
    }
    
    public void consultarLeiloesLicitados() throws IOException {
        Request p = new Request('d');
        out.writeObject(p);
    }
    
    public void pesquisarLeiloes() throws IOException {        
        System.out.print("Pesquisar por: ");
        String criterio = teclado.nextLine();
        
        RequestPesquisa p = new RequestPesquisa('e', criterio);
        out.writeObject(p);
        
        Log.write("User '" + ip + "': Pesquisou leil�es com o texto '" + criterio + "'");
    }
}
