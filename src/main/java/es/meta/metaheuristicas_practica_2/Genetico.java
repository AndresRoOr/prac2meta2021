/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author David
 */
public final class Genetico {
    
    Archivo _archivoDatos;///<Contiene los datos del problema
    private GestorLog _gestor;///<Gestor encargado de la creación del Log
    int _elitismo;
    boolean _operadorMPX;
    int _evaluaciones;
    int _evaluacionesObjetivo;
    int _numeroCromosomas;
    
    float _probMutacion;
    float _probReproduccion;
    float _probMpx;
    
    ArrayList<Set<Integer>> _cromosomas;
    ArrayList<Set<Integer>> _cromosomasPadre;
    ArrayList<Set<Integer>> _cromosomasHijo;
    ArrayList<Float> _costes;
    ArrayList<Boolean> recalcularCostes;
    
    ArrayList<Cromosomas> cromosomasElite;
    
    public Genetico(Archivo _archivoDatos, GestorLog gestor,int evaluaciones, int Elitismo, boolean OperadorMPX, float probReini
    , float probMutacion, float probMpx) {
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
    
    
    void genetico(Random_p aleatorioSemilla){
        
        Random_p aleatorio = aleatorioSemilla;
        
        generarCromosomasIniciales(aleatorio);
        
        for(int i =0; i < _cromosomas.size(); i ++){
            cromosomasElite.add(new Cromosomas(new HashSet<>(_cromosomas.get(i)), 0.0f));
        }
        
        obtenerCostes(_cromosomas, true);
        
        Collections.sort(cromosomasElite);
        
        while(cromosomasElite.size() >= _elitismo){
            cromosomasElite.remove(0);
        }
        
        _evaluaciones+=50;
        
        while(_evaluaciones < _evaluacionesObjetivo){
        
            operadorSeleccion(aleatorio);   
            
            operadorReproduccion(aleatorio);
                   
            operadorRepararCromosomas();
        
            operadorMutación(aleatorio);
            
            obtenerCostes(_cromosomasHijo, false);
            
            operadorElitismo();
            
            _evaluaciones+=50;
        
        }
        
    }
    
    
    void generarCromosomasIniciales(Random_p alea){
        
        int tamCromosoma = _archivoDatos.getTama_Solucion();
        for (int i = 0; i < _numeroCromosomas; i++) {
            
            Set<Integer> cromosoma = new HashSet<>();    
            
            while(cromosoma.size() < tamCromosoma){
                int alelo = alea.Randint(0, _archivoDatos.getTama_Matriz()-1);            
                if(!cromosoma.contains(alelo)){
                    cromosoma.add(alelo);
                }          
            }      
            _cromosomas.add(cromosoma);
        }     
    }
    
    void obtenerCostes( ArrayList<Set<Integer>> cromosomas, boolean ObtenerElite){
        
        float mejorCoste = 0.0f;
        
        _costes.clear();
        
        for(Set<Integer> cromosoma : cromosomas){
            
            float coste = calcularCoste(cromosoma);          
            _costes.add(coste);

            if(coste > mejorCoste){
                
                mejorCoste = coste;
                
                if(ObtenerElite == true){
                    if(mejorCoste > cromosomasElite.get(0).getContribucion()){
                        cromosomasElite.add(new Cromosomas(new HashSet<>(cromosoma), mejorCoste));
                        Collections.sort(cromosomasElite);
                        cromosomasElite.remove(0);
                    }
                }
            }    
        }     
    }
    
    
    float calcularCoste(Set<Integer> cromosoma ){

        float coste = 0.0f;
        Object[] sol = cromosoma.toArray();

        for (int i = 0; i < sol.length - 1; i++) {
            int a = (int) sol[i];
            for (int j = i + 1; j < sol.length; j++) {
                int b = (int) sol[j];
                coste += _archivoDatos.getMatriz()[a][b];
            }
        }
        return coste;        
    }
    
    
    void operadorSeleccion(Random_p ale){
        
        for (int i = 0; i < _numeroCromosomas; i++) {
            
            int candidato1 = ale.Randint(0,_numeroCromosomas -1);
            int candidato2 = ale.Randint(0,_numeroCromosomas -1);
            
            if(_costes.get(candidato1)>_costes.get(candidato2)){
                _cromosomasPadre.add(new HashSet<>(_cromosomas.get(candidato1)));
            }else{
                _cromosomasPadre.add(new HashSet<>(_cromosomas.get(candidato2)));
            }
        } 
    }
    
    void operadorReproduccion(Random_p alea){
        
        //_cromosomasHijo = new ArrayList<>(_cromosomasPadre);
        
        if(_operadorMPX == true){
            
            int pos = 0;
            int max = _archivoDatos.getTama_Solucion();
            
            while(_cromosomasHijo.size()<_numeroCromosomas) {

                float probRepro = (float) alea.Randfloat(0, 1);

                int padre1 = pos;
                int padre2 = pos +1;

                if(probRepro < _probReproduccion){
                    
                    HashSet<Integer> cromosoma = new HashSet<>();

                    for(Integer gen : _cromosomasPadre.get(padre1)){
                        float prob = (float) alea.Randfloat(0, 1);
                        if(prob > 0.5){ cromosoma.add(gen); }
                    }

                    for(Integer gen : _cromosomasPadre.get(padre2)){    
                        cromosoma.add(gen); 
                    }

                    _cromosomasHijo.add(cromosoma);

                }else{
                    _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(padre1)));
                    
                    if(_cromosomasHijo.size()<_numeroCromosomas){
                        _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(padre2)));
                    }
                }
                
