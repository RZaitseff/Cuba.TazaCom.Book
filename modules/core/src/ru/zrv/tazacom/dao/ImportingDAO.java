package ru.zrv.tazacom.dao;


import javax.transaction.Transactional;

/**
 * @author Roman Zaytseff
 * @e-mail RZaytseff@mail.ru
 */

import org.springframework.stereotype.Component;

import com.haulmont.cuba.core.Query;

import ru.zrv.tazacom.entity.Importing;
import ru.zrv.tazacom.entity.StateEnum;

@Component("importingDAO")
public class ImportingDAO extends _DAO<Importing> {
	
	protected String whereCondition = "where e.url = :url "
			+ " and e.date = :date";
	
	@Override
	protected String getWhere() {
		return whereCondition;
	}
	
	protected Query query(String query, Importing importing) {
		return persistence.getEntityManager().createQuery(query)
				.setParameter("url",  importing.getUrl())
				.setParameter("date", importing.getDate())
				;
	}
	
	@Transactional(Transactional.TxType.REQUIRED)
	public Importing add(Importing importing) {
		importing.setState(StateEnum.DONE);
		getEM().persist(importing);
		getEM().flush();
		return importing;
	}

}
