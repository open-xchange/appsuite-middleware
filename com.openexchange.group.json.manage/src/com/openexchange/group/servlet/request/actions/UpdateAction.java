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

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.GroupParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.group.servlet.request.GroupAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateAction extends AbstractGroupAction {

    /**
     * Initializes a new {@link UpdateAction}.
     *
     * @param services
     */
    public UpdateAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final GroupAJAXRequest req) throws OXException, JSONException {
        final int identifier = req.checkInt(AJAXServlet.PARAMETER_ID);
        final Date timestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject data = req.getData();
        final Group group = new Group();
        final GroupParser groupParser = new GroupParser();
        groupParser.parse(group, data);
        group.setIdentifier(identifier);
        final GroupService groupService = getService(GroupService.class);
        final ServerSession session = req.getSession();
        groupService.update(session.getContext(), session.getUser(), group, timestamp, true);
        return new AJAXRequestResult(new JSONObject(0), group.getLastModified(), "json");
    }

}
