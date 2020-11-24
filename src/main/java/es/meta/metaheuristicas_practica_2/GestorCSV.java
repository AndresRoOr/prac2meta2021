package es.meta.metaheuristicas_practica_2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public final class GestorCSV {

    private String _archiveName;
    private FileWriter fichero = null;
    private PrintWriter pw = null;

    public GestorCSV(String name) {
        _archiveName = name;

    }
    
    void cambiarNombre(String nombre) {
        _archiveName = nombre;
    }

    void abrirArchivo() {
        try {
            fichero = new FileWriter("./archivos/CSV/" + _archiveName+".csv");
            pw = new PrintWriter(fichero);

        } catch (IOException e) {
        }
    }

    void escribirArchivo(String linea) {
        pw.println(linea);
    }

    void cerrarArchivo() {
        try {
            if (null != fichero) {
                fichero.close();
            }
        } catch (IOException e2) {
        }
    }
}
