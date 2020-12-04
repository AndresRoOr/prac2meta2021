/**
 * @file    Main.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 27/09/2020
 */
package es.meta.metaheuristicas_practica_2;

import com.formdev.flatlaf.FlatLightLaf;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 * @brief Clase Main del programa
 * @class Main
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 27/09/2020
 */
public class AlgMain_Clase02_Grupo06 {

    
    public static AlgConsola_Clase02_Grupo06 console = new AlgConsola_Clase02_Grupo06();
    public static AlgGestorLog_Clase02_Grupo06 gestor = new AlgGestorLog_Clase02_Grupo06("");
    public static final ExecutorService exec = Executors.newFixedThreadPool(4);

    /**
     * @brief Función principal del programa
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        AlgConfigurador_Clase02_Grupo06 config = new AlgConfigurador_Clase02_Grupo06("./config.txt");

        ArrayList<File> directorios = new ArrayList<>();
        directorios.add(new File("./archivos"));
        directorios.add(new File("./archivos/Datos"));
        directorios.add(new File("./archivos/Log"));
        directorios.add(new File("./archivos/Log/genetico"));

        directorios.stream().filter(directorio -> (!directorio.exists())).forEachOrdered((File directorio) -> {
            if (directorio.mkdirs()) {
            }
        });

        console.presentarSalida("");

        while (console.getEleccion() != 4) {

            while (console.getEleccion() == 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AlgMain_Clase02_Grupo06.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (console.getEleccion() == 4) {
                System.exit(0);
            }

            AlgMetaheuristicas_Clase02_Grupo06 M1 = new AlgMetaheuristicas_Clase02_Grupo06(config.getDirectoriosDatos().get(0),
            config.getDirectoriosDatos().get(0), config);
            M1.lector_Archivos();

            switch (console.getEleccion()) {

                case 1:
                    
                    M1.genetico();
                    break;
                    
                case 2:
                    config = null;
                    config = new AlgConfigurador_Clase02_Grupo06("./config.txt");
                    console.restaurarEleccion();
                    break;
                   
            }
            M1 = null;
            console.restaurarEleccion();
        }
        exec.shutdownNow();
        System.exit(0);
    }
}
