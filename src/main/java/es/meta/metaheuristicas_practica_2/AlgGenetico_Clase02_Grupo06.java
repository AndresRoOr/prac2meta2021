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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @brief Clase que implementa toda la funcionalidad del algoritmo genético
 * @class Genetico
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @date 13/11/2020
 */
public final class AlgGenetico_Clase02_Grupo06 {
    
    /**
     * @brief Clase que implementa la funcionalidad de un hilo para poder
     * reparar una sección de la población
     * @class RepairTask
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     */
    private class RepairTask implements Callable<ArrayList<AlgCromosomas_Clase02_Grupo06>> {

        ///Atributos de la clase:
        private final ArrayList<AlgCromosomas_Clase02_Grupo06> Cromosomas;///<Conjunto de individuos
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
        public RepairTask(ArrayList<AlgCromosomas_Clase02_Grupo06> cromosomas, int empie, int termi) {
            Cromosomas = cromosomas;
            empieza = empie;
            termina = termi;

        }

        @Override
        public ArrayList<AlgCromosomas_Clase02_Grupo06> call() throws Exception {
            int numeroGenes = _archivoDatos.getTama_Solucion();

            ArrayList<AlgCromosomas_Clase02_Grupo06> cromosomaReparado = new ArrayList<>();
            for (int i=empieza;i<=termina;i++) {

                Set<Integer> cromosoma = Cromosomas.get(i).getCromosoma();

                if (cromosoma.size() != numeroGenes) {

                    if (cromosoma.size() < numeroGenes) {

                        while (cromosoma.size() != numeroGenes) {
                            //Calcular mejor coste como en Greedy      
                            int gen = CalcularMayorAporte(cromosoma);
                            cromosoma.add(gen);
                        }

                        Cromosomas.get(i).setCromosoma(cromosoma);

                        cromosomaReparado.add(new AlgCromosomas_Clase02_Grupo06(Cromosomas.get(i)));

                    } else {

                        while (cromosoma.size() != numeroGenes) {
                            //Quitar los que menos aportan
                            int elemento = CalcularMenorAporte(cromosoma);
                            cromosoma.remove(elemento);
                        }

                        Cromosomas.get(i).setCromosoma(cromosoma);

                        cromosomaReparado.add(new AlgCromosomas_Clase02_Grupo06(Cromosomas.get(i)));
                    }
                } else {
                    cromosomaReparado.add(new AlgCromosomas_Clase02_Grupo06(Cromosomas.get(i)));
                }

            }
            return cromosomaReparado;
        }

    }
    
    /**
     * @brief Clase que implementa la funcionalidad de un hilo para poder
     * aplicar el operador de evaluación a una población
     * @class CalcCostTask
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 22/11/2020
     */
    private class CalcCostTask implements Callable<Float> {

        ///Atributos de la clase:
        private final ArrayList<AlgCromosomas_Clase02_Grupo06> Cromosomas;///<Conjunto de individuos
        private final int empieza;///<Posición del primer individuo
        private final int termina;///<Posición del último individuo

        /**
         * @brief Constructor parametrizado de la clase.
         * @author David Díaz Jiménez
         * @author Andrés Rojas Ortega
         * @date 22/11/2020
         * @param cromosomas ArrayList<Cromosomas>
         * @param empie int
         * @param termi int
         */
        public CalcCostTask(ArrayList<AlgCromosomas_Clase02_Grupo06> cromosomas, int empie, int termi) {
            Cromosomas =cromosomas;
            empieza = empie;
            termina = termi;
        }

        @Override
        public Float call() throws Exception {
            float mejorCoste=0.0f;
            for (int i=empieza;i<=termina;i++) {

                if(Cromosomas.get(i).isRecalcular() || Cromosomas.get(i).getContribucion() == 0.0f){
                    float coste = calcularCoste(Cromosomas.get(i).getCromosoma());
                    Cromosomas.get(i).setContribucion(coste);
                    _evaluaciones.incrementAndGet();
                }
            }
            return mejorCoste;
        }
    }

