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

package com.openexchange.pop3.storage.mailaccount;

/**
 * {@link SessionCacheProperties} - Provides constants for session-backed caches.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionCacheProperties {

    /**
     * Initializes a new {@link SessionCacheProperties}.
     */
    private SessionCacheProperties() {
        super();
    }

    /**
     * The time to delay first execution of a scheduled task.
     */
    public static final int SCHEDULED_TASK_INITIAL_DELAY = 1000;

    /**
     * The delay between the termination of one execution and the commencement of the next scheduled task.
     */
    public static final int SCHEDULED_TASK_DELAY = 300000;

    /**
     * The number of empty runs a scheduled task may perform before being canceled and purged from timer queue.
     */
    public static final int SCHEDULED_TASK_ALLOWED_EMPTY_RUNS = 1;

}
