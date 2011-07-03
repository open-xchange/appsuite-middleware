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

package com.openexchange.exception;

/**
 * {@link Log} - A simple wrapper for {@link org.apache.commons.logging.Log} which uses {@link OXException#getLogMessage(LogLevel)} if an
 * {@link OXException} is passed to one of its log methods.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Log implements org.apache.commons.logging.Log {

    /**
     * Gets the appropriate {@link Log} for specified {@link org.apache.commons.logging.Log} instance.
     * 
     * @param log The {@link org.apache.commons.logging.Log} instance
     * @return The appropriate instance
     */
    public static org.apache.commons.logging.Log valueOf(final org.apache.commons.logging.Log log) {
        return new Log(log);
    }

    private final org.apache.commons.logging.Log delegatee;

    /**
     * Initializes a new {@link Log}.
     */
    private Log(final org.apache.commons.logging.Log delegatee) {
        super();
        this.delegatee = delegatee;
    }

    @Override
    public int hashCode() {
        return delegatee.hashCode();
    }

    public boolean isDebugEnabled() {
        return delegatee.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return delegatee.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return delegatee.isFatalEnabled();
    }

    public boolean isInfoEnabled() {
        return delegatee.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return delegatee.isTraceEnabled();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegatee.equals(obj);
    }

    public boolean isWarnEnabled() {
        return delegatee.isWarnEnabled();
    }

    public void trace(final Object message) {
        delegatee.trace(message);
    }

    public void trace(final Object message, final Throwable t) {
        if (OXException.class.isInstance(t)) {
            ((OXException) t).getLogMessage(LogLevel.TRACE);
            return;
        }
        delegatee.trace(message, t);
    }

    public void debug(final Object message) {
        delegatee.debug(message);
    }

    public void debug(final Object message, final Throwable t) {
        if (OXException.class.isInstance(t)) {
            ((OXException) t).getLogMessage(LogLevel.DEBUG);
            return;
        }
        delegatee.debug(message, t);
    }

    public void info(final Object message) {
        delegatee.info(message);
    }

    public void info(final Object message, final Throwable t) {
        if (OXException.class.isInstance(t)) {
            ((OXException) t).getLogMessage(LogLevel.INFO);
            return;
        }
        delegatee.info(message, t);
    }

    public void warn(final Object message) {
        delegatee.warn(message);
    }

    public void warn(final Object message, final Throwable t) {
        if (OXException.class.isInstance(t)) {
            ((OXException) t).getLogMessage(LogLevel.WARNING);
            return;
        }
        delegatee.warn(message, t);
    }

    public void error(final Object message) {
        delegatee.error(message);
    }

    public void error(final Object message, final Throwable t) {
        if (OXException.class.isInstance(t)) {
            ((OXException) t).getLogMessage(LogLevel.ERROR);
            return;
        }
        delegatee.error(message, t);
    }

    public void fatal(final Object message) {
        delegatee.fatal(message);
    }

    public void fatal(final Object message, final Throwable t) {
        if (OXException.class.isInstance(t)) {
            ((OXException) t).getLogMessage(LogLevel.FATAL);
            return;
        }
        delegatee.fatal(message, t);
    }

    @Override
    public String toString() {
        return delegatee.toString();
    }

}
