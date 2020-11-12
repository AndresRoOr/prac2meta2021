/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.meta.metaheuristicas_practica_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
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

    //private ArrayList<Float> _costes;
    
    private ArrayList<Cromosomas> _vcromosomas;
    private ArrayList<Cromosomas> _vcromosomasPadre;
    private ArrayList<Cromosomas> _vcromosomasHijo;
    

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
        
        this._vcromosomas = new ArrayList<>();
        this._vcromosomasPadre = new ArrayList<>();
        this._vcromosomasHijo = new ArrayList<>();
        
        //this._costes = new ArrayList<>();
        this.cromosomasElite = new ArrayList<>();

    }

    void genetico(Random_p aleatorioSemilla) {

        Random_p aleatorio = aleatorioSemilla;

        generarCromosomasIniciales(aleatorio);
    
        cromosomasElite.add(new Cromosomas(_vcromosomas.get(0).getCromosoma(), _vcromosomas.get(0).getContribucion()));

        obtenerCostes(_vcromosomas, true);

        Collections.sort(cromosomasElite);
        
        /*_gestor.escribirArchivo("Cromosomas Iniciales: ");
        
        for(Cromosomas cromosoma : _vcromosomas){
            _gestor.escribirArchivo(cromosoma.getCromosoma().toString());
        }*/

        while (_evaluaciones < _evaluacionesObjetivo) {

            operadorSeleccion(aleatorio);

            operadorReproduccion(aleatorio);

            operadorRepararCromosomas();

            operadorMutación(aleatorio);

            obtenerCostes(_vcromosomasHijo, true);

            operadorElitismo();

        }
        
        _vcromosomasHijo.clear();
        _vcromosomasPadre.clear();

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
            _vcromosomas.add(new Cromosomas(cromosoma, 0.0f,true));
        }
    }

    private void obtenerCostes(ArrayList<Cromosomas> cromosomas, boolean ObtenerElite) {

        float mejorCoste = 0.0f;


        for (Cromosomas cromosoma : cromosomas) {

            if(cromosoma.isRecalcular() == true){
                float coste = calcularCoste(cromosoma.getCromosoma());
                cromosoma.setContribucion(coste);
                _evaluaciones++;

                if (coste > mejorCoste) {

                    mejorCoste = coste;

                    if (ObtenerElite == true) {

                        if (mejorCoste > cromosomasElite.get(0).getContribucion()) {
                            cromosomasElite.remove(0);
                            cromosomasElite.add(0,new Cromosomas(new HashSet<>(cromosoma.getCromosoma()), mejorCoste));
                            Collections.sort(cromosomasElite);
                            if(cromosomasElite.size()> _elitismo){
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
    
    private void operadorCruce2puntos(Random_p alea){

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

                Iterator<Integer> iterator = _vcromosomasPadre.get(i).getCromosoma().iterator();
                Iterator<Integer> iterator1 = _vcromosomasPadre.get(i+1).getCromosoma().iterator();

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

                _vcromosomasHijo.add(new Cromosomas(hijo1,0.0f,true));
                _vcromosomasHijo.add(new Cromosomas(hijo2,0.0f,true));

            } else {

                Cromosomas hijo1 = _vcromosomasPadre.get(i);
                Cromosomas hijo2 = _vcromosomasPadre.get(i+1);

                _vcromosomasHijo.add(new Cromosomas(hijo1.getCromosoma(),hijo1.getContribucion()));
                _vcromosomasHijo.add(new Cromosomas(hijo2.getCromosoma(),hijo2.getContribucion()));

            }
        }
    }
    
    private void operadorCruceMPX(Random_p alea){
        
        int max = _numeroCromosomas-1;

        for (int i = 0; i < _numeroCromosomas || _vcromosomasHijo.size() < _numeroCromosomas ; i += 2) {

            if (i >= max)  i = 0;        
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

                _vcromosomasHijo.add(new Cromosomas(cromosoma, 0.0f,true));

            } else {
                _vcromosomasHijo.add(new Cromosomas(_vcromosomasPadre.get(padre1).getCromosoma(),0.0f));

                if (_vcromosomasHijo.size() < _numeroCromosomas) {
                    _vcromosomasHijo.add(new Cromosomas(_vcromosomasPadre.get(padre2).getCromosoma(),0.0f));
                }
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
                                     
                        int elemento = CalcularMayorAporte(cromosoma);
                        cromosoma.add(elemento);
                    }

                    //Calcular mejor coste como en Greedy
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
    
    private int CalcularMayorAporte(Set<Integer> cromosoma){
        
        float mejor = 0.0f;
        int elemento = 0;

        HashSet<Integer> cromosomaReparado = new HashSet<>(cromosoma);
        int tamMatrix = _archivoDatos.getTama_Matriz();

        for (int i = 0; i < tamMatrix; i++) {

            if (!cromosomaReparado.contains(i)) {

                cromosomaReparado.add(i);
                float coste = calcularCoste(cromosomaReparado);

                if(coste > mejor){
                       mejor = coste;
                       elemento = i;
                }
                cromosomaReparado.remove(i);
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

    private void operadorElitismo() {

        ArrayList<Integer> indice = new ArrayList<>();
        ArrayList<Float> peoresCostes = new ArrayList<>();

        // Eliminamos los peores cromosomas por los mejores de los
        // no seleccionados
        
        
        _vcromosomas.clear();
        _vcromosomas = new ArrayList<>(_vcromosomasHijo);

        _vcromosomasHijo.clear();
        _vcromosomasPadre.clear();

    }

    void PresentarResultados() {

        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Resultados");
        _gestor.escribirArchivo("Cromosomas finales:");
        for(Cromosomas cromosoma: _vcromosomas){
            _gestor.escribirArchivo(cromosoma.getCromosoma().toString());
        }
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Mejor coste: " + cromosomasElite.get(0).getContribucion() );
        _gestor.escribirArchivo("Mejor cromosoma: " + cromosomasElite.get(0).getCromosoma());
        _gestor.escribirArchivo("Tamaño: " + cromosomasElite.get(0).getCromosoma());
        
        Main.console.presentarSalida("Mejor Coste:  " + cromosomasElite.get(0).getContribucion() );
        Main.console.presentarSalida("Tamaño: " + cromosomasElite.get(0).getCromosoma().size());
        Main.console.presentarSalida("");
        
    }

}
