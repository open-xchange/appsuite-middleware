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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
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
    public O fromResultSet(final ResultSet resultSet, final E[] fields, String columnLabelPrefix) throws OXException, SQLException {
        final O object = this.newInstance();
        for (final E field : fields) {
            DbMapping<? extends Object, O> mapping = get(field);
            if (DbMultiMapping.class.isInstance(mapping)) {
                DbMultiMapping<? extends Object, O> multiMapping = (DbMultiMapping<? extends Object, O>) mapping;
                String[] columnLabels = null == columnLabelPrefix ? multiMapping.getColumnLabels() : multiMapping.getColumnLabels(columnLabelPrefix);
                multiMapping.set(resultSet, object, columnLabels);
            } else {
                String columnLabel = null == columnLabelPrefix ? mapping.getColumnLabel() : columnLabelPrefix + mapping.getColumnLabel();
                mapping.set(resultSet, object, columnLabel);
            }
        }
        return object;
    }

    @Override
    public List<O> listFromResultSet(ResultSet resultSet, E[] fields) throws OXException, SQLException {
        List<O> list = new ArrayList<O>();
        while (resultSet.next()) {
            O object = this.newInstance();
            for (E field : fields) {
                get(field).set(resultSet, object);
            }
            list.add(object);
        }
        return list;
    }

    @Override
    public void setParameters(final PreparedStatement stmt, final O object, final E[] fields) throws SQLException, OXException {
        setParameters(stmt, 1, object, fields);
    }

    @Override
    public int setParameters(PreparedStatement stmt, int parameterIndex, O object, E[] fields) throws SQLException, OXException {
        for (E field : fields) {
            parameterIndex += get(field).set(stmt, parameterIndex, object);
        }
        return parameterIndex;
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
    public DbMapping<? extends Object, O> opt(E field) {
        if (null == field) {
            throw new IllegalArgumentException("field");
        }
        return mappings.get(field);
    }

	@Override
    public E getMappedField(String columnLabel) {
		if (null == columnLabel) {
			throw new IllegalArgumentException("columnLabel");
		}
        for (Entry<E, ? extends DbMapping<? extends Object, O>> entry : mappings.entrySet()) {
			if (columnLabel.equals(entry.getValue().getColumnLabel())) {
				return entry.getKey();
			}
            if (DbMultiMapping.class.isInstance(entry.getValue())) {
                for (String column : ((DbMultiMapping<?, ?>) entry.getValue()).getColumnLabels()) {
                    if (columnLabel.equals(column)) {
                        return entry.getKey();
                    }
                }
            }
		}
		return null;
	}

	@Override
    public String getAssignments(E[] fields) throws OXException {
		if (null == fields) {
			throw new IllegalArgumentException("fields");
		}
        StringBuilder stringBuilder = new StringBuilder(10 * fields.length);
		if (null != fields && 0 < fields.length) {
            appendAssignments(stringBuilder, get(fields[0]));
			for (int i = 1; i < fields.length; i++) {
                stringBuilder.append(',');
                appendAssignments(stringBuilder, get(fields[i]));
			}
		}
        return stringBuilder.toString();
	}

    @Override
    public String getColumns(final E[] fields) throws OXException {
        return getColumns(fields, null);
    }

    @Override
    public String getColumns(E[] fields, String columnLabelPrefix) throws OXException {
        if (null == fields) {
            throw new IllegalArgumentException("fields");
        }
        StringBuilder stringBuilder = new StringBuilder(10 * fields.length);
        if (null != fields && 0 < fields.length) {
            appendColumnLabels(stringBuilder, get(fields[0]), columnLabelPrefix);
            for (int i = 1; i < fields.length; i++) {
                stringBuilder.append(',');
                appendColumnLabels(stringBuilder, get(fields[i]), columnLabelPrefix);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets all mapped fields.
     *
     * @return The mapped fields
     */
    public E[] getMappedFields() {
        return getMappedFields(null);
    }

    /**
     * Gets the mapped fields out of the supplied requested fields, ignoring unmapped fields.
     *
     * @param requestedFields The requested fields, or <code>null</code> to get all mapped fields
     * @return The mapped fields
     */
    public E[] getMappedFields(E[] requestedFields) {
        Set<E> knownFields = getMappings().keySet();
        Set<E> mappedFields;
        if (null == requestedFields) {
            mappedFields = knownFields;
        } else {
            mappedFields = new HashSet<E>(requestedFields.length);
            for (E field : requestedFields) {
                if (knownFields.contains(field)) {
                    mappedFields.add(field);
                }
            }
        }
        return mappedFields.toArray(newArray(mappedFields.size()));
    }

    /**
     * Gets a string to be used as parameter values in <code>INSERT</code>- or <code>UPDATE</code>-statements, obeying the number of
     * columns required per field.
     *
     * @param fields The fields to be inserted or updated
     * @return The parameter string without surrounding parentheses, e.g. <code>?,?,?,?</code>
     */
    public String getParameters(E[] fields) throws OXException {
        int count = 0;
        if (null != fields) {
            for (E field : fields) {
                DbMapping<? extends Object, O> mapping = get(field);
                if (DbMultiMapping.class.isInstance(mapping)) {
                    count += ((DbMultiMapping<?, ?>) mapping).getColumnLabels().length;
                } else {
                    count++;
                }
            }
        }
        return getParameters(count);
    }

    /**
     * Gets a string to be used as parameter values in <code>INSERT</code>- or <code>UPDATE</code>-statements.
     *
     * @param count The number of parameters
     * @return The parameter string without surrounding parentheses, e.g.<code>?,?,?,?</code>
     */
    public static String getParameters(int count) {
        StringBuilder stringBuilder = new StringBuilder(2 * count);
        if (0 < count) {
            stringBuilder.append('?');
            for (int i = 1; i < count; i++) {
                stringBuilder.append(",?");
            }
        }
        return stringBuilder.toString();
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

    /**
     * Appends database assignments to parameter values for the columns of a specific mapping, e.g. <code>column=?</code>. Mappings with
     * multiple columns are separated with the <code>,</code>-character automatically.
     *
     * @param stringBuilder The string builder to use for appending
     * @param mapping The mapping to append an assignment for
     * @return The passed string builder reference
     */
    private static StringBuilder appendAssignments(StringBuilder stringBuilder, DbMapping<?, ?> mapping) {
        String[] labels = getColumnLabels(mapping);
        stringBuilder.append(labels[0]).append("=?");
        for (int i = 1; i < labels.length; i++) {
            stringBuilder.append(',').append(labels[i]).append("=?");
        }
        return stringBuilder;
    }

    /**
     * Appends column labels of a specific mapping, optionally prefixed, e.g. <code>prefix.column</code>. Mappings with multiple columns
     * are separated with the <code>,</code>-character automatically.
     *
     * @param stringBuilder The string builder to use for appending
     * @param mapping The mapping to append the column label(s) for
     * @param columnLabelPrefix The column label prefix to use, or <code>null</code> for no prefix
     * @return The passed string builder reference
     */
    private static StringBuilder appendColumnLabels(StringBuilder stringBuilder, DbMapping<?, ?> mapping, String columnLabelPrefix) {
        String[] labels = getColumnLabels(mapping);
        if (null != columnLabelPrefix) {
            stringBuilder.append(columnLabelPrefix);
        }
        stringBuilder.append(labels[0]);
        for (int i = 1; i < labels.length; i++) {
            stringBuilder.append(',');
            if (null != columnLabelPrefix) {
                stringBuilder.append(columnLabelPrefix);
            }
            stringBuilder.append(labels[i]);
        }
        return stringBuilder;
    }

    /**
     * Gets all column labels a specific mapping uses. This is usually a single column, but also multiple column labels are possible.
     *
     * @param mapping The mapping to get the labels for
     * @return The column labels
     */
    private static String[] getColumnLabels(DbMapping<?, ?> mapping) {
        if (DbMultiMapping.class.isInstance(mapping)) {
            return ((DbMultiMapping<?, ?>) mapping).getColumnLabels();
        } else {
            return new String[] { mapping.getColumnLabel() };
        }
    }

}
