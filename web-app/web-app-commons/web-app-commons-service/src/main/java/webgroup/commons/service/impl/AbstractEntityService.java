/*
 * JEE 7 Archetype, base project for a 3 tiers JEE7 maven application
 *
 * Copyright (c) 2016, Luís Ribeiro or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Luís Ribeiro.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, please send an email to:
 * luismmribeiro@gmail.com
 */
package webgroup.commons.service.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.deltaspike.data.api.EntityRepository;
import webgroup.commons.model.AbstractEntity;
import webgroup.commons.service.EntityService;

/**
 * Super class of all the services in the project. Contains the basic CRUD methods for all the services.
 * 
 * @author <a href="mailto:luismmribeiro@gmail.com">Luis Ribeiro</a>
 */
public abstract class AbstractEntityService<T extends AbstractEntity, R extends EntityRepository<T, Long>> implements EntityService<T> {

	@Inject
	private BeanManager beanManager;
	private Class<R> repositoryClass;
	private R repository;

	@SuppressWarnings("unchecked")
	public AbstractEntityService() {
		// Refer to: https://github.com/jhalterman/typetools/blob/master/src/main/java/net/jodah/typetools/TypeResolver.java

		ParameterizedType parameterizedType = getGenericTypesFromClass(this.getClass());
		this.repositoryClass = (Class<R>) parameterizedType.getActualTypeArguments()[1];
	}
	
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		Bean<R> bean = (Bean<R>) beanManager.resolve(beanManager.getBeans(this.repositoryClass));
		this.repository = beanManager.getContext(bean.getScope()).get(bean, beanManager.createCreationalContext(bean));
	}

	private ParameterizedType getGenericTypesFromClass(Class<?> clazz) {
		Type genericType = clazz.getGenericSuperclass();
		if (genericType instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) genericType;
			return paramType;
		}
		else if (genericType instanceof Class) {
			Class<?> genericTypeClass = (Class<?>) genericType;
			return getGenericTypesFromClass(genericTypeClass);
		}

		throw new IllegalStateException("Class: " + genericType.getClass().getName());
	}

	public void delete(T t) {
		getRepository().attachAndRemove(t);
	}

	public T save(T t) {
		return getRepository().save(t);
	}

	protected R getRepository() {
		return this.repository;
	}

	public List<T> findAll() {
		return getRepository().findAll();
	}

	public List<T> findAll(int start, int max) {
		return getRepository().findAll(start, max);
	}

	public Long count() {
		return getRepository().count();
	}

	public T findBy(Long primaryKey) {
		return getRepository().findBy(primaryKey);
	}
}
