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

package com.openexchange.pns.subscription.storage.groupware;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link PnsSubscriptionsAddIndexTask} - Adds an index for 'token', 'transport' and 'client' columns to table "pns_subscription".
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class PnsSubscriptionsAddIndexTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link PnsSubscriptionsAddIndexTask}.
     */
    public PnsSubscriptionsAddIndexTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { PnsSubscriptionsReindexTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        byte rollback = 0;
        try {
            String table = "pns_subscription";
            String[] columns = new String[] { "token", "transport", "client"};
            if (null == Tools.existsIndex(connection, table, columns)) {
                connection.setAutoCommit(false);
                rollback = 1;

                Tools.createIndex(connection, table, "tokenIndex", columns, false);

                connection.commit();
                rollback = 2;
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

}
