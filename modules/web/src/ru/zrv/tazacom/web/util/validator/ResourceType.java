package ru.zrv.tazacom.web.util.validator;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Class is assigned to parse  String representation of URI/URL string 
 * 
 * @author Roman.Zaytseff
 */
class ResourceType {
	
	enum UriType {
		
		URL(java.net.URL.class.getName()) {
			@Override
			public java.net.URL parse(String value) {
				try {
					return new java.net.URL(value);
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException();
				}
			}
		},
		
		URI(java.net.URI.class.getName()) {
			@Override
			public java.net.URI parse(String value) {
				try {
					return new java.net.URI(value);
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException();
				}
			}
		};
		
		private final String className;
	    
		/**
	 	 * get name of class type
	 	 * @return full class name of the class
	 	 */
	    String getName() {
	    	return className;
	    }
	    
	    /**
	     * 
	     * @param value - string representation of object
	     * @return type save object value
	     * @throws IllegalArgumentException
	     */
		abstract Object parse(String value);
	    
		UriType(String className) { 
	    	this.className = className;
	    }
	    
	 	public boolean isValid(String value, String regExPattern) { 
	 		return Arrays.asList(RegExType.values()).stream()
	 				.filter((type) -> (type.parse(value) != null))
	 				.count() > 0;
	 	}
	 	
	}	
	
	public static void main(String... argv) {
    	Object[][] addressExamples = {
    			{"http://yandex.ru", true},
    			{"www.yandex.ru", true},
    			{"https://www.yandex.ru", true},
    			{"13.01.15.12", true},
    			{"13.01.15.12/com/ua", false},
    			{"passportessa.com.ua", false},
    			{"yandex.ru", false},
    			{"www.yandex", false}
    	};
    	
    	for(Object[] address: Arrays.asList(addressExamples)) {
    		System.out.println(address[0] + " is " + (AddressType.isValid((String)address[0]) ? "" : " NOT ") + "valid");
//    		assertFalse(AddressType.isValid((String)address[0]) ^ (Boolean)address[1]);
    	}
    	
	}
	    
}