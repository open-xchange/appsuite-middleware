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

package com.openexchange.threadpool.internal;

import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.log.LogProperties;

/**
 * {@link CustomThreadFactory} - A thread factory taking a custom name prefix for created threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomThreadFactory implements java.util.concurrent.ThreadFactory {

    // private final ThreadGroup group;

    private final AtomicInteger threadNumber;

    private final String namePrefix;

    private final int len;

    /**
     * Initializes a new {@link CustomThreadFactory}.
     *
     * @param namePrefix The name prefix
     */
    public CustomThreadFactory(final String namePrefix) {
        super();
        threadNumber = new AtomicInteger();
        this.namePrefix = namePrefix;
        len = namePrefix.length() + 7;
    }

    @Override
    public Thread newThread(final Runnable r) {
        /*
         * Ensure a positive thread number
         */
        int threadNum = threadNumber.incrementAndGet();
        if (threadNum <= 0) {
            boolean check = false;
            do {
                if (threadNumber.compareAndSet(threadNum, 1)) {
                    threadNum = 1;
                } else {
                    threadNum = threadNumber.get();
                    check = true;
                }
            } while (threadNum <= 0);
            if (check && 1 == threadNum) {
                threadNum = threadNumber.incrementAndGet();
            }
        }
        /*
         * Create thread
         */
        final CustomThread t = new CustomThread(r, getThreadName(threadNum, new com.openexchange.java.StringAllocator(len).append(namePrefix)));
        t.setUncaughtExceptionHandler(new CustomUncaughtExceptionhandler());
        LogProperties.cloneLogProperties(t);
        return t;
    }

    private static String getThreadName(final int threadNumber, final com.openexchange.java.StringAllocator sb) {
        for (int i = threadNumber; i < 1000000; i *= 10) {
            sb.append('0');
        }
        return sb.append(threadNumber).toString();
    }

}
