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

package com.openexchange.sessionCount;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.osgi.util.tracker.ServiceTracker;
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
        final String lineSeparator = System.getProperty("line.separator");
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
