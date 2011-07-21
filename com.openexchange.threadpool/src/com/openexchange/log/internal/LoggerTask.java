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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.log.Loggable;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link LoggerTask}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class LoggerTask extends AbstractTask<Object> {

    /**
     * The poison element.
     */
    public static final Loggable POISON = new Loggable() {
        
        public Throwable getThrowable() {
            return null;
        }
        
        public String getMessage() {
            return null;
        }
        
        public Log getLog() {
            return null;
        }
        
        public Level getLevel() {
            return null;
        }

        public StackTraceElement[] getCallerTrace() {
            return null;
        }
    };

    private final BlockingQueue<Loggable> queue;

    private final AtomicBoolean keepgoing;

    /**
     * Initializes a new {@link LoggerTask}.
     * 
     * @param queue
     */
    protected LoggerTask(final BlockingQueue<Loggable> queue) {
        super();
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

    public Object call() throws Exception {
        final List<Loggable> loggables = new ArrayList<Loggable>(16);
        while (keepgoing.get()) {
            if (queue.isEmpty()) {
                // Blocking wait for at least 1 loggable to arrive.
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
                final String message = prependLocation(loggable.getMessage(), loggable.getCallerTrace());
                switch (loggable.getLevel()) {
                case FATAL:
                    if (null == t) {
                        loggable.getLog().fatal(message);
                    } else {
                        loggable.getLog().fatal(message, t);
                    }
                    break;
                case ERROR:
                    if (null == t) {
                        loggable.getLog().error(message);
                    } else {
                        loggable.getLog().error(message, t);
                    }
                    break;
                case WARNING:
                    if (null == t) {
                        loggable.getLog().warn(message);
                    } else {
                        loggable.getLog().warn(message, t);
                    }
                    break;
                case INFO:
                    if (null == t) {
                        loggable.getLog().info(message);
                    } else {
                        loggable.getLog().info(message, t);
                    }
                    break;
                case DEBUG:
                    if (null == t) {
                        loggable.getLog().debug(message);
                    } else {
                        loggable.getLog().debug(message, t);
                    }
                    break;
                case TRACE:
                    if (null == t) {
                        loggable.getLog().trace(message);
                    } else {
                        loggable.getLog().trace(message, t);
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
        }
        return null;
    }

    private static String prependLocation(final String message, final StackTraceElement[] trace) {
        if (null == trace) {
            return message;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (!className.startsWith("com.openexchange.log")) {
                final StringBuilder sb = new StringBuilder(64).append(className).append(".").append(ste.getMethodName());
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
                            sb.append(':').append(lineNumber).append(")");
                        }
                        sb.append(')');
                    }
                }
                return sb.append("\n").append(message).toString();
            }
        }
        return message;
    }

}
