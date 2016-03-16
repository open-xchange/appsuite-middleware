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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.group.json.GroupAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "search", description = "Search groups", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, requestBody = "An object with search parameters as described in Group search.",
responseDescription = "Response with timestamp: An array of group objects as described in Group data.")
public final class SearchAction extends AbstractGroupAction {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchAction.class);

    /**
     * Initializes a new {@link SearchAction}.
     * @param services
     */
    public SearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final GroupAJAXRequest req) throws OXException, JSONException {
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
            GroupStorage groupStorage = GroupStorage.getInstance();
            if ("*".equals(searchpattern)) {
                groups = groupStorage.getGroups(true, session.getContext());
            } else {
                groups = groupStorage.searchGroups(searchpattern, true, session.getContext());
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
