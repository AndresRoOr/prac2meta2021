/**
 * @file    Genetico.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 13/11/2020
 */
package es.meta.metaheuristicas_practica_2;

import com.sun.codemodel.internal.JExpr;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @brief Clase que implementa toda la funcionalidad del algoritmo genético
 * @class Genetico
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 13/11/2020
 */
public class Genetico {

    ///Atributos de la clase:
    Archivo _archivoDatos;///<Contiene los datos del problema
    private GestorLog _gestor;///<Gestor encargado de la creación del Log
    int _elitismo;///<Número de cromosomas elites que almacenar
    boolean _operadorMPX;///<True: el algoritmo de cruce a aplicar es MPX;
    ///False: el algoritmo de cruce a aplicar es corte en dos puntos
    int _evaluaciones;///<Evaluaciones que se han realizado hasta el momento
    int _evaluacionesObjetivo;///<Número de evaluaciones máximo que realizar
    int _numeroCromosomas;///<Número de cromosomas que contiene cada población
    float _probMutacion;///<Probabilidad de que se produzca una mutación
    float _probReproduccion;///<Probabilidad de que dos individuos se 
    ///reproduzcan
    ArrayList<Cromosomas> _cromosomas;///<Almacena los cromosomas iniciales
    ArrayList<Cromosomas> _cromosomasPadre;///<Almacena los cromosomas padre
    ArrayList<Cromosomas> _cromosomasHijo;///<Almacena los cromosomas hijo
    ArrayList<Float> _costes;///<Almacena los costes de cada cromosoma
    ArrayList<Boolean> recalcularCostes;///<Indica si hay que recalcular los 
    ///costes del cromosoma de la posicion i.
    ArrayList<Cromosomas> cromosomasElite;///<Almacena los cromosomas elites.

    /**
     * @brief Constructor parametrizado de la clase Genetico
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 13/11/2020
     * @param _archivoDatos Archivo
     * @param gestor Gestor
     * @param evaluaciones int
     * @param Elitismo int
     * @param OperadorMPX boolean
     * @param probReini float
     * @apram probMutacion float
     * 
     */
    public Genetico(Archivo _archivoDatos, GestorLog gestor, int evaluaciones, int Elitismo, boolean OperadorMPX, float probReini,
             float probMutacion) {
        this._archivoDatos = _archivoDatos;
        this._gestor = gestor;
        this._elitismo = Elitismo;
        this._operadorMPX = OperadorMPX;

        this._probMutacion = probMutacion;
        this._probReproduccion = probReini;

        _evaluaciones = 0;
        _evaluacionesObjetivo = evaluaciones;

        _numeroCromosomas = 50;

        _cromosomas = new ArrayList<>();
        _cromosomasPadre = new ArrayList<>();
        _cromosomasHijo = new ArrayList<>();
        _costes = new ArrayList<>();

        cromosomasElite = new ArrayList<>();

    }

    /**
     * @brief Genera el conjunto de cromosomas inicial del algoritmo genético.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 13/11/2020
     * @param alea Random_p Semilla utilizada para generar números 
     * pseudoaleatorios
     */
    void generarCromosomasIniciales(Random_p alea) {

        for (int i = 0; i < _numeroCromosomas; i++) {

            Set<Integer> cromosoma = new HashSet<>();

            while (cromosoma.size() < _archivoDatos.getTama_Solucion()) {

                int alelo = alea.Randint(0, _archivoDatos.getTama_Matriz() - 1);
                if (!cromosoma.contains(alelo)) {
                    cromosoma.add(alelo);
                }
            }
            _cromosomas.add(new Cromosomas(cromosoma, 0.0f,true));
        }
    }
    
    /**
     * @brief Función encargada de realizar la reproducción y generación de la
     * siguiente generación del algoritmo genético.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 15/11/2020
     * @param alea Random_p Utilizado para generar números pseudoaleatorios
     */
    private void operadorReproduccion(Random_p alea) {

        if (_operadorMPX == true) {

            operadorCruceMPX(alea);
            
        } else {

            operadorCruce2puntos(alea);
        }
        
        _cromosomasPadre.clear();
    }
    
    /**
     * @brief Selecciona el conjunto de individuos que se reproduciran en la
     * generación actual aplicando torneo binario.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 15/11/2020
     * @param ale Random_p Utilizado para generar números pseudoaleatoios
     */
    private void operadorSeleccion(Random_p ale) {

        for (int i = 0; i < _numeroCromosomas; i++) {

            int candidato1 = ale.Randint(0, _numeroCromosomas - 1);
            int candidato2 = ale.Randint(0, _numeroCromosomas - 1);

            while (candidato1 == candidato2) {
                candidato2 = ale.Randint(0, _numeroCromosomas - 1);
            }

            if (_cromosomas.get(candidato1).getContribucion() > _cromosomas.get(candidato2).getContribucion()) {
                _cromosomasPadre.add(new Cromosomas(_cromosomas.get(candidato1)));
            } else {
                _cromosomasPadre.add(new Cromosomas(_cromosomas.get(candidato2)));;
            }
        }
        
        _cromosomas.clear();
    }

    void PresentarResultados() {

        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Resultados");
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Mejor coste: " + cromosomasElite.get(cromosomasElite.size() - 1).getContribucion());

        Main.console.presentarSalida("");

        System.out.println("");
    }

}
