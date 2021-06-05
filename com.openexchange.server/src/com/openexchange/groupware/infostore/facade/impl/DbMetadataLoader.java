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

package com.openexchange.groupware.infostore.facade.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link DbMetadataLoader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DbMetadataLoader<T> extends MetadataLoader<T> {

    protected DBProvider provider;

    /**
     * Initializes a new {@link DbMetadataLoader}.
     *
     * @param provider The underlying database provider
     */
    protected DbMetadataLoader(DBProvider provider) {
        super();
        this.provider = provider;
    }

    /**
     * Performs a query against the database, utilizing the supplied result processor to iterate over the result set.
     *
     * @param context The context
     * @param query The query, may contain placeholders that will be replaced in the statement by the supplied arguments
     * @param processor The result processor to use
     * @param args The arguments for the statement
     * @return The result
     * @throws SQLException
     * @throws OXException
     */
    protected <R> R performQuery(Context context, String query, ResultProcessor<R> processor, Object...args) throws SQLException, OXException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet results = null;
        try {
            connection = provider.getReadConnection(context);
            stmt = connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
            results = stmt.executeQuery();
            return processor.process(results);
        } finally {
            Databases.closeSQLStuff(results, stmt);
            if (null != connection) {
                provider.releaseReadConnection(context, connection);
            }
        }
    }

    protected static interface ResultProcessor<R> {

        /**
         * Processes a result set, yielding custom results.
         *
         * @param results The result set
         * @return The processed result
         */
        R process(ResultSet results) throws SQLException;
    }

}
