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

import java.util.LinkedList;
import java.util.List;


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
            LOG.error(surroundWithMarker("Thread death"), t);
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            final String message = "The Java Virtual Machine is broken or has run out of resources necessary for it to continue operating.";
            LOG.error(surroundWithMarker(message), t);
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

    private static String surroundWithMarker(final String message) {
        final String marker = " ---=== /!\\ ===--- ";
        return new StringBuilder(message.length() + 40).append(marker).append(message).append(marker).toString();
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
