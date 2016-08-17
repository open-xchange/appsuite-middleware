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

package com.openexchange.java;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link NonBlockingCallableExecutor} - According to pattern introduced in <a
 * href="http://mailinator.blogspot.de/2007/05/readerwriter-in-java-in-nonblocking.html"
 * >http://mailinator.blogspot.de/2007/05/readerwriter-in-java-in-nonblocking.html</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NonBlockingCallableExecutor<V> {

    private final AtomicInteger writeCounter;
    private final Lock lock;

    /**
     * Initializes a new {@link NonBlockingCallableExecutor}.
     */
    public NonBlockingCallableExecutor() {
        super();
        writeCounter = new AtomicInteger();
        lock = new ReentrantLock();
    }

    /**
     * Concurrent/non-exclusive computation of given task.
     *
     * @param task The task
     * @return The concurrently computed result
     * @throws Exception If unable to compute a result
     */
    public V readCall(Callable<V> task) throws Exception {
        int save = 0;
        V value = null;

        do {
            while (((save = writeCounter.get()) & 1) > 0) {
                ;
            }
            value = task.call();
        } while (save != writeCounter.get());

        return value;
    }

    /**
     * Exclusive computation of given task.
     *
     * @param task The task
     * @return The exclusively computed result.
     * @throws Exception If unable to compute a result
     */
    public V writeCall(Callable<V> task) throws Exception {
        V value;

        lock.lock();
        try {
            writeCounter.incrementAndGet();
            value = task.call();
        } finally {
            writeCounter.incrementAndGet();
            lock.unlock();
        }

        return value;
    }

}
