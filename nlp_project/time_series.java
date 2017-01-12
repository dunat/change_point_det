/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp_project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *  Classe che implementa la change point detection
 * @author Donato Aquilino
 */
public class time_series {
    
    /**
    *  Carica da file le serie temporali
    *  @param file file dal quale caricare le serie temporali
    *  @return un hash formato dalla parola come chiave e le rispettive serie temporali come liste
    */
    public HashMap<String,List<Double>> load_time_series(String file) {
        
        //Hash con la parola come chiave e la serie temporale come value
        HashMap<String,List<Double>> series = new HashMap<>();
        
        String csvFile = file;
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(csvFile));
            //leggo la prima riga di intestazione
            line = br.readLine();
            while ((line = br.readLine()) != null) {

                // uso la virgola come separatore
                String[] word = line.split(cvsSplitBy);
                // copio in una lista di supporto i valori della serie riferiti ad una parola
                List<Double> value = new ArrayList<>();
                //l'indice parte da 2 perché dalla colonna 3 parte la serie temporale
                for(int i = 2; i < word.length; i++) {
                    value.add(Double.parseDouble(word[i]));
                 }
                
                // aggiungo alla struttura hash la parola e i relativi valori
                series.put(word[1], value);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //ritorno la mia serie temporale caricata in memoria
        return series;
    }
    
    /**
    *   metodo che normalizza la serie relativa ad una parola
    *   @param word parola su cui effettuare la normalizzazione
    *   @param series parole con relative serie temporali
    *   @return lista di valori normalizzati relativi a word
    */
    public List<Double> normalize(String word, HashMap<String,List<Double>> series) {
        List<Double> medie_temporali = new ArrayList<>();
        int num_snapshot = series.get(word).size();
        double sum = 0.0;
        /*
            Calcolo delle medie
        */
        // itero sugli snapshot
        for(int i = 0; i < num_snapshot; i++){
            //iteratore per le parole
            for (HashMap.Entry entry : series.entrySet()) {
                //prende i valori della parola
                List<Double> values = (List<Double>)entry.getValue();
                //seleziona il valore relativo all'i-esimo snapshot
                sum = sum + values.get(i);
            } 
            double media = sum/series.size();
            sum = 0;
            medie_temporali.add(media);    
         }
        
        /*
            Calcolo delle varianze
        */
        List<Double> varianze_temporali = new ArrayList<>();
        double scarto = 0;
        double somma = 0;
        for(int i = 0; i < num_snapshot; i++){
            for (HashMap.Entry entry : series.entrySet()) {
                //prende i valori della parola
                List<Double> values = (List<Double>)entry.getValue();
                //seleziona il valore relativo all'i-esimo snapshot
                scarto = (values.get(i) - medie_temporali.get(i))*(values.get(i) - medie_temporali.get(i));
                somma = somma + scarto;
            } 
            double var = somma/series.size();
            somma = 0;
            varianze_temporali.add(var);  
        }
        
        /*
            Normalizzazione della serie
        */
        List<Double> serie_norm = new ArrayList<>();
        for (HashMap.Entry entry : series.entrySet()) {
            if(entry.getKey().equals(word)){
                List<Double> values = (List<Double>)entry.getValue();
                for(int i = 0; i < num_snapshot; i++){
                                        
                    serie_norm.add((values.get(i)-medie_temporali.get(i))/(Math.sqrt(varianze_temporali.get(i))));
                   
                }
            }
        } 
        
        return serie_norm;
    }
    
    /**
     * metodo che calcola la mean shift di una serie normalizzata
     *  (la lunghezza della lista sarà infereriore alla lunghezza della serie
     *  in quanto calcola tutti i pissibili time point j)
     *  (gli indici non rispecchiano fedelmente l'articolo poiché
     *  gli indici delle liste partono da valore 0)
     *  
     * @param s serie su cui calcolare la mean shift
     * @return lista di valori della mean schift         
    */
    
    public List<Double> mean_shift(List<Double> s) {
        double sum_pre_j =0;
        double sum_post_j = 0;
        List<Double> mean_shift = new ArrayList<>();
        
        //itero sui possibili valori di j pivoted
        for(int j = 0; j < s.size()-1; j++) {
            
            //serie prima di j
            for(int k = j+1; k < s.size(); k++) {
                sum_post_j = (sum_post_j + s.get(k)); 
            }
            
            sum_post_j = sum_post_j/(s.size()-(j+1));
           
            //serie dopo j
            for(int k = 0; k < j+1; k++) {
                sum_pre_j = sum_pre_j + s.get(k);
            }
            sum_pre_j = sum_pre_j/(j+1);
           
            mean_shift.add(sum_post_j-sum_pre_j);
            sum_post_j = 0;
            sum_pre_j = 0;
        }
        return mean_shift;
    }
    
    /**
     *  Bootstrapping
     *  effettuato utilizzando il metodo shuffle della classe collections
     *  (nb. modifica la lista stessa senza ritornarne una nuova)
     * @param k lista di valori su cui fare il bootstrap
     * @param num_bs numero di campioni da generare
     * @return lista di campioni generati (dove ogni elemento-campione a sua volta è una lista)
    */
    List<List<Double>> bootstrapping (List<Double> k, int num_bs) {
        List<List<Double>> bs = new ArrayList<>();
        
        for(int i = 0; i < num_bs; i++){
           List<Double> temp = new ArrayList<>(k); 
           java.util.Collections.shuffle(temp);
           bs.add(temp);
        }
        return bs;
    }
    
    /**
     * Computa i p-value
     * @param mean_shift lista di valori calcolati dalla mean shift
     * @param samples campioni
     * @return lista di p_valori
    */
    List<Double> p_value(List<Double> mean_shift, List<List<Double>> samples) {
        List<Double> p_values = new ArrayList<>();
        List<List<Double>> samples_ms = new ArrayList();
        
        //Calcola le mean_shift dei samples
        for(List s: samples) {
           samples_ms.add(mean_shift(s));
        }
        System.out.println("mean_shift_samples: "+samples_ms);
        //itera su gli n mean_shift
        for(int i = 0; i < mean_shift.size(); i++) {
           
            int cont = 0;
            //itera sui samples con riferimento all'i-esimo slot temporale
            for(int j = 0; j < samples_ms.size(); j++) {
                
                if(samples_ms.get(j).get(i) > mean_shift.get(i)) {
                    
                    cont++;
                }
            }
           
            double v = (double)cont/(samples.size());
           
            p_values.add(v);
        }
        return p_values;
    }
        
    /**
     *  Change point detection
     * @param norm lista dei valori normalizzati
     * @param soglia threshold
     * @param p_values p_valori
     * @return un hash contenente come chiave il p_valore e come valore l'indice relativo allo snapshot
    */
    HashMap<Double,Integer> cpd (List<Double> norm, double soglia, List<Double> p_values) {
        HashMap<Double,Integer> cgp = new HashMap<>();
        
        //indici della serie maggiori della soglia
        List<Integer> c = new ArrayList<>();
        for(int j = 0; j < norm.size(); j++) {
            if (norm.get(j) > soglia) { 
                c.add(j);
            }
        }
        System.out.println(c);
        if(!c.isEmpty()) {
            double min = p_values.get(0);
            int j = 0;
            for(int i = 1; i < c.size()-1; i++) {
                if(p_values.get(c.get(i)) < min) {
                    j = c.get(i);
                    min = p_values.get(c.get(i));
                }
            }
            
            cgp.put(min,j);
        } else {
            cgp.put(Double.NaN, -1);
        }
        return cgp;
    }
}
