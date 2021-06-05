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

package com.openexchange.saml.impl;

import static com.openexchange.sessiond.ExpirationReason.TIMED_OUT;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLSessionParameters;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;


/**
 * Terminates SAML sessions that exceed a timeout defined by the IdP.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLSessionInspector implements SessionInspectorService {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLSessionInspector.class);

    private final SessiondService sessiondService;

    public SAMLSessionInspector(SessiondService sessiondService) {
        super();
        this.sessiondService = sessiondService;
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        Object parameter = session.getParameter(SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER);
        if (parameter instanceof String) {
            try {
                long notOnOrAfter = Long.parseLong((String) parameter);
                if (System.currentTimeMillis() >= notOnOrAfter) {
                    sessiondService.removeSession(session.getSessionID());
                    OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
                    oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, TIMED_OUT.getIdentifier());
                    throw oxe;
                }
            } catch (NumberFormatException e) {
                LOG.warn("Session contained parameter '{}' but its value was not a valid timestamp: {}", SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER, parameter, e);
            }
        }

        return Reply.NEUTRAL;
    }

    @Override
    public Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.NEUTRAL;
    }

}
