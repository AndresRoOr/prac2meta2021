/**
 * @file    ElementoSolucion.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 02/11/2020
 */
package es.meta.metaheuristicas_practica_2;

import java.util.Set;

/**
 * @brief Clase que almacena la información de cada elemento que forma parte de
 * la solución
 * @class ElementoSolucion
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 02/11/2020
 */
public class Cromosomas implements Comparable<Cromosomas> {

    ///Atributos de la clase:
    private Set<Integer> cromosoma;
    private float _contribucion;///<Coste que aporta a la solución

    /**
     * @param cromo
     * @brief Constructor parametrizado de la clase ElementoSolucion
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 03/10/2020
     * @param id Integer
     * @param _contribucion Float
     */
    public Cromosomas(Set<Integer>cromo, float _contribucion) {
        this.cromosoma = cromo;
        this._contribucion = _contribucion;

    }

    @Override
    public int compareTo(Cromosomas otro) {
        Float ele1 = this.getContribucion();
        Float ele2 = otro.getContribucion();
        int comparativa = ele1.compareTo(ele2);

        if (comparativa < 0) {
            return -1;
        } else if (comparativa > 0) {
            return 1;
        } else {
            return 0;
        }

    }

    /**
     * @brief Metodo getter del atributo _contribucion
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 02/11/2020
     * @return _contribucion float
     */
    public float getContribucion() {
        return this._contribucion;
    }


    /**
     * @brief Metodo setter del atributo _contribucion
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 02/11/2020
     * @param cont float
     */
    public void setContribucion(float cont) {
        this._contribucion = cont;
    }


}
