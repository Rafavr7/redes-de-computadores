package cliente;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main{
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Scanner s = new Scanner(System.in);
        while(true) {
            System.out.println("0) Manual \n 1)Automatico");
            Integer id = Integer.parseInt(s.nextLine());
            if(id == 0) {
                Cliente cliente = new Cliente(args[0], Integer.parseInt(args[1]));
                cliente.inicializar();
            }
            else {
                System.out.println("Introduza o numero de clientes");
                Integer nrClientes = Integer.parseInt(s.nextLine());
                ArrayList<Cliente> clientes = new ArrayList<Cliente>();
                
                for(int i = 0; i < nrClientes; i++) {
                    System.out.println("Introduza o username");
                    String username = s.nextLine();
                    
                    System.out.println("Introduza a password");
                    String password = s.nextLine();
                    
                    System.out.println("Introduza o id do leilao");
                    Integer idLeilao = Integer.parseInt(s.nextLine());
                    
                    System.out.println("Introduza o valor inicial da licitacao");
                    Integer valorInicial = Integer.parseInt(s.nextLine());
                    
                    System.out.println("Introduza o valor a incrementar nas licitacoes");
                    Integer valorLicitacoes = Integer.parseInt(s.nextLine());
                    
                    System.out.println("Introduza o numero de propostas");
                    Integer nrPropostas = Integer.parseInt(s.nextLine());
                    
                    Cliente cliente = new Cliente(args[0], Integer.parseInt(args[1]), idLeilao, valorInicial, valorLicitacoes, nrPropostas, username, password);
                    clientes.add(cliente);
                }
                for(Cliente c : clientes) {
                    c.inicializarAutomatico();
                }
            }
        }

    }
}
