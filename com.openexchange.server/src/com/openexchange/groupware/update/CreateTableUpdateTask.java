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

package com.openexchange.groupware.update;

import static com.openexchange.groupware.update.UpdateConcurrency.BLOCKING;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;


/**
 * {@link CreateTableUpdateTask} - Wraps an existing {@link CreateTableService} instance as an update task.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CreateTableUpdateTask implements UpdateTaskV2 {

    private final CreateTableService create;
    private final String[] dependencies;

    /**
     * Initializes a new {@link CreateTableUpdateTask} from specified arguments.
     * <p>
     * This is the legacy constructor for maintaining the former constructor declaration.
     *
     * @param create The create-table service
     * @param dependencies The dependencies to preceding update tasks
     * @param version The version number; no more used
     * @param databaseService The database service; no more used
     * @deprecated Please use {@link #CreateTableUpdateTask(CreateTableService, String[])}
     */
    @Deprecated
    public CreateTableUpdateTask(CreateTableService create, String[] dependencies, int version, DatabaseService databaseService) {
        this(create, dependencies, databaseService);
    }

    /**
     * Initializes a new {@link CreateTableUpdateTask} from specified arguments.
     * <p>
     * This is the legacy constructor for maintaining the former constructor declaration.
     *
     * @param create The create-table service
     * @param dependencies The dependencies to preceding update tasks
     * @param databaseService The database service; no more used
     * @deprecated Please use {@link #CreateTableUpdateTask(CreateTableService, String[])}
     */
    @Deprecated
    public CreateTableUpdateTask(CreateTableService create, String[] dependencies, DatabaseService databaseService) {
        this(create, dependencies);
    }

    /**
     * Initializes a new {@link CreateTableUpdateTask} from specified arguments.
     *
     * @param create The create-table service
     * @param dependencies The dependencies to preceding update tasks or <code>null</code> for no dependencies
     */
    public CreateTableUpdateTask(CreateTableService create, String[] dependencies) {
        super();
        this.create = create;
        this.dependencies = null == dependencies ? new String[0] : dependencies;
    }

    @Override
    public TaskAttributes getAttributes() {
        // Creating Tables is blocking and schema level.
        return new Attributes(BLOCKING, SCHEMA);
    }

    @Override
    public String[] getDependencies() {
        return dependencies;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false); // BEGIN
            rollback = 1;

            create.perform(con);

            con.commit(); // COMMIT
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

}
