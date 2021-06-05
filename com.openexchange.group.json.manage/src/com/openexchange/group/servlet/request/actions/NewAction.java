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

package com.openexchange.group.servlet.request.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.GroupFields;
import com.openexchange.ajax.parser.GroupParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.group.servlet.request.GroupAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NewAction extends AbstractGroupAction {

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param services
     */
    public NewAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final GroupAJAXRequest req) throws OXException, JSONException {
        final Group group = new Group();
        final JSONObject jsonobject = req.getData();
        final GroupParser groupParser = new GroupParser();
        groupParser.parse(group, jsonobject);
        final GroupService groupService = getService(GroupService.class);
        final ServerSession session = req.getSession();
        groupService.create(session.getContext(), session.getUser(), group, true);
        final JSONObject response = new JSONObject();
        response.put(GroupFields.IDENTIFIER, group.getIdentifier());
        return new AJAXRequestResult(response, group.getLastModified(), "json");
    }

}
