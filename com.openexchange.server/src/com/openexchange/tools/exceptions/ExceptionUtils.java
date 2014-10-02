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

package com.openexchange.tools.exceptions;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;

/**
 * Utilities for handling <tt>Throwable</tt>s and <tt>Exception</tt>s.
 */
public class ExceptionUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExceptionUtils.class);

    private static final String MARKER = " ---=== /!\\ ===--- ";

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be rethrown and swallows all others.
     *
     * @param t The <tt>Throwable</tt> to check
     */
    public static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            final Map<String, String> taskProperties = LogProperties.getPropertyMap();
            if (null == taskProperties) {
                LOG.error("{}Thread death{}", MARKER, MARKER, t);
            } else {
                final StringBuilder logBuilder = new StringBuilder(512);
                final Map<String, String> sorted = new TreeMap<String, String>();
                for (final Map.Entry<String, String> entry : taskProperties.entrySet()) {
                    final String propertyName = entry.getKey();
                    final String value = entry.getValue();
                    if (null != value) {
                        sorted.put(propertyName, value);
                    }
                }
                for (final Map.Entry<String, String> entry : sorted.entrySet()) {
                    logBuilder.append(Strings.getLineSeparator()).append(entry.getKey()).append('=').append(entry.getValue());
                }
                logBuilder.deleteCharAt(0);
                logBuilder.append(Strings.getLineSeparator()).append(Strings.getLineSeparator());
                logBuilder.append(MARKER);
                logBuilder.append("Thread death");
                logBuilder.append(MARKER);
                LOG.error(logBuilder.toString(), t);
            }
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
        }
        if (t instanceof VirtualMachineError) {
            final Map<String, String> taskProperties = LogProperties.getPropertyMap();
            if (null == taskProperties) {
                LOG.error(MARKER + "The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating." + MARKER, t);
            } else {
                final StringBuilder logBuilder = new StringBuilder(512);
                final Map<String, String> sorted = new TreeMap<String, String>();
                for (final Map.Entry<String, String> entry : taskProperties.entrySet()) {
                    final String propertyName = entry.getKey();
                    final String value = entry.getValue();
                    if (null != value) {
                        sorted.put(propertyName, value);
                    }
                }
                for (final Map.Entry<String, String> entry : sorted.entrySet()) {
                    logBuilder.append(Strings.getLineSeparator()).append(entry.getKey()).append('=').append(entry.getValue());
                }
                logBuilder.deleteCharAt(0);
                logBuilder.append(Strings.getLineSeparator()).append(Strings.getLineSeparator());
                logBuilder.append(MARKER);
                logBuilder.append("The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.");
                logBuilder.append(MARKER);
                LOG.error(logBuilder.toString(), t);
            }
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

    private static String surroundWithMarker(final String message) {
        return new StringBuilder(message.length() + 40).append(MARKER).append(message).append(MARKER).toString();
    }
}
