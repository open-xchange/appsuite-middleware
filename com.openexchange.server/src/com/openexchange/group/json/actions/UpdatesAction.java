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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.group.json.GroupAJAXRequest;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdatesAction extends AbstractGroupAction {

    /**
     * Initializes a new {@link UpdatesAction}.
     * @param services
     */
    public UpdatesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final GroupAJAXRequest req) throws OXException {
        GroupService groupService = ServerServiceRegistry.getServize(GroupService.class, true);
        Date modifiedSince = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);
        Group[] modifiedGroups = groupService.listModifiedGroups(req.getSession().getContext(), modifiedSince);
        Group[] deletedGroups = groupService.listDeletedGroups(req.getSession().getContext(), modifiedSince);

        List<Group> modified = new LinkedList<Group>();
        List<Group> deleted= new LinkedList<Group>();

        long lm = 0;
        for (Group group: modifiedGroups) {
            modified.add(group);
            lm = group.getLastModified().getTime() > lm ? group.getLastModified().getTime() : lm;
        }
        for (Group group: deletedGroups){
            deleted.add(group);
            lm = group.getLastModified().getTime() > lm ? group.getLastModified().getTime() : lm;
        }

        return new AJAXRequestResult(new CollectionDelta<Group>(modified, deleted), new Date(lm), "group");
    }

}
