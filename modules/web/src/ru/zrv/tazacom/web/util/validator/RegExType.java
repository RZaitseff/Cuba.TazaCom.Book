package ru.zrv.tazacom.web.util.validator;

/**
 * Class is assigned to parse  String representation of URI/URL string 
 * 
 * @author Roman.Zaytseff
 */
interface RegExPattern {
	String URL =
			"\\A(?<url>(?:(?:classpath|dns|file|filesystem|ftp|git|http|https|jabber|jdbc|jms|ldap|pkcs11|proxy|resource|rmi|sip|skype|smtp|snmp|soap|ssh|svn|udp|ws|wss|xmpp?)://|(?:ftp|www)\\.)[\\d.a-z-]+\\.[a-z]{2,6}+(?::\\d{1,5}+)?(?:/[]\\d!\"#$%&'()*+,.:;<=>?@\\[\\\\_`a-z{|}~^-]+){0,9}(?:/[]\\d!\"#$%&'()*+,.:;<=>?@\\[\\\\_`a-z{|}~^-]*)?(?:\\?[]\\d!\"#$%&'()*+,./:;<=>?@\\[\\\\_`a-z{|}~^-]*)?)";
	String URI =
			"^([_a-zA-Z][_a-zA-Z0-9]*)(:([_a-zA-Z][_a-zA-Z0-9]*))*(://)([_a-zA-Z][_a-zA-Z0-9]*)([./]([_a-zA-Z][_a-zA-Z0-9]*))";
	String IPv4 =
			"\\G(?<url>[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})";
}

enum RegExType {
	URL(RegExPattern.URL),	
	URI(RegExPattern.URI),
	IPv4(RegExPattern.IPv4);
	
	protected final String regExPattern;
	
    RegExType(final String regExPattern) { 
       	this.regExPattern = regExPattern;
    }
    
	String parse(String value) {
		return parse(value, regExPattern);
	}
	
	/**
 	 * define regular string type by it string representation
 	 * @param value
 	 * @return String value in upperCase if OK or NULL if not validate 
 	 * @throw IllegalArgumentException, PatternSyntaxException
 	 */
 	private String parse(final String value, String regExPattern) { 
 		if(value == null) return null;
 		if(value.matches(regExPattern)) {
 			return value; 
 		} else {
 			return null;
 		}
	}

}
