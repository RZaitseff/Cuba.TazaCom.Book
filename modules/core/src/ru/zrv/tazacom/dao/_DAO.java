package ru.zrv.tazacom.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.NoResultException;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.StandardEntity;

import ru.zrv.tazacom.entity.StateEnum;
import ru.zrv.tazacom.web.util.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Abstract class assign to do base operation as:
 * 		 Create, Read, Update, Mark as Delete or as Active, List, Count
 * 
 * @author Roman.Zaitseff
 *
 * @param generic <Entity> - Entity
 * 
 */
abstract public class _DAO<E extends StandardEntity> {
	
	protected Logger logger  = new Logger(_DAO.class.getName());
	
	protected String entityName;
	
	@Inject
	protected Persistence persistence;
	
	protected String getEntityName(E entity) {
		if(entityName != null) {
			return entityName;
		}
		@SuppressWarnings("rawtypes")
		Class aClass = entity.getClass();
		Annotation[] annotations = aClass.getAnnotations();
		
		for(Annotation annotation : annotations){
		    if(annotation instanceof Entity){
		        Entity e = (Entity) annotation;
		        return entityName = e.name();
		    }
		}
		return "tazacom$_Entity";
	}
	
	protected EntityManager getEM() {
		return  persistence.getEntityManager();
	}
	
	protected String getCountQuery(E entity) {
		String query = "SELECT count(e.id) FROM " + getEntityName(entity) + " e " + getWhere();
		return query;
	}
	
	protected String getListQuery(E entity) {
		return "SELECT e FROM " + getEntityName(entity) + " e " + getWhere();
	}
	
	abstract protected Query query(String query, E entity);
	
	abstract protected String getWhere();
	
	public boolean isUnique(E entity) {
		return count(entity) == 0;
	}
	
	public int count(E entity) {
		try (Transaction tx = persistence.createTransaction())  {
			int count =  ((Long) query(getCountQuery(entity), entity).getSingleResult()).intValue();
			tx.commit();
			return count;
		} catch (NoResultException e) {
			logger.error(e.getStackTrace().toString());
			return 0;
		} 
	}
	
	public E read(E entity) {
		try (Transaction tx = persistence.createTransaction())  {
			List<E> l = list(entity);
			tx.commit();
			return (l == null || l.size() == 0 ? null : l.get(0));
		} catch (NoResultException e) {
			logger.error(e.getStackTrace().toString());
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public List<E> list(E entity) {
		try (Transaction tx = persistence.createTransaction())  {
			List<E> l = (List<E>)query(getListQuery(entity), entity).getResultList();
			tx.commit();
			return l; 
		} catch (NoResultException e) {
			logger.error(e.getStackTrace().toString());
			return new ArrayList<E>();
		}
	}

	public E markAsActive(E entity) {
		return markState(entity, StateEnum.ACTIVE);
	}

	public E markAsDelete(E entity) {
		return markState(entity, StateEnum.DELETED);
	}
	
	public E markState(E entity, StateEnum state) {
		if(entity != null) {
			try {
				Method m = entity.getClass().getDeclaredMethod("setState", StateEnum.class);
				m.invoke(entity, state);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		return entity;
	}

	public E setState(E entity, StateEnum state) {
		try (Transaction tx = persistence.createTransaction())  {
			markState(entity, state);
			getEM().merge(entity);
			getEM().flush();
			tx.commit();
		}
		return entity;
	}

	public E include(E entity) {
		try (Transaction tx = persistence.createTransaction())  {
			if(count(entity) == 0) {
				getEM().merge(entity);
			} else {
				entity = read(entity);
			}
			markAsActive(entity);
			getEM().flush();
			tx.commit();
		}
		return entity;
	}
}
