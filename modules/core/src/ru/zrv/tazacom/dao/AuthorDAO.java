package ru.zrv.tazacom.dao;

/**
 * @author Roman Zaitseff
 * @e-mail RZaytseff@mail.ru
 */

import org.springframework.stereotype.Component;
import com.haulmont.cuba.core.Query;

import ru.zrv.tazacom.entity.Author;

@Component("authorDAO")
public class AuthorDAO extends _DAO<Author> {

	protected String whereCondition = "where e.firstName = :firstName "
			+ " and e.middleName LIKE :middleName "
			+ " and e.lastName = :lastName";

	protected Query query(String query, Author author) {
		return getEM().createQuery(query, AuthorDAO.class)
				.setParameter("firstName",   author.getFirstName())
				.setParameter("lastName",    author.getLastName())
				.setParameter("middleName", (author.getMiddleName() != null ? author.getMiddleName() : ""))
				;
	}

	@Override
	protected String getWhere() {
		return whereCondition;
	}
}
