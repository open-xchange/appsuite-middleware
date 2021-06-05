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

package com.openexchange.database.internal.change.utf8mb4.configdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.database.update.AbstractLiquibaseUtf8mb4Adapter;

/**
 * 
 * {@link AdvertisementConfigCustomTaskChange}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CountTablesToUtf8mb4Change extends AbstractLiquibaseUtf8mb4Adapter {

    @Override
    public String getConfirmationMessage() {
        return "Count tables successfully converted to utf8mb4";
    }

    @Override
    protected List<String> tablesToConvert() {
        return ImmutableList.of("contexts_per_filestore", "contexts_per_dbpool", "dbpool_lock", "contexts_per_dbschema", "dbschema_lock", "ctx_per_schema_sem");
    }

    @Override
    protected void before(Connection configDbCon, String schemaName) throws SQLException {
        // nothing to do
    }

    @Override
    protected void after(Connection configDbCon, String schemaName) throws SQLException {
        // nothing to do
    }

    @Override
    protected String getDefaultSchemaName() {
        return "configdb";
    }

}
