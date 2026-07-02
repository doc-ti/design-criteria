package edu.doc_ti.phivalidation.common.objects;

import java.lang.reflect.Field;

public class BaseObjectStreaming {

	String defaultSeparator = "|" ;
	
	public String getHeaders() {
		return getHeaders(defaultSeparator) ;
	}
	
	public String getHeaders(String separator) {
//        if (this == null || separator == null) {
//            throw new IllegalArgumentException("Object and separator cannot be null");
//        }

        StringBuilder result = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Object value = fields[i].getName();
            result.append(value != null ? value.toString() : "null");

            if (i < fields.length - 1) {
                result.append(separator);
            }
        }

        return result.toString();
	}
	
	public  String concatenateFields() {
		return concatenateFields(defaultSeparator);
	}
	
	public  String concatenateFields(String separator) {

        StringBuilder result = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true); // Allow access to private fields
            try {
                Object value = fields[i].get(this);
                result.append(value != null ? value.toString() : "null");
            } catch (IllegalAccessException e) {
                result.append("[inaccessible]");
            }

            if (i < fields.length - 1) {
                result.append(separator);
            }
        }

        return result.toString();
    }

}
