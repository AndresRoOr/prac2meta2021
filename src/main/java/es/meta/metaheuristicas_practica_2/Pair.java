/** @file    Pair.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 30/09/2020
 */
package es.meta.metaheuristicas_practica_2;

/**
 * @brief Representa un par formado por un candidato y un coste asociado a este.
 * @class Pair
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 30/09/2020
 */
public class Pair {

    ///Atributos de la clase:
    Integer candidato;
    float coste;

    /**
     * @brief Constructor parametrizado de la clase Pair
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @param candidato Integer
     * @param coste double
     */
    public Pair(Integer candidato, float coste) {
        this.candidato = candidato;
        this.coste = coste;
    }

    /**
     * @brief Metodo getter del atributo candidato
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @return candidato Integer
     */
    public Integer getCandidato() {
        return candidato;
    }

    /**
     * @brief Metodo getter del atributo coste
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @return coste double
     */
    public float getCoste() {
        return coste;
    }

    /**
     * @brief Metodo setter del atributo candidato
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @param candidato Integer
     */
    public void setCandidato(Integer candidato) {
        this.candidato = candidato;
    }

    /**
     * @brief Metodo setter del atributo coste
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 30/09/2020
     * @param coste double
     */
    public void setCoste(float coste) {
        this.coste += coste;
    }

}
