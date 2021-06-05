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

package com.openexchange.html.internal.jsoup.control;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * {@link JsoupParseControl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class JsoupParseControl {

    private static final JsoupParseControl INSTANCE = new JsoupParseControl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static JsoupParseControl getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final DelayQueue<JsoupParseTask> queue;

    /**
     * Initializes a new {@link JsoupParseControl}.
     */
    private JsoupParseControl() {
        super();
        queue = new DelayQueue<JsoupParseTask>();
    }

    /**
     * Adds the specified task.
     *
     * @param task The task
     * @return <tt>true</tt>
     */
    public boolean add(JsoupParseTask task) {
        return queue.offer(task);
    }

    /**
     * Removes the specified task.
     *
     * @param task The task to remove
     * @return <code>true</code> if such a task was removed; otherwise <code>false</code>
     */
    public boolean remove(JsoupParseTask task) {
        return queue.remove(task);
    }

    /**
     * Await expired parse tasks from this control.
     *
     * @return The expired parse tasks
     * @throws InterruptedException If interrupted while waiting
     */
    List<JsoupParseTask> awaitExpired() throws InterruptedException {
        JsoupParseTask expired = queue.take();
        List<JsoupParseTask> expirees = new LinkedList<JsoupParseTask>();
        expirees.add(expired);
        queue.drainTo(expirees);
        return expirees;
    }

    /**
     * Removes expired parse tasks from this control.
     *
     * @return The expired parse tasks
     */
    List<JsoupParseTask> removeExpired() {
        List<JsoupParseTask> expirees = new LinkedList<JsoupParseTask>();
        queue.drainTo(expirees);
        return expirees;
    }

}
