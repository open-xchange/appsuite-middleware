/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.log;



/**
 * {@link Log} - The <code>org.apache.commons.logging.Log</code> using {@link LogService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated Use slf4j logger
 */
@Deprecated
public final class Log implements org.apache.commons.logging.Log {

    /**
     * Gets the appropriate {@link org.slf4j.Logger logger} for specified class.
     *
     * @param clazz The class
     * @return The logger.
     * @deprecated Use slf4j logger
     */
    @Deprecated
    public static org.apache.commons.logging.Log loggerFor(final Class<?> clazz) {
        return org.apache.commons.logging.LogFactory.getLog(clazz);
    }

    /**
     * Gets the appropriate {@link org.slf4j.Logger logger} for specified class.
     *
     * @param clazz The class name
     * @return The logger.
     * @deprecated Use slf4j logger
     */
    @Deprecated
    public static org.apache.commons.logging.Log loggerFor(final String clazz) {
        return org.apache.commons.logging.LogFactory.getLog(clazz);
    }

    /**
     * Gets the appropriate {@link com.openexchange.log.Log} for specified {@link org.apache.commons.logging.Log} instance.
     *
     * @param log The {@link org.apache.commons.logging.Log} instance
     * @return The appropriate instance
     * @deprecated Use slf4j logger
     */
    @Deprecated
    public static org.apache.commons.logging.Log valueOf(final org.apache.commons.logging.Log log) {
        return log;
    }

    private final org.apache.commons.logging.Log delegate;

    /**
     * Initializes a new {@link Log}.
     *
     * @deprecated Use slf4j logger
     */
    @Deprecated
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
        delegate.trace(message);
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        delegate.trace(message, t);
    }

    @Override
    public void debug(final Object message) {
        delegate.debug(message);
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        delegate.debug(message, t);
    }

    @Override
    public void info(final Object message) {
        delegate.info(message);
    }

    @Override
    public void info(final Object message, final Throwable t) {
        delegate.info(message, t);
    }

    @Override
    public void warn(final Object message) {
        delegate.warn(message);
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        delegate.warn(message, t);
    }

    @Override
    public void error(final Object message) {
        delegate.error(message);
    }

    @Override
    public void error(final Object message, final Throwable t) {
        delegate.error(message, t);
    }

    @Override
    public void fatal(final Object message) {
        delegate.fatal(message);
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        delegate.fatal(message, t);
    }

}
