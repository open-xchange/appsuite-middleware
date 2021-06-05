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

package com.openexchange.gdpr.dataexport.impl.storage;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Collection;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.server.ServiceLookup;

/**
 * {@link UserDbDataExportSql} - The SQL access using common user pay-load database.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class UserDbDataExportSql extends AbstractDataExportSql<Integer> {

    private final ContextService contextService;

    /**
     * Initializes a new {@link UserDbDataExportSql}.
     *
     * @param databaseService The database service
     * @param contextService The context service
     * @param config The configuration
     * @param services The service look-up
     */
    public UserDbDataExportSql(DatabaseService databaseService, ContextService contextService, DataExportConfig config, ServiceLookup services) {
        super(databaseService, config, services);
        this.contextService = contextService;
    }

    @Override
    protected void backReadOnly(Integer contextId, Connection con) {
        if (con != null) {
            databaseService.backReadOnly(contextId.intValue(), con);
        }
    }

    @Override
    protected void backWritable(boolean modified, Integer contextId, Connection con) {
        if (con != null) {
            if (modified) {
                databaseService.backWritable(contextId.intValue(), con);
            } else {
                databaseService.backWritableAfterReading(contextId.intValue(), con);
            }
        }
    }

    @Override
    protected Connection getReadOnly(Integer contextId) throws OXException {
        return databaseService.getReadOnly(contextId.intValue());
    }

    @Override
    protected Connection getWritable(Integer contextId) throws OXException {
        return databaseService.getWritable(contextId.intValue());
    }

    @Override
    protected Collection<Integer> getSchemaReferences() throws OXException {
        return contextService.getDistinctContextsPerSchema();
    }

    @Override
    protected Integer getSchemaReference(int userId, int contextId) throws OXException {
        return I(contextId);
    }

}
