/**
 * @file    Genetico.java
 * @author Andrés Rojas Ortega
 * @author David Díaz Jiménez
 * @version 1.0
 * @date 13/11/2020
 */
package es.meta.metaheuristicas_practica_2;

import java.util.ArrayList;
import java.util.Collection;
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
public final class Genetico {

    private class CostTask implements Callable<ArrayList<Cromosomas>> {

        private ArrayList<Cromosomas> Cromosomas;
        private final int empieza;
        private final int termina;

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

    private class CalcCostTask implements Callable<Float> {

        private ArrayList<Cromosomas> Cromosomas;
        private final int empieza;
        private final int termina;
        private final boolean obtenerElite;

        public CalcCostTask(ArrayList<Cromosomas> cromosomas, boolean obtenerElite, int empie, int termi) {
            Cromosomas = new ArrayList<>(cromosomas);
            this.obtenerElite = obtenerElite;
            empieza = empie;
            termina = termi;

        }

        @Override
        public Float call() throws Exception {
            float mejorCoste = 0.0f;
            for (int i = empieza; i <= termina; i++) {

                if (Cromosomas.get(i).isRecalcular() == true || Cromosomas.get(i).getContribucion() == 0.0f) {
                    float coste = calcularCoste(Cromosomas.get(i).getCromosoma());
                    Cromosomas.get(i).setContribucion(coste);
                    _evaluaciones++;

                    if (coste > mejorCoste) {

                        mejorCoste = coste;

                        if (obtenerElite == true) {

                            if (mejorCoste > cromosomasElite.get(0).getContribucion()) {
                                cromosomasElite.add(new Cromosomas(new HashSet<>(Cromosomas.get(i).getCromosoma()), mejorCoste));
                                Collections.sort(cromosomasElite);
                                if (cromosomasElite.size() > _elitismo) {
                                    cromosomasElite.remove(0);
                                }
                            }
                        }
                    }
                }
            }
            return mejorCoste;
        }

    }

    private final Archivo _archivoDatos;///<Contiene los datos del problema
    private final GestorLog _gestor;///<Gestor encargado de la creación del Log
    private final int _elitismo;
    private final boolean _operadorMPX;
    private int _evaluaciones;
    private final int _evaluacionesObjetivo;
    private final int _numeroCromosomas;

    private final float _probMutacion;
    private final float _probReproduccion;
    private final float _probMpx;

    private final boolean _generacional;

    private int generacion;

    //private ArrayList<Float> _costes;
    private ArrayList<Cromosomas> _vcromosomas;
    private ArrayList<Cromosomas> _vcromosomasPadre;
    private ArrayList<Cromosomas> _vcromosomasHijo;

    private Cromosomas _mejorCromosoma;

    private ArrayList<Cromosomas> cromosomasElite;

    private ExecutorService exec;
    private int _numHilos;

    public Genetico(Archivo _archivoDatos, GestorLog gestor, int evaluaciones, int Elitismo, boolean OperadorMPX, float probReini,
            float probMutacion, float probMpx, int numeroCromosomas, int numHilos) {

        this.exec = Executors.newFixedThreadPool(numHilos);
        this._archivoDatos = _archivoDatos;
        this._gestor = gestor;

        this._operadorMPX = OperadorMPX;
        this._probMutacion = probMutacion;
        this._probReproduccion = probReini;
        this._probMpx = probMpx;

        this._evaluaciones = 0;
        this._evaluacionesObjetivo = evaluaciones;

        this._numeroCromosomas = numeroCromosomas;

        this._vcromosomas = new ArrayList<>();
        this._vcromosomasPadre = new ArrayList<>();
        this._vcromosomasHijo = new ArrayList<>();

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

        _numHilos = numHilos;

    }

    void genetico(Random_p aleatorioSemilla) {

        Random_p aleatorio = aleatorioSemilla;

        generarCromosomasIniciales(aleatorio);

        registroConfiguración();

        cromosomasElite.add(new Cromosomas(_vcromosomas.get(0).getCromosoma(), _vcromosomas.get(0).getContribucion()));

        obtenerCostes(_vcromosomas, true);

        Collections.sort(cromosomasElite);

        while (_evaluaciones < _evaluacionesObjetivo) {

            operadorSeleccion(aleatorio);

            operadorReproduccion(aleatorio);

            repararConcurrente();

            operadorMutación(aleatorio);

            obtenerCostesConcurrente(_vcromosomasHijo, false);

            operadorElitismo();

            generacion++;

        }

        _vcromosomasHijo.clear();
        _vcromosomasPadre.clear();
        _vcromosomas.clear();
        cromosomasElite.clear();
        exec.shutdown();

    }