    ///Atributos de la clase:
    private final AlgArchivo_Clase02_Gropo_06 _archivoDatos;///<Contiene los datos del problema
    private final AlgGestorLog_Clase02_Grupo06 _gestor;///<Gestor encargado de la creación del Log
    private final int _elitismo;///<Número de los mejores individuos de la generación anterior que pueden pasar a la siguiente generación
    private final boolean _operadorMPX;///<True: el algoritmo de cruce a aplicar es MPX;
    ///False: el algoritmo de cruce a aplicar es corte en dos puntos 
    private final AtomicInteger _evaluaciones;///<Evaluaciones que se han realizado hasta el momento
    private final int _evaluacionesObjetivo;///<Número máximo de evaluaciones a realizar
    private final int _numeroCromosomas;///<Tamaño de la población
    private final float _probMutacion;///<Probabilidad de mutación para cada gen
    private final float _probReproduccion;///<Probabilidad de que dos individuos se crucen
    private final float _probMpx;///<Probabilidad de que un gen del padre se 
    ///incluya en el hijo
    private final boolean _generacional;///<Generacional o estacionario
    private int generacion;///<Indica la generación actual
    private ArrayList<AlgCromosomas_Clase02_Grupo06> _vcromosomas;///<Población inicial
    private ArrayList<AlgCromosomas_Clase02_Grupo06> _vcromosomasPadre;///<Población resultante del torneo binario
    private ArrayList<AlgCromosomas_Clase02_Grupo06> _vcromosomasHijo;///<Población resultante del cruce
    private AlgCromosomas_Clase02_Grupo06 _mejorCromosoma;///<Almacena el mejor individuo hasta el momento
    private ArrayList<AlgCromosomas_Clase02_Grupo06> cromosomasElite;///<Alamacena a los elites de la generación
    private int _genSinMejora;
    private float costeSinMejora;

    /**
     * @brief Constructor parametrizado de la clase Genetico
     * @author Andrés Rojas Ortega
     * @author David Díaz Jiménez
     * @date 13/11/2020
     * @param _archivoDatos AlgArchivo_Clase02_Gropo_06
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
    public AlgGenetico_Clase02_Grupo06(AlgArchivo_Clase02_Gropo_06 _archivoDatos, AlgGestorLog_Clase02_Grupo06 gestor, int evaluaciones, int Elitismo, boolean OperadorMPX, float probReini,
            float probMutacion, float probMpx, int numeroCromosomas) {

        this._archivoDatos = _archivoDatos;
        this._gestor = gestor;
        this._operadorMPX = OperadorMPX;
        this._probMutacion = probMutacion;
        this._probReproduccion = probReini;
        this._probMpx = probMpx;
        this._evaluaciones = new AtomicInteger(0);
        this._evaluacionesObjetivo = evaluaciones;
        this._numeroCromosomas = numeroCromosomas;
        this._vcromosomas = new ArrayList<>();
        this._vcromosomasPadre = new ArrayList<>();
        this._vcromosomasHijo = new ArrayList<>();
        this.cromosomasElite = new ArrayList<>();
        this._mejorCromosoma = new AlgCromosomas_Clase02_Grupo06(new HashSet<>(), 0.0f);
        this._genSinMejora = 0;

        if (Elitismo == 0) {
            this._elitismo= 1;
            this._generacional = false;
        } else {
            this._elitismo = Elitismo;
            this._generacional = true;
        }

        this.generacion = 1;
        this.costeSinMejora = 0.0f;

    }

    /**
     * @brief Algoritmo principal.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 18/11/2020
     * @param aleatorioSemilla AlgRandom_p_Clase02_Grupo06 Semilla aleatoria
     */
    void genetico(AlgRandom_p_Clase02_Grupo06 aleatorioSemilla) {

        AlgRandom_p_Clase02_Grupo06 aleatorio = aleatorioSemilla;

        generarCromosomasIniciales(aleatorio);

        registroConfiguración();

        obtenerCostes(_vcromosomas, true);

        Collections.sort(cromosomasElite);

        while (_evaluaciones.getAcquire() < _evaluacionesObjetivo) {

            operadorSeleccion(aleatorio);

            operadorReproduccion(aleatorio);

            repararConcurrente();
            
            operadorMutación(aleatorio);

            obtenerCostesConcurrente(_vcromosomasHijo);

            operadorElitismo();

            generacion++;
            
        }

        _vcromosomasHijo.clear();
        _vcromosomasPadre.clear();
        _vcromosomas.clear();
        cromosomasElite.clear();
        
    }

    /**
     * @brief Genera el conjunto de cromosomas inicial del algoritmo genético.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 13/11/2020
     * @param alea AlgRandom_p_Clase02_Grupo06 Semilla utilizada para generar números
 pseudoaleatorios
     */
    private void generarCromosomasIniciales(AlgRandom_p_Clase02_Grupo06 alea) {

        int tamCromosoma = _archivoDatos.getTama_Solucion();
        for (int i = 0; i < _numeroCromosomas; i++) {

            Set<Integer> cromosoma = new HashSet<>();

            while (cromosoma.size() < tamCromosoma) {
                int alelo = alea.Randint(0, _archivoDatos.getTama_Matriz() - 1);
                if (!cromosoma.contains(alelo)) {
                    cromosoma.add(alelo);
                }
            }
            _vcromosomas.add(new AlgCromosomas_Clase02_Grupo06(cromosoma, 0.0f, true));
        }
        
        while(cromosomasElite.size()< _elitismo){
            cromosomasElite.add(new AlgCromosomas_Clase02_Grupo06(_vcromosomas.get(0).getCromosoma(), _vcromosomas.get(0).getContribucion()));
        }
    }
    
