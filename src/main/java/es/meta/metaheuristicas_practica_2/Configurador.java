/**
 * @file    Configurador.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 27/09/2020
 */
package es.meta.metaheuristicas_practica_2;

import java.util.ArrayList;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import org.apache.commons.lang3.StringUtils;

/**
 * @brief Clase que almacena todos los parámetros principales del programa
 * @class Configurador
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 27/09/2020
 */
public class Configurador {

    ///Atributos de la clase:
     ArrayList<String> directoriosDatos;///<Almacena los directorios donde se 
    ///encuentran los archivos con la información del problema
    private Long semilla;///<Semilla utilizada para generar número aleatorios
    private Integer evaluaciones;///<Número de iteraciones
    private Long recuperarSemilla;///<Almacena el valor inicial de la semilla
    private Integer elitismo;
    private Boolean cruceMpx;
    private Float probReproduccion;
    private Float probMutacion;
    private Float probMpx;
    private Integer numeroCromosomas;


    /**
     * @brief Constructor parametrizado de la clase Configurador
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @param ruta String Contiene la ruta completa del archivo que contiene la
     * información de los parámetros
     */
    public Configurador(String ruta) {

        directoriosDatos = new ArrayList<>();

        String linea;
        FileReader f = null;
        try {
            f = new FileReader(ruta);
            BufferedReader b = new BufferedReader(f);

            while ((linea = b.readLine()) != null) {
                String[] split = StringUtils.split(linea,"=");
                switch (split[0]) {
                    case "Datos":
                        String[] v = split[1].split(" ");
                        for (int i = 0; i < v.length; i++) {
                            directoriosDatos.add(v[i]);
                        }
                        break;
                    case "Semilla":
                        semilla = Long.parseLong(split[1]);
                        recuperarSemilla = semilla;
                        break;
                    case "Evaluaciones":
                        evaluaciones = Integer.parseInt(split[1]);
                        break;
                      
                    case "Elitismo":
                        elitismo = Integer.parseInt(split[1]);
                        break;
                            
                    case "OperadorMPX":
                        cruceMpx = Boolean.parseBoolean(split[1]);
                        break;
                        
                    case "Probabilidad de mutacion":
                        probMutacion = Float.parseFloat(split[1]);
                        break;
                        
                    case "Probabilidad de reproduccion":
                        probReproduccion = Float.parseFloat(split[1]);
                        break;  
                        
                        
                    case "Cromosomas":
                        numeroCromosomas = Integer.parseInt(split[1]);
                        break;
                                
                    case "Probabilidad MPX":
                        probMpx = Float.parseFloat(split[1]);
                        break;
                }
            }
            b.close();

        } catch (IOException e) {
            Main.console.presentarSalida("No se ha encontrado el archivo de configuración");
        } finally {
            try {
                if (null != f) {
                    f.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * @brief Funcion getter del atributo directoriosDatos
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @return directoriosDatos ArrayList
     */
    public ArrayList<String> getDirectoriosDatos() {
        return directoriosDatos;
    }

    /**
     * @brief Funcion getter del atributo semilla
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @return semilla Long
     */
    public Long getSemilla() {
        return semilla;
    }

    /**
     * @brief Funcion getter del atributo iteraciones
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 27/09/2020
     * @return iteraciones Integer
     */
    public Integer getEvaluaciones() {
        return evaluaciones;
    }
    
    
     public Integer getElitismo() {
        return elitismo;
    }

    public Boolean getCruceMpx() {
        return cruceMpx;
    }

    public Float getProbReproduccion() {
        return probReproduccion;
    }

    public Float getProbMutacion() {
        return probMutacion;
    }
    
    public Integer getNumeroCromosomas() {
        return numeroCromosomas;
    }
    
    public Float getProbMpx() {
        return probMpx;
    }

    /**
     * @brief Rota las posiciones de la semilla una posición a la derecha
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 06/10/2020
     */
    void rotarSemilla() {

        char[] cadenaSemilla = semilla.toString().toCharArray();
        char[] cadenaRotada = new char[cadenaSemilla.length];

        cadenaRotada[cadenaSemilla.length - 1] = cadenaSemilla[0];

            for (int i = 0; i < cadenaSemilla.length - 1; i++) {
                cadenaRotada[i] = cadenaSemilla[i + 1];
            }

        while (cadenaRotada[0] == '0') {
            char[] cadenaAux = cadenaRotada;

            cadenaRotada[cadenaSemilla.length - 1] = cadenaAux[0];

            for (int i = 0; i < cadenaAux.length - 1; i++) {
                cadenaRotada[i] = cadenaAux[i + 1];
            }
        }

        semilla = Long.parseLong(String.valueOf(cadenaRotada));
    }


    /**
     * @brief Restaura la semilla a su estado original
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 06/10/2020
     */
    void RecuperarSemilla() {
        semilla = recuperarSemilla;
    }
}
