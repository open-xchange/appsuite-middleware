/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openexchange.osgi;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.openexchange.java.Strings;


/**
 * Utilities for handling <tt>Throwable</tt>s and <tt>Exception</tt>s.
 */
public class ExceptionUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExceptionUtils.class);

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be rethrown and swallows all others.
     *
     * @param t The <tt>Throwable</tt> to check
     */
    public static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            String marker = " ---=== /!\\ ===--- ";
            LOG.error("{}Thread death{}", marker, marker, t);
            throw (ThreadDeath) t;
        }
        if (t instanceof OutOfMemoryError) {
            OutOfMemoryError oom = (OutOfMemoryError) t;
            handleOOM(oom);
            throw oom;
        }
        if (t instanceof VirtualMachineError) {
            String marker = " ---=== /!\\ ===--- ";
            LOG.error("{}The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.{}", marker, marker, t);
            throw (VirtualMachineError) t;
        }
    }

    /**
     * Handles given OutOfMemoryError instance
     * <p>
     * <b><i>Does not re-throw given OutOfMemoryError instance</i></b>
     *
     * @param oom The OutOfMemoryError instance
     */
    public static void handleOOM(OutOfMemoryError oom) {
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

                String mbeanName = "com.sun.management:type=HotSpotDiagnostic";
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.US);
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String fn = "/tmp/" + dateFormat.format(new Date()) + "-heap.hprof";

                server.invoke(new ObjectName(mbeanName), "dumpHeap", new Object[] { fn, Boolean.TRUE }, new String[] { String.class.getCanonicalName(), "boolean" });
                LOG.info("{}    Heap snapshot dumped to file {}{}", Strings.getLineSeparator(), fn, Strings.getLineSeparator());
            } catch (Exception e) {
                // Failed for any reason...
            }
        }
        String marker = " ---=== /!\\ ===--- ";
        LOG.error("{}The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.{}", marker, marker, oom);
    }

    /**
     * Truncates given {@link Throwable}'s stack trace.
     *
     * @param t The {@code Throwable} instance
     * @return The truncated {@code Throwable} instance
     */
    public static Throwable truncateThrowable(Throwable t) {
        if (null == t) {
            return t;
        }

        StackTraceElement[] stackTrace = t.getStackTrace();
        int threshold = 20;
        int length = stackTrace.length;
        if (length <= threshold) {
            return t;
        }

        List<StackTraceElement> truncatedStackTrace = new LinkedList<StackTraceElement>();
        for (int i = 0; i < length; i++) {
            if (i <= threshold) {
                truncatedStackTrace.add(stackTrace[i]);
            } else {
                String prefix = "com.openexchange.http.grizzly.service.http";
                String className = stackTrace[i].getClassName();
                if (!className.startsWith(prefix)) {
                    truncatedStackTrace.add(stackTrace[i]);
                }
            }
        }

        FastThrowable ft = new FastThrowable(t.getMessage(), t.getCause());
        ft.setStackTrace(truncatedStackTrace.toArray(new StackTraceElement[truncatedStackTrace.size()]));
        return ft;
    }

    // ----------------------------------------------------------------------------- //

    static final class FastThrowable extends Throwable {

        FastThrowable(String message, Throwable cause) {
            super(null == message ? "<unknkown>" : message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

}