    /**
     * @brief Operador de evaluación haciendo uso de concurrencia.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 22/11/2020
     * @param cromosomas ArrayList<Cromosomas>
     * @param ObtenerElite boolean
     */
    private void obtenerCostesConcurrente(ArrayList<AlgCromosomas_Clase02_Grupo06> cromosomas) {
        
        Future<Float> future;
        Future<Float> future2;
        Future<Float> future3;
        Future<Float> future4;
        
        ArrayList<AlgCromosomas_Clase02_Grupo06> copia = new ArrayList<>(cromosomas);

        int tam = ((_numeroCromosomas) / 4) - 1;
        future = AlgMain_Clase02_Grupo06.exec.submit(new CalcCostTask(copia,0, tam));
        future2 = AlgMain_Clase02_Grupo06.exec.submit(new CalcCostTask(copia,tam + 1, tam * 2));
        future3 = AlgMain_Clase02_Grupo06.exec.submit(new CalcCostTask(copia, tam * 2 + 1, tam * 3));
        future4 = AlgMain_Clase02_Grupo06.exec.submit(new CalcCostTask(copia, tam * 3 + 1, _numeroCromosomas - 1));

        try {

            float mejor1=future.get();
            float mejor2=future2.get();
            float mejor3=future3.get();
            float mejor4=future4.get();
            

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(AlgGenetico_Clase02_Grupo06.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
    private void obtenerCostes(ArrayList<AlgCromosomas_Clase02_Grupo06> cromosomas, boolean ObtenerElite) {

        float mejorCoste = 0.0f;

        for (AlgCromosomas_Clase02_Grupo06 cromosoma : cromosomas) {

            if (cromosoma.isRecalcular() == true || cromosoma.getContribucion() == 0.0f) {
                float coste = calcularCoste(cromosoma.getCromosoma());
                cromosoma.setContribucion(coste);
                _evaluaciones.incrementAndGet();

                if (coste > mejorCoste) {

                    mejorCoste = coste;

                    if (ObtenerElite == true) {

                        if (mejorCoste > cromosomasElite.get(0).getContribucion()) {
                            cromosomasElite.add(new AlgCromosomas_Clase02_Grupo06(new HashSet<>(cromosoma.getCromosoma()), mejorCoste));
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

    /**
     * @brief Selecciona el conjunto de individuos que se reproduciran en la
     * generación actual aplicando torneo binario.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 15/11/2020
     * @param ale AlgRandom_p_Clase02_Grupo06 Utilizado para generar números pseudoaleatoios
     */
    private void operadorSeleccion(AlgRandom_p_Clase02_Grupo06 ale) {

        for (int i = 0; i < _numeroCromosomas; i++) {

            int candidato1 = ale.Randint(0, _numeroCromosomas - 1);
            int candidato2 = ale.Randint(0, _numeroCromosomas - 1);

            while (candidato1 == candidato2) {
                candidato2 = ale.Randint(0, _numeroCromosomas - 1);
            }

            if (_vcromosomas.get(candidato1).getContribucion() > _vcromosomas.get(candidato2).getContribucion()) {
                _vcromosomasPadre.add(new AlgCromosomas_Clase02_Grupo06(_vcromosomas.get(candidato1)));
            } else {
                _vcromosomasPadre.add(new AlgCromosomas_Clase02_Grupo06(_vcromosomas.get(candidato2)));
            }
        }

        _vcromosomas.clear();
    }

    /**
     * @brief Función encargada de realizar la reproducción y generación de la
     * siguiente generación del algoritmo genético.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 15/11/2020
     * @param alea AlgRandom_p_Clase02_Grupo06 Utilizado para generar números pseudoaleatorios
     */
    private void operadorReproduccion(AlgRandom_p_Clase02_Grupo06 alea) {

        if (_operadorMPX == true) {

            operadorCruceMPX(alea);

        } else {

            operadorCruce2puntos(alea);
        }

        _vcromosomasPadre.clear();
    }

    /**
     * @brief Realiza el cruce de la poblacion utilizando el operador de cruce
     * en dos puntos.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 15/11/2020
     * @param alea AlgRandom_p_Clase02_Grupo06 Utilizado para generar números pseudoaleatorios
     */
    private void operadorCruce2puntos(AlgRandom_p_Clase02_Grupo06 alea) {

        //Recorremos el vector por parejas de padres
        for (int i = 0; i < _numeroCromosomas; i += 2) {

            //Comprobamos si debemos realizar el cruce para la preja
            float probRepro = (float) alea.Randfloat(0, 1);

            if (probRepro < _probReproduccion) {

                HashSet<Integer> hijo1 = new HashSet<>();
                HashSet<Integer> hijo2 = new HashSet<>();

                //Generamos los puntos de corte aleatoriamente
                int crosspoint1 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);
                int crosspoint2 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);

                //Comprobamos que los puntos de corte no coincidan
                while (crosspoint1 == crosspoint2) {
                    crosspoint2 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);
                }
                
                if(crosspoint1 > crosspoint2){
                    int aux = crosspoint2;
                    crosspoint2 = crosspoint1;
                    crosspoint1 = aux;
                }

                //Realizamos el cruce
                Iterator<Integer> iterator = _vcromosomasPadre.get(i).getCromosoma().iterator();
                Iterator<Integer> iterator1 = _vcromosomasPadre.get(i + 1).getCromosoma().iterator();

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
                _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(hijo1, 0.0f, true));
                _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(hijo2, 0.0f, true));

            } else {

                //Guardamos los padres sin cruzar como resultado del cruce
                AlgCromosomas_Clase02_Grupo06 hijo1 = _vcromosomasPadre.get(i);
                AlgCromosomas_Clase02_Grupo06 hijo2 = _vcromosomasPadre.get(i + 1);

                _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(hijo1.getCromosoma(), hijo1.getContribucion()));
                _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(hijo2.getCromosoma(), hijo2.getContribucion()));

            }
        }
    }

    /**
     * @brief Realiza el cruce de la población utilizando el operador de cruce
     * MPX
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 17/11/2020
     * @param alea AlgRandom_p_Clase02_Grupo06 Semilla aleatoria utilizada para generar números
     */
    private void operadorCruceMPX(AlgRandom_p_Clase02_Grupo06 alea) {

        for (int i = 0; i <= _numeroCromosomas && _vcromosomasHijo.size() < _numeroCromosomas; i += 2) {

            float probRepro = (float) alea.Randfloat(0, 1);

            int padre1 = i;
            int padre2 = i + 1;

            if (probRepro < _probReproduccion) {

                HashSet<Integer> cromosoma = new HashSet<>();

                for (Integer gen : _vcromosomasPadre.get(padre1).getCromosoma()) {
                    float prob = (float) alea.Randfloat(0, 1);
                    if (prob > 1-_probMpx) {
                        cromosoma.add(gen);
                    }
                }

                for (Integer gen : _vcromosomasPadre.get(padre2).getCromosoma()) {
                    cromosoma.add(gen);
                }

                _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(cromosoma, 0.0f, true));

            } else {
                _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(_vcromosomasPadre.get(padre1).getCromosoma(), 0.0f));

                if (_vcromosomasHijo.size() < _numeroCromosomas) {
                    _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(_vcromosomasPadre.get(padre2).getCromosoma(), 0.0f));
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

        Future<ArrayList<AlgCromosomas_Clase02_Grupo06>> future;
        Future<ArrayList<AlgCromosomas_Clase02_Grupo06>> future2;
        Future<ArrayList<AlgCromosomas_Clase02_Grupo06>> future3;
        Future<ArrayList<AlgCromosomas_Clase02_Grupo06>> future4;

        ArrayList<AlgCromosomas_Clase02_Grupo06> copia = new ArrayList<>(_vcromosomasHijo);

        int tam = ((_numeroCromosomas) / 4) - 1;
        future = AlgMain_Clase02_Grupo06.exec.submit(new RepairTask(copia, 0, tam));
        future2 = AlgMain_Clase02_Grupo06.exec.submit(new RepairTask(copia, tam + 1, tam * 2));
        future3 = AlgMain_Clase02_Grupo06.exec.submit(new RepairTask(copia, tam * 2 + 1, tam * 3));
        future4 = AlgMain_Clase02_Grupo06.exec.submit(new RepairTask(copia, tam * 3 + 1, _numeroCromosomas - 1));

        try {
            _vcromosomasHijo.clear();
            for (AlgCromosomas_Clase02_Grupo06 cromo : future.get()) {
                _vcromosomasHijo.add(cromo);
            }
            for (AlgCromosomas_Clase02_Grupo06 cromo : future2.get()) {
                _vcromosomasHijo.add(cromo);
            }
            for (AlgCromosomas_Clase02_Grupo06 cromo : future3.get()) {
                _vcromosomasHijo.add(cromo);
            }
            for (AlgCromosomas_Clase02_Grupo06 cromo : future4.get()) {
                _vcromosomasHijo.add(cromo);
            }

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(AlgGenetico_Clase02_Grupo06.class.getName()).log(Level.SEVERE, null, ex);
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
    private int CalcularMayorAporte(Set<Integer> cromosoma) {

        float mejor = 0.0f;
        int elemento = 0;

        HashSet<Integer> cromosomaReparado = new HashSet<>(cromosoma);
        int tamMatrix = _archivoDatos.getTama_Matriz();

        for (int i = 0; i < tamMatrix; i++) {

            if (!cromosomaReparado.contains(i)) {

                float costeMas = 0.0f;
                float coste;

                for(Integer k : cromosomaReparado) {
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
        
        float menorAporte = Float.MAX_VALUE;
        Integer elemenor = -1;

        for(Integer gen : cromosoma){

            for(Integer gen2 : cromosoma){
                aporte += _archivoDatos.getMatriz()[gen][gen2];
            }

            if (aporte < menorAporte) {
                menorAporte = aporte;
                elemenor = gen;
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
     * @param ale AlgRandom_p_Clase02_Grupo06 Semilla aleatoria para generar números
     */
    private void operadorMutación(AlgRandom_p_Clase02_Grupo06 ale) {

        for (AlgCromosomas_Clase02_Grupo06 Cromosoma : _vcromosomasHijo) {

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
     * @brief Registra los cromosomas inciales en un archivo
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 18/11/2020
     */
    private void registroConfiguración() {
        _gestor.escribirArchivo("Cromosomas Iniciales: ");
        for (AlgCromosomas_Clase02_Grupo06 cromosoma : _vcromosomas) {
            _gestor.escribirArchivo(cromosoma.getCromosoma().toString());
        }
    }

    /**
     * @brief Registra la información de los individuos élites en un archivo
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 18/11/2020
     */
    private void registroElites() {
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Generación: " + generacion + ", " + _elitismo + " mejores individuos de la población");
        for (AlgCromosomas_Clase02_Grupo06 cromosoma : cromosomasElite) {
            _gestor.escribirArchivo(cromosoma.getCromosoma().toString());
        }
        _gestor.escribirArchivo("Mejor coste hasta el momento: " + _mejorCromosoma.getContribucion());
    }

    /**
     * @brief Operador de elitismo
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 16/11/2020
     */
    private void operadorElitismo() {

        if (_generacional == true) {
            for (AlgCromosomas_Clase02_Grupo06 Mejores : cromosomasElite) {
                _vcromosomasHijo.add(new AlgCromosomas_Clase02_Grupo06(Mejores));
            }
        }

        Collections.sort(_vcromosomasHijo);

        while (_vcromosomasHijo.size() > _numeroCromosomas) {
            _vcromosomasHijo.remove(0);
        }

        int i = 1;
        for (AlgCromosomas_Clase02_Grupo06 elite : cromosomasElite) {
            elite.setCromosoma(_vcromosomasHijo.get(_numeroCromosomas - i).getCromosoma());
            elite.setContribucion(_vcromosomasHijo.get(_numeroCromosomas - i).getContribucion());
            i++;
        }

        _vcromosomas.clear();
        _vcromosomas = new ArrayList<>(_vcromosomasHijo);

        _vcromosomasHijo.clear();
        _vcromosomasPadre.clear();

        if (cromosomasElite.get(0).getContribucion() > _mejorCromosoma.getContribucion()) {
            _mejorCromosoma = new AlgCromosomas_Clase02_Grupo06(cromosomasElite.get(0));
        }

        registroElites();
    }

    /**
     * @brief Resgistra los resultados del algoritmo en un archivo.
     * @author David Díaz Jiménez
     * @author Andrés Rojas Ortega
     * @date 18/11/2020
     */
    void PresentarResultados() {

        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Resultados");

        float coste = calcularCoste(_mejorCromosoma.getCromosoma());
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Mejor coste: " + coste);
        _gestor.escribirArchivo("Mejor cromosoma: " + _mejorCromosoma.getCromosoma());
        
        AlgMain_Clase02_Grupo06.console.presentarSalida("Mejor Coste:  " + _mejorCromosoma.getContribucion());
        AlgMain_Clase02_Grupo06.console.presentarSalida("");

    }
}
