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

package com.openexchange.admin.taskmanagement;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * {@link TaskManagerUncaughtExceptionhandler} - The uncaught exception handler for task manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.0
 */
final class TaskManagerUncaughtExceptionhandler implements UncaughtExceptionHandler {

    private static final TaskManagerUncaughtExceptionhandler INSTANCE = new TaskManagerUncaughtExceptionhandler();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    static TaskManagerUncaughtExceptionhandler getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link TaskManagerUncaughtExceptionhandler}.
     */
    private TaskManagerUncaughtExceptionhandler() {
        super();
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskManagerUncaughtExceptionhandler.class);
        LOG.error("Thread '{}' terminated abruptly with an uncaught RuntimeException or Error.", t.getName(), e);
    }

}
