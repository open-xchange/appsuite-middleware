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

package com.openexchange.exception;

import java.util.Arrays;
import java.util.Comparator;

/**
 * An enumeration for log levels.
 */
public enum LogLevel {
    /**
     * Trace log level.
     */
    TRACE,
    /**
     * Debug log level.
     */
    DEBUG,
    /**
     * Info log level.
     */
    INFO,
    /**
     * Warn log level.
     */
    WARNING,
    /**
     * Error log level.
     */
    ERROR;

    /**
     * Checks if this log level equals {@link #DEBUG}.
     *
     * @return <code>true</code> if this log level equals {@link #DEBUG}; otherwise <code>false</code>
     */
    public boolean isDebug() {
        return DEBUG.equals(this);
    }

    /**
     * The default comparator for {@link LogLevel}.
     */
    public static final Comparator<LogLevel> COMPARATOR = new Comparator<LogLevel>() {

        @Override
        public int compare(final LogLevel o1, final LogLevel o2) {
            return (-o1.ordinal() + o2.ordinal());
        }

    };

    /**
     * Checks if this log level implies specified category's log level; e.g. {@link LogLevel#DEBUG DEBUG} implies {@link LogLevel#ERROR
     * ERROR}.
     * <pre>
     * TRACE -&gt; DEBUG -&gt; INFO -&gt; WARNING -&gt; ERROR -&gt; FATAL
     * </pre>
     *
     * @param category The category whose loglevel is possibly included
     * @return <code>true</code> if this log level implies specified category's log level; otherwise <code>false</code>
     */
    public boolean implies(final Category category) {
        return implies(category.getLogLevel());
    }

    /**
     * Checks if this log level implies specified log level; e.g. {@link LogLevel#DEBUG DEBUG} implies {@link LogLevel#ERROR ERROR}.
     * <pre>
     * TRACE -&gt; DEBUG -&gt; INFO -&gt; WARNING -&gt; ERROR -&gt; FATAL
     * </pre>
     *
     * @param logLevel The log level possibly included
     * @return <code>true</code> if this log level implies specified log level; otherwise <code>false</code>
     */
    public boolean implies(final LogLevel logLevel) {
        return this.ordinal() <= logLevel.ordinal();
    }

    /**
     * Checks if this log level applies to specified logger.
     *
     * @param logger The logger
     * @return <code>true</code> if specified logger applies; otherwise <code>false</code>
     */
    public boolean appliesTo(final org.slf4j.Logger logger) {
        switch (this) {
        case TRACE:
            return logger.isTraceEnabled();
        case DEBUG:
            return logger.isDebugEnabled();
        case INFO:
            return logger.isInfoEnabled();
        case WARNING:
            return logger.isWarnEnabled();
        case ERROR:
            return logger.isErrorEnabled();
        default:
            return false;
        }
    }

    /**
     * Logs specified logging and exception in appropriate log level.
     *
     * @param logging The logging
     * @param exception The exception
     * @param logger The logger
     */
    public void log(final String logging, final OXException exception, final org.slf4j.Logger logger) {
        switch (this) {
        case TRACE:
            logger.trace(logging, exception);
            break;
        case DEBUG:
            logger.debug(logging, exception);
            break;
        case INFO:
            logger.info(logging, exception);
            break;
        case WARNING:
            logger.warn(logging, exception);
            break;
        case ERROR:
            logger.error(logging, exception);
            break;
        default:
            break;
        }
    }

    /**
     * Gets the log levels in ranked order.
     *
     * @return The log levels in ranked order
     */
    public static LogLevel[] rankedOrder() {
        final LogLevel[] values = LogLevel.values();
        final LogLevel[] ret = new LogLevel[values.length];
        System.arraycopy(values, 0, ret, 0, values.length);
        Arrays.sort(ret, COMPARATOR);
        return ret;
    }

    /**
     * Gets the highest ranked log level appropriate for specified log instance.
     *
     * @param log The log instance
     * @return The appropriate log level
     */
    public static LogLevel valueOf(final org.slf4j.Logger log) {
        if (log.isErrorEnabled()) {
            return ERROR;
        }
        if (log.isWarnEnabled()) {
            return WARNING;
        }
        if (log.isInfoEnabled()) {
            return INFO;
        }
        if (log.isDebugEnabled()) {
            return DEBUG;
        }
        return TRACE;
    }

}
