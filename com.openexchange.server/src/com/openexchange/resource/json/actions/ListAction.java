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
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.ResourceAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;


/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ListAction extends AbstractResourceAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(ListAction.class);

    /**
     * Initializes a new {@link ListAction}.
     * @param services
     */
    public ListAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        UserStorage userStorage = null;

        JSONArray jsonArray = req.getData();
        int len = jsonArray.length();
        Date timestamp;
        List<Resource> resources = new LinkedList<Resource>();

        if (len > 0) {
            long lastModified = Long.MIN_VALUE;
            ServerSession session = req.getSession();

            for (int a = 0; a < len; a++) {
                final JSONObject jData = jsonArray.getJSONObject(a);
                final int id = DataParser.checkInt(jData, DataFields.ID);
                com.openexchange.resource.Resource r = null;

                try {
                    r = services.getServiceSafe(ResourceService.class).getResource(id, session.getContext());
                } catch (OXException exc) {
                    LOG.debug("resource not found try to find id in user table", exc);
                }

                if (r == null) {
                    if (userStorage == null) {
                        userStorage = UserStorage.getInstance();
                    }

                    final User u = userStorage.getUser(id, session.getContext());

                    r = new com.openexchange.resource.Resource();
                    r.setIdentifier(u.getId());
                    r.setDisplayName(u.getDisplayName());
                    r.setLastModified(new Date(0));
                }

                if (lastModified < r.getLastModified().getTime()) {
                    lastModified = r.getLastModified().getTime();
                }

                resources.add(r);
            }
            timestamp = new Date(lastModified);
        } else {
            timestamp = new Date(0);
        }

        return new AJAXRequestResult(resources, timestamp, "resource");
    }

}
