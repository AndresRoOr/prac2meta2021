/** @file    Random_p.java
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 03/10/2020
 */
package es.meta.metaheuristicas_practica_2;

/**
 * @brief Clase para generar números aleatorios
 * @class Random_p
 * @author David Díaz Jiménez
 * @date 03/10/2020
 */
public class Random_p {

    long Seed = 0L;
    public static final int MASK = 2147483647;
    public static final int PRIME = 65539;
    public static double SCALE = 0.4656612875e-9;

    void Set_random(long x) {
        /* Inicializa la semilla al valor x.
       Solo debe llamarse a esta funcion una vez en todo el programa */

        Seed = (long) x;
    }

    long Get_random() /* Devuelve el valor actual de la semilla */ {
        return Seed;
    }

    double Rand() /* Genera un numero aleatorio real en el intervalo [0,1[
       (incluyendo el 0 pero sin incluir el 1) */ {
        return ((Seed = ((Seed * PRIME) & MASK)) * SCALE);
    }

    int Randint(int low, int high) /* Genera un numero aleatorio entero en {low,...,high} */ {
        return (int) (low + (high - (low) + 1) * Rand());
    }

    double Randfloat(float low, float high) /* Genera un numero aleatorio real en el intervalo [low,...,high[
       (incluyendo 'low' pero sin incluir 'high') */ {
        return (low + (high - (low)) * Rand());
    }

}
