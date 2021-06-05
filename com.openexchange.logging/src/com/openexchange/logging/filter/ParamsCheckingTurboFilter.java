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

package com.openexchange.logging.filter;

import java.sql.Statement;
import org.slf4j.Marker;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;


/**
 * {@link ParamsCheckingTurboFilter} - Checks if parameters can be safely passed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.4.2
 */
public class ParamsCheckingTurboFilter extends ExtendedTurboFilter {

    /**
     * Initializes a new {@link ParamsCheckingTurboFilter}.
     */
    public ParamsCheckingTurboFilter() {
        super();
    }

    @Override
    public int getRanking() {
        return -10;
    }

    @Override
    public FilterReply decide(final Marker marker, final Logger logger, final Level level, final String format, final Object[] params, final Throwable t) {
        if (null != params && params.length > 0) {
            if (logger.getEffectiveLevel().levelInt <= level.levelInt) {
                for (int i = params.length; i-- > 0;) {
                    Object param = params[i];
                    if (param instanceof Statement) {
                        // Statements might already be closed due to asynchronous nature of logging framework
                        // Ensure no exception occurs when trying to acquire statement's string representation
                        Statement stmt = (Statement) param;
                        params[i] = getSqlStatement(stmt, "<unknown>");
                    }
                }
            }
        }

        return FilterReply.NEUTRAL;
    }

    /**
     * Gets the SQL statement from given <code>Statement</code> instance.
     *
     * @param stmt The <code>Statement</code> instance
     * @param query The optional query associated with given <code>Statement</code> instance
     * @return The SQL statement
     */
    private String getSqlStatement(Statement stmt, String query) {
        if (stmt == null) {
            return query;
        }
        try {
            String sql = stmt.toString();
            int pos = sql.indexOf(": ");
            return pos < 0 ? sql : sql.substring(pos + 2);
        } catch (Exception x) {
            return query;
        }
    }

}
