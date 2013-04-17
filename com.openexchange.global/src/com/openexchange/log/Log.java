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

package com.openexchange.log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.log.Loggable.Level;

/**
 * {@link Log} - The <code>org.apache.commons.logging.Log</code> using {@link LogService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Log implements org.apache.commons.logging.Log {

    private static final AtomicBoolean APPEND_TRACE_TO_MESSAGE = new AtomicBoolean();

    /**
     * Sets whether to prefer to append stack traces to message itself or pass them as separate argument.
     *
     * @param appendTraceToMessage <code>true</code> to append stack traces to message; otherwise <code>false</code>
     */
    public static void setAppendTraceToMessage(final boolean appendTraceToMessage) {
        APPEND_TRACE_TO_MESSAGE.set(appendTraceToMessage);
    }

    /**
     * Checks whether to prefer to append stack traces to message itself or pass them as separate argument.
     *
     * @return <code>true</code> to append stack traces to message; otherwise <code>false</code>
     */
    public static boolean appendTraceToMessage() {
        return APPEND_TRACE_TO_MESSAGE.get();
    }

    private static final AtomicInteger MAX_MESSAGE_LENGTH = new AtomicInteger(-1);

    /**
     * Sets the max. message length.
     *
     * @param maxMessageLength The max. message length
     */
    public static void setMaxMessageLength(final int maxMessageLength) {
        MAX_MESSAGE_LENGTH.set(maxMessageLength);
    }

    /**
     * Gets the max. message length.
     *
     * @return The max. message length
     */
    public static int maxMessageLength() {
        return MAX_MESSAGE_LENGTH.get();
    }

    private static final AtomicReference<LogService> LOGSERVICE_REFERENCE = new AtomicReference<LogService>();

    /**
     * Sets the log service.
     *
     * @param logService The log service
     */
    public static void set(final LogService logService) {
        LOGSERVICE_REFERENCE.set(logService);
    }

    /**
     * Gets the appropriate {@link org.apache.commons.logging.Log logger} for specified class.
     *
     * @param clazz The class
     * @return The logger.
     */
    public static org.apache.commons.logging.Log loggerFor(final Class<?> clazz) {
        return valueOf(com.openexchange.log.LogFactory.getLog(clazz));
    }

    /**
     * Gets the appropriate {@link com.openexchange.log.Log} for specified {@link org.apache.commons.logging.Log} instance.
     *
     * @param log The {@link org.apache.commons.logging.Log} instance
     * @return The appropriate instance
     */
    public static org.apache.commons.logging.Log valueOf(final org.apache.commons.logging.Log log) {
        if ((log instanceof Log) || (log instanceof PropertiesAppendingLogWrapper)) {
            return log;
        }
        return new Log(log);
    }

    private final org.apache.commons.logging.Log delegate;

    /**
     * Initializes a new {@link Log}.
     */
    private Log(final org.apache.commons.logging.Log delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return delegate.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void trace(final Object message) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.trace(message);
        } else {
            logService.log(logService.loggableFor(Level.TRACE, delegate, null == message ? null : message.toString()));
        }
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.trace(message, t);
        } else {
            logService.log(logService.loggableFor(Level.TRACE, delegate, null == message ? null : message.toString(), t));
        }
    }

    @Override
    public void debug(final Object message) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.debug(message);
        } else {
            logService.log(logService.loggableFor(Level.DEBUG, delegate, null == message ? null : message.toString()));
        }
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.debug(message, t);
        } else {
            logService.log(logService.loggableFor(Level.DEBUG, delegate, null == message ? null : message.toString(), t));
        }
    }

    @Override
    public void info(final Object message) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.info(message);
        } else {
            logService.log(logService.loggableFor(Level.INFO, delegate, null == message ? null : message.toString()));
        }
    }

    @Override
    public void info(final Object message, final Throwable t) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.info(message, t);
        } else {
            logService.log(logService.loggableFor(Level.INFO, delegate, null == message ? null : message.toString(), t));
        }
    }

    @Override
    public void warn(final Object message) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.warn(message);
        } else {
            logService.log(logService.loggableFor(Level.WARNING, delegate, null == message ? null : message.toString()));
        }
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.warn(message, t);
        } else {
            logService.log(logService.loggableFor(Level.WARNING, delegate, null == message ? null : message.toString(), t));
        }
    }

    @Override
    public void error(final Object message) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.error(message);
        } else {
            logService.log(logService.loggableFor(Level.ERROR, delegate, null == message ? null : message.toString()));
        }
    }

    @Override
    public void error(final Object message, final Throwable t) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.error(message, t);
        } else {
            logService.log(logService.loggableFor(Level.ERROR, delegate, null == message ? null : message.toString(), t));
        }
    }

    @Override
    public void fatal(final Object message) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.fatal(message);
        } else {
            logService.log(logService.loggableFor(Level.FATAL, delegate, null == message ? null : message.toString()));
        }
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        final LogService logService = LOGSERVICE_REFERENCE.get();
        if (null == logService) {
            delegate.fatal(message, t);
        } else {
            logService.log(logService.loggableFor(Level.FATAL, delegate, null == message ? null : message.toString(), t));
        }
    }

}
