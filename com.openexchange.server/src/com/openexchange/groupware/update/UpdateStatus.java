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

import java.util.Date;

/**
 * {@link UpdateStatus}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface UpdateStatus {

    boolean blockingUpdatesRunning();

    boolean backgroundUpdatesRunning();

    boolean needsBlockingUpdates();

    boolean needsBackgroundUpdates();

    Date blockingUpdatesRunningSince();

    Date backgroundUpdatesRunningSince();

    /**
     * Gets a value indicating whether a specific update task has been executed successfully on the associated database schema or not.
     *
     * @param taskName The name of the update task to check
     * @return <code>true</code> if the update task was executed successfully, <code>false</code>, otherwise
     */
    boolean isExecutedSuccessfully(String taskName);

    /**
     * Checks if blocking updates are considered as timed-out.
     *
     * @return <code>true</code> if timed-out; otherwise <code>false</code>
     */
    boolean blockingUpdatesTimedOut();

    /**
     * Gets the identifier of the database pool associated with thus status' schema.
     *
     * @return The database pool identifier
     */
    int getPoolId();

    /**
     * Gets the name of the schema.
     *
     * @return The schema name
     */
    String getSchemaName();
}
