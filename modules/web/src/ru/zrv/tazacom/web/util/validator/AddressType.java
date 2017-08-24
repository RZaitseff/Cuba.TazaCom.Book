package ru.zrv.tazacom.web.util.validator;

import java.util.Arrays;
import java.util.function.Function;

import static ru.zrv.tazacom.web.util.validator.ResourceType.*;

/**
 * The class is assigned to define type of URI address
 * It's include list of known types that could be extended by another demanded types 
 * 
 * The classes that extend the list of known types must include two methods
 * named for example as 'valueOf(String value)' to parse demanded type
 * and return full class name by executing for example method 'getName()'
 * 
 * @param value - value of property
 * @return type of value
 * 
 * @author Roman.Zaytseff
 */
public enum AddressType  {
	URL(UriType.URL::parse),
	URL_STRING(RegExType.URL::parse),
	URI_STRING(RegExType.URI::parse),
	IPv4(RegExType.IPv4::parse),
	UNDEFINED ((s) -> null);
   
	private final Function<String, Object> valueOf;
	
    AddressType(Function<String, Object> valueOf) { 
    	this.valueOf = valueOf; 
    }
    
    /**
     * The method try parse input value and 
     * cast any possible Exception to runtime IllegalArgumentException
     * 
     * @param value - string representation of address
     * @return type safe representation of address
     * @throws IllegalArgumentException
     */
	public Object parse(String value) {
		try {
			return valueOf.apply(value); 
		} catch (Exception e) {
			return null;
		}
    }
	
 	public boolean validate(String address) {
 		return parse(address) != null;
 	}
 	
 	public static boolean isValid(String value) { 
 		return Arrays.asList(AddressType.values()).stream()
 				.filter((type) -> (type.parse(value) != null))
 				.count() > 0;
 	}
 	
 	        
}
