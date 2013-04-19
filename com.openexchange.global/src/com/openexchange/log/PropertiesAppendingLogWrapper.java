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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogProperties.Name;
import com.openexchange.log.LogPropertyName.LogLevel;

/**
 * {@link PropertiesAppendingLogWrapper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PropertiesAppendingLogWrapper implements Log, PropertiesAppender {

    /**
     * Line separator string.  This is the value of the line.separator
     * property at the moment that the PropertiesAppendingLogWrapper was created.
     */
    private final String lineSeparator;
    private final org.apache.commons.logging.Log delegate;
    private final boolean enabled;
    private final boolean delegateAppending;

    /**
     * Initializes a new {@link PropertiesAppendingLogWrapper}.
     *
     * @param delegate The delegate logger
     */
    protected PropertiesAppendingLogWrapper(final org.apache.commons.logging.Log delegate) {
        super();
        enabled = LogProperties.isEnabled();
        lineSeparator = System.getProperty("line.separator");
        this.delegate = delegate;
        delegateAppending = ((delegate instanceof com.openexchange.exception.Log) || (delegate instanceof com.openexchange.log.Log));
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        delegate.debug(appendProperties(message, LogLevel.DEBUG), t);
    }

    @Override
    public void debug(final Object message) {
        delegate.debug(appendProperties(message, LogLevel.DEBUG));
    }

    @Override
    public void error(final Object message, final Throwable t) {
        delegate.error(appendProperties(message, LogLevel.ERROR), t);
    }

    @Override
    public void error(final Object message) {
        delegate.error(appendProperties(message, LogLevel.ERROR));
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        delegate.fatal(appendProperties(message, LogLevel.FATAL), t);
    }

    @Override
    public void fatal(final Object message) {
        delegate.fatal(appendProperties(message, LogLevel.FATAL));
    }

    @Override
    public void info(final Object message, final Throwable t) {
        delegate.info(appendProperties(message, LogLevel.INFO), t);
    }

    @Override
    public void info(final Object message) {
        delegate.info(appendProperties(message, LogLevel.INFO));
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        delegate.trace(appendProperties(message, LogLevel.TRACE), t);
    }

    @Override
    public void trace(final Object message) {
        delegate.trace(appendProperties(message, LogLevel.TRACE));
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        delegate.warn(appendProperties(message, LogLevel.WARNING), t);
    }

    @Override
    public void warn(final Object message) {
        delegate.warn(appendProperties(message, LogLevel.WARNING));
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

    // --------------------------------- PropertiesAppender ---------------------------------------- //

    @Override
    public Object appendProperties(final Object message, final LogLevel logLevel) {
        if (!enabled) {
            return message;
        }
        if (delegateAppending) {
            return new PropertiesAppendingMessage(message, logLevel, this);
        }
        return innerAppendProperties(message, logLevel);
    }

    /**
     * Appends the log properties
     *
     * @param message The message
     * @param logLevel The log level
     * @return The message with properties appended
     */
    protected Object innerAppendProperties(final Object message, final LogLevel logLevel) {
        /*
         * Prepend properties
         */
        final Set<LogProperties.Name> propertiesToLog = getPropertiesFor(logLevel);
        if (propertiesToLog.isEmpty()) {
            return message;
        }
        /*
         * Check available properties
         */
        final Map<LogProperties.Name, Object> properties;
        {
            final Props logProps = LogProperties.optLogProperties();
            if (logProps == null) {
                return message;
            }
            properties = logProps.getMap();
            if (properties == null) {
                return message;
            }
        }
        /*
         * Sorted output
         */
        final Map<String, String> sorted = new TreeMap<String, String>();
        boolean isEmpty = true;
        for (final LogProperties.Name propertyName : propertiesToLog) {
            final Object value = properties.get(propertyName);
            if (null != value) {
                sorted.put(propertyName.getName(), value.toString());
                isEmpty = false;
            }
        }
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(256);
        if (!isEmpty) {
            final String lineSeparator = this.lineSeparator;
            boolean first = true;
            for (final Entry<String, String> entry : sorted.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(lineSeparator);
                }
                sb.append(entry.getKey()).append('=').append(entry.getValue());
            }
            sb.append(lineSeparator).append(lineSeparator);
        }
        sb.append(message);
        return sb.toString();
    }

    @Override
    public Set<LogProperties.Name> getPropertiesFor(final LogLevel logLevel) {
        if (!enabled) {
            return Collections.emptySet();
        }
        // Get all available log properties
        final Map<LogProperties.Name, Object> properties;
        {
            final Props logProps = LogProperties.optLogProperties();
            if (logProps == null) {
                return Collections.emptySet();
            }
            properties = logProps.getMap();
            if (properties == null) {
                return Collections.emptySet();
            }
        }
        // First add the configured ones
        final Set<LogProperties.Name> propertiesToLog = EnumSet.noneOf(LogProperties.Name.class);
        {
            final List<LogPropertyName> names = LogProperties.getPropertyNames();
            if (!names.isEmpty()) {
                for (final LogPropertyName name : names) {
                    if (name.implies(logLevel)) {
                        propertiesToLog.add(name.getPropertyName());
                    }
                }
            }
        }
        // Now add properties of type "com.openexchange.log.ForceLog"
        for (final Entry<LogProperties.Name, Object> entry : properties.entrySet()) {
            final LogProperties.Name propertyName = entry.getKey();
            if (!propertiesToLog.contains(propertyName) && (entry.getValue() instanceof ForceLog)) {
                propertiesToLog.add(propertyName);
            }
        }
        return propertiesToLog;
    }

    private static final class PropertiesAppendingMessage implements PropertiesAppender {

        final Object message;
        final PropertiesAppendingLogWrapper appender;
        final LogLevel logLevel;

        PropertiesAppendingMessage(final Object message, final LogLevel logLevel, final PropertiesAppendingLogWrapper appender) {
            super();
            this.message = message;
            this.appender = appender;
            this.logLevel = logLevel;
        }

        @Override
        public String toString() {
            return appendProperties(message, logLevel).toString();
        }

        @Override
        public Object appendProperties(final Object message, final LogLevel logLevel) {
            return appender.innerAppendProperties(message, logLevel);
        }

        @Override
        public Set<Name> getPropertiesFor(final LogLevel logLevel) {
            return appender.getPropertiesFor(logLevel);
        }
    } // End of PropertiesAppendingMessage

}
