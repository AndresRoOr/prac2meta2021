/**
 * @file    Genetico.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 13/11/2020
 */
package es.meta.metaheuristicas_practica_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @brief Clase que implementa toda la funcionalidad del algoritmo genético
 * @class Genetico
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 13/11/2020
 */
public class Genetico {

    /**
     * @brief Clase que implementa la funcionalidad de un hilo para poder
     * reparar una sección de la población
     * @class CostTask
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     */
    private class CostTask implements Callable<ArrayList<Cromosomas>> {

        ///Atributos de la clase:
        private ArrayList<Cromosomas> Cromosomas;///<Conjunto de individuos
        private final int empieza;///<Posición del primer individuo
        private final int termina;///<Posición del último individuo

        /**
         * @brief Constructor parametrizado de la clase.
         * @author David Díaz Jiménez
         * @author Andrés Rojas Ortega
         * @date 16/11/2020
         * @param cromosomas ArrayList<Cromosomas>
         * @param empie int
         * @param termi int
         */
        public CostTask(ArrayList<Cromosomas> cromosomas, int empie, int termi) {
            Cromosomas = new ArrayList<>(cromosomas);
            empieza = empie;
            termina = termi;

        }

        @Override
        public ArrayList<Cromosomas> call() throws Exception {
            int numeroGenes = _archivoDatos.getTama_Solucion();

            ArrayList<Cromosomas> cromosomaReparado = new ArrayList<>();
            int i = 0;
            for (Cromosomas Cromosoma : Cromosomas) {

                if (i >= empieza && i <= termina) {

                    Set<Integer> cromosoma = Cromosoma.getCromosoma();

                    if (cromosoma.size() != numeroGenes) {

                        if (cromosoma.size() < numeroGenes) {

                            while (cromosoma.size() != numeroGenes) {
                                //Calcular mejor coste como en Greedy      
                                int gen = CalcularMayorAporte(cromosoma);
                                cromosoma.add(gen);
                            }

                            Cromosoma.setCromosoma(cromosoma);

                            cromosomaReparado.add(new Cromosomas(Cromosoma));

                        } else {

                            while (cromosoma.size() != numeroGenes) {
                                //Quitar los que menos aportan
                                int elemento = CalcularMenorAporte(cromosoma);
                                cromosoma.remove(elemento);
                            }

                            Cromosoma.setCromosoma(cromosoma);

                            cromosomaReparado.add(new Cromosomas(Cromosoma));
                        }
                    } else {
                        cromosomaReparado.add(new Cromosomas(Cromosoma));
                    }
                    i++;
                } else {
                    i++;
                }
            }
            return cromosomaReparado;
        }
    }

