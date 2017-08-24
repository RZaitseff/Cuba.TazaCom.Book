package ru.zrv.tazacom.web.util.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.function.Function;

import org.json.JSONObject;

import ru.zrv.tazacom.web.util.Logger;


/**
 * Factory method is assigned to construct
 *  URI in depends of prefix of resource's string representation 
 * and gather info from particular kind of resource 
 * in depends on prefix of resource's path
 * Supported prefixes are "system:", "file:", "classpath:",  "http(s)://", "www." 
 * and without prefix. In last case is used DEFAULT uri strategy.
 * 
 * @author Roman.Zaytseff
 */
public enum ResourceReaderFabrica {
	
	SYSTEM("system:", ResourceReaderFabrica::defaultURI),
	
	CLASSPATH("classpath:", ResourceReaderFabrica::classPathURI),
			
	FILE("file:", ResourceReaderFabrica::fileURI),
	
	HTTP("http://",   ResourceReaderFabrica::httpURI),
	
	HTTPS("https://", ResourceReaderFabrica::httpURI),
	
	WWW("www.",       ResourceReaderFabrica::httpURI),
	
	DEFAULT("", ResourceReaderFabrica::defaultURI);
	
	protected final String prefix;
	
	String getProtocol() {
		return prefix;
	}
	
	protected final Function<String, URI> uri;
	
	ResourceReaderFabrica(String prefix, Function<String, URI> uri) {
		this.prefix = prefix;
		this.uri = uri;
	}
	
	protected static ResourceReaderFabrica parse(String source) {
		if(source != null) {
			source = source.toLowerCase();
			for( ResourceReaderFabrica fabrica: ResourceReaderFabrica.values()) {
				if(source.startsWith(fabrica.getProtocol())) {
					return fabrica;
				} 
			}
		}
		return DEFAULT;
	}
	
	public JSONObject read(Reader reader) throws IOException {
		BufferedReader br = new BufferedReader(reader);
		String jsonString = br.lines().reduce("", (s1, s2) -> s1 + s2);
		return new JSONObject(jsonString); 
	}
	
	/**
	 * Method for read properties from any type and format resource
	 * @param source - stringName of resource
	 * @return properties that was red from the resource
	 */
	protected JSONObject read(String source) {
		
		if(ResourceReaderFabrica.CLASSPATH.equals(parse(source))) {
			source = source.substring(prefix.length());	
		}
		return read(uri.apply(source));
	}

	static JSONObject read(URI uri) {
		try (Reader reader
					= new InputStreamReader(uri.toURL().openStream(), StandardCharsets.UTF_8)) { 
			
			return parse(uri.toString()).read(reader);
			
		} catch (MalformedURLException e) {
			loggerError(uri.toString());
		} catch (UnsupportedOperationException | IOException e) {
			loggerError(uri.toString());
		} 
		return null;
	}

	/**
	 * Static method for read property from any type and format resource
	 * @param source - stringName of resource
	 * @return properties that was red from the resource
	 */
	public static JSONObject gather(String source) throws IOException {
		return parse(source).read(source);
	}

	/**
	 * create URI for resources with prefixes "http/s:", "www."
	 */
	static URI httpURI(String source) {
		try {
			return new URI(source);
		} catch (URISyntaxException e) {
			loggerError(source);
			return defaultURI(source);
		}
	}

	/**
	 * create URI for  resources with prefix "file:"
	 */
	static URI fileURI(String source) {
		try {
			return new URI(source);			
		} catch (URISyntaxException e) {
			loggerError(source);
			return defaultURI(source);
		}
	}

	
	/**
	 * create URI for resources with prefix "classpath:"
	 */
	static URI classPathURI(String source) {
		try { 
			return ResourceReaderFabrica.class.getClassLoader().getResource(source).toURI();
		} catch (URISyntaxException e) {
			loggerError(source);
			return defaultURI(source);
		}
	}

	/**
	 * create URI for resources without prefix or unknown protocol
	 * @throws InvalidPathException, java.io.IOError,  SecurityException
	 */
	static URI defaultURI(String source) {
		return Paths.get(source).toUri(); 
	}

	private static Logger logger = new Logger(ResourceReaderFabrica.class.getName());

	private static void loggerError(String uri) {
		logger.error("Can't recognize uri = " + uri 
				+ " -----> So will be used DEFAULT startegy with this file path");
	}
}
