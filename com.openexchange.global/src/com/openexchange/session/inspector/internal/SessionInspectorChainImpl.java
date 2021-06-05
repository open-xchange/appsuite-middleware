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

package com.openexchange.session.inspector.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorChain;
import com.openexchange.session.inspector.SessionInspectorService;


/**
 * {@link SessionInspectorChainImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class SessionInspectorChainImpl implements SessionInspectorChain {

    private final ServiceSet<SessionInspectorService> chain;

    /**
     * Initializes a new {@link SessionInspectorChainImpl}.
     */
    public SessionInspectorChainImpl(ServiceSet<SessionInspectorService> chain) {
        super();
        this.chain = chain;
    }

    @Override
    public boolean isEmpty() {
        return chain.isEmpty();
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        for (SessionInspectorService inspector : chain) {
            Reply r = inspector.onSessionHit(session, request, response);
            if (r != Reply.NEUTRAL) {
                return r;
            }
        }
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException {
        for (SessionInspectorService inspector : chain) {
            Reply r = inspector.onSessionMiss(sessionId, request, response);
            if (r != Reply.NEUTRAL) {
                return r;
            }
        }
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) throws OXException {
        for (SessionInspectorService inspector : chain) {
            Reply r = inspector.onAutoLoginFailed(reason, request, response);
            if (r != Reply.NEUTRAL) {
                return r;
            }
        }
        return Reply.NEUTRAL;
    }

}
