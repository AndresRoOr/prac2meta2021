/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.meta.metaheuristicas_practica_2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author David
 */
public final class Genetico {
  
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

    private ArrayList<Set<Integer>> _cromosomas;
    private ArrayList<Set<Integer>> _cromosomasPadre;
    private ArrayList<Set<Integer>> _cromosomasHijo;
    private ArrayList<Float> _costes;
    private ArrayList<Boolean> recalcularCostes;

    private ArrayList<Cromosomas> cromosomasElite;

    public Genetico(Archivo _archivoDatos, GestorLog gestor, int evaluaciones, int Elitismo, boolean OperadorMPX, float probReini,
             float probMutacion, float probMpx) {
        this._archivoDatos = _archivoDatos;
        this._gestor = gestor;
        this._elitismo = Elitismo;
        this._operadorMPX = OperadorMPX;

        this._probMutacion = probMutacion;
        this._probReproduccion = probReini;
        this._probMpx = probMpx;

        this._evaluaciones = 0;
        this._evaluacionesObjetivo = evaluaciones;

        this._numeroCromosomas = 50;

        this._cromosomas = new ArrayList<>();
        this._cromosomasPadre = new ArrayList<>();
        this._cromosomasHijo = new ArrayList<>();
        this._costes = new ArrayList<>();
        this.cromosomasElite = new ArrayList<>();

    }

