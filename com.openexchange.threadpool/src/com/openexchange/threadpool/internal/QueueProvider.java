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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.threadpool.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * {@link QueueProvider} - Provider for appropriate queue instance dependent on JRE version.
 * <p>
 * Java6 synchronous queue implementation is up to 3 times faster than Java5 one.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QueueProvider {

    private static QueueProvider INSTANCE = new QueueProvider();

    /**
     * Gets the {@link QueueProvider} instance.
     *
     * @return The {@link QueueProvider} instance
     */
    public static QueueProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a newly created synchronous queue.
     *
     * @param <V> The queue's type
     * @return A newly created synchronous queue
     */
    public <V> BlockingQueue<V> newSynchronousQueue() {
        return new SynchronousQueue<V>();
    }

    /**
     * Gets a newly created synchronous queue.
     *
     * @param <V> The queue's type
     * @param clazz The queue's type class
     * @return A newly created synchronous queue
     */
    public <V extends Object> BlockingQueue<V> newSynchronousQueue(final Class<? extends V> clazz) {
        return new SynchronousQueue<V>();
    }

    /**
     * Gets a newly created linked queue.
     *
     * @param <V> The queue's type
     * @param fixedCapacity The fixed capacity
     * @return A newly created linked queue
     */
    public final <V> BlockingQueue<V> newLinkedQueue(final int fixedCapacity) {
        return fixedCapacity > 0 ? new LinkedBlockingQueue<V>(fixedCapacity) : new LinkedBlockingQueue<V>();
    }

    /**
     * Gets a newly created linked queue.
     *
     * @param <V> The queue's type
     * @param clazz The queue's type class
     * @param fixedCapacity The fixed capacity
     * @return A newly created linked queue
     */
    public final <V extends Object> BlockingQueue<V> newLinkedQueue(final Class<? extends V> clazz, final int fixedCapacity) {
        return fixedCapacity > 0 ? new LinkedBlockingQueue<V>(fixedCapacity) : new LinkedBlockingQueue<V>();
    }
}
