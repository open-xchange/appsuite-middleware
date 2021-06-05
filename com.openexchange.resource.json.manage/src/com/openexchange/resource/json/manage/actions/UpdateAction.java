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

package com.openexchange.resource.json.manage.actions;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.manage.request.ResourceAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateAction extends AbstractResourceAction {

    /**
     * Initializes a new {@link UpdateAction}.
     * @param services
     */
    public UpdateAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        final ResourceService resourceService = getService(ResourceService.class);
        if (null == resourceService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( ResourceService.class.getName());
        }
        /*
         * Check for "data"
         */
        final int identifier = req.checkInt(AJAXServlet.PARAMETER_ID);
        final JSONObject jData = req.getData();
        final com.openexchange.resource.Resource resource = com.openexchange.resource.json.ResourceParser.parseResource(jData);
        resource.setIdentifier(identifier);
        final Date clientLastModified = req.getDate(AJAXServlet.PARAMETER_TIMESTAMP);
        /*
         * Update resource
         */
        final ServerSession session = req.getSession();
        resourceService.update(session.getUser(), session.getContext(), resource, clientLastModified);
        /*
         * Write empty JSON object
         */
        return new AJAXRequestResult(new JSONObject(0), resource.getLastModified(), "json");
    }

}
