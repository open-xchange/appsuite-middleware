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
