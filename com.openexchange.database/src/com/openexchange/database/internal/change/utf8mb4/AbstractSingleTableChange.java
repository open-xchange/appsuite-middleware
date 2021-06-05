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

package com.openexchange.database.internal.change.utf8mb4;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.database.update.AbstractLiquibaseUtf8mb4Adapter;
import com.openexchange.java.Strings;

/**
 * {@link AbstractSingleTableChange}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractSingleTableChange extends AbstractLiquibaseUtf8mb4Adapter {

    @Override
    public String getConfirmationMessage() {
        return tableToConvert() + " table successfully converted to utf8mb4";
    }

    @Override
    protected void before(Connection connection, String schemaName) throws SQLException {
        // nothing to do
    }

    @Override
    protected void after(Connection connection, String schemaName) throws SQLException {
        // nothing to do
    }

    @Override
    protected List<String> tablesToConvert() {
        String tableToConvert = tableToConvert();
        if (tableToConvert == null || Strings.isEmpty(tableToConvert)) {
            return Collections.emptyList();
        }
        return ImmutableList.of(tableToConvert);
    }

    protected abstract String tableToConvert();
}
