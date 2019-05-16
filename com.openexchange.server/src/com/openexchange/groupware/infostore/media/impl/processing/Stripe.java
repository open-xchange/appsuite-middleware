/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
