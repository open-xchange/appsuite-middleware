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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.startup.impl;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.java.Streams;
import com.openexchange.startup.CloseableControlService;


/**
 * {@link CloseableControl} - The singleton Closeable control.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CloseableControl implements CloseableControlService {

    private static final CloseableControlService INSTANCE = new CloseableControl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CloseableControlService getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    private final ConcurrentMap<Thread, Queue<Closeable>> closeables;

    /**
     * Initializes a new {@link CloseableControl}.
     */
    private CloseableControl() {
        super();
        closeables = new ConcurrentHashMap<Thread, Queue<Closeable>>(256, 0.9F, 1);
    }

    @Override
    public boolean addCloseable(Closeable closeable) {
        if (null == closeable) {
            return false;
        }

        Thread thread = Thread.currentThread();
        Queue<Closeable> queue = closeables.get(thread);
        if (null == queue) {
            Queue<Closeable> nq = new ConcurrentLinkedQueue<Closeable>();
            queue = closeables.putIfAbsent(thread, nq);
            if (null == queue) {
                queue = nq;
            }
        }
        return queue.offer(closeable);
    }

    @Override
    public boolean removeCloseable(Closeable closeable) {
        if (null == closeable) {
            return false;
        }

        Queue<Closeable> queue = closeables.get(Thread.currentThread());
        return null == queue ? false : queue.remove(closeable);
    }

    @Override
    public Collection<Closeable> getCurrentCloseables() {
        Queue<Closeable> queue = closeables.get(Thread.currentThread());
        return null == queue ? Collections.<Closeable> emptyList() : Collections.<Closeable> unmodifiableCollection(queue);
    }

    @Override
    public void closeAll() {
        Queue<Closeable> queue = closeables.remove(Thread.currentThread());
        if (null != queue) {
            for (Closeable closeable; (closeable = queue.poll()) != null;) {
                Streams.close(closeable);
            }
        }
    }

}
