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

package com.openexchange.http.grizzly.util;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.startup.ThreadControlService;


/**
 * {@link ThreadControlReference} - The USM-JSON thread control.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ThreadControlReference {

    private static final AtomicReference<ThreadControlService> THREAD_CONTOL_REF = new AtomicReference<ThreadControlService>(null);

    /**
     * Sets the current thread control.
     *
     * @param service The service reference to set or <code>null</code>
     */
    public static void setThreadControlService(ThreadControlService service) {
        THREAD_CONTOL_REF.set(service);
    }

    /**
     * Gets the current thread control.
     *
     * @return The service reference or <code>null</code>
     */
    public static ThreadControlService getThreadControlService() {
        return THREAD_CONTOL_REF.get();
    }

}
