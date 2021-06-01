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
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.ResourceAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;


/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetAction extends AbstractResourceAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(GetAction.class);

    /**
     * Initializes a new {@link GetAction}.
     * @param services
     */
    public GetAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        final int id = req.checkInt(AJAXServlet.PARAMETER_ID);
        final ServerSession session = req.getSession();
        com.openexchange.resource.Resource r = null;
        try {
            r = services.getServiceSafe(ResourceService.class).getResource(id, session.getContext());
        } catch (OXException exc) {
            LOG.debug("resource not found try to find id in user table", exc);
        }

        if (r == null) {
            final User u = UserStorage.getInstance().getUser(id, session.getContext());

            r = new com.openexchange.resource.Resource();
            r.setIdentifier(u.getId());
            r.setDisplayName(u.getDisplayName());
            r.setLastModified(new Date(0));
        }
        return new AJAXRequestResult(r, r.getLastModified(), "resource");


    }

}
