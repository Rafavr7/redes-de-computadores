package servidor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static final String LOG_FILE_NAME = "server_log.txt";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss:SSS");
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private Log() {
        // EMPTY
    }
    
    public static void write(String toWrite) {
        try {
            File logFile = new File(LOG_FILE_NAME);
            FileWriter logWriter = new FileWriter(logFile, true);
            Date agora = new Date();
            
            logWriter.write("*** " + DATE_FORMAT.format(agora) + "\n" + toWrite + LINE_SEPARATOR + LINE_SEPARATOR);
            logWriter.close();
        }
        catch(IOException ex) {
            // Do nothing
        }
    }
}
