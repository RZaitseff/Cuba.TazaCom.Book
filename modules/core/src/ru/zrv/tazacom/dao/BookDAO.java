package ru.zrv.tazacom.dao;

import java.util.List;

/**
 * @author Roman Zaytseff
 * @e-mail RZaytseff@mail.ru
 */

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import ru.zrv.tazacom.entity.Book;
import ru.zrv.tazacom.entity.Genre;

/**
 * DAO class with specific BOOK info * 
 * @author Roman.Zaitseff
 */
@Component("bookrDAO")
public class BookDAO extends _DAO<Book> {
	
	protected Logger logger = Logger.getLogger(BookDAO.class);
	
	protected String whereCondition = "where "
			+ "     e.author.firstName = :firstName "
			+ " and e.author.middleName LIKE :middleName "
			+ " and e.author.lastName = :lastName"
			+ " and e.name = :name"
			+ " and e.year = :year"
			+ " and e.edition = :edition"
			;

	@Override
	protected String getWhere() {
		return whereCondition;
	}
	protected Query query(String query, Book book) {
		return getEM().createQuery(query)
				.setParameter("firstName",   book.getAuthor().getFirstName())
				.setParameter("lastName",    book.getAuthor().getLastName())
				.setParameter("middleName", (book.getAuthor().getMiddleName() != null ? book.getAuthor().getMiddleName() : ""))
				.setParameter("name",        book.getName())
				.setParameter("year",        book.getYear())
				.setParameter("edition",     book.getEdition())
				;
	}
	
	public Book connectBookWithGenres(Book book, List<Genre> genres) {
		try (Transaction tx = persistence.createTransaction())  {
			EntityManager em = getEM();
			book = em.find(Book.class, book.getId());
			if(genres != null) {
				for(Genre genre: genres) {
					genre = em.find(Genre.class, genre.getId());
					try (Transaction txGenre = persistence.createTransaction())  {
						book.getGenre().add(genre);
						em.merge(book);
						em.flush();
						txGenre.commit();
					} catch (Exception e) { 
						logger.error("################## GENRE ALREADY BIND WIH BOOK !!!!!!!!!!!! ");
						logger.error("################## book name: " + book.getName()); 
						logger.error("################## ganre: " + genre.getName());
					}
				}
			}
			em.merge(book);
			em.flush();
			tx.commit();
		} catch (Exception e) { 
			logger.error("################## List of genres DON'T binded with book !!!!!!!!!!!! ");
			logger.error("################## book name: " + book.getName());
			genres.forEach((g) -> logger.error("################## ganre: " + g.getName()));
			logger.error(e.getStackTrace().toString());
		}
		return book;
	}
	
}
