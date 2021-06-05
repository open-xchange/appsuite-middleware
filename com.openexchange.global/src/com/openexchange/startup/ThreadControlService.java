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

package com.openexchange.startup;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ThreadControlService} - The thread control to register interruptable threads on shut-down.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
@SingletonService
public interface ThreadControlService {

    /**
     * The dummy thread control that does nothing at all.
     */
    public static final ThreadControlService DUMMY_CONTROL = new ThreadControlService() {

        @Override
        public boolean removeThread(Thread thread) {
            return false;
        }

        @Override
        public void interruptAll() {
            // Nothing to do
        }

        @Override
        public Collection<Thread> getCurrentThreads() {
            return Collections.emptyList();
        }

        @Override
        public boolean addThread(Thread thread) {
            return false;
        }
    };

    // -------------------------------------------------------------------------------------------------------------------------------

    /**
     * Adds specified thread to this thread control
     *
     * @param thread The thread to add
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     */
    boolean addThread(Thread thread);

    /**
     * Removes specified thread from this thread control
     *
     * @param thread The thread to remove
     * @return <code>true</code> if successfully removed; otherwise <code>false</code> if no such thread is present in this thread control
     */
    boolean removeThread(Thread thread);

    /**
     * Gets the (unmodifiable) {@link Collection} view on currently managed threads.
     *
     * @return The currently managed threads
     */
    Collection<Thread> getCurrentThreads();

    /**
     * Interrupts all currently managed threads.
     */
    void interruptAll();

}
