package servidor;

import cliente.Request;
import cliente.RequestLeilao;
import cliente.RequestLicitacao;
import cliente.RequestPesquisa;
import leilao.Leilao;
import licitador.Licitador;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class Servidor {
    public static ServerSocket serverSocket;

    private List<Leilao> leiloesList;
    private List<Credenciais> registosList;
    private List<Licitador> clientesList;
    private List<InetAddress> addressesList;

    private int port;


    public Servidor(int porta) throws IOException, ClassNotFoundException {
        this.port = porta;
        serverSocket = new ServerSocket(porta);
        addressesList = Collections.synchronizedList(new ArrayList<InetAddress>());
    }

    
    public void start() throws IOException {
        System.out.println("O servidor foi inicializado!");
        Log.write("Servidor inicializado no porto: " + port);
        
        // Carrega lista de Leilões cajo haja um ficheiro equivalente
        leiloesList = (List)Serializador.deserialize("Leiloes");
        
        // Inicializa a thread que controla o fim de cada leilão
        verificaSeLeilaoAcabou();
        if(leiloesList == null) {
            leiloesList = Collections.synchronizedList(new ArrayList<Leilao>());
            Serializador.serialize(leiloesList, "Leiloes");
        }
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("New Connection");
                        InetAddress address = clientSocket.getInetAddress();
                        System.out.println(address.getHostAddress());
                        addressesList.add(address);
                        processarPedidos(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    
    
    private void processarPedidos(final Socket clientSocket) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    Licitador licitadorAtual = login(clientSocket, in);
                    while (true) {
                        System.out.println("olaaa");
                        Request p = (Request)in.readObject();
                        switch (p.getKeyWord()) {
                            case 'a':
                                criarLeilao(clientSocket, licitadorAtual, p);
                                break;
                            case 'b':
                                criarUmaLicitacao(clientSocket, licitadorAtual,p);
                                break;
                            case 'c':
                                consultarLeiloes(clientSocket, licitadorAtual);
                                break;
                            case 'd':
                                consultarLeiloesLicitados(clientSocket, licitadorAtual);
                                break;
                            case 'e':
                                pesquisarLeiloes(clientSocket, licitadorAtual, p);
                                break;
                            default:
                                break;
                        }
                    }


                }
                catch (SocketException e) {
                    System.err.println("LIGACAO CAIU");
                    addressesList.remove(clientSocket.getInetAddress().getHostAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });thread.start();
    }
    
    
    private Licitador login(Socket clientSocket, ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        boolean passErrada = true;
        Licitador licitadorAtual = null;
        while (passErrada) {
            Credenciais c1 = (Credenciais) in.readObject();
            registosList = (List) Serializador.deserialize("Registos");
            clientesList = (List)Serializador.deserialize("Clientes");
            if(registosList == null) {
                registosList = Collections.synchronizedList(new ArrayList<Credenciais>());
                Serializador.serialize(registosList, "Registos");
            }
            if(clientesList == null) {
                clientesList = Collections.synchronizedList(new ArrayList<Licitador>());
                Serializador.serialize(clientesList, "Clientes");
            }
            String passCifrada = SHA256.generate(c1.getPassword().getBytes());
            c1.setPassword(passCifrada);
            for(Credenciais credenciais: registosList) {
                if(credenciais.getUsername().equals(c1.getUsername()) && credenciais.getPassword().equals(c1.getPassword())) {
                    Licitador licitador = getLicitadorByCredencials(credenciais, clientesList);
                    if(licitador == null) {
                        licitadorAtual = new Licitador(c1.getUsername(), c1.getPassword(), credenciais.getPlafondInicial(), clientSocket.getInetAddress());
                        clientesList.add(licitadorAtual);
                        Serializador.serialize(clientesList, "Clientes");
                        System.out.println("primeiro login do: " + c1.getUsername());
                    } else {
                        licitador.setAddress(clientSocket.getInetAddress());
                        Serializador.serialize(clientesList, "Clientes");
                        licitadorAtual = licitador;
                    }
                    passErrada = false;
                }
            }
            if(passErrada) {
                envia(clientSocket.getInetAddress(),"Credenciais Erradas, tente novamente");
            } else {
                envia(clientSocket.getInetAddress(), "Login feito com sucesso!");
            }
        }
        return licitadorAtual;
    }
    
    
    private void verificaSeLeilaoAcabou() {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        if(leiloesList != null) {
                            for(Leilao leilao : leiloesList) {
                                if(!leilao.getExpirou()) {
                                    if(leilao.getDataLimite().before(new Date(System.currentTimeMillis()))) {
                                        Log.write("Leilão '" + leilao.getId() + "' expirou. A conferir resultados!");
                                        leilao.setExpirou(true);
                                        Licitador autor = getLicitadorByNameAndPassword(leilao.getAutor(), clientesList);
                                        
                                        if(leilao.getVencedorAtual() == null) {
                                            Log.write("Leilão '" + leilao.getId() + "' expirou sem licitadores!");
                                            mostrarAUmLicitador(autor, "Lamentamos, mas o seu leilão com o ID '" + leilao.getId() + "' fechou sem qualquer licitação");
                                        }
                                        else {
                                            Licitador vencedorAtual = getLicitadorByNameAndPassword(leilao.getVencedorAtual(), clientesList);
                                            
                                            mostrarAUmLicitador(vencedorAtual,  "Parabéns! Foste o vencedor do leilão '" + leilao.getId() + "' no valor de €" + leilao.getPropostaMaisAlta().getValor() + ".");
                                            mostrarAUmLicitador(autor, "O seu leilão '" + leilao.getId() + "' terminou e foi vendido ao utilizador '" + vencedorAtual.getUsername() + "'"
                                                    + ". O valor arrecadado foi de €" + leilao.getPropostaMaisAlta().getValor() + ".");
                                            
                                            Log.write("Leilão '" + leilao.getId() + "' vendido ao utilizador '" + vencedorAtual.getUsername() + "' pelo total de €" + leilao.getPropostaMaisAlta().getValor() + ".");
                                            for(Licitador l: leilao.getLicitadores()) {
                                                Licitador licitador = getLicitadorByNameAndPassword(l, clientesList);
                                                if(!licitador.equals(vencedorAtual)) {
                                                    mostrarAUmLicitador(licitador, "O Leilão com o ID '" + leilao.getId() + "' expirou. Infelizmente o seu lance não foi o mais alto...");
                                                }
                                            }
                                            autor.reporPLafond(leilao.getPropostaMaisAlta().getValor());
                                        }
                                        Serializador.serialize(leiloesList, "Leiloes");
                                        Serializador.serialize(clientesList, "Clientes");
                                    }
                                }

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    
    
    private void criarLeilao(Socket clientSocket, Licitador licitadorAtual, Request pedido) throws IOException, ClassNotFoundException {
        RequestLeilao p = (RequestLeilao) pedido;
        System.out.println(clientSocket.getInetAddress().getHostAddress());
        System.out.println(licitadorAtual);
        int leilaoIDCount = leiloesList.size()+1;
        Leilao leilao = new Leilao(leilaoIDCount, p.getAno(), p.getMes(), p.getDia(), p.getHora(), p.getMin(),licitadorAtual, p.getDescricao(), p.getValor());
        mostraParaTodosMenosOQuePediu(clientSocket, "Há um novo leilão disponível com o ID: " + leilao.getId()
                + ". Queira consultar os leilões disponíveis!");
        mostrarApenasAoQuePediu(clientSocket, "O seu leilão foi criado com sucesso com o ID: " + leilao.getId());
        leiloesList.add(leilao);
        Serializador.serialize(leiloesList, "Leiloes");
    }
    
    
    private void criarUmaLicitacao(Socket clientSocket, Licitador licitadorAtual, Request pedido) throws IOException, ClassNotFoundException {
        RequestLicitacao p = (RequestLicitacao) pedido;
        int idPretendido = p.getIdLicitacao();
        if(existeLeilao(idPretendido)) {
            int valorLicitacao = p.getValorLicitacao();
            Leilao leilaoPretendido =getLeilaoById(idPretendido, leiloesList);
            if(valorLicitacao > leilaoPretendido.getPropostaMaisAlta().getValor()) {
                if(valorLicitacao <= licitadorAtual.getPlafond() || eMesmoLicitador(leilaoPretendido.getPropostaMaisAlta(), licitadorAtual, valorLicitacao)) {
                    mostraParaTodosMenosOQuePediu(clientSocket, "Foi recebida uma nova licitação no leilão com ID " + idPretendido);
                    mostrarApenasAoQuePediu(clientSocket, "A sua licitação foi aceite.");
                    leilaoPretendido.setVencedorAtual(licitadorAtual);
                    leilaoPretendido.adicionarLicitador(licitadorAtual);
                    if(leilaoPretendido.getPropostaMaisAlta().getLicitador() != null) {
                        Licitador licitadorPretendido = getLicitadorByNameAndPassword(leilaoPretendido.getPropostaMaisAlta().getLicitador(), clientesList);
                        System.out.println("entrou aquiii");
                        System.out.println(leilaoPretendido.getPropostaMaisAlta().getLicitador());
                        licitadorPretendido.reporPLafond(leilaoPretendido.getPropostaMaisAlta().getValor());
                        System.out.println(leilaoPretendido.getPropostaMaisAlta().getLicitador());
                    }
                    licitadorAtual.retirarPlafond(valorLicitacao);
                    Proposta proposta = new Proposta(licitadorAtual, valorLicitacao);
                    leilaoPretendido.setPropostaMaisAlta(proposta);
                    Serializador.serialize(leiloesList,"Leiloes");
                    Serializador.serialize(clientesList, "Clientes");
                } else {
                    mostrarApenasAoQuePediu(clientSocket, "A sua licitação não foi aceite! O valor da sua proposta é superior ao seu plafond.");
                }
            } else {
                mostrarApenasAoQuePediu(clientSocket, "A sua licitação não foi aceite! A sua proposta deve ser mais alta que a atual!");
            }
        } else {
            mostrarApenasAoQuePediu(clientSocket, "O leilão com ID '" + idPretendido + "' não existe ou já não se encontra disponível.");
        }
    }
    
    
    private void consultarLeiloes(Socket clientSocket, Licitador licitadorAtual) throws IOException, ClassNotFoundException {
        mostrarApenasAoQuePediu(clientSocket, "Plafond disponivel: " + licitadorAtual.getPlafond());
        for(Leilao leilao : leiloesList) {
            if(!leilao.getExpirou()) {
                mostrarApenasAoQuePediu(clientSocket, leilao.toString());
            }

        }
    }
    
    private void consultarLeiloesLicitados(Socket clientSocket, Licitador licitadorAtual) throws IOException, ClassNotFoundException {
        StringBuilder response = new StringBuilder("Lista de leilões em que já licitou:\n");
        for(Leilao temp : leiloesList) {
            if(temp.foiLicitadoPorLicitador(licitadorAtual)) {
                response.append(temp).append("\n");
            }
        }
        
        mostrarApenasAoQuePediu(clientSocket, response.toString());
    }
    
    private void pesquisarLeiloes(Socket clientSocket, Licitador licitadorAtual, Request pedido) throws IOException, ClassNotFoundException {
        RequestPesquisa requestPesquisa = (RequestPesquisa) pedido;
        String criterioPesquisa = requestPesquisa.getCriterio().toLowerCase();
        StringBuilder response = new StringBuilder("ID\tDescrição\tData e Hora de fecho\tValor da proposta");
        
        for(Leilao temp : leiloesList) {
            if(temp.getDescricao().toLowerCase().contains(criterioPesquisa)) {
                response.append(temp.getPesquisaResponseString());
            }
        }
        
        mostrarApenasAoQuePediu(clientSocket, response.toString());
    }
    
    
    private boolean eMesmoLicitador(Proposta p, Licitador l2, int valorProposta) {
        if(p.getLicitador().getUsername().equals(l2.getUsername()) && p.getLicitador().getPassword().equals(l2.getPassword())) {
            int plafondDisponivel = p.getValor() + l2.getPlafond();
            if(plafondDisponivel - valorProposta >= 0) {
                return true;
            } else {
                return false;
            }

        }
        return false;
    }
    
    
    private void mostrarApenasAoQuePediu(Socket clientSocket, String mensagem) throws IOException{
        for(InetAddress inetAddress: addressesList) {
            if(inetAddress.equals(clientSocket.getInetAddress())) {
                envia(inetAddress, mensagem);
            }
        }
    }
    
    
    private void mostraParaTodosMenosOQuePediu(Socket clientSocket, String mensagem) throws IOException {
        for(InetAddress inetAddress: addressesList) {
            if(!inetAddress.equals(clientSocket.getInetAddress())) {
                envia(inetAddress, mensagem);
            }
        }
    }
    
    
    private void mostrarAUmLicitador(Licitador licitador, String mensagem) throws IOException{
        Log.write("Mensagem enviada para o utilizador '" + licitador.getAddress() + "': [" + mensagem + "]");
        envia(licitador.getAddress(), mensagem);
    }
    
    
    private void envia(InetAddress address, String mensagem) throws IOException{
        DatagramPacket packet = new DatagramPacket(mensagem.getBytes(), mensagem.getBytes().length, address, port);
        DatagramSocket out = new DatagramSocket();
        out.send(packet);
    }
    
    
    private Licitador getLicitadorByCredencials(Credenciais credenciais, List<Licitador> clientes) throws IOException, ClassNotFoundException {
        for(Licitador l: clientes) {
            if(credenciais.getUsername().equals(l.getUsername()) && credenciais.getPassword().equals(l.getPassword())) {
                return l;
            }
        }
        return null;
    }

    
    private boolean existeLeilao(int idPretendido) {
        for(Leilao leilao: leiloesList) {
            if(leilao.getId() == idPretendido && !leilao.getExpirou()) {
                return true;
            }
        }
        return false;
    }
    
    
    private Licitador getLicitadorByNameAndPassword(Licitador licitador, List<Licitador> clientes) {
        for(Licitador l : clientes) {
            if(l.getUsername().equals(licitador.getUsername()) && l.getPassword().equals(licitador.getPassword())) {
                return l;
            }
        }
        return null;
    }
    
    
    private Leilao getLeilaoById(int id, List<Leilao> leiloes) {
        for(Leilao leilao: leiloes) {
            if(leilao.getId() == id) {
                return leilao;
            }
        }
        return null;
    }
}
