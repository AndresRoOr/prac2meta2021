/** @file    Timer.java
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 03/10/2020
 */
package es.meta.metaheuristicas_practica_2;

/**
 * @brief Clase para gestionar los tiempos de ejecución del algoritmo
 * @class Timer
 * @author David Díaz Jiménez
 * @date 03/10/2020
 */
public final class AlgTimer_Clase02_Grupo06 {

    private long _inicio;
    private long _fin;

    public AlgTimer_Clase02_Grupo06() {

        this._fin = 0;
        this._inicio = 0;
    }

    void startTimer() {
        this._inicio = System.currentTimeMillis();
    }

    double stopTimer() {
        this._fin = System.currentTimeMillis();
        double tiempo = (double) ((_fin - _inicio));

        return tiempo;
    }

}
