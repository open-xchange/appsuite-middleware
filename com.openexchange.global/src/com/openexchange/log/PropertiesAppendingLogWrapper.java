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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogPropertyName.LogLevel;

/**
 * {@link PropertiesAppendingLogWrapper}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PropertiesAppendingLogWrapper implements Log {

    private final org.apache.commons.logging.Log delegate;

    /**
     * Initializes a new {@link PropertiesAppendingLogWrapper}.
     * 
     * @param delegate The delegate logger
     */
    protected PropertiesAppendingLogWrapper(final org.apache.commons.logging.Log delegate) {
        this.delegate = delegate;
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        delegate.debug(appendProperties(message == null ? null : message.toString(), LogLevel.DEBUG), t);
    }

    @Override
    public void debug(final Object message) {
        delegate.debug(appendProperties(message == null ? null : message.toString(), LogLevel.DEBUG));
    }

    @Override
    public void error(final Object message, final Throwable t) {
        delegate.error(appendProperties(message == null ? null : message.toString(), LogLevel.ERROR), t);
    }

    @Override
    public void error(final Object message) {
        delegate.error(appendProperties(message == null ? null : message.toString(), LogLevel.ERROR));
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        delegate.fatal(appendProperties(message == null ? null : message.toString(), LogLevel.FATAL), t);
    }

    @Override
    public void fatal(final Object message) {
        delegate.fatal(appendProperties(message == null ? null : message.toString(), LogLevel.FATAL));
    }

    @Override
    public void info(final Object message, final Throwable t) {
        delegate.info(appendProperties(message == null ? null : message.toString(), LogLevel.INFO), t);
    }

    @Override
    public void info(final Object message) {
        delegate.info(appendProperties(message == null ? null : message.toString(), LogLevel.INFO));
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        delegate.trace(appendProperties(message == null ? null : message.toString(), LogLevel.TRACE), t);
    }

    @Override
    public void trace(final Object message) {
        delegate.trace(appendProperties(message == null ? null : message.toString(), LogLevel.TRACE));
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        delegate.warn(appendProperties(message == null ? null : message.toString(), LogLevel.WARNING), t);
    }

    @Override
    public void warn(final Object message) {
        delegate.warn(appendProperties(message == null ? null : message.toString(), LogLevel.WARNING));
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

    /**
     * Append properties to specified message (if available).
     * 
     * @param message The message to append to
     * @param logLevel The log level
     * @return The message with properties appended
     */
    public String appendProperties(final String message, final LogLevel logLevel) {
        if (!LogProperties.isEnabled()) {
            return message;
        }
        final Props logProps = LogProperties.optLogProperties();
        if (logProps == null) {
            return message;
        }
        final Map<String, Object> properties = logProps.getMap();
        if (properties == null) {
            return message;
        }
        /*
         * Properties available
         */
        final Map<String, String> sorted = new TreeMap<String, String>();
        boolean isEmpty = true;
        {
            final List<LogPropertyName> names = LogProperties.getPropertyNames();
            final Set<String> alreadyLogged;
            if (names.isEmpty()) {
                alreadyLogged = Collections.emptySet();
            } else {
                alreadyLogged = new HashSet<String>(names.size());
                for (final LogPropertyName name : names) {
                    if (name.implies(logLevel)) {
                        final String propertyName = name.getPropertyName();
                        alreadyLogged.add(propertyName);
                        final Object value = properties.get(propertyName);
                        if (null != value) {
                            sorted.put(propertyName, value.toString());
                            isEmpty = false;
                        }
                    }
                }
            }
            for (final Entry<String, Object> entry : properties.entrySet()) {
                final String propertyName = entry.getKey();
                if (!alreadyLogged.contains(propertyName)) {
                    final Object value = entry.getValue();
                    if (value instanceof ForceLog) {
                        sorted.put(propertyName, value.toString());
                        isEmpty = false;
                    }
                }
            }
        }
        final StringBuilder sb = new StringBuilder(256);
        if (!isEmpty) {
            for (final Entry<String, String> entry : sorted.entrySet()) {
                sb.append('\n').append(entry.getKey()).append('=').append(entry.getValue());
            }
            sb.deleteCharAt(0).append("\n\n");
        }
        sb.append(message);
        return sb.toString();
    }

}
