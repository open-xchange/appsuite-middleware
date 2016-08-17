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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;
import java.util.Map.Entry;

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
        final String lineSeparator = System.getProperty("line.separator");
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
