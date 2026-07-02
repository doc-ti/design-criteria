package edu.doc_ti.designcriteria.common.objects;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyLoader {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyLoader.class);

    private static final Properties properties = new Properties();

    public static void loadProperties(String filename) {
    	
    	LOG.info("Loading " + filename);
    	
        try (FileInputStream input = new FileInputStream(filename)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + filename);
            System.err.println(e.getMessage());
            System.exit(-1) ;
        }
    }

    private PropertyLoader() {
        // Constructor privado para evitar instanciación
    }

    public static String getProperty(String key) {
    	
    	LOG.info("Value used for property [{}] : [{}]" , key,  properties.getProperty(key));
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
    	LOG.info("Value used for property [{}] : [{}]" , key,  properties.getProperty(key, defaultValue));
        return properties.getProperty(key, defaultValue);
    }
    

    public static int getIntProperty(String key, int defaultValue) {
    	int val = defaultValue ;
    	try {
    		val = Integer.parseInt(getProperty(key));
    	} catch (Exception e) {} ;
    	
    	LOG.info("Value used for INT property [{}] : [{}]" , key,  val);
    	return val ;
    }
    
    public static double getDoubleProperty(String key, double defaultValue) {
    	double val = defaultValue ;
    	try {
    		val = Double.parseDouble(getProperty(key));
    	} catch (Exception e) {} ;
    	
    	LOG.info("Value used for DOUBLE property [{}] : [{}]" , key,  val);
    	return val ;
    }

	public static Properties getProps() {
		return properties;
	}
    
}
