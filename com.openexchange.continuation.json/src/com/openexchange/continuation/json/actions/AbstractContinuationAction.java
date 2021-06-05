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

package com.openexchange.continuation.json.actions;

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.continuation.json.ContinuationRequest;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractContinuationAction} - The abstract continuation action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public abstract class AbstractContinuationAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractContinuationAction}.
     */
    protected AbstractContinuationAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        if (checkCapability() && !hasContinuationCapability(session)) {
            throw OXException.noPermissionForModule("continuation");
        }
        try {
            return perform(new ContinuationRequest(requestData, session));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private boolean hasContinuationCapability(final ServerSession session) throws OXException {
        final CapabilityService capabilityService = getService(CapabilityService.class);
        return (null != capabilityService && capabilityService.getCapabilities(session).contains("continuation"));
    }

    /**
     * Checks if capability should be checked prior to serving action.
     *
     * @return <code>true</code> to check; otherwise <code>false</code>
     */
    protected boolean checkCapability() {
        return true;
    }

    /**
     * Performs specified continuationRequest request.
     *
     * @param req The continuationRequest request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(ContinuationRequest req) throws OXException, JSONException;

}
