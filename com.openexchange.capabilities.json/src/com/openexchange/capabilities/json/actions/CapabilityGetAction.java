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

package com.openexchange.capabilities.json.actions;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CapabilityGetAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@DispatcherNotes(noSession = true)
public class CapabilityGetAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CapabilityGetAction}.
     *
     * @param services The service look-up
     */
    public CapabilityGetAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        // Get capability
        String id = requestData.checkParameter("id");
        CapabilityService capabilityService = services.getService(CapabilityService.class);
        CapabilitySet capabilities;
        if (session == null || session.isAnonymous()) {
            capabilities = capabilityService.getCapabilities(-1, -1, true, true);
        } else {
            capabilities = capabilityService.getCapabilities(session, true);
        }
        Capability capability = null != capabilities ? capabilities.get(id) : null;
        return null == capability ? new AJAXRequestResult() : new AJAXRequestResult(capability, "capability");
    }

}
