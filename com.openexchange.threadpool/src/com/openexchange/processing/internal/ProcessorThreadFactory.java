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

package com.openexchange.processing.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * {@link ProcessorThreadFactory} - The thread factory for a processor.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.1
 */
public final class ProcessorThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber;
    private final String namePrefix;
    private final boolean supportMDC;

    /**
     * Initializes a new {@link ProcessorThreadFactory}.
     *
     * @param name The name prefix; e.g. <code>"MyThread"</code>
     * @param supportMDC Whether to support MDC log properties; otherwise MDC will be cleared prior to each log output
     */
    public ProcessorThreadFactory(String name, boolean supportMDC) {
        super();
        this.supportMDC = supportMDC;
        threadNumber = new AtomicInteger();
        this.namePrefix = null == name ? "ProcessorThread-" : new StringBuilder(name).append('-').toString();
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = supportMDC ? new PseudoOXThread(r, getThreadName(getThreadNumber(), namePrefix)) : new Thread(r, getThreadName(getThreadNumber(), namePrefix));
        t.setUncaughtExceptionHandler(ProcessorUncaughtExceptionhandler.getInstance());
        return t;
    }

    private int getThreadNumber() {
        int number;
        do {
            number = threadNumber.incrementAndGet();
            if (number > 0) {
                return number;
            }
        } while (!threadNumber.compareAndSet(number, 0));
        return threadNumber.incrementAndGet();
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

}
