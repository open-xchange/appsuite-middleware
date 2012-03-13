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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13.najp.threadpool;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link AJPv13Thread} - An AJP thread providing additional debug information about created/alive threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Thread extends Thread {

    private static final String DEFAULT_NAME = "AJPThread-";

    private static final int PREFIX_LENGTH = 5;

    private static final AtomicInteger CREATED_COUNTER = new AtomicInteger(1);

    private static final AtomicInteger ALIVE_COUNTER = new AtomicInteger();

    /**
     * Initializes a new {@link AJPv13Thread} with default name prefix <code>&quot;AJPThread-&quot;</code>.
     *
     * @param target The object whose <code>run</code> method is called.
     */
    public AJPv13Thread(final Runnable target) {
        this(target, DEFAULT_NAME);
    }

    /**
     * Initializes a new {@link AJPv13Thread}.
     *
     * @param target The object whose <code>run</code> method is called.
     * @param name The name prefix; e.g. <code>&quot;MyAJPThread-&quot;</code>
     */
    public AJPv13Thread(final Runnable target, final String name) {
        super(target, getThreadName(CREATED_COUNTER.getAndIncrement(), new StringBuilder(name.length() + PREFIX_LENGTH).append(name)));
    }

    @Override
    public void run() {
        try {
            ALIVE_COUNTER.incrementAndGet();
            super.run();
        } finally {
            ALIVE_COUNTER.decrementAndGet();
        }
    }

    /*-
     * ########################## STATIC METHODS ##########################
     */

    /**
     * Gets the number of created threads so far.
     *
     * @return The number of created threads
     */
    public static int getThreadsCreated() {
        return CREATED_COUNTER.get();
    }

    /**
     * Gets the current number of alive threads.
     *
     * @return The current number of alive threads.
     */
    public static int getThreadsAlive() {
        return ALIVE_COUNTER.get();
    }

    /**
     * Composes a thread name using specified string builder.
     *
     * @param threadNumber The thread number to prepend
     * @param sb The string builder to use
     * @return The composed thread name
     */
    private static String getThreadName(final int threadNumber, final StringBuilder sb) {
        for (int i = threadNumber; i < 10000; i *= 10) {
            sb.append('0');
        }
        return sb.append(threadNumber).toString();
    }
}
