package ru.zrv.tazacom.web.importing;

import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.gui.WebWindow;

import ru.zrv.tazacom.entity.Author;
import ru.zrv.tazacom.entity.Book;
import ru.zrv.tazacom.entity.Genre;
import ru.zrv.tazacom.entity.Importing;
import ru.zrv.tazacom.entity.StateEnum;
import ru.zrv.tazacom.service.ImportService;
import ru.zrv.tazacom.web.util.reader.ResourceReader;
import ru.zrv.tazacom.web.util.validator.AddressType;

/**
 * Класс-контроллер импорта книг из внешнего исторчника соответствующий модальному окну importing-url-dialog.xml
 * 
 *  Для демонстрационных целей здесь представлены два разных способа вызова удалeнноiо сервиса:
 *  1. как RESTful - сервис - см. метод callRest,
 *  2. через proxy-делегат удалённого доступа к сервису- см. метод confirm
 * 
 * В обоих случаях оба метода получают из модального окна парметр со строкой URL для запроса к внешнему источнику со списком книг.
 * С той разницей, что:
 * - В первом случае метод лишь делает GET - запрос к RESTful - сервису с адресом URL для импорта 
 *   и получает в ответ лишь код ошибки, если таковая случится во время импорта.
 *   Остаётся лишь отобразить на экране код ошибки, дабы таковая случится,
 *   либо просто закрыть модальное окно запроса URL-адреса импорта.
 *   
 *  - Во втором случае метод делает запрос к удалённому сервису бизнес-уровня через внедрённый proxy-делегат и
 *  	---	получает в ответ код нормального завершения со списком импортированныз книг,
 *  либо 
 *  	---	код ошибки
 *  Особенность прокси такова, что он самостоятельно разбирается действительно ли требуемый сервис бизнес-уровня
 *  является удалённым, или это локальный вызов. В случае удалённого вызова автоматически формируется GET-запрос.
 *   Парсинг списка книг и дальнейшие изменения в базе данных ложатся в этом случае на этот метод.
 *   Для доступа к базе данных используется стандартный сервис DataService.
* 
 * Для тестовых целей можно использовать специальный URL-адрес импорта, который должен заканчиваться так:
 *  <один из префиксов CLASSPATH, FILE, SYSTEM, HTTP, WWW>:<любой HOST>/BookList.Example.json
 *  например:
 *  		file:BookList.Example.json
 *  или
 *  		http://<localhost:8080/app>/rest/v2/services/tazacom_ImportService/doImportRest?file:BookList.Example.json
 * 
 * 
 * @author Roman.Zaitseff
 */
 public class ImportingUrlDialog extends AbstractWindow {
	
    final String HOST = "http://localhost:8080/app";
    
    final String REST_SERVICE_URI = "/rest/v2/services/tazacom_ImportService/doImportRest?url=";
        
    @Inject
    private Label label;

    @Inject
    private TextField urlString;
    
    @WindowParam
    private String url;
    
    @Inject
    ImportService importService; //ImportService.NAME
    
    @Inject
    private DataService dataService;
    
	@Inject
	Metadata metadata;
	   
    @Inject
    private CollectionDatasource<Book, UUID> booksDs;

    @Inject
    private CollectionDatasource<Author, UUID> authorsDs;

    @Inject
    private CollectionDatasource<Genre, UUID> genresDs;

    @Inject
    private CollectionDatasource<Importing, UUID> importingsDs;
    
    private Importing importing;
    
    @Override
    public void init(Map<String, Object> params) {
        label.setValue("Enter imported URL");
//        urlString.setDatatype(Datatypes.get(String.class));
    }

    /**
     * Returns the value entered by user.
     * @throws MalformedURLException 
     */
    public String getUrl() {
        return urlString.getValue().toString();
    }

    /**
    *   URL validation
    **/
    private boolean isValidUrl(Component source) {
        url = getUrl();
		if(!AddressType.isValid(url)) {
			showError(source, "-----> Import error: Not valid URL");	
			return false;
		} 
		
        	return true;
    }

    /**
    *   Call outer RESTfull services
    *   but according to PRESENTATION aims the REST services install on this server
    */
    public void callRest(Component source) {
		if(!isValidUrl(source)) {
			return;
		}
		JSONObject answer = new JSONObject(new ResourceReader().getUriRequest(HOST + REST_SERVICE_URI + url));
		String code = answer.getString("code");
		if(!"OK".equals(code)) {
			showError(source, "-----> Import ERROR: " + code);	
			return;			
		}
		
		closeModalWindow();
    }

    public void confirm(Component source) {
		if(!isValidUrl(source)) {
			return;
		}
		
		
		// call injected import service
		JSONObject answer = new JSONObject(importService.doImport(url));
		if(!"OK".equals(answer.getString("code"))) {
			showError(source, "-----> Import warm: " + answer.getString("code"));	
			return;			
		}
		
		JSONArray books= null;
		try {
			books = answer.getJSONArray("answer");
			System.out.println(books.toString());
		} catch (JSONException e) {
			showError(source, "-----> Import ERROR: importing data has not valid format");	
			System.err.println(answer);
			return;				
		}
		
		if(books == null || books.length() == 0) {
			showError(source, "-----> Import warm: book list is empty");	
			return;
		}
		
		// add book  to database with new IMPORT_ID and if demand then author and genre too
		importing = createNewImporting(url);
		List<Book> bookList = new ArrayList<>();
		for (int i = 0; i < books.length(); i++) {
			bookList.add(updateBookList(books.getJSONObject(i)));
		}
		
		// update book list in importing record
		updateImporting(bookList);
		
	    closeModalWindow();	        
    }
    
	// show error message in main modal dialog window
	private void closeModalWindow() {
		close(Window.COMMIT_ACTION_ID);			    	
    }

	// show error message in main modal dialog window
	private void showError(Component source, String error) {
		((WebWindow)source.getParent().getParent()).setCaption(error);			    	
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private LoadContext getLoadContext(StandardEntity entity) {
		LoadContext context = null;
		if(entity instanceof Author) {
			Author author = (Author) entity;
		    context = new LoadContext(Author.class);
		    context.setQueryString("select author from tazacom$Author author where author.firstName = :firstName and author.middleName = :middleName and author.lastName = :lastName")
		            .setParameter("firstName", author.getFirstName())
		            .setParameter("middleName", author.getMiddleName())
		            .setParameter("lastName", author.getLastName());
		    
		} else if(entity instanceof Genre) {
			Genre genre = (Genre) entity;
		    context = new LoadContext(Genre.class);
		    context.setQueryString("select genre from tazacom$Genre genre where genre.name = :name")
		            .setParameter("name", genre.getName());
			
		} else if(entity instanceof Book) {
			Book book = (Book) entity;
		    context = new LoadContext(Book.class);
		    context.setQueryString("select book from tazacom$Book book where book.name = :name and book.year = :year and book.edition = :edition "
		    		+ " and book.author.firstName = :firstName and book.author.middleName = :middleName and book.author.lastName = :lastName")
		            .setParameter("name", book.getName())
		    	    .setParameter("year", book.getYear())
		    	    .setParameter("edition", book.getEdition())
		    	    .setParameter("firstName", book.getAuthor().getFirstName())
		    	    .setParameter("middleName", book.getAuthor().getMiddleName())
		    	    .setParameter("lastName", book.getAuthor().getLastName());
		}
			
		return context;
	}
	
	@SuppressWarnings("unchecked")
	private long getCount(StandardEntity entity) {
		return dataService.getCount(getLoadContext(entity));
	}
	
	@SuppressWarnings("unused")
	private boolean isUnique(StandardEntity entity) {
		return getCount(entity) == 0;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private StandardEntity includeEntity(StandardEntity entity, CollectionDatasource ds) {
		long count = getCount(entity);
		if(count == 0) {
			// there is no exists entity
		    try {
	   			ds.addItem(entity);  
	   			return entity;
	   		} catch(RuntimeException e) {
	   			// if record is not unique then nothing to do
	   			authorsDs.size();		// for check in debug mode
	   			return null;
	   		}
			
		} else if(count == 1) {
			// find lonely entity
			return (StandardEntity) dataService.load(getLoadContext(entity));
		} else {
			// find first among many entities
			return (StandardEntity) dataService.loadList(getLoadContext(entity)).get(0);
		}
	}
	
	private Author updateAuthorList(JSONObject book) { 
		JSONObject authorJson = book.getJSONObject("author");
		
		// Add new author if needed
		Author author = metadata.create(Author.class);
		author.setState(StateEnum.ACTIVE);
		author.setFirstName(authorJson.getString("firstName"));
		try {
			author.setMiddleName(authorJson.getString("middleName"));
		} catch (JSONException e) {
//			author.setMiddleName("");
		}
		author.setLastName(authorJson.getString("lastName"));
		
		return (Author)includeEntity(author, authorsDs);
	}
	
	private List<Genre> updateGenreList(JSONObject book) {
		JSONArray genreJson = book.getJSONArray("genreCollection");
		List<Genre> genres = new ArrayList<>();
		
		// Add new genre if needed
		for(int i = 0; i < genreJson.length(); i++) {
			Genre genre = metadata.create(Genre.class);
			genre.setState(StateEnum.ACTIVE);
			genre.setName(((JSONObject)genreJson.get(i)).getString("name"));
			
			genre = (Genre)includeEntity(genre, genresDs);
			genres.add(genre);
		}
   		return genres;		
	}

	private Book updateBookList(JSONObject bookJson) {
		// Add new book if needed
		Book book = metadata.create(Book.class);
		book.setState(StateEnum.ACTIVE);
		book.setAuthor(updateAuthorList(bookJson));
		book.setName(bookJson.getString("name"));
		book.setYear(bookJson.getInt("year"));
		book.setEdition("" + bookJson.get("edition"));
		book.setGenre(updateGenreList(bookJson));
		
		return (Book)includeEntity(book, booksDs);
	}

	private Importing createNewImporting(String url) {
		// Create new importing records
		importing = metadata.create(Importing.class);
		importing.setState(StateEnum.ACTIVE);
		importing.setDate(new Date());
		importing.setUrl(url);
		return importing;
	}

	private Importing updateImporting(List<Book> bookList) {
		// Create new importing records
		importing.setState(StateEnum.DONE);
//		importing.setBookList(bookList);
		importing.setQuantity(bookList.size());
      // Add new import records to the datasource
		try {
			importingsDs.addItem(importing); 
			
			authorsDs.commit();
			genresDs.commit();
			booksDs.commit();
			importingsDs.commit();
			
  		} catch(RuntimeException e) {
   			// nothing to do
  			importingsDs.size(); // for check in debug mode
   		}
		return importing;
	}

    public void cancel(Component source) {
        close(Window.CLOSE_ACTION_ID);
    }

	public void setDate() {
		setCaption(getCaption() + ", Now is " + new Date());
		 
	}
}