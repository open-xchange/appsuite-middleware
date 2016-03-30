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

package com.openexchange.http.requestwatcher.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.config.ConfigurationService;
import com.openexchange.http.requestwatcher.osgi.services.RequestRegistryEntry;
import com.openexchange.http.requestwatcher.osgi.services.RequestTrace;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.log.LogProperties;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link RequestWatcherServiceImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RequestWatcherServiceImpl implements RequestWatcherService {

    /** The logger. */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RequestWatcherServiceImpl.class);

    /** The request number */
    private static final AtomicLong NUMBER = new AtomicLong();

    // --------------------------------------------------------------------------------------------------------------------------

    /** Navigable set, entries ordered by age(youngest first), weakly consistent iterator */
    private final ConcurrentSkipListSet<RequestRegistryEntry> requestRegistry;

    /** The watcher task */
    private volatile ScheduledTimerTask requestWatcherTask;

    /**
     * Initializes a new {@link RequestWatcherServiceImpl}
     *
     * @param configService The configuration service used for initialization
     * @param timerService The timer service used for initialization
     */
    public RequestWatcherServiceImpl(ConfigurationService configService, TimerService timerService) {
        super();
        requestRegistry = new ConcurrentSkipListSet<RequestRegistryEntry>();
        // Get Configuration
        boolean isWatcherEnabled = configService.getBoolProperty("com.openexchange.requestwatcher.isEnabled", true);
        int watcherFrequency = configService.getIntProperty("com.openexchange.requestwatcher.frequency", 30000);
        int requestMaxAge = configService.getIntProperty("com.openexchange.requestwatcher.maxRequestAge", 60000);
        if (isWatcherEnabled) {
            // Create ScheduledTimerTask to watch requests
            ConcurrentSkipListSet<RequestRegistryEntry> requestRegistry = this.requestRegistry;
            Watcher task = new Watcher(requestRegistry, requestMaxAge);
            ScheduledTimerTask requestWatcherTask = timerService.scheduleAtFixedRate(task, requestMaxAge, watcherFrequency);
            this.requestWatcherTask = requestWatcherTask;
        }
    }

    @Override
    public RequestRegistryEntry registerRequest(HttpServletRequest request, HttpServletResponse response, Thread thread, Map<String, String> propertyMap) {
        RequestRegistryEntry registryEntry = new RequestRegistryEntry(NUMBER.incrementAndGet(), request, response, thread, propertyMap);
        requestRegistry.add(registryEntry);
        return registryEntry;
    }

    @Override
    public boolean unregisterRequest(RequestRegistryEntry registryEntry) {
        return requestRegistry.remove(registryEntry);
    }

    @Override
    public boolean stopWatching() {
        ScheduledTimerTask requestWatcherTask = this.requestWatcherTask;
        if (null != requestWatcherTask) {
            boolean canceled = requestWatcherTask.cancel();
            this.requestWatcherTask = null;
            return canceled;
        }
        return true;
    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private final static class Watcher implements Runnable {

        private final String lineSeparator;
        private final ConcurrentSkipListSet<RequestRegistryEntry> requestRegistry;
        private final int requestMaxAge;
        private final String propSessionId = LogProperties.Name.SESSION_SESSION_ID.getName();

        /**
         * Initializes a new {@link RunnableImplementation}.
         */
        Watcher(ConcurrentSkipListSet<RequestRegistryEntry> requestRegistry, int requestMaxAge) {
            super();
            this.lineSeparator = System.getProperty("line.separator");
            this.requestRegistry = requestRegistry;
            this.requestMaxAge = requestMaxAge;
        }

        /**
         * Start at the end of the navigable Set to get the oldest request first. Then proceed to the younger requests. Stop
         * processing at the first yet valid request.
         */
        @Override
        public void run() {
            try {
                boolean debugEnabled = LOG.isDebugEnabled();
                Iterator<RequestRegistryEntry> descendingEntryIterator = requestRegistry.descendingIterator();
                StringBuilder sb = new StringBuilder(256);
                boolean stillOldRequestsLeft = true;
                while (stillOldRequestsLeft && descendingEntryIterator.hasNext()) {
                    // Debug logging
                    if (debugEnabled) {
                        sb.setLength(0);
                        for (RequestRegistryEntry entry : requestRegistry) {
                            sb.append(lineSeparator).append("RegisteredThreads:").append(lineSeparator).append("    age: ").append(entry.getAge()).append(" ms").append(
                                ", thread: ").append(entry.getThreadInfo());
                        }
                        final String entries = sb.toString();
                        if (!entries.isEmpty()) {
                            LOG.debug(sb.toString());
                        }
                    }

                    // Check entry's age
                    RequestRegistryEntry entry = descendingEntryIterator.next();
                    if (entry.getAge() > requestMaxAge) {
                        sb.setLength(0);
                        boolean interrupted = handleEntry(entry, sb);
                        if (interrupted) {
                            requestRegistry.remove(entry);
                        }
                    } else {
                        stillOldRequestsLeft = false;
                    }
                }
            } catch (Exception e) {
                LOG.error("Request watcher run failed", e);
            }
        }

        private boolean handleEntry(RequestRegistryEntry entry, StringBuilder logBuilder) {
            // Get trace for associated thread's trace
            Throwable trace = new RequestTrace();
            boolean interrupt;
            {
                StackTraceElement[] stackTrace = entry.getStackTrace();
                interrupt = interrupt(stackTrace, entry);
                if (dontLog(stackTrace)) {
                    if (interrupt) {
                        entry.getThread().interrupt();
                        return true;
                    }
                    return false;
                }
                trace.setStackTrace(stackTrace);
            }
            logBuilder.append("Request with age ").append(entry.getAge()).append("ms exceeds max. age of ").append(requestMaxAge).append("ms.");

            // Append log properties from the ThreadLocal to logBuilder
            if (false == appendLogProperties(entry, logBuilder)) {
                // Turns out to be an invalid registry entry -- already interrupted at this point
                return true;
            }

            // Check if request's thread is supposed to be interrupted
            if (interrupt) {
                logBuilder.append(lineSeparator).append("Associated thread will be interrupted!");
                LOG.info(logBuilder.toString(), trace);
                entry.getThread().interrupt();
                return true;
            }

            // Non-interrupted entry
            LOG.info(logBuilder.toString(), trace);
            return false;
        }

        private boolean interrupt(StackTraceElement[] trace, RequestRegistryEntry entry) {
            StackTraceElement traceElement = trace[0];

            // Kept in socket read and exceeded doubled max. request age
            /*-
            if (traceElement.isNativeMethod() && "socketRead0".equals(traceElement.getMethodName()) && entry.getAge() > (requestMaxAge << 1)) {
                return true;
            }
            */

            // TODO: More interruptible traces?
            return false;
        }

        private boolean appendLogProperties(RequestRegistryEntry entry, StringBuilder logBuilder) {
            Map<String, String> propertyMap = entry.getPropertyMap();
            if (null != propertyMap) {
                // Sort the properties for readability
                Map<String, String> sorted = new TreeMap<String, String>();
                for (Entry<String, String> propertyEntry : propertyMap.entrySet()) {
                    String propertyName = propertyEntry.getKey();
                    String value = propertyEntry.getValue();
                    if (null != value) {
                        if (propSessionId.equals(propertyName) && !isValidSession(value)) {
                            // Non-existent or elapsed session
                            entry.getThread().interrupt();
                            requestRegistry.remove(entry);
                            return false;
                        }
                        sorted.put(propertyName, value);
                    }
                }
                logBuilder.append("Request's properties:").append(lineSeparator);

                // And add them to the logBuilder
                Iterator<Entry<String, String>> it = sorted.entrySet().iterator();
                if (it.hasNext()) {
                    String prefix = "  ";
                    Map.Entry<String, String> propertyEntry = it.next();
                    logBuilder.append(prefix).append(propertyEntry.getKey()).append('=').append(propertyEntry.getValue());
                    while (it.hasNext()) {
                        propertyEntry = it.next();
                        logBuilder.append(lineSeparator).append(prefix).append(propertyEntry.getKey()).append('=').append(propertyEntry.getValue());
                    }
                }
            }
            return true;
        }

        private boolean isValidSession(String sessionId) {
            SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
            return sessiondService instanceof SessiondServiceExtended ? ((SessiondServiceExtended) sessiondService).isActive(sessionId) : true;
        }

        private boolean dontLog(StackTraceElement[] trace) {
            for (StackTraceElement ste : trace) {
                String className = ste.getClassName();
                if (null != className) {
                    if (className.startsWith("org.apache.commons.fileupload.MultipartStream$ItemInputStream")) {
                        // A long-running file upload. Ignore
                        return true;
                    }
                }
            }
            return false;
        }
    }

}
