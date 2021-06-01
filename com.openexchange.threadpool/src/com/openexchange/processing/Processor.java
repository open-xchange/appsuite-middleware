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

package com.openexchange.processing;

import com.openexchange.exception.OXException;

/**
 * {@link Processor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface Processor {

    /**
     * Gets this processor's name.
     *
     * @return The name
     */
    String getName();

    /**
     * Schedules the specified task for being executed associated with given key (if any).
     *
     * @param optKey The optional key; if <code>null</code> calling {@link Thread} instance is referenced as key
     * @param task The task to execute
     * @return <code>true</code> if successfully scheduled for execution; otherwise <code>false</code> to signal that task cannot be accepted
     */
    boolean execute(Object optKey, Runnable task);

    /**
     * Gets the number of buffered tasks awaiting being executed.
     *
     * @return The number of buffered tasks
     * @throws OXException If number of buffered tasks cannot be returned
     */
    long getNumberOfBufferedTasks() throws OXException;

    /**
     * Gets the number of tasks that are currently executed.
     *
     * @return The number of executing tasks
     * @throws OXException If number of executing tasks cannot be returned
     */
    long getNumberOfExecutingTasks() throws OXException;

    /**
     * Stops this processor waiting until empty.
     *
     * @throws InterruptedException If interrupted while waiting
     */
    void stopWhenEmpty() throws InterruptedException;

    /**
     * Shuts-down this processor.
     */
    void stop();

}
