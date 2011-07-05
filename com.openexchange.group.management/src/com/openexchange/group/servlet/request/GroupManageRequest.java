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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.group.servlet.request;

import static com.openexchange.group.servlet.services.GroupRequestServiceRegistry.getServiceRegistry;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.GroupFields;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.GroupParser;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.UserService;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class GroupManageRequest implements AJAXRequestHandler {

    private static final String MODULE_GROUP = "group";

    private static final Set<String> ACTIONS;

    /**
     * Default constructor.
     */
    public GroupManageRequest() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getModule() {
        return MODULE_GROUP;
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getSupportedActions() {
        return ACTIONS;
    }

    /**
     * {@inheritDoc}
     */
    public AJAXRequestResult performAction(final String action, final JSONObject json, final Session session, final Context ctx) throws OXException, JSONException {
        final AJAXRequestResult retval;
        final UserService userService = getServiceRegistry().getService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
        }
        {
            final User user = userService.getUser(session.getUserId(), ctx);
            if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW)) {
                retval = actionNew(ctx, user, json);
            } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
                retval = actionDelete(ctx, user, json);
            } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
                retval = actionUpdate(ctx, user, json);
            } else {
                throw AjaxExceptionCodes.UnknownAction.create( action);
            }
            return retval;
        }
    }

    private AJAXRequestResult actionNew(final Context ctx, final User user, final JSONObject json) throws OXException, JSONException {
        final Group group = new Group();
        final JSONObject jsonobject = DataParser.checkJSONObject(json, ResponseFields.DATA);
        final GroupParser groupParser = new GroupParser();
        groupParser.parse(group, jsonobject);
        final GroupService groupService = getServiceRegistry().getService(GroupService.class);
        groupService.create(ctx, user, group);
        final JSONObject response = new JSONObject();
        response.put(GroupFields.IDENTIFIER, group.getIdentifier());
        return new AJAXRequestResult(response, group.getLastModified());
    }

    private AJAXRequestResult actionDelete(final Context ctx, final User user, final JSONObject json) throws OXException {
        final JSONObject jsonobject = DataParser.checkJSONObject(json, ResponseFields.DATA);
        final int groupId = DataParser.checkInt(jsonobject, AJAXServlet.PARAMETER_ID);
        final Date timestamp = DataParser.checkDate(json, AJAXServlet.PARAMETER_TIMESTAMP);
        final GroupService groupService = getServiceRegistry().getService(GroupService.class);
        groupService.delete(ctx, user, groupId, timestamp);
        return new AJAXRequestResult(new JSONArray(), timestamp);
    }

    private AJAXRequestResult actionUpdate(final Context ctx, final User user, final JSONObject json) throws OXException, JSONException {
        final int identifier = DataParser.checkInt(json, AJAXServlet.PARAMETER_ID);
        final Date timestamp = DataParser.checkDate(json, AJAXServlet.PARAMETER_TIMESTAMP);
        final JSONObject data = DataParser.checkJSONObject(json, ResponseFields.DATA);
        final Group group = new Group();
        final GroupParser groupParser = new GroupParser();
        groupParser.parse(group, data);
        group.setIdentifier(identifier);
        final GroupService groupService = getServiceRegistry().getService(GroupService.class);
        groupService.update(ctx, user, group, timestamp);
        return new AJAXRequestResult(new JSONObject(), group.getLastModified());
    }

    static {
        final Set<String> tmp = new HashSet<String>(3, 1);
        tmp.add(AJAXServlet.ACTION_NEW);
        tmp.add(AJAXServlet.ACTION_UPDATE);
        tmp.add(AJAXServlet.ACTION_DELETE);
        ACTIONS = Collections.unmodifiableSet(tmp);
    }
}
