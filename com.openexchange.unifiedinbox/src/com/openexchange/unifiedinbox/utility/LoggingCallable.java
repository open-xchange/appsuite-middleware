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

package com.openexchange.unifiedinbox.utility;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import com.openexchange.session.Session;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.threadpool.Trackable;

/**
 * {@link LoggingCallable} - Extends {@link Callable} interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class LoggingCallable<V> implements Task<V>, Trackable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoggingCallable.class);

    private final Session session;

    private final int accountId;

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
        this(session, -1);
    }

    /**
     * Initializes a new {@link LoggingCallable}.
     *
     * @param session The session
     * @param accountId The account ID
     */
    public LoggingCallable(final Session session, final int accountId) {
        super();
        this.session = session;
        this.accountId = accountId;
    }

    /**
     * Gets the logger.
     *
     * @return The logger
     */
    public Logger getLogger() {
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

    /**
     * Gets the account ID
     *
     * @return The account ID or <code>-1</code> if not set
     */
    public int getAccountId() {
        return accountId;
    }

    @Override
    public void afterExecute(final Throwable t) {
        // NOP
    }

    @Override
    public void beforeExecute(final Thread t) {
        // NOP
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // NOP
    }

}
