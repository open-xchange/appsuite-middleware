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

package com.openexchange.group.json.actions;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.group.json.GroupAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchAction extends AbstractGroupAction {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchAction.class);

    /**
     * Initializes a new {@link SearchAction}.
     * 
     * @param services
     */
    public SearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final GroupAJAXRequest req) throws OXException {
        if (!req.getSession().getUserPermissionBits().hasGroupware()) {
            return new AJAXRequestResult(new JSONArray(0), "json");
        }

        Group[] groups;
        {
            JSONObject jData = req.getData();
            if (!jData.hasAndNotNull(SearchFields.PATTERN)) {
                LOG.warn("Missing field \"{}\" in JSON data. Searching for all as fallback", SearchFields.PATTERN);
                return new com.openexchange.group.json.actions.AllAction(services).perform(req);
            }

            String searchpattern = DataParser.parseString(jData, SearchFields.PATTERN);
            ServerSession session = req.getSession();
            GroupService servize = ServerServiceRegistry.getServize(GroupService.class, true);
            if ("*".equals(searchpattern)) {
                groups = servize.getGroups(session, true);
            } else {
                groups = servize.searchGroups(session, searchpattern, true);
            }
        }

        List<Group> groupList = new LinkedList<Group>();
        Date timestamp = new Date(0);
        for (int a = 0; a < groups.length; a++) {
            Group group = groups[a];
            groupList.add(group);

            Date lastModified = group.getLastModified();
            if (null != lastModified && lastModified.after(timestamp)) {
                timestamp = group.getLastModified();
            }
        }
        return new AJAXRequestResult(groupList, timestamp, "group");
    }
}
