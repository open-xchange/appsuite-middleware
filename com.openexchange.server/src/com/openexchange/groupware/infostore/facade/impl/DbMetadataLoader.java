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

package com.openexchange.groupware.infostore.facade.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.sql.DBUtils;

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
            DBUtils.closeSQLStuff(results, stmt);
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
