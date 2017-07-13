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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.storage.rdb;

import com.openexchange.chronos.FieldAware;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;

/**
 * {@link SQLStatementBuilder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class SQLStatementBuilder {

    /**
     * Constructs the initial INSERT SQL statement. The INSERT statement is NOT ended with the semicolon ';'.
     * 
     * @param tableName The table name
     * @param mapper The mapper to use
     * @return The initial INSERT SQL statement
     * @throws OXException if an error is occurred
     */
    static <O extends FieldAware, E extends Enum<E>> StringBuilder buildInsertQueryBuilder(String tableName, DefaultDbMapper<O, E> mapper) throws OXException {
        E[] mappedFields = mapper.getMappedFields();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName);
        sb.append(" (cid,").append(mapper.getColumns(mappedFields)).append(") ");
        sb.append(" VALUES (?,").append(mapper.getParameters(mappedFields)).append(")");
        return sb;
    }

    /**
     * Constructs the initial SELECT SQL statement. The SELECT statement is NOT ended with the semicolon ';'.
     * 
     * @param tableName The table name
     * @param mapper The mapper to use
     * @return The initial SELECT SQL statement
     * @throws OXException if an error is occurred
     */
    static <O extends FieldAware, E extends Enum<E>> StringBuilder buildSelectQueryBuilder(String tableName, DefaultDbMapper<O, E> mapper) throws OXException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(mapper.getColumns(mapper.getMappedFields()));
        sb.append(" FROM ").append(tableName);
        sb.append(" WHERE cid=? ");
        return sb;
    }

    /**
     * Constructs the initial DELETE SQL statement. The DELETE statement is NOT ended with the semicolon ';'.
     * 
     * @param tableName The table name
     * @return the initial DELETE SQL statement
     */
    static StringBuilder buildDeleteQueryBuilder(String tableName) {
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(tableName).append(" WHERE cid=? ");
        return sb;
    }
}
