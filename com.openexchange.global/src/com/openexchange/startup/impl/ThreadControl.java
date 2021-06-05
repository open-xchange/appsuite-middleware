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

package com.openexchange.startup.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.startup.ThreadControlService;


/**
 * {@link ThreadControl} - The singleton thread control.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ThreadControl implements ThreadControlService {

    private static final Object PRESENT = new Object();

    private static final ThreadControlService INSTANCE = new ThreadControl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ThreadControlService getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    private final ConcurrentMap<Thread, Object> threads;

    /**
     * Initializes a new {@link ThreadControl}.
     */
    private ThreadControl() {
        super();
        threads = new ConcurrentHashMap<Thread, Object>(256, 0.9F, 1);
    }

    @Override
    public boolean addThread(Thread thread) {
        if (null == thread) {
            return false;
        }

        return null == threads.putIfAbsent(thread, PRESENT);
    }

    @Override
    public boolean removeThread(Thread thread) {
        if (null == thread) {
            return false;
        }

        return null != threads.remove(thread);
    }

    @Override
    public Collection<Thread> getCurrentThreads() {
        return Collections.unmodifiableCollection(threads.keySet());
    }

    @Override
    public void interruptAll() {
        for (Thread thread : threads.keySet()) {
            interruptSafe(thread);
        }
    }

    private void interruptSafe(Thread thread) {
        try {
            // Request interrupt
            thread.interrupt();
        } catch (Exception e) {
            // Ignore
        }
    }

}
