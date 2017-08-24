package ru.zrv.tazacom.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import ru.zrv.tazacom.dao.AuthorDAO;
import ru.zrv.tazacom.dao.BookDAO;
import ru.zrv.tazacom.dao.GenreDAO;
import ru.zrv.tazacom.dao.ImportingDAO;
import ru.zrv.tazacom.entity.Author;
import ru.zrv.tazacom.entity.Book;
import ru.zrv.tazacom.entity.Genre;
import ru.zrv.tazacom.entity.Importing;
import ru.zrv.tazacom.entity.StateEnum;
import ru.zrv.tazacom.web.util.reader.ResourceReader;

/**
 * Сервис импорта книг из внешнего исторчника
 * 
 *  Для демонстрационных целей здесь представлены два разных способа вызова сервиса:
 *  1. как RESTful - сервис - см. метод doImportRest,
 *  2. внедрить в класс Web-слоя proxy-делегат для удалённого доступа к сервису- см. метод doImport
 * 
 * В обоих случаях оба метода имеют парметром строку URL для запроса к внешнему источнику со списком книг.
 * С той разницей, что:
 * - В первом случае парсинг полученной иформации производится здесь же 
 * и сразу же производятся все необходимые записи в базу данных.
 * На Web-уровень возвращается лишь код нормального завершения импорта, либо код ошибки.
 * 
 * - Во втором случае, метод лишь запрашивает список книг по указанному URL-адресу и возвращает этот список обратно 
 * на Web-уровень вместе с кодом ошибки, если таковая случится во время импорта.
 * Парсинг списка книг и дальнейшие изменения в базе данных ложатся в этом случае на Web-уровень.
 * 
 * Для тестовых целей служит метод doImport() без параметров, результат выполнения которого 
 * можно посмотреть прямо в окне браузера по адресу:
 * 
 * 	http://localhost:8080/app/rest/v2/services/tazacom_ImportService/doImport?url=file:BookList.Example.json
 * 
 * Формально, все три метода доступны как внешние RESTful-сервисы, но для реальных целей нужно в строку 
 * GET-запроса добавить параметр c URL-адресом для импорта, например:
 * 		- 	http://<localhost:8080/app>/rest/v2/services/tazacom_ImportService/doImportRest?URL=<outer-URL>
 * 	либо
 * 		-	http://<localhost:8080/app>/rest/v2/services/tazacom_ImportService/doImport?URL=<outer-URL>
 * 
 *  
 * @author Roman.Zaitseff
 */
@Service(ImportService.NAME)
public class ImportServiceBean implements ImportService {
	
	@Inject
 	private GenreDAO genreDAO;

	@Inject
 	private AuthorDAO authorDAO;
	
	@Inject
 	private BookDAO bookDAO;

	@Inject
 	private ImportingDAO importingDAO;
	
	final String CODE_OK = "OK";
	final String CODE_NOK = "NOK";
	
	public String doImport(String url) {
		//-----------------------			
		//for test aims was realized follow opportunity:
		if(checkTestModeImport(url)) {
			return testModeImport(url);
		}
		//------------------------			
		return new ResourceReader().getUriRequest(url);
	}

	private boolean checkTestModeImport(String url) {
		return url.endsWith("BookList.Example.json");
	}
	
	private String testModeImport(String url) {
		JSONObject answer = new JSONObject();
		answer.put("code", CODE_OK);
		answer.put("answer", new JSONArray(doImport()));
		return answer.toString();
	}
		
	public String doImportRest(@DefaultValue("classpath:BookList.Example.json") @QueryParam("url")  String url) {
		System.out.println("===============>>> url >>>" + url);
		String code = CODE_NOK;

		//for test aims was realized follow opportunity:
		JSONObject answer = new JSONObject(
				checkTestModeImport(url)
					? testModeImport(url)
					: new ResourceReader().getUriRequest(url));
		code = answer.getString("code");
		System.out.println("===============>>> return code  >>>" + code);
		
		if(!CODE_OK.equals(code)) {
			return answer.toString();
		}
		
		//----------------
		// return code is OK;
		//----------------
		JSONArray books = answer.getJSONArray("answer");
		for(int i = 0; i < books.length(); i++) {
			JSONObject bookJS = books.getJSONObject(i);
			Book book = new Book();
			book.setState(StateEnum.ACTIVE);
			book.setName(bookJS.getString("name"));
			book.setYear(bookJS.getInt("year"));
			book.setEdition(bookJS.getString("edition"));
			
			JSONObject authorJS = bookJS.getJSONObject("author");
			Author author = new Author();
			author.setState(StateEnum.ACTIVE);
			author.setFirstName(authorJS.getString("firstName"));
			author.setMiddleName(authorJS.getString("middleName"));
			author.setLastName(authorJS.getString("lastName")); 
			author = authorDAO.include(author);
			book.setAuthor(author); 

			book = bookDAO.include(book);

			JSONArray genresJS = bookJS.getJSONArray("genreCollection");
			List<Genre> genres = new ArrayList<Genre>();
			if(genresJS.length() > 0) {
				for(int j = 0; j < genresJS.length(); j++) {
					JSONObject genreJS = genresJS.getJSONObject(j);
					Genre genre = new Genre();
					genre.setState(StateEnum.ACTIVE);
					genre.setName(genreJS.getString("name"));
					genre = genreDAO.include(genre);
					genres.add(genre);
				}

				bookDAO.connectBookWithGenres(book, genres);
			}
		}
		
		Importing importing = new Importing();
		importing.setState(StateEnum.ACTIVE);
		importing.setUrl(url);
		importing.setDate(new Date());
		importing.setQuantity(books.length());
		importingDAO.add(importing);

		return answer.toString();
	}

