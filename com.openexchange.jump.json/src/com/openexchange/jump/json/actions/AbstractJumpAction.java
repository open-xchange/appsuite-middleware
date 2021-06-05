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

package com.openexchange.jump.json.actions;

import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.jump.JumpService;
import com.openexchange.jump.json.EndpointHandlerRegistry;
import com.openexchange.jump.json.JumpRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractJumpAction} - The abstract jump action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractJumpAction implements AJAXActionService {

    private static final AtomicReference<EndpointHandlerRegistry> HANDLERS_REF = new AtomicReference<EndpointHandlerRegistry>();

    /**
     * Sets the end-point handler registry.
     *
     * @param registry The registry
     */
    public static void setEndpointHandlerRegistry(final EndpointHandlerRegistry registry) {
        HANDLERS_REF.set(registry);
    }

    /**
     * Gets the end-point handler registry.
     *
     * @return The registry
     */
    protected static EndpointHandlerRegistry getEndpointHandlerRegistry() {
        return HANDLERS_REF.get();
    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractJumpAction.class);

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractJumpAction}.
     */
    protected AbstractJumpAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            return perform(new JumpRequest(requestData, session));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Gets the jump service.
     *
     * @return The jump serivce
     * @throws OXException If jump servive is absent
     */
    protected JumpService getJumpSerivce() throws OXException {
        final JumpService jumpService = services.getOptionalService(JumpService.class);
        if (null == jumpService) {
            throw ServiceExceptionCode.absentService(JumpService.class);
        }
        return jumpService;
    }

    /**
     * Performs given request.
     *
     * @param request The request
     * @return The result
     * @throws OXException If an Open-Xchange error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(JumpRequest request) throws OXException, JSONException;

}
