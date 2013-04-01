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

package com.openexchange.log.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogPropertyName;
import com.openexchange.log.Loggable;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link LoggerTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class LoggerTask extends AbstractTask<Object> {

    private static final String PREFIX = "Logged at: ";

    /**
     * The poison element.
     */
    private static final Loggable POISON = new Loggable() {

        @Override
        public Throwable getThrowable() {
            return null;
        }

        @Override
        public String getMessage() {
            return null;
        }

        @Override
        public Log getLog() {
            return null;
        }

        @Override
        public Level getLevel() {
            return null;
        }

        @Override
        public StackTraceElement[] getCallerTrace() {
            return null;
        }

        @Override
        public boolean isLoggable() {
            return false;
        }

        @Override
        public Map<LogProperties.Name, Object> properties() {
            return Collections.emptyMap();
        }
    };

    private final BlockingQueue<Loggable> queue;
    private final AtomicBoolean keepgoing;
    private final String lineSeparator;

    /**
     * Initializes a new {@link LoggerTask}.
     *
     * @param queue
     */
    protected LoggerTask(final BlockingQueue<Loggable> queue) {
        super();
        lineSeparator = System.getProperty("line.separator");
        keepgoing = new AtomicBoolean(true);
        this.queue = queue;
    }

    /**
     * Stops this task.
     */
    protected void stop() {
        keepgoing.set(false);
        /*
         * Feed poison element to enforce quit
         */
        try {
            queue.put(POISON);
        } catch (final InterruptedException e) {
            /*
             * Cannot occur, but keep interrupted state
             */
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        threadRenamer.rename("OX-Logger");
    }

    @Override
    public Object call() throws Exception {
        try {
            final List<Loggable> loggables = new ArrayList<Loggable>(16);
            while (keepgoing.get()) {
                try {
                    if (queue.isEmpty()) {
                        /*
                         * Blocking wait for at least 1 Loggable to arrive.
                         */
                        final Loggable loggable = queue.take();
                        if (POISON == loggable) {
                            return null;
                        }
                        loggables.add(loggable);
                    }
                    queue.drainTo(loggables);
                    final boolean quit = loggables.remove(POISON);
                    for (final Loggable loggable : loggables) {
                        final Throwable t = loggable.getThrowable();
                        final Log log = loggable.getLog();
                        final String message = null == loggable.getMessage() ? "" : loggable.getMessage();
                        switch (loggable.getLevel()) {
                        case FATAL:
                            if (log.isFatalEnabled()) {
                                if (null == t) {
                                    log.fatal(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.FATAL));
                                } else {
                                    log.fatal(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.FATAL), t);
                                }
                            }
                            break;
                        case ERROR:
                            if (log.isErrorEnabled()) {
                                if (null == t) {
                                    log.error(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.ERROR));
                                } else {
                                    log.error(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.ERROR), t);
                                }
                            }
                            break;
                        case WARNING:
                            if (log.isWarnEnabled()) {
                                if (null == t) {
                                    log.warn(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.WARNING));
                                } else {
                                    log.warn(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.WARNING), t);
                                }
                            }
                            break;
                        case INFO:
                            if (log.isInfoEnabled()) {
                                if (null == t) {
                                    log.info(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.INFO));
                                } else {
                                    log.info(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.INFO), t);
                                }
                            }
                            break;
                        case DEBUG:
                            if (log.isDebugEnabled()) {
                                if (null == t) {
                                    log.debug(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.DEBUG));
                                } else {
                                    log.debug(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.DEBUG), t);
                                }
                            }
                            break;
                        case TRACE:
                            if (log.isTraceEnabled()) {
                                if (null == t) {
                                    log.trace(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.TRACE));
                                } else {
                                    log.trace(message.startsWith(PREFIX) ? message : prependLocation(message, loggable, LogPropertyName.LogLevel.TRACE), t);
                                }
                            }
                            break;
                        default:
                            // No-op
                        }
                    }
                    loggables.clear();
                    if (quit) {
                        return null;
                    }
                } catch (final RuntimeException e) {
                    // Log task run failed...
                }
            }
        } catch (final Exception e) {
            // Log task failed...
        }
        return null;
    }

    private static final Pattern CRLF = Pattern.compile("\r?\n");

    private String prependLocation(final String message, final Loggable loggable, final LogPropertyName.LogLevel logLevel) {
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator((null == message ? 0 : message.length()) + 64);
        final StackTraceElement[] trace = loggable.getCallerTrace();
        // String logClass = null;
        if (null != trace) {
            for (final StackTraceElement ste : trace) {
                final String className = ste.getClassName();
                if (null != className && !className.startsWith("com.openexchange.log") && !className.startsWith("com.openexchange.exception.Log") && className.indexOf("LoggingLogic", 16) < 0) {
                    sb.append(PREFIX).append(className).append('.').append(ste.getMethodName());
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
                    sb.append(lineSeparator).append(' ');
                    // logClass = className;
                    break;
                }
            }
        }
        // TODO: Check for PropertiesAppender
        if (null != message) {
            sb.append(CRLF.matcher(message).replaceAll(lineSeparator + " "));
        }
        return sb.toString();
    }

}
