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

package com.openexchange.threadpool.osgi;

import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.session.Session;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.session.ThreadCountEntry;
import com.openexchange.sessionCount.SessionThreadCounterImpl;
import com.openexchange.sessiond.SessiondService;


/**
 * {@link SessionThreadCountEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionThreadCountEventHandler extends ServiceTracker<SessiondService, SessiondService> implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionThreadCountEventHandler.class);

    private final SessionThreadCounterImpl counterImpl;

    private final int threshold;

    /**
     * Initializes a new {@link SessionThreadCountEventHandler}.
     */
    public SessionThreadCountEventHandler(final BundleContext context, final int threshold, final SessionThreadCounterImpl counterImpl) {
        super(context, SessiondService.class, null);
        this.counterImpl = counterImpl;
        this.threshold = threshold;
    }

    @Override
    public void handleEvent(final Event event) {
        final Set<Thread> threads = ((ThreadCountEntry) event.getProperty(SessionThreadCounter.EVENT_PROP_ENTRY)).getThreads();
        final int num = threads.size();
        if (num < threshold) {
            return;
        }
        final SessiondService sessiondService = getService();
        if (null == sessiondService) {
            return;
        }
        final String sessionId = (String) event.getProperty(SessionThreadCounter.EVENT_PROP_SESSION_ID);
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            counterImpl.remove(sessionId);
            return;
        }
        final StringBuilder info = new StringBuilder(1024);
        final String lineSeparator = System.getProperty("line.separator");
        info.append("Detected ").append(num).append(" threads belonging to session \"").append(sessionId);
        info.append("\" (user=").append(session.getUserId()).append(", context=").append(session.getContextId()).append("):").append(lineSeparator);

        for (final Thread thread : threads) {
            info.append(lineSeparator).append("--------------------------------------------------------------------------").append(lineSeparator);
            appendStackTrace(thread.getStackTrace(), info, lineSeparator);
        }
        LOG.warn(info.toString());
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
