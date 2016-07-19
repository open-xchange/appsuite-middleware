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
        set(object, get(resultSet, columnLabels));
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
