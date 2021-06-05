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

package com.openexchange.folderstorage.outlook.sql;

import java.sql.PreparedStatement;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.outlook.osgi.Services;

/**
 * {@link Utility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    /**
     * Gets the {@link DatabaseService} from service registry.
     *
     * @return The {@link DatabaseService} from service registry
     * @throws OXException If {@link DatabaseService} is not contained in service registry
     */
    public static DatabaseService getDatabaseService() throws OXException {
        return Services.getService(DatabaseService.class);
    }

    /**
     * Debugs given statement's SQL string.
     *
     * @param stmt The statement
     */
    public static void debugSQL(final PreparedStatement stmt) {
        if (null != stmt) {
            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Utility.class);
            if (log.isDebugEnabled()) {
                final String sql = getSQLString(stmt);
                log.debug("Failed SQL:\n\t{}",  sql);
            }
        }
    }

    /**
     * Extracts SQL string from passed statement.
     *
     * @param stmt The statement
     * @return The extracted SQL string
     */
    public static String getSQLString(final PreparedStatement stmt) {
        final String toString = stmt.toString();
        return toString.substring(toString.indexOf(": ") + 2);
    }

}
