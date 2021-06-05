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
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.ResourceAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdatesAction extends AbstractResourceAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(UpdatesAction.class);

    /**
     * Initializes a new {@link UpdatesAction}.
     * @param services
     */
    public UpdatesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        Date lastModified = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        ServerSession session = req.getSession();

        Resource[] updatedResources = null;
        Resource[] deletedResources = null;
        try {
            ResourceService resService = services.getServiceSafe(ResourceService.class);
            updatedResources = resService .listModified(lastModified, session.getContext());
            deletedResources = resService.listDeleted(lastModified, session.getContext());
        } catch (OXException exc) {
            LOG.debug("Tried to find resources that were modified since {}", lastModified, exc);
        }

        List<Resource> modified = new LinkedList<Resource>();
        List<Resource> deleted= new LinkedList<Resource>();

        long lm = 0;
        if (updatedResources != null){
            for(final Resource res: updatedResources){
                if (res.getLastModified().getTime() > lm) {
                    lm = res.getLastModified().getTime();
                }
                modified.add(res);
            }
        }

        if (deletedResources != null){
            for(final Resource res: deletedResources){
                if (res.getLastModified().getTime() > lm) {
                    lm = res.getLastModified().getTime();
                }

                deleted.add(res);
            }
        }

        return new AJAXRequestResult(new CollectionDelta<Resource>(modified, deleted), new Date(lm), "resource");
    }

}
