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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.meta.internal;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.ajax.meta.MetaContributionConstants;
import com.openexchange.ajax.meta.MetaContributor;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.session.Session;

/**
 * {@link MetaContributorReference} - A reference for <code>MetaContributor</code>s.
 * <p>
 * This class caches property values and performs final checks before calling the wrapped contributor.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public class MetaContributorReference implements MetaContributor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MetaContributorReference.class);

    private final ServiceReference<MetaContributor> reference;
    private final BundleContext context;
    private MetaContributor contributor;
    private String[] topics;

    /**
     * Create an MetaContributorReference.
     *
     * @param reference Reference to the EventHandler
     * @param context Bundle Context of the Event Admin bundle
     * @param log LogService object for logging
     */
    public MetaContributorReference(final ServiceReference<MetaContributor> reference, final BundleContext context) {
        super();
        this.reference = reference;
        this.context = context;
    }

    @Override
    public void contributeTo(Map<String, Object> meta, String id, Session session) throws OXException {
        doContribution(meta, id, session);
    }

    /**
     * Cache values from service properties
     *
     * @return true if the contributor should be called; false if the contributor should not be called
     */
    public synchronized boolean init() {
        topics = null;

        // Get topic names
        final Object o = reference.getProperty(MetaContributionConstants.CONTRIBUTOR_TOPIC);
        if (o instanceof String) {
            topics = new String[] { (String) o };
        } else if (o instanceof String[]) {
            topics = (String[]) o;
        } else if (o instanceof Collection) {
            try {
                @SuppressWarnings("unchecked") final Collection<String> c = (Collection<String>) o;
                topics = c.toArray(new String[c.size()]);
            } catch (final ArrayStoreException e) {
                LOG.error("Invalid event contributor topics", e);
            }
        }

        if (topics == null) {
            return false;
        }

        return true;
    }

    /**
     * Flush the contributor service if it has been obtained.
     */
    public void flush() {
        synchronized (this) {
            if (contributor == null) {
                return;
            }
            contributor = null;
        }
        try {
            context.ungetService(reference);
        } catch (final IllegalStateException e) {
            // ignore event admin must have stopped
        }
    }

    /**
     * Get the event topics for the wrapped contributor.
     *
     * @return The wrapped contributor's event topics
     */
    public synchronized String[] getTopics() {
        return topics;
    }

    /**
     * Return the wrapped contributor.
     *
     * @return The wrapped contributor.
     */
    private MetaContributor getContributor() {
        synchronized (this) {
            // if we already have a contributor, return it
            if (contributor != null) {
                return contributor;
            }
        }

        // we don't have the contributor, so lets get it outside the sync region
        MetaContributor tempHandler = null;
        try {
            tempHandler = context.getService(reference);
        } catch (final IllegalStateException e) {
            // ignore; service may have stopped
        }

        synchronized (this) {
            // do we still need the contributor we just got?
            if (contributor == null) {
                contributor = tempHandler;
                return contributor;
            }
            // get the current contributor
            tempHandler = contributor;
        }

        // unget the contributor we just got since we don't need it
        try {
            context.ungetService(reference);
        } catch (final IllegalStateException e) {
            // ignore; event admin may have stopped
        }

        // return the current contributor (copied into the local var)
        return tempHandler;
    }

    /**
     * Dispatch event to contributor. Perform final tests before actually calling the contributor.
     *
     * @param event The event to dispatch
     */
    public void doContribution(Map<String, Object> meta, String id, Session session) {
        final Bundle bundle = reference.getBundle();
        // is service unregistered?
        if (bundle == null) {
            return;
        }

        // get contributor service
        final MetaContributor contributor = getContributor();
        if (contributor == null) {
            return;
        }

        try {
            contributor.contributeTo(meta, id, session);
        } catch (Throwable t) {
            // log/handle any Throwable thrown by the listener
            handleThrowable(t);
            LOG.error("Entity contribution failed", t);
        }
    }

    private void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            String marker = " ---=== /!\\ ===--- ";
            LOG.error("{}Thread death{}", marker, marker, t);
            throw (ThreadDeath) t;
        }
        if (t instanceof OutOfMemoryError) {
            OutOfMemoryError oom = (OutOfMemoryError) t;
            String message = oom.getMessage();
            if ("unable to create new native thread".equalsIgnoreCase(message)) {
                // Dump all the threads to the log
                Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
                LOG.info("{}Threads: {}", Strings.getLineSeparator(), threads.size());
                for (Map.Entry<Thread, StackTraceElement[]> mapEntry : threads.entrySet()) {
                    Thread thread = mapEntry.getKey();
                    LOG.info("        thread: {}", thread);
                    for (final StackTraceElement stackTraceElement : mapEntry.getValue()) {
                        LOG.info(stackTraceElement.toString());
                    }
                }
                LOG.info("{}    Thread dump finished{}", Strings.getLineSeparator(), Strings.getLineSeparator());
            } else if ("Java heap space".equalsIgnoreCase(message)) {
                try {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

                    Pair<Boolean, String> heapDumpArgs = checkHeapDumpArguments();

                    // Is HeapDumpOnOutOfMemoryError enabled?
                    if (!heapDumpArgs.getFirst().booleanValue() && !Boolean.TRUE.equals(System.getProperties().get("__heap_dump_created"))) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.US);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                        // Either "/tmp" or path configured through "-XX:HeapDumpPath" JVM argument
                        String path = null == heapDumpArgs.getSecond() ? "/tmp" : heapDumpArgs.getSecond();
                        String fn = path + "/" + dateFormat.format(new Date()) + "-heap.hprof";

                        String mbeanName = "com.sun.management:type=HotSpotDiagnostic";
                        server.invoke(new ObjectName(mbeanName), "dumpHeap", new Object[] { fn, Boolean.TRUE }, new String[] { String.class.getCanonicalName(), "boolean" });
                        System.getProperties().put("__heap_dump_created", Boolean.TRUE);
                        LOG.info("{}    Heap snapshot dumped to file {}{}", Strings.getLineSeparator(), fn, Strings.getLineSeparator());
                    }
                } catch (Exception e) {
                    // Failed for any reason...
                }
            }
            String marker = " ---=== /!\\ ===--- ";
            LOG.error("{}The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.{}", marker, marker, t);
            throw oom;
        }
        if (t instanceof VirtualMachineError) {
            String marker = " ---=== /!\\ ===--- ";
            LOG.error("{}The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.{}", marker, marker, t);
            throw (VirtualMachineError) t;
        }
    }

    private static Pair<Boolean, String> checkHeapDumpArguments() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        boolean heapDumpOnOOm = false;
        String path = null;
        for (String argument : arguments) {
            if ("-XX:+HeapDumpOnOutOfMemoryError".equals(argument)) {
                heapDumpOnOOm = true;
            } else if (argument.startsWith("-XX:HeapDumpPath=")) {
                path = argument.substring(17).trim();
                File file = new File(path);
                if (!file.exists() || !file.canWrite()) {
                   path = null;
                }
            }
        }
        return new Pair<Boolean, String>(Boolean.valueOf(heapDumpOnOOm), path);
    }

}
