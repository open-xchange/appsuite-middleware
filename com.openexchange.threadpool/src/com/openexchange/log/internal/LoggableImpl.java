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

import java.util.EnumMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Loggable;

/**
 * {@link LoggableImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LoggableImpl implements Loggable {

    private final Level level;
    private final Log log;
    private final Object message;
    private final Throwable throwable;
    private final Throwable callerTrace;
    private final Map<LogProperties.Name, Object> properties;

    /**
     * Initializes a new {@link LoggableImpl}.
     *
     * @param level
     * @param log
     * @param message
     * @param throwable
     */
    public LoggableImpl(final Level level, final Log log, final Object message, final Throwable throwable, final Throwable callerTrace) {
        super();
        this.level = level;
        this.log = log;
        this.message = message;
        this.throwable = throwable;
        this.callerTrace = callerTrace;
        properties = new EnumMap<LogProperties.Name, Object>(LogProperties.Name.class);
    }

    @Override
    public Map<LogProperties.Name, Object> properties() {
        return properties;
    }

    /**
     * Puts specified properties.
     *
     * @param properties The properties
     * @return This loggable with properties applied
     */
    public LoggableImpl putProperties(final Map<LogProperties.Name, Object> properties) {
        if (null != properties) {
            this.properties.putAll(properties);
        }
        return this;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public Log getLog() {
        return log;
    }

    @Override
    public Object getMessage() {
        return message;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public StackTraceElement[] getCallerTrace() {
        return callerTrace.getStackTrace();
    }

    @Override
    public boolean isLoggable() {
        return null != message || null != throwable;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("LoggableImpl [");
        if (level != null) {
            builder.append("level=").append(level).append(", ");
        }
        if (log != null) {
            builder.append("log=").append(log).append(", ");
        }
        if (message != null) {
            builder.append("message=\"").append(message).append("\", ");
        }
        if (throwable != null) {
            builder.append("throwable=").append("<available>").append(", ");
        }
        if (callerTrace != null) {
            builder.append("callerTrace=").append("<available>");
        }
        builder.append(']');
        return builder.toString();
    }

}
