package ru.zrv.tazacom.web.util.reader;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.zrv.tazacom.web.util.Logger;

/**
 * @author Roman.Zaytseff
 */
public class ResourceReader {
	
	public String getUriRequest(String url) {
		String code = "NOK";
		JSONObject answer = null;
		JSONObject ret = new JSONObject();
		JSONArray books = new JSONArray();
		
		ret.put("code", ("----- >  Import start yet"));
					
		// Read JSONArray from source uri link
		try {
			answer = ResourceReaderFabrica.gather(url);
			code = answer.getString("code");
			
			if(answer != null && (books = answer.getJSONArray("answer")).length() > 0) {
				code = "OK";
			} else if (books != null && books.length() == 0) {
				code =  "----- >  Book list is Empty";
			} else {
				code =  "----- >  Import ERROR";
			}

		} catch (IOException e) {
			code = "----- >  IO ERROR: \n" + e;
			 loggerError(url);
			 logger.error(e.getMessage());
			 e.printStackTrace();
		}
		
		answer.put("code", code);
		answer.put("answer", books);
		return answer.toString();
	}

	private static Logger logger = new Logger(ResourceReader.class.getName());

	private static void loggerError(String uri) {
		logger.error("Can't recognize uri = " + uri 
				+ " -----> So will be used DEFAULT startegy with this file path");
	}
}
