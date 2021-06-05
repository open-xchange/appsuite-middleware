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

package com.openexchange.authentication.application.impl.api;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.authentication.application.AppPasswordService;
import com.openexchange.authentication.application.AppPasswordUtils;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractAppPasswordAction}
 * Abstract ApplicationPassword action
 * Does security checks/requirements before performing action
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
@RestrictedAction(module = RestrictedAction.REQUIRES_FULL_AUTH)
public abstract class AbstractAppPasswordAction implements AJAXActionService {

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractAppPasswordAction}.
     *
     * @param services The service lookup
     */
    public AbstractAppPasswordAction(ServiceLookup services) {
        this.services = services;
    }

    /**
     * Gets the service from registry
     *
     * @return AppPasswordService
     * @throws OXException if the service is absent
     */
    protected AppPasswordService getService() throws OXException {
        return services.getServiceSafe(AppPasswordService.class);
    }

    /**
     * Verify the login has sufficient permission
     * Will not allow an already restricted session to change passwords
     *
     * @param session The seission
     * @throws OXException if the specified session is not authorised
     */
    protected static void checkPermission(Session session) throws OXException {
        if (AppPasswordUtils.isRestricted(session)) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create();
        }
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        checkPermission(session);
        return doPerform(requestData, session);
    }

    /**
     * Perform the action after security check was done
     *
     * @param requestData The {@link AJAXRequestData}x
     * @param session The session
     * @return The {@link AJAXRequestResult}
     * @throws OXException if an error is occurred
     */
    protected abstract AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException;

}
