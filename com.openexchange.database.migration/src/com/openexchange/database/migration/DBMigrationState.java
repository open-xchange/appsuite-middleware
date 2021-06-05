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

package com.openexchange.database.migration;

import java.util.concurrent.ExecutionException;

/**
 * {@link DBMigrationState}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface DBMigrationState {

    /**
     * Returns whether this migration was already executed.
     *
     * @return <code>true</code> if the task is done.
     */
    boolean isDone();

    /**
     * Awaits the completion of this migration. If the current thread is interrupted while its blocked, {@link InterruptedException} is
     * thrown.
     *
     * @throws ExecutionException If an error occurred during the migrations execution. The original exception can be accessed via
     *             {@link ExecutionException#getCause()}. The according exception has already been logged, you don't need to do that again.
     * @throws InterruptedException If the current thread is interrupted while waiting for completion.
     */
    void awaitCompletion() throws ExecutionException, InterruptedException;

    /**
     * Gets the underlying database migration.
     *
     * @return The database migration
     */
    DBMigration getMigration();

}
