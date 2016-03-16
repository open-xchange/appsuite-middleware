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
