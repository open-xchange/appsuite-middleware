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

import java.sql.Connection;
import java.util.Collection;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GlobalDbDataExportSql} - The SQL access using global database.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class GlobalDbDataExportSql extends AbstractDataExportSql<String> {

    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link GlobalDbDataExportSql}.
     *
     * @param databaseService The database service
     * @param configViewFactory The factory for config views
     * @param config The configuration
     * @param services The service look-up
     */
    public GlobalDbDataExportSql(DatabaseService databaseService, ConfigViewFactory configViewFactory, DataExportConfig config, ServiceLookup services) {
        super(databaseService, config, services);
        this.configViewFactory = configViewFactory;
    }

    @Override
    protected void backReadOnly(String group, Connection con) {
        if (con != null) {
            databaseService.backReadOnlyForGlobal(group, con);
        }
    }

    @Override
    protected void backWritable(boolean modified, String group, Connection con) {
        if (con != null) {
            if (modified) {
                databaseService.backWritableForGlobal(group, con);
            } else {
                databaseService.backWritableForGlobalAfterReading(group, con);
            }
        }
    }

    @Override
    protected Connection getReadOnly(String group) throws OXException {
        return databaseService.getReadOnlyForGlobal(group);
    }

    @Override
    protected Connection getWritable(String group) throws OXException {
        return databaseService.getWritableForGlobal(group);
    }

    @Override
    protected Collection<String> getSchemaReferences() throws OXException {
        return databaseService.getDistinctGroupsPerSchema();
    }

    /** The name used for the special "default" context group */
    private static final String DEFAULT_GROUP = "default";

    @Override
    protected String getSchemaReference(int userId, int contextId) throws OXException {
        ConfigView view = configViewFactory.getView(userId, contextId);
        String group = view.opt("com.openexchange.context.group", String.class, null);
        return Strings.isEmpty(group) ? DEFAULT_GROUP : group;
    }

}
