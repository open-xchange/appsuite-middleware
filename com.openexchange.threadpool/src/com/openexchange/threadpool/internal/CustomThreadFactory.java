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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link CustomThreadFactory} - A thread factory taking a custom name prefix for created threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomThreadFactory implements java.util.concurrent.ThreadFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomThreadFactory.class);

    private final AtomicInteger threadNumber;
    private final String namePrefix;
    private final ExecutorService threadCreatorService;

    /**
     * Initializes a new {@link CustomThreadFactory}.
     *
     * @param namePrefix The name prefix
     */
    public CustomThreadFactory(final String namePrefix) {
        super();
        threadNumber = new AtomicInteger();
        this.namePrefix = namePrefix;
        threadCreatorService = Executors.newSingleThreadExecutor(new MasterThreadFactory(namePrefix));
    }

    @Override
    public CustomThread newThread(Runnable r) {
        // Ensure a positive thread number
        int threadNum;
        while ((threadNum = threadNumber.incrementAndGet()) <= 0) {
            if (threadNumber.compareAndSet(threadNum, 1)) {
                threadNum = 1;
            } else {
                threadNum = threadNumber.incrementAndGet();
            }
        }
        try {
            return createThreadWithMaster(r, threadNum);
        } catch (InterruptedException e) {
            LOG.error("Single thread pool for creating threads was interrupted.", e);
            return null;
        } catch (ExecutionException e) {
            LOG.error("Single thread pool for creating threads catched an exception while creating one.", e);
            return null;
        }
    }

    private static String getThreadName(int threadNumber, String namePrefix) {
        StringBuilder retval = new StringBuilder(namePrefix.length() + 7);
        retval.append(namePrefix);
        for (int i = threadNumber; i < 1000000; i *= 10) {
            retval.append('0');
        }
        retval.append(threadNumber);
        return retval.toString();
    }

    private CustomThread createThreadWithMaster(Runnable r, int threadNum) throws InterruptedException, ExecutionException {
        ThreadCreateCallable callable = new ThreadCreateCallable(r, getThreadName(threadNum, namePrefix));
        Future<CustomThread> future = threadCreatorService.submit(callable);
        return future.get();
    }
}
