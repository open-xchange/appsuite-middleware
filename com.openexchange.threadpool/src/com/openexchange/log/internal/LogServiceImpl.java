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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.LogService;
import com.openexchange.log.Loggable;
import com.openexchange.log.PropertiesAppender;
import com.openexchange.log.Loggable.Level;
import com.openexchange.log.Props;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.AbortBehavior;

/**
 * {@link LogServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LogServiceImpl implements LogService {

    /**
     * The default queue capacity.
     */
    private static final int DEFAULT_CAPACITY = 8192;

    private final BlockingQueue<Loggable> queue;
    private final Future<Object> future;
    private final LoggerTask loggerTask;
    private final boolean enabled;

    /**
     * Initializes a new {@link LogServiceImpl}.
     */
    public LogServiceImpl(final ThreadPoolService threadPool, final int queueCapacity) {
        super();
        queue = new LinkedBlockingQueue<Loggable>(queueCapacity > 0 ? queueCapacity : DEFAULT_CAPACITY);
        loggerTask = new LoggerTask(queue);
        future = threadPool.submit(loggerTask, AbortBehavior.getInstance());
        enabled = LogProperties.isEnabled();
    }

    /**
     * Stops the log service orderly.
     */
    public void stop() {
        loggerTask.stop();
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            // Wait time elapsed; enforce cancelation
            future.cancel(true);
        } catch (final InterruptedException e) {
            /*
             * Cannot occur, but keep interrupted state
             */
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            // What?!
        }
    }

    @Override
    public void log(final Level level, final Log log, final Throwable throwable) {
        log(loggableFor(level, log, throwable.getMessage(), throwable));
    }

    @Override
    public void log(final Loggable loggable) {
        final Log log = loggable.getLog();
        if (null == log) {
            return;
        }
        if (loggable.isLoggable()) {
            try {
                queue.offer(loggable); // Throw away Loggable if queue capacity is exceeded
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    @Override
    public Loggable loggableFor(final Level level, final Log log, final Object message, final Throwable throwable) {
        final LoggableImpl loggable = new LoggableImpl(level, log, message, throwable, new Throwable());
        if (enabled && !(message instanceof PropertiesAppender)) {
            final Props props = LogProperties.optLogProperties();
            loggable.putProperties(null == props ? null : props.getMap());
        }
        return loggable;
    }

    @Override
    public Loggable loggableFor(final Level level, final Log log, final Object message) {
        final LoggableImpl loggable = new LoggableImpl(level, log, message, null, new Throwable());
        if (enabled && !(message instanceof PropertiesAppender)) {
            final Props props = LogProperties.optLogProperties();
            loggable.putProperties(null == props ? null : props.getMap());
        }
        return loggable;
    }

}
