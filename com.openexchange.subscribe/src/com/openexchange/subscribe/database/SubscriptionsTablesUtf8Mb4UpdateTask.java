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

package com.openexchange.subscribe.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask;

/**
 * {@link SubscriptionsTablesUtf8Mb4UpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SubscriptionsTablesUtf8Mb4UpdateTask extends SimpleConvertUtf8ToUtf8mb4UpdateTask {

    /**
     * Initialises a new {@link SubscriptionsTablesUtf8Mb4UpdateTask}.
     */
    public SubscriptionsTablesUtf8Mb4UpdateTask() {
        super(Arrays.asList("subscriptions", "sequence_subscriptions"), AddFolderIndex.class.getName());
    }

    @Override
    protected void before(PerformParameters params, Connection connection) throws SQLException {
        recreateKey(connection, "subscriptions", new String[] { "cid", "folder_id" }, new int[] { -1, 191 });
    }
}
