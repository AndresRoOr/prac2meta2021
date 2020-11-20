/** @file    Pair.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 30/09/2020
 */
package es.meta.metaheuristicas_practica_2;

import java.util.ArrayList;

/**
 * @brief Representa un par formado por un candidato y un coste asociado a este.
 * @class Pair
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 30/09/2020
 */
public class Pair {

    ///Atributos de la clase:
    private Float mejorCoste;
    private ArrayList<Cromosomas> resultados;

    /**
     * @brief Constructor parametrizado de la clase Pair
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @param candidato Integer
     * @param coste double
     */
    public Pair(Float mejorCoste, ArrayList<Cromosomas> resultados) {
        this.mejorCoste = mejorCoste;
        this.resultados = resultados;
    }

    /**
     * @brief Metodo getter del atributo candidato
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @return candidato Integer
     */
    public Float getMejorCoste() {
        return mejorCoste;
    }

    /**
     * @brief Metodo getter del atributo coste
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @return coste double
     */
    public ArrayList<Cromosomas> getResultados() {
        return resultados;
    }

    /**
     * @brief Metodo setter del atributo candidato
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @param candidato Integer
     */
    public void setMejorCoste(Float mejorCoste) {
        this.mejorCoste = mejorCoste;
    }

    /**
     * @brief Metodo setter del atributo coste
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @param coste double
     */
    public void setResultados(ArrayList<Cromosomas> resultados) {
        this.resultados = resultados;
    }

}
