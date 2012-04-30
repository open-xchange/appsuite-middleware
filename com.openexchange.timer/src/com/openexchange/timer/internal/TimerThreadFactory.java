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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.timer.internal;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicInteger;

final class TimerThreadFactory implements java.util.concurrent.ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;

    private final int len;

    /**
     * Initializes a new {@link TimerThreadFactory}.
     *
     * @param namePrefix The name prefix
     */
    public TimerThreadFactory(final String namePrefix) {
        super();
        this.namePrefix = namePrefix;
        len = namePrefix.length() + 5;
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(r, getThreadName(threadNumber.getAndIncrement(), new StringBuilder(len).append(namePrefix)));
        t.setUncaughtExceptionHandler(new TimerUncaughtExceptionhandler());
        return t;
    }

    private static String getThreadName(final int threadNumber, final StringBuilder sb) {
        for (int i = threadNumber; i < 10000; i *= 10) {
            sb.append('0');
        }
        return sb.append(threadNumber).toString();
    }

    /*-
     * #######################################################################################################
     */

    private static final class TimerUncaughtExceptionhandler implements UncaughtExceptionHandler {

        private static final org.apache.commons.logging.Log LOG1 = com.openexchange.log.LogFactory.getLog(TimerUncaughtExceptionhandler.class);

        /**
         * Initializes a new {@link TimerUncaughtExceptionhandler}.
         */
        public TimerUncaughtExceptionhandler() {
            super();
        }

        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            LOG1.fatal("Thread terminated with exception: " + t.getName(), e);
        }

    }
}