    private void generarCromosomasIniciales(Random_p alea) {

        int tamCromosoma = _archivoDatos.getTama_Solucion();
        for (int i = 0; i < _numeroCromosomas; i++) {

            Set<Integer> cromosoma = new HashSet<>();

            while (cromosoma.size() < tamCromosoma) {
                int alelo = alea.Randint(0, _archivoDatos.getTama_Matriz() - 1);
                if (!cromosoma.contains(alelo)) {
                    cromosoma.add(alelo);
                }
            }
            _vcromosomas.add(new Cromosomas(cromosoma, 0.0f, true));
        }
    }

    private void obtenerCostesConcurrente(ArrayList<Cromosomas> cromosomas, boolean ObtenerElite) {
        float mejorCoste = 0.0f;

        ArrayList<Future<Float>> future = new ArrayList<>();

        for (int i = 0; i < _numHilos; i++) {
            future.add(null);
        }

        ArrayList<Cromosomas> copia = new ArrayList<>(_vcromosomasHijo);

        int tam = ((_numeroCromosomas) / _numHilos) - 1;
        int tamIni = 0;
        int tamFin = tam;

        for (int i = 0; i < _numHilos; i++) {
            if (i == _numHilos-1) {
                tamIni = (tam * (i - 1)) + 1;
                tamFin = _numeroCromosomas - 1;
            }
            future.set(i, exec.submit(new CalcCostTask(cromosomas, ObtenerElite, tamIni, tamFin)));
            tamIni = tamFin + 1;
            tamFin += tam;
        }

        try {
            ArrayList<Float> mejor = new ArrayList<>();
            
            for(int i=0; i<_numHilos;i++){
                mejor.add(future.get(i).get());
            }

            for(int i=0; i<_numHilos;i++){
                if(mejor.get(i)>mejorCoste){
                    mejorCoste=mejor.get(i);
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Genetico.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Genetico.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

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

    private void operadorSeleccion(Random_p ale) {

        for (int i = 0; i < _numeroCromosomas; i++) {

            int candidato1 = ale.Randint(0, _numeroCromosomas - 1);
            int candidato2 = ale.Randint(0, _numeroCromosomas - 1);

            while (candidato1 == candidato2) {
                candidato2 = ale.Randint(0, _numeroCromosomas - 1);
            }

            if (_vcromosomas.get(candidato1).getContribucion() > _vcromosomas.get(candidato2).getContribucion()) {
                _vcromosomasPadre.add(new Cromosomas(_vcromosomas.get(candidato1)));
            } else {
                _vcromosomasPadre.add(new Cromosomas(_vcromosomas.get(candidato2)));;
            }
        }

        _vcromosomas.clear();
    }

    private void operadorReproduccion(Random_p alea) {

        if (_operadorMPX == true) {

            operadorCruceMPX(alea);

        } else {

            operadorCruce2puntos(alea);
        }

        _vcromosomasPadre.clear();
    }

    private void operadorCruce2puntos(Random_p alea) {

        for (int i = 0; i < _numeroCromosomas; i += 2) {

            float probRepro = (float) alea.Randfloat(0, 1);

            if (probRepro < _probReproduccion) {

                HashSet<Integer> hijo1 = new HashSet<>();
                HashSet<Integer> hijo2 = new HashSet<>();

                int crosspoint1 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);
                int crosspoint2 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);

                while (crosspoint1 == crosspoint2) {
                    crosspoint2 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);
                }

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

                _vcromosomasHijo.add(new Cromosomas(hijo1, 0.0f, true));
                _vcromosomasHijo.add(new Cromosomas(hijo2, 0.0f, true));

            } else {

                Cromosomas hijo1 = _vcromosomasPadre.get(i);
                Cromosomas hijo2 = _vcromosomasPadre.get(i + 1);

                _vcromosomasHijo.add(new Cromosomas(hijo1.getCromosoma(), hijo1.getContribucion()));
                _vcromosomasHijo.add(new Cromosomas(hijo2.getCromosoma(), hijo2.getContribucion()));

            }
        }
    }

    private void operadorCruceMPX(Random_p alea) {

        for (int i = 0; i <= _numeroCromosomas && _vcromosomasHijo.size() < _numeroCromosomas; i += 2) {

            float probRepro = (float) alea.Randfloat(0, 1);

            int padre1 = i;
            int padre2 = i + 1;

            if (probRepro < _probReproduccion) {

                HashSet<Integer> cromosoma = new HashSet<>();

                for (Integer gen : _vcromosomasPadre.get(padre1).getCromosoma()) {
                    float prob = (float) alea.Randfloat(0, 1);
                    if (prob > 0.5) {
                        cromosoma.add(gen);
                    }
                }

                for (Integer gen : _vcromosomasPadre.get(padre2).getCromosoma()) {
                    cromosoma.add(gen);
                }

                _vcromosomasHijo.add(new Cromosomas(cromosoma, 0.0f, true));

            } else {
                _vcromosomasHijo.add(new Cromosomas(_vcromosomasPadre.get(padre1).getCromosoma(), 0.0f));

                if (_vcromosomasHijo.size() < _numeroCromosomas) {
                    _vcromosomasHijo.add(new Cromosomas(_vcromosomasPadre.get(padre2).getCromosoma(), 0.0f));
                }
            }

            if (i == _numeroCromosomas - 2) {
                i = 0;
            }
        }
    }

    private void operadorRepararCromosomas() {

        int numeroGenes = _archivoDatos.getTama_Solucion();

        for (Cromosomas Cromosoma : _vcromosomasHijo) {

            Set<Integer> cromosoma = Cromosoma.getCromosoma();

            if (cromosoma.size() != numeroGenes) {

                if (cromosoma.size() < numeroGenes) {

                    while (cromosoma.size() != numeroGenes) {
                        //Calcular mejor coste como en Greedy      
                        int gen = CalcularMayorAporte(cromosoma);
                        cromosoma.add(gen);
                    }

                } else {

                    while (cromosoma.size() != numeroGenes) {
                        //Quitar los que menos aportan
                        int elemento = CalcularMenorAporte(cromosoma);
                        cromosoma.remove(elemento);
                    }
                }
            }
        }
    }

    private void repararConcurrente() {

        ArrayList<Future<ArrayList<Cromosomas>>> future = new ArrayList<>();

        for (int i = 0; i < _numHilos; i++) {
            Future<ArrayList<Cromosomas>> f = null;
            future.add(f);
        }

        int tam = ((_numeroCromosomas) / _numHilos) - 1;
        int tamIni = 0;
        int tamFin = tam;

        for (int i = 0; i < _numHilos; i++) {
            if (i == _numHilos-1) {
                tamIni = (tam * (i - 1)) + 1;
                tamFin = _numeroCromosomas - 1;
            }
            future.set(i, exec.submit(new CostTask(_vcromosomasHijo, tamIni, tamFin)));
            tamIni = tamFin + 1;
            tamFin += tam;
        }

        try {
            _vcromosomasHijo.clear();

            for (int i = 0; i < _numHilos; i++) {
                ArrayList<Cromosomas> c = future.get(i).get();
                c.forEach(cromo -> {
                    _vcromosomasHijo.add(cromo);
                });
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Genetico.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(Genetico.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private int CalcularMayorAporte(Set<Integer> cromosoma) {

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

    private void operadorMutación(Random_p ale) {

        for (Cromosomas Cromosoma : _vcromosomasHijo) {

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

    private void registroConfiguración() {
        _gestor.escribirArchivo("Cromosomas Iniciales: ");
        for (Cromosomas cromosoma : _vcromosomas) {
            _gestor.escribirArchivo(cromosoma.getCromosoma().toString());
        }
    }

    private void registroElites() {
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Generación: " + generacion + ", " + _elitismo + " mejores individuos de la población");
        for (Cromosomas cromosoma : cromosomasElite) {
            _gestor.escribirArchivo(cromosoma.getCromosoma().toString());
        }
    }

    private void operadorElitismo() {

        if (_generacional == false) {
            for (Cromosomas Mejores : cromosomasElite) {
                _vcromosomasHijo.add(new Cromosomas(Mejores));
            }
        }

        Collections.sort(_vcromosomasHijo);

        while (_vcromosomasHijo.size() > _numeroCromosomas) {
            _vcromosomasHijo.remove(0);
        }

        int i = 1;
        for (Cromosomas elite : cromosomasElite) {
            elite.setCromosoma(_vcromosomasHijo.get(_numeroCromosomas - i).getCromosoma());
            elite.setContribucion(_vcromosomasHijo.get(_numeroCromosomas - i).getContribucion());
            i++;
        }

        _vcromosomas.clear();
        _vcromosomas = new ArrayList<>(_vcromosomasHijo);

        _vcromosomasHijo.clear();
        _vcromosomasPadre.clear();

        if (cromosomasElite.get(0).getContribucion() > _mejorCromosoma.getContribucion()) {
            _mejorCromosoma = new Cromosomas(cromosomasElite.get(0));
        }

        registroElites();
    }

    void PresentarResultados() {

        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Resultados");

        float coste = calcularCoste(_mejorCromosoma.getCromosoma());
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Mejor coste: " + coste);
        _gestor.escribirArchivo("Mejor cromosoma: " + _mejorCromosoma.getCromosoma());
        _gestor.escribirArchivo("Tamaño: " + coste);

        Main.console.presentarSalida("Mejor Coste:  " + _mejorCromosoma.getContribucion());
        Main.console.presentarSalida("");

    }
}
