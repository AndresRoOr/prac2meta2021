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
public final class AlgMetaheuristicas_Clase02_Grupo06 {

    ///Atributos de la clase:
    private final AlgConfigurador_Clase02_Grupo06 _config;///<Contiene los parámetros principales del programa
    private final String _nombre;///<Nombre del objeto AlgMetaheuristicas_Clase02_Grupo06
    private ArrayList<AlgArchivo_Clase02_Gropo_06> _archivos;///<Contiene el nombre de los archivos que 
    ///contienen los datos sobre los que hacer los cálculos
    private String _ruta_Carpeta_Archivos;///<Directorio que contiene los archivos

    /**
     * @brief Constructor parametrizado de la clase Metaheuristicas
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @param nombre String Nombre de la nueva instancia
     * @param ruta String Ruta del directorio que contiene los archivos
     * @param config AlgConfigurador_Clase02_Grupo06 Objeto con los parámetros del programa
     */
    AlgMetaheuristicas_Clase02_Grupo06(String nombre, String ruta, AlgConfigurador_Clase02_Grupo06 config) {
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
                AlgArchivo_Clase02_Gropo_06 ar = new AlgArchivo_Clase02_Gropo_06(fichero_Entrada.getName(),
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
        for (AlgArchivo_Clase02_Gropo_06 ar : _archivos) {
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

            int aumento = (1000 / (_archivos.size() * 5));

            for (AlgArchivo_Clase02_Gropo_06 ar : _archivos) {

                int ite = 1;

                while (ite <= 5) {

                    AlgMain_Clase02_Grupo06.console.setValue(aumento / 2);

                    AlgTimer_Clase02_Grupo06 t = new AlgTimer_Clase02_Grupo06();

                    AlgMain_Clase02_Grupo06.gestor.cambiarNombre("genetico/MPX_"+_config.getCruceMpx()+";Elitismo_"+_config.getElitismo()+";SEM_" + _config.getSemilla()+"_" + ar.getNombre());
                    AlgMain_Clase02_Grupo06.gestor.abrirArchivo();

                    AlgRandom_p_Clase02_Grupo06 sem = new AlgRandom_p_Clase02_Grupo06();
                    sem.Set_random(_config.getSemilla());

                    AlgGenetico_Clase02_Grupo06 g = new AlgGenetico_Clase02_Grupo06(ar, AlgMain_Clase02_Grupo06.gestor, _config.getEvaluaciones(),_config.getElitismo(),
                            _config.getCruceMpx(), _config.getProbReproduccion(),
                            _config.getProbMutacion(), _config.getProbMpx(),_config.getNumeroCromosomas());

                    t.startTimer();

                    g.genetico(sem);

                    double tiempo = t.stopTimer();

                    AlgMain_Clase02_Grupo06.console.presentarSalida("Datos de la solución al problema: " + ar.getNombre() + ", con SEMILLA: " + _config.getSemilla());
                    AlgMain_Clase02_Grupo06.console.presentarSalida("Tiempo de ejecución del algoritmo: " + tiempo + " milisegundos");

                    g.PresentarResultados();

                    AlgMain_Clase02_Grupo06.gestor.cerrarArchivo();

                    AlgMain_Clase02_Grupo06.console.setValue(aumento / 2);

                    ite++;

                    _config.rotarSemilla();
                }

                _config.RecuperarSemilla();
            }
        } else {
            AlgMain_Clase02_Grupo06.console.presentarSalida("No hay datos en el directorio: " + _config.directoriosDatos.get(0));
        }
    }
}
