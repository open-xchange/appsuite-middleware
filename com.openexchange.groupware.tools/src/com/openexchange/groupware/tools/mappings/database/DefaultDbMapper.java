/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.tools.mappings.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map.Entry;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.Mapping;


/**
 * {@link DefaultDbMapper} - Abstract {@link DbMapper} implementation.
 *
 * @param <O> the type of the object
 * @param <E> the enum type for the fields
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultDbMapper<O, E extends Enum<E>> extends DefaultMapper<O, E> implements DbMapper<O, E> {

	/**
	 * Map containing all available mappings.
	 */
	protected final EnumMap<E, ? extends DbMapping<? extends Object, O>> mappings;
	
	/**
	 * Initializes a new {@link DefaultDbMapper}.
	 */
	public DefaultDbMapper() {
		super();
		this.mappings = createMappings();
	}

	@Override
	public O fromResultSet(final ResultSet resultSet, final E[] fields) throws OXException, SQLException {
		final O object = this.newInstance();   	
	    for (final E field : fields) {
	    	get(field).set(resultSet, object);
	    }
	    return object;
	}
	
	@Override
	public void setParameters(final PreparedStatement stmt, final O object, final E[] fields) throws SQLException, OXException {
	    for (int i = 0; i < fields.length; i++) {
	    	this.get(fields[i]).set(stmt, i + 1, object);
	    }
	}
	
	@Override
	public DbMapping<? extends Object, O> get(final E field) throws OXException {
		if (null == field) {
			throw new IllegalArgumentException("field");
		}
		final DbMapping<? extends Object, O> mapping = this.mappings.get(field);
		if (null == mapping) {
			throw OXException.notFound(field.toString());
		}
		return mapping;
	}
	
	@Override
	public E getMappedField(final String columnLabel) {
		if (null == columnLabel) {
			throw new IllegalArgumentException("columnLabel");
		}
		for (final Entry<E, ? extends DbMapping<? extends Object, O>> entry : this.mappings.entrySet()) {
			if (columnLabel.equals(entry.getValue().getColumnLabel())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	@Override
	public String getAssignments(final E[] fields) throws OXException {
		if (null == fields) {
			throw new IllegalArgumentException("fields");
		}
		final StringBuilder columnsParamsBuilder = new StringBuilder(10 * fields.length); 
		if (null != fields && 0 < fields.length) {
			columnsParamsBuilder.append(get(fields[0]).getColumnLabel()).append("=?");
			for (int i = 1; i < fields.length; i++) {
				columnsParamsBuilder.append(',').append(get(fields[i]).getColumnLabel()).append("=?");
			}
		}
		return columnsParamsBuilder.toString();
	}

	@Override
	public String getColumns(final E[] fields) throws OXException {
		if (null == fields) {
			throw new IllegalArgumentException("fields");
		}
		final StringBuilder columnsBuilder = new StringBuilder(10 * fields.length);                
		if (null != fields && 0 < fields.length) {
			columnsBuilder.append(get(fields[0]).getColumnLabel());
			for (int i = 1; i < fields.length; i++) {
				columnsBuilder.append(',').append(get(fields[i]).getColumnLabel());
			}
		}
		return columnsBuilder.toString();
	}

	@Override
	protected EnumMap<E, ? extends Mapping<? extends Object, O>> getMappings() {
		return this.mappings;
	}
	
	/**
	 * Creates the mappings for all possible values of the underlying enum. 
	 * 
	 * @return the mappings
	 */
	protected abstract EnumMap<E, ? extends DbMapping<? extends Object, O>> createMappings();

}
