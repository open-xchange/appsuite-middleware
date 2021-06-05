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

package com.openexchange.html.internal.html2text.control;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * {@link Html2TextControl} - A registry for threads currently performing a HTML-to-text conversion.
 * <p>
 * This registry manages a {@link DelayQueue} to track expired tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class Html2TextControl {

    private static final Html2TextControl INSTANCE = new Html2TextControl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static Html2TextControl getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final DelayQueue<Html2TextTask> queue;

    /**
     * Initializes a new {@link Html2TextControl}.
     */
    private Html2TextControl() {
        super();
        queue = new DelayQueue<Html2TextTask>();
    }

    /**
     * Adds the specified task.
     *
     * @param task The task
     * @return <tt>true</tt>
     */
    public boolean add(Html2TextTask task) {
        return queue.offer(task);
    }

    /**
     * Removes the specified task.
     *
     * @param task The task to remove
     * @return <code>true</code> if such a task was removed; otherwise <code>false</code>
     */
    public boolean remove(Html2TextTask task) {
        return queue.remove(task);
    }

    /**
     * Await expired push listeners from this control.
     *
     * @return The expired push listeners
     * @throws InterruptedException If interrupted while waiting
     */
    List<Html2TextTask> awaitExpired() throws InterruptedException {
        Html2TextTask expired = queue.take();
        List<Html2TextTask> expirees = new LinkedList<Html2TextTask>();
        expirees.add(expired);
        queue.drainTo(expirees);
        return expirees;
    }

    /**
     * Removes expired push listeners from this control.
     *
     * @return The expired push listeners
     */
    List<Html2TextTask> removeExpired() {
        List<Html2TextTask> expirees = new LinkedList<Html2TextTask>();
        queue.drainTo(expirees);
        return expirees;
    }

}
