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

package com.openexchange.contactcollector.internal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.mail.internet.InternetAddress;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.session.Session;

/**
 * {@link ContactCollectorServiceImpl}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ContactCollectorServiceImpl implements ContactCollectorService {

    /**
     * The contact collector's executor.<br>
     * TODO: Replace with global thread pool if supported later on.
     */
    private ExecutorService executor;

    /**
     * Initializes a new {@link ContactCollectorServiceImpl}.
     */
    public ContactCollectorServiceImpl() {
        super();
    }

    public void memorizeAddresses(final List<InternetAddress> addresses, final Session session) {
        /*
         * Enqueue in executor
         */
        executor.execute(new Memorizer(addresses, session));
    }

    /**
     * Starts this contact collector.
     */
    public void start() {
        executor = Executors.newSingleThreadExecutor(new CollectorThreadFactory("Collector-"));
    }

    /**
     * Stops this contact collector.
     * 
     * @throws InterruptedException If shut-down is interrupted
     */
    public void stop() throws InterruptedException {
        // Maybe an abrupt shut-down through executor.shutdownNow();?
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    /*-
     * #####################################################################
     */

    private static final class CollectorThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber;

        private final String namePrefix;

        public CollectorThreadFactory(final String namePrefix) {
            super();
            threadNumber = new AtomicInteger(1);
            this.namePrefix = namePrefix;
        }

        public Thread newThread(final Runnable r) {
            return new Thread(r, getThreadName(
                threadNumber.getAndIncrement(),
                new StringBuilder(namePrefix.length() + 5).append(namePrefix)));
        }

        private static String getThreadName(final int threadNumber, final StringBuilder sb) {
            for (int i = threadNumber; i < 10000; i *= 10) {
                sb.append('0');
            }
            return sb.append(threadNumber).toString();
        }
    }
}
