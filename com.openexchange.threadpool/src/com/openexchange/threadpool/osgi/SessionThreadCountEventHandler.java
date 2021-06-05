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

package com.openexchange.threadpool.osgi;

import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.java.Strings;
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
        final String lineSeparator = Strings.getLineSeparator();
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
