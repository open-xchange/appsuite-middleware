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

package com.openexchange.session.management.json.actions;

import java.util.Collection;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.management.ManagedSession;
import com.openexchange.session.management.SessionManagementService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ClearAction}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ClearAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ClearAction}.
     *
     * @param services The service look-up
     */
    public ClearAction(ServiceLookup services) {
        super();
        this.services = services;

    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        // Get the service
        SessionManagementService service = services.getService(SessionManagementService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(SessionManagementService.class);
        }

        // Get the data
        String sessionIdToKeep = session.getSessionID();
        Collection<ManagedSession> userSessions = service.getSessionsForUser(session);

        // Remove all sessions except blacklisted and transmitted to keep
        for (ManagedSession mSession : userSessions) {
            String mSessionId = mSession.getSessionId();
            if (false == sessionIdToKeep.equals(mSessionId)) {
                service.removeSession(session, mSessionId);
            }
        }
        return new AJAXRequestResult();
    }
}
