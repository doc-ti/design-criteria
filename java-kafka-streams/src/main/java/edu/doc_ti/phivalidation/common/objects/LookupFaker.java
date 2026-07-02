package edu.doc_ti.phivalidation.common.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

public class LookupFaker {
	
	static Random rand = new Random();

	static ArrayList<ArrayList<ArrayList<String>>> allData = new ArrayList<ArrayList<ArrayList<String>>>() ;
	static ArrayList<ArrayList<String>> headers = new ArrayList<ArrayList<String>>() ;
	
	static {
    	loadFile("/maestros/cause_closing.txt");
    	loadFile("/maestros/tarifas.txt");
    	loadFile("/maestros/change_condition.txt");
    	loadFile("/maestros/operadores.txt");
    	loadFile("/maestros/record_type.txt");
    	loadFile("/maestros/tac-v2.txt");
    	loadFile("/maestros/topologia.txt"); 
    }

    public static void loadFile(String filePath) {
    	
    	ArrayList<ArrayList<String>> myData =  new ArrayList<ArrayList<String>>();
    	
        try (InputStream inputStream = LookupFaker.class.getResourceAsStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

               if (inputStream == null) {
                   System.err.println("Archivo no encontrado: " + filePath);
                   return;
               }

               String line;
               int contador = 0 ;
               while ((line = br.readLine()) != null) {
            	   contador++ ;
                   ArrayList<String> values = new ArrayList<String> (Arrays.asList(line.split(";")) ) ;
            	   if (contador == 1) {
            		   headers.add(values);
            	   } else {
            		   myData.add(values) ;
            	   }
               }
               allData.add(myData) ;
               
           } catch (IOException e) {
               System.err.println("Error al leer el archivo: " + e.getMessage());
           }
    }
    
    public static String generateJSON() {
    	StringBuilder json = new StringBuilder() ;
    	
    	for ( int posH = 0 ; posH< headers.size() ; posH++) {
    		ArrayList<String> myHeader = headers.get(posH) ;
    		ArrayList<ArrayList<String>> dataG = allData.get(posH) ;
    		ArrayList<String> dataRand = dataG.get(rand.nextInt(dataG.size())) ;

    		int minSize = ( dataRand.size() > myHeader.size() ) ? myHeader.size() : dataRand.size() ;  
    		
    		for ( int posD = 0 ; posD< minSize ; posD++) {
    			if ( posD > 0 || posH > 0 ) {
    				json.append(", ") ;
    			}
    			json.append("\"" + myHeader.get(posD) + "\" : \"" + dataRand.get(posD) + "\"") ; 
        	}
    	}
    	
    	return json.toString() ;
    }

    public static Hashtable<String, String> generateHash() {
    	Hashtable<String, String> hashT = new Hashtable<String, String>() ;
    	
    	for ( int posH = 0 ; posH< headers.size() ; posH++) {
    		ArrayList<String> myHeader = headers.get(posH) ;
    		ArrayList<ArrayList<String>> dataG = allData.get(posH) ;
    		ArrayList<String> dataRand = dataG.get(rand.nextInt(dataG.size())) ;

    		int minSize = ( dataRand.size() > myHeader.size() ) ? myHeader.size() : dataRand.size() ;  
    		
    		for ( int posD = 0 ; posD< minSize ; posD++) {
    			hashT.put(myHeader.get(posD), dataRand.get(posD)) ;
        	}
    	}
    	
		return hashT;
    	
    }

    public static void test() {
    	@SuppressWarnings("unused")
		Hashtable<String, String> hashT = new Hashtable<String, String>() ;
    	
    	for ( int posH = 0 ; posH< headers.size() ; posH++) {
    		ArrayList<String> myHeader = headers.get(posH) ;
    		ArrayList<ArrayList<String>> dataG = allData.get(posH) ;
    		ArrayList<String> dataRand = dataG.get(rand.nextInt(dataG.size())) ;

    		int minSize = ( dataRand.size() > myHeader.size() ) ? myHeader.size() : dataRand.size() ;  
    		
    		for ( int posD = 0 ; posD< minSize ; posD++) {
    			System.out.println(posH + "/" + posD + " - " + myHeader.get(posD) + " : " + dataRand.get(posD)) ; 
        	}
    	}
    }

    public static void main(String[] args) {

    	System.out.println(generateHash() ) ;
    	System.out.println(generateJSON() );
//    	test() ;
    }
    
    
} 