    ///Atributos de la clase:
    private Archivo _archivoDatos;///<Contiene los datos del problema
    private GestorLog _gestor;///<Gestor encargado de la creación del Log
    private int _elitismo;///<Número de cromosomas elites que almacenar
    private boolean _operadorMPX;///<True: el algoritmo de cruce a aplicar es MPX;
    ///False: el algoritmo de cruce a aplicar es corte en dos puntos
    private int _evaluaciones;///<Evaluaciones que se han realizado hasta el momento
    private final int _evaluacionesObjetivo;///<Número de evaluaciones máximo que realizar
    private final int _numeroCromosomas;///<Número de cromosomas que contiene cada población
    private final float _probMutacion;///<Probabilidad de que se produzca una mutación
    private final float _probReproduccion;///<Probabilidad de que dos individuos se 
    ///reproduzcan
    private final float _probMPX;///<Probabilidad de que un gen del padre se 
    ///incluya en el hijo
    private final boolean _generacional;///<Generacional o estacionario
    private int generacion;///<Generación actual
    private ArrayList<Cromosomas> _cromosomas;///<Almacena los cromosomas iniciales
    private ArrayList<Cromosomas> _cromosomasPadre;///<Almacena los cromosomas padre
    private ArrayList<Cromosomas> _cromosomasHijo;///<Almacena los cromosomas hijo
    private ArrayList<Float> _costes;///<Almacena los costes de cada cromosoma
    private ArrayList<Boolean> recalcularCostes;///<Indica si hay que recalcular los 
    ///costes del cromosoma de la posicion i.
    private ArrayList<Cromosomas> cromosomasElite;///<Almacena los cromosomas elites.
    private ExecutorService exec;
    private Cromosomas _mejorCromosoma;///<Mejor individuo de la generación

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
     * @param probMutacion float
     * @param probMpx float
     * @param numeroCromosomas int
     *
     */
    public Genetico(Archivo _archivoDatos, GestorLog gestor, int evaluaciones, int Elitismo, boolean OperadorMPX, float probReini,
            float probMutacion, float probMpx, int numeroCromosomas) {

        this.exec = Executors.newFixedThreadPool(2);
        this._archivoDatos = _archivoDatos;
        this._gestor = gestor;

        this._operadorMPX = OperadorMPX;
        this._probMutacion = probMutacion;
        this._probReproduccion = probReini;
        this._probMPX = probMpx;

        this._evaluaciones = 0;
        this._evaluacionesObjetivo = evaluaciones;

        this._numeroCromosomas = numeroCromosomas;

        this._cromosomas = new ArrayList<>();
        this._cromosomasPadre = new ArrayList<>();
        this._cromosomasHijo = new ArrayList<>();

        this.cromosomasElite = new ArrayList<>();

        this._mejorCromosoma = new Cromosomas(new HashSet<Integer>(), 0.0f);

        if (Elitismo == 0) {
            _elitismo = 1;
            _generacional = true;
        } else {
            this._elitismo = Elitismo;
            this._generacional = false;
        }

        generacion = 1;

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
            _cromosomas.add(new Cromosomas(cromosoma, 0.0f, true));
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

    /**
     * @brief Realiza el cruce de la poblacion utilizando el operador de cruce
     * en dos puntos.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 15/11/2020
     * @param alea Random_p Utilizado para generar números pseudoaleatorios
     */
    private void operadorCruce2puntos(Random_p alea) {

        //Recorremos el vector por parejas de padres
        for (int i = 0; i < _numeroCromosomas - 1; i += 2) {

            //Comprobamos si debemos realizar el cruce para la preja
            float probRepro = (float) alea.Randfloat(0, 1);

            if (probRepro <= _probReproduccion) {

                HashSet<Integer> hijo1 = new HashSet<>();
                HashSet<Integer> hijo2 = new HashSet<>();

                //Generamos los puntos de corte aleatoriamente
                int crosspoint1 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);
                int crosspoint2 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);

                //Comprobamos que los puntos de corte no coincidan
                while (crosspoint1 == crosspoint2) {
                    crosspoint2 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);
                }

                //Realizamos el cruce
                Iterator<Integer> iterator = _cromosomasPadre.get(i).getCromosoma().iterator();
                Iterator<Integer> iterator1 = _cromosomasPadre.get(i + 1).getCromosoma().iterator();

                int length = _archivoDatos.getTama_Solucion();
                for (int j = 0; j < length; j++) {

                    if (j < crosspoint1 || j > crosspoint2) {
                        hijo1.add(iterator.next());
                        hijo2.add(iterator1.next());
                    } else {
                        hijo1.add(iterator1.next());
                        hijo2.add(iterator.next());
                    }
                }

                //Almacenamos los hijos generados
                _cromosomasHijo.add(new Cromosomas(hijo1, 0.0f, true));
                _cromosomasHijo.add(new Cromosomas(hijo2, 0.0f, true));

            } else {

                //Guardamos los padres sin cruzar como resultado del cruce
                Cromosomas hijo1 = _cromosomasPadre.get(i);
                Cromosomas hijo2 = _cromosomasPadre.get(i + 1);

                _cromosomasHijo.add(new Cromosomas(hijo1.getCromosoma(), hijo1.getContribucion()));
                _cromosomasHijo.add(new Cromosomas(hijo2.getCromosoma(), hijo2.getContribucion()));

            }
        }
    }

    /**
     * @brief Realiza el cruce de la población utilizando el operador de cruce
     * MPX
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 17/11/2020
     * @param alea Random_p Semilla aleatoria utilizada para generar números
     */
    private void operadorCruceMPX(Random_p alea) {

        for (int i = 0; i <= _numeroCromosomas && _cromosomasHijo.size() < _numeroCromosomas; i += 2) {

            float probRepro = (float) alea.Randfloat(0, 1);

            int padre1 = i;
            int padre2 = i + 1;

            if (probRepro < _probReproduccion) {

                HashSet<Integer> cromosoma = new HashSet<>();

                for (Integer gen : _cromosomasPadre.get(padre1).getCromosoma()) {
                    float prob = (float) alea.Randfloat(0, 1);
                    if (prob > 0.5) {
                        cromosoma.add(gen);
                    }
                }

                for (Integer gen : _cromosomasPadre.get(padre2).getCromosoma()) {
                    cromosoma.add(gen);
                }

                _cromosomasHijo.add(new Cromosomas(cromosoma, 0.0f, true));

            } else {
                _cromosomasHijo.add(new Cromosomas(_cromosomasPadre.get(padre1).getCromosoma(), 0.0f));

                if (_cromosomasHijo.size() < _numeroCromosomas) {
                    _cromosomasHijo.add(new Cromosomas(_cromosomasPadre.get(padre2).getCromosoma(), 0.0f));
                }
            }

            if (i == _numeroCromosomas - 2) {
                i = 0;
            }
        }
    }

    /**
     * @brief Repara los cromosomas que no son una solución válida haciendo uso
     * de concurrencia.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     */
    private void repararConcurrente() {

        Future<ArrayList<Cromosomas>> future = null;
        Future<ArrayList<Cromosomas>> future2 = null;

        int tam = ((_numeroCromosomas) / 2) - 1;
        future = exec.submit(new CostTask(_cromosomasHijo, 0, tam));
        future2 = exec.submit(new CostTask(_cromosomasHijo, tam + 1, _numeroCromosomas - 1));

        try {

            _cromosomasHijo.clear();
            for (Cromosomas cromo : future.get()) {
                _cromosomasHijo.add(cromo);
            }
            for (Cromosomas cromo : future2.get()) {
                _cromosomasHijo.add(cromo);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Genetico.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Genetico.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @brief Calcula el gen que más coste aporta al individuo si lo añadimos
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     * @param cromosoma Setz<Integer> Material genético del individuo
     * @return elemento int El elemento que más aporta si lo añadimos al materal
     * genético
     */
    private int CalcularMayorAporte(Set<Integer> cromosoma) {                   //Revisar

        float mejor = 0.0f;
        int elemento = 0;

        HashSet<Integer> cromosomaReparado = new HashSet<>(cromosoma);
        int tamMatrix = _archivoDatos.getTama_Matriz();

        for (int i = 0; i < tamMatrix; i++) {

            if (!cromosomaReparado.contains(i)) {

                float costeMas = 0.0f;
                float coste;
                Iterator<Integer> iterator = cromosomaReparado.iterator();

                while (iterator.hasNext()) {
                    int k = iterator.next();
                    costeMas += _archivoDatos.getMatriz()[k][i];
                }

                coste = costeMas;

                if (coste > mejor) {
                    mejor = coste;
                    elemento = i;
                }

            }
        }

        return elemento;
    }

    /**
     * @brief Calcula el gen del individuo que menos coste aporta.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     * @param cromosoma Set<Integer> Material genético del individuo
     * @return elemenor int El elemento que menos coste aporta
     */
    private int CalcularMenorAporte(Set<Integer> cromosoma) {

        float aporte = 0.0f;
        Iterator<Integer> iterator = cromosoma.iterator();
        float menorAporte = Float.MAX_VALUE;
        int elemenor = -1;

        while (iterator.hasNext()) {

            Iterator<Integer> iterator2 = cromosoma.iterator();
            int i = iterator.next();

            while (iterator2.hasNext()) {

                int j = iterator2.next();
                aporte += _archivoDatos.getMatriz()[i][j];

            }

            if (aporte < menorAporte) {
                menorAporte = aporte;
                elemenor = i;
            }

            aporte = 0.0f;
        }

        return elemenor;
    }

    /**
     * @brief Operador de mutación.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     * @param ale Random_p Semilla aleatoria para generar números
     */
    private void operadorMutación(Random_p ale) {

        for (Cromosomas Cromosoma : _cromosomasHijo) {

            ArrayList<Integer> alelosMutados = new ArrayList<>();
            ArrayList<Integer> alelosNuevos = new ArrayList<>();

            Set<Integer> cromosoma = Cromosoma.getCromosoma();

            for (Integer gen : cromosoma) {

                double probabilidad = ale.Randfloat(0, 1);

                if (probabilidad < _probMutacion) {

                    Cromosoma.setRecalcular(true);
                    alelosMutados.add(gen);

                    boolean reemplazo = false;

                    while (reemplazo == false) {

                        int generado = ale.Randint(0, _archivoDatos.getTama_Matriz() - 1);

                        if (!cromosoma.contains(generado) && !alelosNuevos.contains(generado)) {
                            alelosNuevos.add(generado);
                            reemplazo = true;
                        }
                    }
                }
            }

            while (!alelosMutados.isEmpty() || !alelosNuevos.isEmpty()) {
                cromosoma.remove(alelosMutados.remove(0));
                cromosoma.add(alelosNuevos.remove(0));
            }

            Cromosoma.setCromosoma(cromosoma);
        }
    }

    /**
     * @brief Operador de elitismo
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     */
    private void operadorElitismo() {

        if (_generacional == false) {
            for (Cromosomas Mejores : cromosomasElite) {
                _cromosomasHijo.add(new Cromosomas(Mejores));
            }
        }

        Collections.sort(_cromosomasHijo);

        while (_cromosomasHijo.size() > _numeroCromosomas) {
            _cromosomasHijo.remove(0);
        }

        int i = 1;
        for (Cromosomas elite : cromosomasElite) {
            elite.setCromosoma(_cromosomasHijo.get(_numeroCromosomas - i).getCromosoma());
            elite.setContribucion(_cromosomasHijo.get(_numeroCromosomas - i).getContribucion());
            i++;
        }

        _cromosomas.clear();
        _cromosomas = new ArrayList<>(_cromosomasHijo);

        _cromosomasHijo.clear();
        _cromosomasPadre.clear();

        if (cromosomasElite.get(0).getContribucion() > _mejorCromosoma.getContribucion()) {
            _mejorCromosoma = new Cromosomas(cromosomasElite.get(0));
        }

        registroElites();
    }

    /**
     * @brief Calcula el coste de todos los individuos de la poblacion
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 17/11/2020
     * @param cromosomas ArrayList<Cromosomas> La población de individuos
     * @param ObtenerElite boolena Indica si la función debe calcular el élite
     * de la población
     */
    private void obtenerCostes(ArrayList<Cromosomas> cromosomas, boolean ObtenerElite) {

        float mejorCoste = 0.0f;

        for (Cromosomas cromosoma : cromosomas) {

            if (cromosoma.isRecalcular() == true || cromosoma.getContribucion() == 0.0f) {
                float coste = calcularCoste(cromosoma.getCromosoma());
                cromosoma.setContribucion(coste);
                _evaluaciones++;

                if (coste > mejorCoste) {

                    mejorCoste = coste;

                    if (ObtenerElite == true) {

                        if (mejorCoste > cromosomasElite.get(0).getContribucion()) {
                            cromosomasElite.add(new Cromosomas(new HashSet<>(cromosoma.getCromosoma()), mejorCoste));
                            Collections.sort(cromosomasElite);
                            if (cromosomasElite.size() > _elitismo) {
                                cromosomasElite.remove(0);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @brief Calcula el coste de una solución
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 17/11/2020
     * @param cromosoma Set<Integer> Material genético de un individuo
     * @return coste float Coste calculado del individuo solución
     */
    private float calcularCoste(Set<Integer> cromosoma) {

        float coste = 0.0f;
        Object[] sol = cromosoma.toArray();
        int length = sol.length;

        for (int i = 0; i < length - 1; i++) {
            int a = (int) sol[i];
            for (int j = i + 1; j < length; j++) {
                int b = (int) sol[j];
                coste += _archivoDatos.getMatriz()[a][b];
            }
        }
        return coste;
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