                pos+=2;
                
                if(pos == max ){
                    pos = 0;
                }
            }
            
        }else{
            
            //Aplico cruce en dos puntos

            for (int i = 0; i < _cromosomasPadre.size(); i+=2) {

                float probRepro = (float) alea.Randfloat(0, 1);

                if(probRepro < _probReproduccion){
                    
                    HashSet<Integer> hijo1 = new HashSet<>();
                    HashSet<Integer> hijo2 = new HashSet<>();
                    
                    int crosspoint1 = alea.Randint(1,_archivoDatos.getTama_Solucion()-2 );
                    int crosspoint2 = alea.Randint(1,_archivoDatos.getTama_Solucion()-2 );
                    
                    if (crosspoint1 == crosspoint2){
                        crosspoint1--;  
                    }

                    if (crosspoint2 < crosspoint1) {
                        int temp = crosspoint1;
                        crosspoint1 = crosspoint2;
                        crosspoint2 = temp;
                    }
                    
                    Iterator<Integer> iterator = _cromosomasPadre.get(i).iterator();
                    Iterator<Integer> iterator1 = _cromosomasPadre.get(+1).iterator();
                    
                    for (int j = 0; j <_archivoDatos.getTama_Solucion(); j++) {
                        
                        if (i < crosspoint1 || i > crosspoint2){
                            hijo1.add(iterator.next());
                            hijo2.add(iterator1.next());
                        }else{
                            hijo1.add(iterator1.next());
                            hijo2.add(iterator.next());
                        }
                    }
                    
                    _cromosomasHijo.add(hijo1);
                    _cromosomasHijo.add(hijo2);
                   
                }else{
                    
                    _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(i)));
                    _cromosomasHijo.add(new HashSet<>(_cromosomasPadre.get(i+1)));
                               
                }        
            }
        }    
    }
    
    void operadorRepararCromosomas(){
        
        int numeroGenes = _archivoDatos.getTama_Solucion();
        
        for(Set<Integer> cromosoma : _cromosomasHijo){
            
            if(cromosoma.size()!=numeroGenes){
                
                if(cromosoma.size()<numeroGenes){
                    
                    ArrayList<ElementoSolucion> mejorAporte = new ArrayList<>();
                        
                    HashSet<Integer> cromosomaReparado = new HashSet<>(cromosoma);
                    int tamMatrix = _archivoDatos.getTama_Matriz();

                    for (int i = 0; i < tamMatrix ; i++) {

                        if(!cromosomaReparado.contains(i)){
                            float coste = calcularCoste(cromosomaReparado);
                            mejorAporte.add(new ElementoSolucion(i, coste));
                            cromosomaReparado.remove(i);
                        }
                    }
                    
                    Collections.sort(mejorAporte);
                    
                    while(cromosoma.size()!= numeroGenes){
                        cromosoma.add(mejorAporte.remove(mejorAporte.size() -1).getId());               
                    }
                   
                        //Calcular mejor coste como en Greedy
                          
                } else if(cromosoma.size()>numeroGenes){
                    
                    while(cromosoma.size()!=numeroGenes){
                    
                        //Quitar los que menos aportan
                    }           
                }
            }         
        }      
    }
    
    
    void operadorMutación(Random_p ale){
        
        for(Set<Integer> cromosoma : _cromosomasHijo){
            
            ArrayList<Integer> alelosMutados = new ArrayList<>();
            ArrayList<Integer> alelosNuevos = new ArrayList<>();
            
           for(Integer gen : cromosoma){
               
               double probabilidad = ale.Randfloat(0, 1);
               
               if(probabilidad < _probMutacion){     
                   
                   alelosMutados.add(gen);    
                   
                   boolean reemplazo = false;  
                   
                   while(reemplazo == false){
                     
                       int generado = ale.Randint(0,_archivoDatos.getTama_Matriz()-1);
                       
                       if(!cromosoma.contains(generado) && !alelosNuevos.contains(generado)){
                           alelosNuevos.add(generado);
                           reemplazo = true;
                       }          
                   }              
               }                
           }   

           while(!alelosMutados.isEmpty() || !alelosNuevos.isEmpty()){ 
               cromosoma.remove(alelosMutados.remove(0));
               cromosoma.add(alelosNuevos.remove(0));
           }
        }    
    }
    
    
    void operadorElitismo(){
        
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
        _gestor.escribirArchivo("");
        _gestor.escribirArchivo("Mejor coste: " + cromosomasElite.get(cromosomasElite.size()-1).getContribucion() );
        
        Main.console.presentarSalida("");
        
        System.out.println("");
    }
    
}
