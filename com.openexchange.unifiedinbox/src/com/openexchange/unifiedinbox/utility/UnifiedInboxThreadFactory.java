/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.unifiedinbox.utility;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link UnifiedInboxThreadFactory} - A thread factory for Unified Mail threads taking a custom name prefix for created threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxThreadFactory implements java.util.concurrent.ThreadFactory {

    // private final ThreadGroup group;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;

    private final int len;

    /**
     * Initializes a new {@link UnifiedInboxThreadFactory} with default prefix <code>"UnifiedINBOX-"</code> applied to each created thread.
     */
    public UnifiedInboxThreadFactory() {
        this("UnifiedINBOX-");
    }

    /**
     * Initializes a new {@link UnifiedInboxThreadFactory} with specified prefix applied to each created thread.
     *
     * @param namePrefix The name prefix
     */
    public UnifiedInboxThreadFactory(final String namePrefix) {
        super();
        // final java.lang.SecurityManager s = System.getSecurityManager();
        // group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
        this.namePrefix = namePrefix;
        len = namePrefix.length() + 4;
    }

    @Override
    public Thread newThread(final Runnable r) {
        // final Thread t = new Thread(group, r, getThreadName(
        // threadNumber.getAndIncrement(),
        // new StringBuilder(NAME_LENGTH).append(namePrefix)), 0);
        // if (t.isDaemon()) {
        // t.setDaemon(false);
        // }
        // if (t.getPriority() != Thread.NORM_PRIORITY) {
        // t.setPriority(Thread.NORM_PRIORITY);
        // }

        return new Thread(r, getThreadName(threadNumber.getAndIncrement(), new StringBuilder(len).append(namePrefix)));
    }

    private static String getThreadName(final int threadNumber, final StringBuilder sb) {
        for (int i = threadNumber; i < 1000; i *= 10) {
            sb.append('0');
        }
        return sb.append(threadNumber).toString();
    }

}
