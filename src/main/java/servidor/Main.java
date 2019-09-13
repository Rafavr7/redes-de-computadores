package servidor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main{
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Servidor servidor = new Servidor(Integer.parseInt(args[0]));
        servidor.start();
        menuServidor();
    }
    public static void menuServidor() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        System.out.println(" a) Fazer Registo");
                        String word = scanner.nextLine();
                        char keyWord = word.charAt(0);
                        if(word.length() > 1) {
                            keyWord = 'z';
                        }
                        if(keyWord == 'a') {
                            Log.write("A iniciar novo registo de utilizador");
                            ArrayList<Credenciais> registos = (ArrayList)Serializador.deserialize("Registos");
                            if(registos == null) {
                                registos = new ArrayList<Credenciais>();
                                Serializador.serialize(registos,"Registos");
                            }
                            System.out.println("Introduza o username");
                            String user = scanner.nextLine();
                            System.out.println("Introduza a password");
                            String password = scanner.nextLine();
                            String passwordCifrada = SHA256.generate(password.getBytes());
                            System.out.println("Introduza o plafond");
                            Integer plafond = Integer.parseInt(scanner.nextLine());
                            Credenciais credeciais = new Credenciais(user,passwordCifrada, plafond);
                            registos.add(credeciais);
                            Log.write("Utilizador criado! Username: " + user + " Plafond: " + plafond);
                            Serializador.serialize(registos, "Registos");

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