	// we can do request from browser
	public String doImport() {
		String books = 
				"[{"
	 + "'id': '0ae2a56c-5dd4-47ed-b230-690c11a786be',"
	 + "'name': 'Alice`s Adventures in Wonderland',"
	 + "'author': {"
				+ 	"'id': 'bdd6741d-71b9-493c-9c31-38 af19b9e27c',"
				+ "'lastName': 'Carroll',"
				+ "'middleName': 'Edward',"
				+ "'firstName': 'Lewis'"
			+ "},"
	 + "'year': 1865,"
	 + "'edition': 'Издание первое, дополненное',"
	 + "'genreCollection': "
					 + "["
					 	+ "{"
						 + "'id': 'f022820c-cb84-4981-9184-46b6f9a17de8',"
						 + "'name': 'Children`s fiction'"
						 + "},"
						 + "{"
						 + "'id': '94c06668-5214-4483-b99c-77d7aa7048b3',"
						 + "'name': 'Fiction'"
						 + "}"
					 + "]"
					 + "}"
					 +",{"
					 + "'id': '0ae2a56c-5dd4-47ed-b230-690c11a786be',"
					 + "'name': 'Alice`s Adventures through the Looking Glass',"
					 + "'author': {"
								+ "'id': 'bdd6741d-71b9-493c-9c31-38 af19b9e27a',"
								+ "'lastName': 'Carroll',"
								+ "'middleName': 'Goward',"
								+ "'firstName': 'Lewis'"
							+ "},"
					 + "'year': 1865,"
					 + "'edition': 'Издание второе, расширенное',"
					 + "'genreCollection': "
									 + "["
									 	+ "{"
										 + "'id': 'f022820c-cb84-4981-9184-46b6f9a17deb',"
										 + "'name': 'Adventure`s fantasy'"
										 + "},"
									 	+ "{"
										 + "'id': 'f022820c-cb84-4981-9184-46b6f9a17de8',"
										 + "'name': 'Children`s fiction'"
										 + "},"
										 + "{"
										 + "'id': '94c06668-5214-4483-b99c-77d7aa7048b3',"
										 + "'name': 'Fiction'"
										 + "}"
									 + "]"
					+ "}"

					 +",{"
					 + "'id': '0ae2a56c-5dd4-47ed-b230-690c11a786be',"
					 + "'name': 'Алиса в зазеркалье',"
					 + "'author': {"
								+ "'id': 'bdd6741d-71b9-493c-9c31-38 af19b9e27a',"
								+ "'lastName': 'Кэрол',"
								+ "'middleName': 'Эдвард',"
								+ "'firstName': 'Льюис'"
							+ "},"
					 + "'year': 1865,"
					 + "'edition': 'Издание третье, переработанное',"
					 + "'genreCollection': "
									 + "["
									 	+ "{"
										 + "'id': 'f022820c-cb84-4981-9184-46b6f9a17deb',"
										 + "'name': 'Фантастические приключения'"
										 + "},"
									 	+ "{"
										 + "'id': 'f022820c-cb84-4981-9184-46b6f9a17de8',"
										 + "'name': 'Детские'"
										 + "}"
									 + "]"
									 + "}"

					 +",{"
					 + "'id': '0ae2a56c-5dd4-47ed-b230-690c11a786ba',"
					 + "'name': 'Материализм и эмпириокритицизм'," 
					 + "'author': {"
								+ "'id': 'bdd6741d-71b9-493c-9c31-38 af19b9e27a',"
								+ "'lastName': 'Ленин',"
								+ "'middleName': 'Ильич',"
								+ "'firstName': 'Владимир'"
							+ "},"
					 + "'year': 1903,"
					 + "'edition': 'Издание двадцать третье, расширенное',"
					 + "'genreCollection': "
									 + "["
									 	+ "{"
										 + "'id': 'f022820c-cb84-4981-9184-46b6f9a17deb',"
										 + "'name': 'Философия'"
										 + "},"
									 	+ "{"
										 + "'id': 'f022820c-cb84-4981-9184-46b6f9a17de8',"
										 + "'name': 'Политика'"
										 + "}"
									 + "]"
									 + "}"

					 + "]";
		
		return books;
	}
}