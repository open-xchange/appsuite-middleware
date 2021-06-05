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

package com.openexchange.sessionCount;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SessionThreadCountMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionThreadCountMBeanImpl extends StandardMBean implements SessionThreadCountMBean {

    private final SessionThreadCounter counter;
    private final ServiceTracker<SessiondService, SessiondService> sessiondServiceTracker;

    /**
     * Initializes a new {@link SessionThreadCountMBeanImpl}.
     *
     * @throws NotCompliantMBeanException If the MBean interface does not follow JMX design patterns for Management Interfaces, or if this
     *             does not implement the specified interface.
     */
    public SessionThreadCountMBeanImpl(final SessionThreadCounter counter, final ServiceTracker<SessiondService, SessiondService> sessiondServiceTracker) throws NotCompliantMBeanException {
        super(SessionThreadCountMBean.class);
        this.counter = counter;
        this.sessiondServiceTracker = sessiondServiceTracker;
    }

    @Override
    public String getThreads(final int threshold) {
        final StringBuilder info = new StringBuilder(8192);
        final SessiondService service = sessiondServiceTracker.getService();
        final Map<String, Set<Thread>> threads = counter.getThreads(threshold);
        final String lineSeparator = Strings.getLineSeparator();
        for (final Entry<String, Set<Thread>> entry : threads.entrySet()) {
            final Set<Thread> set = entry.getValue();
            info.append(lineSeparator).append(lineSeparator).append(set.size()).append(" threads belonging to session \"").append(entry.getKey());
            if (null == service) {
                info.append("\":").append(lineSeparator);
            } else {
                final Session session = service.getSession(entry.getKey());
                if (null == session) {
                    info.append("\":").append(lineSeparator);
                } else {
                    info.append("\" (user=").append(session.getUserId()).append(", context=").append(session.getContextId()).append("):").append(lineSeparator);
                }
            }
            for (final Thread thread : set) {
                info.append(lineSeparator).append("--------------------------------------------------------------------------").append(lineSeparator);
                appendStackTrace(thread.getStackTrace(), info, lineSeparator);
            }
        }
        return info.toString();
    }

    private static void appendStackTrace(final StackTraceElement[] trace, final StringBuilder sb, final String lineSeparator) {
        if (null == trace) {
            sb.append("<missing stack trace>").append(lineSeparator);
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
