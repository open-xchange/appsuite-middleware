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

package com.openexchange.server.services;

import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorChain;

/**
 * Registry for tracked {@link SessionInspectorChain} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessionInspector {

    private static final SessionInspectorChain NOOP_CHAIN = new SessionInspectorChain() {

        @Override
        public Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException {
            return Reply.CONTINUE;
        }

        @Override
        public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
            return Reply.CONTINUE;
        }

        @Override
        public Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) throws OXException {
            return Reply.CONTINUE;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    private static final SessionInspector SINGLETON = new SessionInspector();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static SessionInspector getInstance() {
        return SINGLETON;
    }

    // -------------------------------------------------------------------------------------------------

    private final AtomicReference<SessionInspectorChain> reference;

    private SessionInspector() {
        super();
        reference = new AtomicReference<SessionInspectorChain>(NOOP_CHAIN);
    }

    /**
     * Gets the registered {@code SessionInspectorChain}.
     *
     * @return The registered {@code SessionInspectorChain} or <code>null</code>
     */
    public SessionInspectorChain getChain() {
        return reference.get();
    }

    /**
     * Sets the tracked <code>SessionInspectorChain</code> instance.
     *
     * @param service The tracked <code>SessionInspectorChain</code> instance
     * @return <code>true</code> if given <code>SessionInspectorChain</code> instance could be successfully supplied; otherwise <code>false</code>
     */
    public boolean setService(final SessionInspectorChain service) {
        return reference.compareAndSet(NOOP_CHAIN, service);
    }

    /**
     * Drops the tracked <code>SessionInspectorChain</code> instance.
     *
     * @param service The tracked <code>SessionInspectorChain</code> instance
     * @return <code>true</code> if given <code>SessionInspectorChain</code> instance could be successfully dropped; otherwise <code>false</code>
     */
    public boolean dropService(final SessionInspectorChain service) {
        return reference.compareAndSet(service, NOOP_CHAIN);
    }

}
