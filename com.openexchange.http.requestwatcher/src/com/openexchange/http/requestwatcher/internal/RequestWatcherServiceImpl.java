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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.http.requestwatcher.RequestWatcherExceptionMessage;
import com.openexchange.http.requestwatcher.osgi.RequestWatcherServiceRegistry;
import com.openexchange.http.requestwatcher.osgi.services.RequestRegistryEntry;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link RequestWatcherServiceImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RequestWatcherServiceImpl implements RequestWatcherService {

    /**
     * The logger.
     */
    protected static final Log LOG = com.openexchange.log.Log.loggerFor(RequestWatcherServiceImpl.class);

    // Navigable set, entries ordered by age(youngest first), weakly consistent iterator
    private final ConcurrentSkipListSet<RequestRegistryEntry> requestRegistry;

    private final RequestWatcherServiceRegistry serviceRegistry;

    private volatile ScheduledTimerTask requestWatcherTask;

    /**
     * Initializes a new {@link RequestWatcherServiceImpl}.
     */
    public RequestWatcherServiceImpl() {
        super();
        requestRegistry = new ConcurrentSkipListSet<RequestRegistryEntry>();
        serviceRegistry = RequestWatcherServiceRegistry.getInstance();
        // Get Configuration
        final ConfigurationService configService = serviceRegistry.getService(ConfigurationService.class);
        final boolean isWatcherEnabled = configService.getBoolProperty("com.openexchange.requestwatcher.isEnabled", true);
        final int watcherFrequency = configService.getIntProperty("com.openexchange.requestwatcher.frequency", 30000);
        final int requestMaxAge = configService.getIntProperty("com.openexchange.requestwatcher.maxRequestAge", 60000);
        if (isWatcherEnabled) {
            // Create ScheduledTimerTask to watch requests
            final TimerService timerService = serviceRegistry.getService(TimerService.class);
            final ConcurrentSkipListSet<RequestRegistryEntry> requestRegistry = this.requestRegistry;
            final ScheduledTimerTask requestWatcherTask = timerService.scheduleAtFixedRate(new Runnable() {

                /*
                 * Start at the end of the navigable Set to get the oldest request first. Then proceed to the younger requests. Stop
                 * processing at the first still valid request.
                 */
                @Override
                public void run() {
                    final boolean debugEnabled = LOG.isDebugEnabled();
                    final Iterator<RequestRegistryEntry> descendingEntryIterator = requestRegistry.descendingIterator();
                    final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(256);
                    boolean stillOldRequestsLeft = true;
                    while (stillOldRequestsLeft && descendingEntryIterator.hasNext()) {
                        sb.reinitTo(0);
                        if (debugEnabled) {
                            for (final RequestRegistryEntry entry : requestRegistry) {
                                sb.append("RegisteredThreads:\n\tage: ").append(entry.getAge()).append(" ms").append(", thread: ").append(
                                    entry.getThreadInfo());
                            }
                            final String entries = sb.toString();
                            if (!entries.isEmpty()) {
                                LOG.debug(sb);
                            }
                        }
                        final RequestRegistryEntry requestRegistryEntry = descendingEntryIterator.next();
                        if (requestRegistryEntry.getAge() > requestMaxAge) {
                            sb.reinitTo(0);
                            logRequestRegistryEntry(requestRegistryEntry, sb);
                            requestRegistry.remove(requestRegistryEntry);
                            
                        } else {
                            stillOldRequestsLeft = false;
                        }
                    }
                }

                private void logRequestRegistryEntry(final RequestRegistryEntry entry, final com.openexchange.java.StringAllocator logBuilder) {
                    final Throwable trace = new Throwable();
                    trace.setStackTrace(entry.getStackTrace());
                    Props logProperties = LogProperties.optLogProperties(entry.getThread());
                    logBuilder.append("Request\n");

                    // If we have additional log properties from the ThreadLocal add it to the logBuilder
                    if (logProperties != null) {
                        Map<String, Object> propertyMap = logProperties.asMap();
                        // Sort the properties for readability
                        Map<String, String> sorted = new TreeMap<String, String>();
                        for (Entry<String, Object> propertyEntry : propertyMap.entrySet()) {
                            String propertyName = propertyEntry.getKey();
                            Object value = propertyEntry.getValue();
                            if (null != value) {
                                sorted.put(propertyName, value.toString());
                            }
                        }
                        logBuilder.append("with properties:\n");
                        // And add them to the logBuilder
                        for (Map.Entry<String, String> propertyEntry : sorted.entrySet()) {
                            logBuilder.append(propertyEntry.getKey()).append('=').append(propertyEntry.getValue()).append('\n');
                        }
                    }

                      // Make sure not to log any client specific parameters as security consideration
//                    String requestParameters = entry.getRequestParameters();
//                    if(!requestParameters.isEmpty()) {
//                        logBuilder
//                        .append("with parameters:\n")
//                        .append(requestParameters).append("\n");
//                    }

                    RequestWatcherServiceImpl.LOG.info(
                        logBuilder
                        .append("with age: ").append(entry.getAge()).append(" ms").append("\n")
                        .append("exceeds max. age of: ").append(requestMaxAge).append(" ms.")
                        .toString(),
                        trace);
                }
            },
                1000,
                watcherFrequency,
                TimeUnit.MILLISECONDS);
            this.requestWatcherTask = requestWatcherTask;
        }
    }

    @Override
    public RequestRegistryEntry registerRequest(final HttpServletRequest request, final HttpServletResponse response, final Thread thread) {
        final RequestRegistryEntry registryEntry = new RequestRegistryEntry(request, response, thread);
        requestRegistry.add(registryEntry);
        return registryEntry;
    }

    @Override
    public boolean unregisterRequest(final RequestRegistryEntry registryEntry) {
        return requestRegistry.remove(registryEntry);
    }

    @Override
    public boolean stopWatching() {
        final ScheduledTimerTask requestWatcherTask = this.requestWatcherTask;
        if (null != requestWatcherTask) {
            return requestWatcherTask.cancel();
        }
        return true;
    }

}
