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

package com.openexchange.threadpool.internal;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.java.Strings;

/**
 * {@link CustomUncaughtExceptionhandler} - A custom {@link UncaughtExceptionHandler}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class CustomUncaughtExceptionhandler implements UncaughtExceptionHandler {

    private static final CustomUncaughtExceptionhandler INSTANCE = new CustomUncaughtExceptionhandler();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CustomUncaughtExceptionhandler getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link CustomUncaughtExceptionhandler}.
     */
    private CustomUncaughtExceptionhandler() {
        super();
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomUncaughtExceptionhandler.class);
        LOG.error("Thread terminated with exception: {}", t.getName(), e);
        /*
         * Gather thread information
         */
        final Map<Thread, StackTraceElement[]> stackMap = Thread.getAllStackTraces();
        final StringBuilder sb = new StringBuilder(256);
        final String lineSeparator = Strings.getLineSeparator();
        for (final Entry<Thread, StackTraceElement[]> threadEntry : stackMap.entrySet()) {
            Thread thread = threadEntry.getKey();
            sb.append(thread.getName()).append(" ID:").append(thread.getId());
            sb.append(" State:").append(thread.getState()).append(" Prio:").append(thread.getPriority());
            sb.append(lineSeparator);
            appendStackTrace(threadEntry.getValue(), sb, lineSeparator);
            sb.append(lineSeparator);
        }
        LOG.error(sb.toString());
    }

    private static void appendStackTrace(final StackTraceElement[] trace, final StringBuilder sb, final String lineSeparator) {
        if (null == trace) {
            sb.append("<missing stack trace>\n");
            return;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

}
