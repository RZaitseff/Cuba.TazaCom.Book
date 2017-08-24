package ru.zrv.tazacom.web.util;

import java.io.PrintStream;
import java.util.Date;

/**
 * Application Logger
 */
public class Logger {
	
	/** standard system output reserved for the application logging */
	private final static PrintStream logger = System.out;
	
	private final String className;

	public Logger(String className) {
		this.className = (className != null ? className : "Logger");
	}
	
	/**
	 * log error
	 */
	public void error(String message) {
		logger.println(new Date() + " -> " + className + ": " + message);
	}


	/**
	 * print any formant message
	 */
	public void print(String message) {
		logger.println(message);
	}

}
