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

/**
 * {@link QueueType} - The queue type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum QueueType {

    /**
     * Synchronous queue type.
     */
    SYNCHRONOUS("synchronous", false, new IQueueProvider() {

        @Override
        public BlockingQueue<Runnable> newWorkQueue(final int fixedCapacity) {
            return QueueProvider.getInstance().newSynchronousQueue();
        }
    }),
    /**
     * Linked queue type.
     */
    LINKED("linked", true, new IQueueProvider() {

        @Override
        public BlockingQueue<Runnable> newWorkQueue(final int fixedCapacity) {
            return QueueProvider.getInstance().newLinkedQueue(fixedCapacity);
        }
    });

    private final String type;

    private final IQueueProvider queueProvider;

    private final boolean fixedSize;

    private QueueType(final String type, final boolean fixedSize, final IQueueProvider queueProvider) {
        this.fixedSize = fixedSize;
        this.type = type;
        this.queueProvider = queueProvider;
    }


    /**
     * Checks whether the queue type enforces the thread pool being at fixed-size.
     * <ul>
     * <li>A <b>synchronous</b> queue is appropriate for <code>core-size &lt; max-size</code></li>
     * <li>A <b>linked</b> queue is appropriate for <code>core-size = max-size</code></li>
     * </ul>
     *
     * @return <code>true</code> if the queue type enforces the thread pool being at fixed-size; otherwsie <code>false</code>
     */
    public boolean isFixedSize() {
        return fixedSize;
    }

    /**
     * Creates a new work queue of this type.
     *
     * @param fixedCapacity The fixed capacity
     * @return A new work queue of this type
     */
    public BlockingQueue<Runnable> newWorkQueue(final int fixedCapacity) {
        return queueProvider.newWorkQueue(fixedCapacity);
    }

    /**
     * Gets the queue type for given type string.
     *
     * @param type The type string
     * @return The queue type for given type string or <code>null</code>
     */
    public static QueueType getQueueType(final String type) {
        final QueueType[] queueTypes = QueueType.values();
        for (final QueueType queueType : queueTypes) {
            if (queueType.type.equalsIgnoreCase(type)) {
                return queueType;
            }
        }
        return null;
    }

    private static interface IQueueProvider {

        BlockingQueue<Runnable> newWorkQueue(int fixedCapacity);
    }

}
