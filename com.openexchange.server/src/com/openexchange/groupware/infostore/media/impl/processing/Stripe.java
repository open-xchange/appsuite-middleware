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

package com.openexchange.groupware.infostore.media.impl.processing;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.session.UserAndContext;

/**
 * {@link Stripe} - A stripe queues tasks associated with the same stripe key.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Stripe {

    /** The special poison stripe to halt execution */
    public static final Stripe POISON = new Stripe();

    private final Map<String, ExtractionTask> tasks;
    private final UserAndContext stripeKey;

    /**
     * Initializes a new {@link Stripe}.
     */
    private Stripe() {
        super();
        tasks = null;
        stripeKey = null;
    }

    /**
     * Initializes a new {@link Stripe}.
     *
     * @param task The initial extraction task
     * @param stripeKey The key associated with this stripe
     */
    public Stripe(ExtractionTask task, UserAndContext stripeKey) {
        super();
        this.stripeKey = stripeKey;
        Map<String, ExtractionTask> tasks = new LinkedHashMap<String, ExtractionTask>();
        tasks.put(task.getKey(), task);
        this.tasks = tasks;
    }

    /**
     * Gets the stripe key
     *
     * @return The key
     */
    public UserAndContext getStripeKey() {
        return stripeKey;
    }

    /**
     * Gets the number of tasks currently held by this stripe.
     *
     * @return The number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Checks if this stripe contains no tasks.
     *
     * @return <tt>true</tt> if this stripe contains no tasks; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Retrieves and removes the next available task from this stripe.
     *
     * @return The next task or <code>null</code>
     */
    public ExtractionTask poll() {
        Iterator<ExtractionTask> iterator = tasks.values().iterator();
        if (false == iterator.hasNext()) {
            return null;
        }

        ExtractionTask nextTask = iterator.next();
        iterator.remove();
        return nextTask;
    }

    /**
     * Adds given task to this stripe.
     * <p>
     * If there is already a task with the same key, the previous one is replaced.
     *
     * @param task The task to add
     */
    public void add(ExtractionTask task) {
        if (null == task) {
            return;
        }

        ExtractionTask previous = tasks.put(task.getKey(), task);
        if (null != previous) {
            previous.interrupt();
        }
    }

    /**
     * Removes the task associated with given key from this stripe.
     *
     * @param taskKey The task key
     * @return The removed task or <code>null</code> if no such tasks is present or it has already been processed
     */
    public ExtractionTask remove(String taskKey) {
        if (null == taskKey) {
            return null;
        }

        ExtractionTask removed = tasks.remove(taskKey);
        if (null != removed) {
            removed.interrupt();
        }
        return removed;
    }

}
