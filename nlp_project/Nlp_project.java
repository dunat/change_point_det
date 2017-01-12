/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp_project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Donato Aquilino
 */
public class Nlp_project {

    /**
     * @param args[] 
     *  0.file con le serie da caricare
     *  1.parola su cui eseguire la change point detection
     *  2.file dove scrivere i risultati
     *  3.numero di bootstrap
     *  4.threshold
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
       time_series series = new time_series(); 
       HashMap<String,List<Double>> s = series.load_time_series(args[0]);
       
       /*
         Se voglio eseguire la change point detection su una sola parola
       */
       List<Double> ser = series.normalize(args[1], s);
       System.out.println("serie normalizzata: "+ser);
     
       List<Double> ms = series.mean_shift(ser);
       System.out.println("mean_shift: "+ms);     
      // System.out.println("samples: "+series.bootstrapping(ser, 5));
       
       List<List<Double>> bs = series.bootstrapping(ser, Integer.parseInt(args[3]));
       System.out.println("samples: "+bs);
       
       List<Double> pv = series.p_value(ms, bs);
       System.out.println("p_values: "+pv);
       
       System.out.println(series.cpd(ser, Double.parseDouble(args[4]), pv));
       
       /*
         Se voglio eseguire la change point detection su un insieme di parole
       */
       
       /*
       List<String> words = new ArrayList<>();
       String csvFile = args[0];
       BufferedReader br = null;
       String line;
        String cvsSplitBy = ",";

            br = new BufferedReader(new FileReader(csvFile));
            //leggo la prima riga di intestazione
            line = br.readLine();
            while ((line = br.readLine()) != null) {

                // uso la virgola come separatore
                String[] word = line.split(cvsSplitBy);
                // copio in una lista di supporto i valori della serie riferiti ad una parola
                words.add(word[1]);
                
            }
         
        String path = args[2];
 
        File file = new File(path);

        FileWriter fw = new FileWriter(file);
       
        for(int i = 0; i < words.size(); i++) {
            List<Double> w_ser = series.normalize(words.get(i), s);
            List<Double> w_ms = series.mean_shift(w_ser);
            List<List<Double>> w_bs = series.bootstrapping(w_ser, Integer.parseInt(args[3]));
            List<Double> w_pv = series.p_value(w_ms, w_bs);
            HashMap<Double,Integer> w_cpg = series.cpd(w_ser, Double.parseDouble(args[4]), w_pv);
            
            fw.write(words.get(i)+","+w_cpg.toString()+"\n*"); 
            
        }
        fw.flush();

        fw.close();
        */
    }
    
}
