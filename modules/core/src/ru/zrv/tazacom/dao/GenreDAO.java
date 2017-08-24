package ru.zrv.tazacom.dao;

/**
 * @author Roman Zaytseff
 * @e-mail RZaytseff@mail.ru
 */

import org.springframework.stereotype.Component;
import com.haulmont.cuba.core.Query;

import ru.zrv.tazacom.entity.Genre;

@Component("genreDAO")
public class GenreDAO extends _DAO<Genre> {
	
	protected String whereCondition = "where e.name = :name ";

	@Override
	protected Query query(String query, Genre genre) {
		return getEM().createQuery(query).setParameter("name", genre.getName());
	}
	
	protected String getWhere() {
		return whereCondition;
	}
}