    void genetico(Random_p aleatorioSemilla) {

        Random_p aleatorio = aleatorioSemilla;

        generarCromosomasIniciales(aleatorio);

        for (int i = 0; i < _cromosomas.size(); i++) {
            cromosomasElite.add(new Cromosomas(new HashSet<>(_cromosomas.get(i)), 0.0f));
        }

        obtenerCostes(_cromosomas, true);

        Collections.sort(cromosomasElite);

        while (cromosomasElite.size() >= _elitismo) {
            cromosomasElite.remove(0);
        }
        
        _gestor.escribirArchivo("Cromosomas Iniciales: ");
        
        for(Set<Integer> cromosoma : _cromosomas){
            _gestor.escribirArchivo(cromosoma.toString());
        }

        _evaluaciones += 50;

        while (_evaluaciones < _evaluacionesObjetivo) {

            operadorSeleccion(aleatorio);

            operadorReproduccion(aleatorio);

            operadorRepararCromosomas();

            operadorMutación(aleatorio);

            obtenerCostes(_cromosomasHijo, true);

            operadorElitismo();

            _evaluaciones += 50;

        }

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
            _cromosomas.add(cromosoma);
        }
    }

    private void obtenerCostes(ArrayList<Set<Integer>> cromosomas, boolean ObtenerElite) {

        float mejorCoste = 0.0f;

        _costes.clear();

        for (Set<Integer> cromosoma : cromosomas) {

            float coste = calcularCoste(cromosoma);
            _costes.add(coste);

            if (coste > mejorCoste) {

                mejorCoste = coste;

                if (ObtenerElite == true) {
                    
                    if (mejorCoste > cromosomasElite.get(0).getContribucion()) {
                        cromosomasElite.add(0,new Cromosomas(new HashSet<>(cromosoma), mejorCoste));
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

            if (_costes.get(candidato1) > _costes.get(candidato2)) {
                _cromosomasPadre.add(new HashSet<>(_cromosomas.get(candidato1)));
            } else {
                _cromosomasPadre.add(new HashSet<>(_cromosomas.get(candidato2)));
            }
        }
    }

    private void operadorReproduccion(Random_p alea) {

        //_cromosomasHijo = new ArrayList<>(_cromosomasPadre);
        if (_operadorMPX == true) {

            int pos = 0;
            int max = _numeroCromosomas-1;

            while (_cromosomasHijo.size() < _numeroCromosomas) {
                
                float probRepro = (float) alea.Randfloat(0, 1);

                int padre1 = pos;
                int padre2 = pos + 1;

                if (probRepro < _probReproduccion) {

                    HashSet<Integer> cromosoma = new HashSet<>();

                    for (Integer gen : _cromosomasPadre.get(padre1)) {
                        float prob = (float) alea.Randfloat(0, 1);
                        if (prob > 0.5) {
                            cromosoma.add(gen);
                        }
                    }

                    for (Integer gen : _cromosomasPadre.get(padre2)) {
                        cromosoma.add(gen);
                    }

                    _cromosomasHijo.add(cromosoma);

                } else {
                    _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(padre1)));

                    if (_cromosomasHijo.size() < _numeroCromosomas) {
                        _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(padre2)));
                    }
                }

                pos += 2;
                
                if (pos >= max) {
                    pos = 0;
                }
            }

        } else {

            //Aplico cruce en dos puntos
            for (int i = 0; i < _numeroCromosomas; i += 2) {

                float probRepro = (float) alea.Randfloat(0, 1);

                if (probRepro < _probReproduccion) {

                    HashSet<Integer> hijo1 = new HashSet<>();
                    HashSet<Integer> hijo2 = new HashSet<>();

                    int crosspoint1 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);
                    int crosspoint2 = alea.Randint(1, _archivoDatos.getTama_Solucion() - 2);

                    if (crosspoint1 == crosspoint2) {
                        crosspoint1--;
                    }

                    if (crosspoint2 < crosspoint1) {
                        int temp = crosspoint1;
                        crosspoint1 = crosspoint2;
                        crosspoint2 = temp;
                    }

                    Iterator<Integer> iterator = _cromosomasPadre.get(i).iterator();
                    Iterator<Integer> iterator1 = _cromosomasPadre.get(+1).iterator();

                    int length = _archivoDatos.getTama_Solucion();
                    for (int j = 0; j < length; j++) {

                        if (i < crosspoint1 || i > crosspoint2) {
                            hijo1.add(iterator.next());
                            hijo2.add(iterator1.next());
                        } else {
                            hijo1.add(iterator1.next());
                            hijo2.add(iterator.next());
                        }
                    }

                    _cromosomasHijo.add(hijo1);
                    _cromosomasHijo.add(hijo2);

                } else {

                    _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(i)));
                    _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(i + 1)));

                }
            }
        }
    }

    private void operadorRepararCromosomas() {

        int numeroGenes = _archivoDatos.getTama_Solucion();

        for (Set<Integer> cromosoma : _cromosomasHijo) {

            if (cromosoma.size() != numeroGenes) {

                if (cromosoma.size() < numeroGenes) {

                    while (cromosoma.size() != numeroGenes) {
                        
                        float mejor = 0.0f;
                        int elemento = 0;

                        HashSet<Integer> cromosomaReparado = new HashSet<>(cromosoma);
                        int tamMatrix = _archivoDatos.getTama_Matriz();

                        for (int i = 0; i < tamMatrix; i++) {

                            if (!cromosomaReparado.contains(i)) {
                                float coste = calcularCoste(cromosomaReparado);
                                
                                if(coste > mejor){
                                       mejor = coste;
                                       elemento = 1;
                                }
                                cromosomaReparado.remove(i);
                            }
                        }

                    
                        cromosoma.add(elemento);
                    }

                    //Calcular mejor coste como en Greedy
                } else if (cromosoma.size() > numeroGenes) {

                    while (cromosoma.size() != numeroGenes) {

                        //Quitar los que menos aportan
                        int elemento = CalcularMenorAporte(cromosoma);
                        cromosoma.remove(elemento);
                    }
                }
            }
        }
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

        for (Set<Integer> cromosoma : _cromosomasHijo) {

            ArrayList<Integer> alelosMutados = new ArrayList<>();
            ArrayList<Integer> alelosNuevos = new ArrayList<>();

            for (Integer gen : cromosoma) {

                double probabilidad = ale.Randfloat(0, 1);

                if (probabilidad < _probMutacion) {

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
        }
    }

    private void operadorElitismo() {

        ArrayList<Integer> indice = new ArrayList<>();
        ArrayList<Float> peoresCostes = new ArrayList<>();

        // Eliminamos los peores cromosomas por los mejores de los
        // no seleccionados
        _cromosomas.clear();
        _cromosomas = new ArrayList<>(_cromosomasHijo);

        _cromosomasHijo.clear();
        _cromosomasPadre.clear();

    }

    void PresentarResultados() {

        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Resultados");
        _gestor.escribirArchivo("Cromosomas finales:");
        for(Set<Integer> cromosoma : _cromosomas){
            _gestor.escribirArchivo(cromosoma.toString());
        }
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Mejor coste: " + cromosomasElite.get(0).getContribucion() );
        
        Main.console.presentarSalida("Mejor Coste:  " + cromosomasElite.get(0).getContribucion() );
        Main.console.presentarSalida("");
        
    }

}
