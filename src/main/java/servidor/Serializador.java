package servidor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializador {
    public static void serialize(Object toSerialize, String fileName) throws IOException {
        Log.write("A iniciar serializa��o do ficheiro '" + fileName + "'");
        FileOutputStream file = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(toSerialize);
        out.close();
        file.close();
    }
    
    public static Object deserialize (String fileName){
        Log.write("A iniciar desserializa��o do ficheiro '" + fileName + "'");
        Object deserialized = null;
        try {
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(file);
            deserialized = in.readObject();
            in.close();
            file.close();
        } catch (FileNotFoundException e) {
            System.out.println("O ficheiro " + fileName + " não existe");
            Log.write("Falhas na desserializa��o do ficheiro '" + fileName + "'");
            Log.write("Exce��o lan�ada: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.write("Falhas na desserializa��o do ficheiro '" + fileName + "'");
            Log.write("Exce��o lan�ada: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.write("Falhas na desserializa��o do ficheiro '" + fileName + "'");
            Log.write("Exce��o lan�ada: " + e.getMessage());
        }
        
        Log.write("Ficheiro '" + fileName + "' desserializado com sucesso!");
        return deserialized;
    }

}
