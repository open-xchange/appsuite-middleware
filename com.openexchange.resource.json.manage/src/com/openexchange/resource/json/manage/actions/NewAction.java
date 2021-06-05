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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.ResourceFields;
import com.openexchange.resource.json.manage.request.ResourceAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NewAction extends AbstractResourceAction {

    /**
     * Initializes a new {@link NewAction}.
     * @param services
     */
    public NewAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        /*
         * Check for "data"
         */
        final JSONObject jData = req.getData();
        /*
         * Parse resource out of JSON object
         */
        final com.openexchange.resource.Resource resource = com.openexchange.resource.json.ResourceParser.parseResource(jData);
        /*
         * Create new resource
         */
        final ResourceService resourceService = getService(ResourceService.class);
        if (null == resourceService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( ResourceService.class.getName());
        }
        final ServerSession session = req.getSession();
        resourceService.create(session.getUser(), session.getContext(), resource);
        /*
         * Return its ID wrapped by a JSON object
         */
        final JSONObject resultObject = new JSONObject();
        resultObject.put(ResourceFields.ID, resource.getIdentifier());
        return new AJAXRequestResult(resultObject, resource.getLastModified(), "json");
    }

}
