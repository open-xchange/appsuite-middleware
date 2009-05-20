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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.unifiedinbox.utility;

import static com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility.appendStackTrace2StringBuilder;
import java.util.concurrent.Callable;
import com.openexchange.session.Session;

/**
 * {@link LoggingCallable} - Extends {@link Callable} interface.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class LoggingCallable<V> implements Callable<V> {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LoggingCallable.class);

    private final Session session;

    private final String description;

    private final Throwable trace;

    /**
     * Initializes a new {@link LoggingCallable}.
     */
    public LoggingCallable() {
        this(null);
    }

    /**
     * Initializes a new {@link LoggingCallable}.
     * 
     * @param session The session
     */
    public LoggingCallable(final Session session) {
        this(session, null);
    }

    /**
     * Initializes a new {@link LoggingCallable}.
     * 
     * @param session The session
     * @param description A description for this callable task
     */
    public LoggingCallable(final Session session, final String description) {
        super();
        this.session = session;
        this.description = description;
        this.trace = LOG.isDebugEnabled() ? new Throwable() : null;
    }

    /**
     * Gets the logger.
     * 
     * @return The logger
     */
    public org.apache.commons.logging.Log getLogger() {
        return LOG;
    }

    /**
     * Gets the session.
     * 
     * @return The session or <code>null</code> if not set
     */
    public Session getSession() {
        return session;
    }

    public V call() throws Exception {
        if (LOG.isDebugEnabled()) {
            final long start = System.currentTimeMillis();
            final V retval = callInternal();
            final long dur = System.currentTimeMillis() - start;
            final StringBuilder sb = new StringBuilder(32).append(Thread.currentThread().getName()).append(" needed ").append(dur).append(
                "msec to perform task");
            if (null == description) {
                sb.append('.');
            } else {
                sb.append(" \"").append(description).append("\".");
            }
            sb.append('\n');
            appendStackTrace2StringBuilder(trace, sb);
            sb.append('\n');
            LOG.debug(sb.toString());
            return retval;
        }
        return callInternal();
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     * 
     * @return The computed result
     * @throws Exception If unable to compute a result
     */
    protected abstract V callInternal() throws Exception;

}
