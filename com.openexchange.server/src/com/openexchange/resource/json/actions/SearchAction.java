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

package com.openexchange.resource.json.actions;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.ResourceAJAXRequest;
import com.openexchange.server.ServiceLookup;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchAction extends AbstractResourceAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchAction.class);

    /**
     * Initializes a new {@link SearchAction}.
     */
    public SearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        ResourceService resourceService = services.getServiceSafe(ResourceService.class);

        if (!req.getSession().getUserPermissionBits().hasGroupware()) {
            return new AJAXRequestResult(new JSONArray(0), "json");
        }

        // Appropriate permission is granted
        // Continue processing search request

        JSONObject jData = req.getData();
        if (!jData.hasAndNotNull(SearchFields.PATTERN)) {
            LOG.warn("Missing field \"{}\" in JSON data. Searching for all as fallback", SearchFields.PATTERN);
            return new AllAction(services).perform(req);
        }

        String searchpattern = jData.getString(SearchFields.PATTERN);
        Resource[] resources = resourceService.searchResources(req.getSession(), searchpattern);
        Date timestamp;
        List<Resource> list = new LinkedList<Resource>();

        if (resources.length > 0) {
            long lastModified = Long.MIN_VALUE;
            for (final com.openexchange.resource.Resource resource : resources) {
                if (lastModified < resource.getLastModified().getTime()) {
                    lastModified = resource.getLastModified().getTime();
                }
                list.add(resource);
            }
            timestamp = new Date(lastModified);
        } else {
            timestamp = new Date(0);
        }

        return new AJAXRequestResult(list, timestamp, "resource");
    }

}
