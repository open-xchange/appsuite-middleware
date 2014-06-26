/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.group.json.actions;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.GroupWriter;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.group.json.GroupAJAXRequest;
import com.openexchange.server.ServiceLookup;


/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "updates", description = "Get updates (since v6.18.1)", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "timestamp", description = "Timestamp of the last update of the requested groups.")
}, responseDescription = "Response with timestamp: An array with new, modified and deleted groups. New, modified and deleted groups are represented by JSON objects as described in Group data.")
public final class UpdatesAction extends AbstractGroupAction {

    /**
     * Initializes a new {@link UpdatesAction}.
     * @param services
     */
    public UpdatesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final GroupAJAXRequest req) throws OXException, JSONException {
        Date timestamp = new Date(0);
        final GroupStorage groupStorage = GroupStorage.getInstance();
        final Date modifiedSince = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);

        final Group[] modifiedGroups = groupStorage.listModifiedGroups(modifiedSince, req.getSession().getContext());
        final Group[] deletedGroups = groupStorage.listDeletedGroups(modifiedSince, req.getSession().getContext());
        final GroupWriter groupWriter = new GroupWriter();
        final JSONArray modified = new JSONArray();
        final JSONArray deleted= new JSONArray();

        long lm = 0;
        for(final Group group: modifiedGroups){
            final JSONObject temp = new JSONObject();
            groupWriter.writeGroup(group, temp);
            modified.put(temp);
            lm = group.getLastModified().getTime() > lm ? group.getLastModified().getTime() : lm;
        }
        for(final Group group: deletedGroups){
            final JSONObject temp = new JSONObject();
            groupWriter.writeGroup(group, temp);
            deleted.put(temp);
            lm = group.getLastModified().getTime() > lm ? group.getLastModified().getTime() : lm;
        }
        timestamp = new Date(lm);
        final JSONObject retVal = new JSONObject();

        retVal.put("new", modified);
        retVal.put("modified", modified);
        retVal.put("deleted", deleted);

        return new AJAXRequestResult(retVal, timestamp, "json");
    }

}
