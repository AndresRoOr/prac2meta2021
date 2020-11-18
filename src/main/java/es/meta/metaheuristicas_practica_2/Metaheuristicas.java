/**
 * @file    Metaheuristicas.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 27/09/2020
 */
package es.meta.metaheuristicas_practica_2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @brief Clase que calcula todos los resultados con los algoritmos solicitados
 * sobre todos los datos facilitados
 * @class Metaheuristicas
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 27/09/2020
 */
public class Metaheuristicas {

    ///Atributos de la clase:
    Configurador _config;///<Contiene los parámetros principales del programa
    String _nombre;///<Nombre del objeto Metaheuristicas
    ArrayList<Archivo> _archivos;///<Contiene el nombre de los archivos que 
    ///contienen los datos sobre los que hacer los cálculos
    String _ruta_Carpeta_Archivos;///<Directorio que contiene los archivos

    /**
     * @brief Constructor parametrizado de la clase Metaheuristicas
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @param nombre String Nombre de la nueva instancia
     * @param ruta String Ruta del directorio que contiene los archivos
     * @param config Configurador Objeto con los parámetros del programa
     */
    Metaheuristicas(String nombre, String ruta, Configurador config) {
        _config = config;
        _nombre = nombre;
        _ruta_Carpeta_Archivos = ruta;
        _archivos = new ArrayList<>();
    }

    /**
     * @brief Realiza la lectura de todos los datos de todos los archivos
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @throws FileNotFoundException
     * @throws IOException
     */
    void lector_Archivos() throws FileNotFoundException, IOException {
        final File carpeta = new File(_ruta_Carpeta_Archivos);
        for (final File fichero_Entrada : carpeta.listFiles()) {

            if (fichero_Entrada.isFile()) {
                Archivo ar = new Archivo(fichero_Entrada.getName(),
                        _ruta_Carpeta_Archivos + "/"
                        + fichero_Entrada.getName());
                _archivos.add(ar);
            }
        }

    }

    /**
     * @brief Muestra por pantalla los datos de todos los archivos leídos
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     */
    void mostrar_Datos() {
        for (Archivo ar : _archivos) {
            ar.presentarDatos();
        }
    }

    /**
     * @brief Calcula la solución para todos los archivos utilizando el
     * algoritmo Greedy y muestra el resultado por pantalla
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     */
    void genetico() {

        if (_archivos.size() > 0) {

            GestorLog gestor = new GestorLog("");

            int aumento = (1000 / (_archivos.size() * 5));

            for (Archivo ar : _archivos) {

                int ite = 1;

                while (ite <= 5) {

                    Main.console.setValue(aumento / 2);

                    Timer t = new Timer();

                    gestor.cambiarNombre("genetico/Log_SEM_" + _config.getSemilla() + ar.getNombre());
                    gestor.abrirArchivo();

                    Random_p sem = new Random_p();
                    sem.Set_random(_config.getSemilla());

                    Genetico g = new Genetico(ar, Main.gestor, _config.getEvaluaciones(),_config.getElitismo(),
                            _config.getCruceMpx(), _config.getProbReproduccion(),
                            _config.getProbMutacion(), _config.getProbMpx(),_config.getNumeroCromosomas());

                    t.startTimer();

                    g.genetico(sem);

                    double tiempo = t.stopTimer();

                    Main.console.presentarSalida("Datos de la solución al problema: " + ar._nombre);
                    Main.console.presentarSalida("Tiempo de ejecución del algoritmo: " + tiempo + " milisegundos");

                    g.PresentarResultados();

                    gestor.cerrarArchivo();

                    Main.console.setValue(aumento / 2);

                    ite++;

                    _config.rotarSemilla();
                }

                _config.RecuperarSemilla();
            }
        } else {
            Main.console.presentarSalida("No hay datos en el directorio: " + _config.directoriosDatos.get(0));
        }
    }
}
