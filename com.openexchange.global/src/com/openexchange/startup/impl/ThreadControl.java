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

package com.openexchange.startup.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.startup.ThreadControlService;


/**
 * {@link ThreadControl} - The singleton thread control.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ThreadControl implements ThreadControlService {

    private static final Object PRESENT = new Object();

    private static final ThreadControlService INSTANCE = new ThreadControl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ThreadControlService getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------------ //

    private final ConcurrentMap<Thread, Object> threads;

    /**
     * Initializes a new {@link ThreadControl}.
     */
    private ThreadControl() {
        super();
        threads = new ConcurrentHashMap<Thread, Object>(256, 0.9F, 1);
    }

    @Override
    public boolean addThread(Thread thread) {
        if (null == thread) {
            return false;
        }

        return null == threads.putIfAbsent(thread, PRESENT);
    }

    @Override
    public boolean removeThread(Thread thread) {
        if (null == thread) {
            return false;
        }

        return null != threads.remove(thread);
    }

    @Override
    public Collection<Thread> getCurrentThreads() {
        return Collections.unmodifiableCollection(threads.keySet());
    }

    @Override
    public void interruptAll() {
        for (Thread thread : threads.keySet()) {
            interruptSafe(thread);
        }
    }

    private void interruptSafe(Thread thread) {
        try {
            // Request interrupt
            thread.interrupt();
        } catch (Exception e) {
            // Ignore
        }
    }

}
