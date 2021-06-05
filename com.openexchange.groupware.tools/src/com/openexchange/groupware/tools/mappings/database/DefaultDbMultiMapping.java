/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.tools.mappings.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapping;

/**
 * {@link DefaultDbMultiMapping} - Abstract {@link DbMapping} implementation.
 *
 * @param <T> the type of the property
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultDbMultiMapping<T, O> extends DefaultMapping<T, O> implements DbMultiMapping<T, O> {

    private final String[] columnLabels;
    private final String readableName;

    /**
     * Initializes a new {@link DefaultDbMultiMapping}.
     *
     * @param columnLabels The column labels
     * @param readableName The readable name
     */
    public DefaultDbMultiMapping(String[] columnLabels, String readableName) {
        super();
        this.columnLabels = columnLabels;
        this.readableName = readableName;
    }

    @Override
    public T get(ResultSet resultSet, String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getColumnLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getColumnLabel(String prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getReadableName(O object) {
        return readableName;
    }

    @Override
    public int getSqlType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(ResultSet resultSet, O object) throws SQLException, OXException {
        set(resultSet, object, getColumnLabels());
    }

    @Override
    public void set(ResultSet resultSet, O object, String columnLabel) throws SQLException, OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(ResultSet resultSet, O object, String[] columnLabels) throws SQLException, OXException {
        T value = get(resultSet, columnLabels);
        if (null != value) {
            set(object, value);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Neutral default implementation, override if applicable.
     */
    @Override
    public void validate(O object) throws OXException {
        // empty
    }

    @Override
    public String[] getColumnLabels() {
        return columnLabels;
    }

    @Override
    public String[] getColumnLabels(String prefix) {
        String[] prefixedLabels = new String[columnLabels.length];
        for (int i = 0; i < columnLabels.length; i++) {
            prefixedLabels[i] = prefix + columnLabels[i];
        }
        return prefixedLabels;
    }

}
