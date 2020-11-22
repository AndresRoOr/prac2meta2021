/**
 * @file    Cromosomas.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 22/11/2020
 */
package es.meta.metaheuristicas_practica_2;

import java.util.HashSet;
import java.util.Set;

/**
 * @brief Clase que almacena la información de cada elemento que forma parte de
 * una población
 * @class Cromosomas
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 22/11/2020
 */
public final class Cromosomas implements Comparable<Cromosomas> {

    ///Atributos de la clase:
    private Set<Integer> cromosoma;///<Conjunte de genes solución del problema
    private float _contribucion;///<Coste que aporta a la solución
    private boolean recalcular;///<Indica si es necesario recalcular el coste

    /**
     * @brief Constructor parametrizado de la clase Cromosomas
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 22/11/2020
     * @param cromo Set<Integer>
     * @param _contribucion Float
     */
    public Cromosomas(Set<Integer> cromo, float _contribucion) {
        this.cromosoma = cromo;
        this._contribucion = _contribucion;
        this.recalcular = false;

    }

    /**
     * @brief Constructor parametrizado de la clase Cromosomas
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 22/11/2020
     * @param cromo Set<Integer>
     * @param _contribucion float
     * @param recal boolean
     */
    public Cromosomas(Set<Integer> cromo, float _contribucion, boolean recal) {
        this.cromosoma = cromo;
        this._contribucion = _contribucion;
        this.recalcular = recal;

    }

    /**
     * @brief Constructor por copia de la clase Cromosoma
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 22/11/2020
     * @param otro Cromosomas
     */
    public Cromosomas(Cromosomas otro) {
        this.cromosoma = new HashSet<>(otro.getCromosoma());
        this._contribucion = otro.getContribucion();
        this.recalcular = false;

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
     * @date 22/11/2020
     * @param cont float
     */
    public void setContribucion(float cont) {
        this._contribucion = cont;
    }

    /**
     * @brief Método getter del atributo cromosoma
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 22/11/2020
     * @return cromosoma Set<Integer>
     */
    public Set<Integer> getCromosoma() {
        return cromosoma;
    }

    /**
     * @brief Método setter del atributo cromosoma
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 22/11/2020
     * @param cromosoma Set<Integer> 
     */
    public void setCromosoma(Set<Integer> cromosoma) {
        this.cromosoma = cromosoma;
    }

    /**
     * @brief Método getter del atributo recalcular
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 22/11/2020
     * @return recalcular boolean
     */
    public boolean isRecalcular() {
        return recalcular;
    }

    /**
     * @brief Método setter del atributo recalcular
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 22/11/2020
     * @param recalcular boolean 
     */
    public void setRecalcular(boolean recalcular) {
        this.recalcular = recalcular;
    }

}
