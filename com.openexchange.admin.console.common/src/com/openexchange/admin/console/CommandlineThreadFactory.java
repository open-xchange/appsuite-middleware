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

package com.openexchange.admin.console;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link CommandlineThreadFactory} - A thread factory for command-line.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CommandlineThreadFactory implements java.util.concurrent.ThreadFactory {

    private final AtomicInteger threadNumber;

    /**
     * Initializes a new {@link CommandlineThreadFactory}.
     *
     * @param namePrefix The name prefix
     */
    public CommandlineThreadFactory() {
        super();
        threadNumber = new AtomicInteger();
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(r, getThreadName(threadNumber.incrementAndGet(), new StringBuilder("Commandline-Worker-")));
        t.setUncaughtExceptionHandler(new CommandlineUncaughtExceptionhandler());
        return t;
    }

    private static String getThreadName(final int threadNumber, final StringBuilder sb) {
        for (int i = threadNumber; i < 100; i *= 10) {
            sb.append('0');
        }
        return sb.append(threadNumber).toString();
    }

}
