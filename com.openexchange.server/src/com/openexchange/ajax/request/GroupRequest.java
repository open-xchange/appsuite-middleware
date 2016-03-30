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

package com.openexchange.ajax.request;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_ID;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestHandler;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.GroupWriter;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.Group.Field;
import com.openexchange.group.GroupStorage;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerRequestHandlerRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * Implements all possible request types for groups.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GroupRequest {

    private final ServerSession session;

    private Date timestamp;

    public GroupRequest(final ServerSession session) {
        super();
        this.session = session;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    private static final String MODULE_GROUP = "group";

    public Object action(final String action, final JSONObject jsonObject) throws OXException, JSONException {
        JSONValue retval = null;
        if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
            retval = actionList(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
            retval = actionGet(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
            retval = actionSearch(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
            retval = actionAll(jsonObject);
        } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
            retval = actionUpdates(jsonObject);
        } else {
            /*
             * Look-up manage request
             */
            final AJAXRequestHandler handler = ServerRequestHandlerRegistry.getInstance().getHandler(MODULE_GROUP, action);
            if (null == handler) {
                /*
                 * No appropriate handler
                 */
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
            }
            /*
             * ... and delegate to manage request
             */
            final AJAXRequestResult result = handler.performAction(action, jsonObject, session, session.getContext());
            timestamp = result.getTimestamp();
            return result.getResultObject();
        }
        return retval;
    }


    public JSONValue actionUpdates(final JSONObject jsonObject) throws JSONException, OXException {
        timestamp = new Date(0);
        final GroupStorage groupStorage = GroupStorage.getInstance();
        final Date modifiedSince = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_TIMESTAMP);

        final Group[] modifiedGroups = groupStorage.listModifiedGroups(modifiedSince, session.getContext());
        final Group[] deletedGroups = groupStorage.listDeletedGroups(modifiedSince, session.getContext());
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

        return retVal;
    }

    public JSONArray actionList(final JSONObject jsonObj) throws JSONException, OXException {
        final JSONArray jsonArray = DataParser.checkJSONArray(jsonObj, "data");
        timestamp = new Date(0);
        Date lastModified = null;
        final JSONArray jsonResponseArray = new JSONArray();
        final GroupStorage groupStorage = GroupStorage.getInstance();
        final GroupWriter groupWriter = new GroupWriter();
        for (int a = 0; a < jsonArray.length(); a++) {
            final JSONObject jData = jsonArray.getJSONObject(a);
            final Group group = groupStorage.getGroup(DataParser.checkInt(jData, DataFields.ID), session.getContext());
            final JSONObject jsonGroupObj = new JSONObject();
            groupWriter.writeGroup(group, jsonGroupObj);
            jsonResponseArray.put(jsonGroupObj);
            lastModified = group.getLastModified();
            if (timestamp.getTime() < lastModified.getTime()) {
                timestamp = lastModified;
            }
        }
        return jsonResponseArray;
    }

    public JSONObject actionGet(final JSONObject json) throws JSONException, OXException, OXException, OXException {
        final int groupId = DataParser.checkInt(json, PARAMETER_ID);
        timestamp = new Date(0);
        final GroupStorage groupStorage = GroupStorage.getInstance();
        final Group group = groupStorage.getGroup(groupId, session.getContext());
        final GroupWriter groupWriter = new GroupWriter();
        final JSONObject retval = new JSONObject();
        groupWriter.writeGroup(group, retval);
        timestamp = group.getLastModified();
        return retval;
    }

    public JSONArray actionSearch(final JSONObject jsonObj) throws JSONException, OXException {
        final JSONObject jData = DataParser.checkJSONObject(jsonObj, "data");

        String searchpattern = null;
        if (jData.has(SearchFields.PATTERN)) {
            searchpattern = DataParser.parseString(jData, SearchFields.PATTERN);
        }

        timestamp = new Date(0);
        final JSONArray jsonResponseArray = new JSONArray();
        final GroupStorage groupStorage = GroupStorage.getInstance();
        Group[] groups = null;
        if ("*".equals(searchpattern)) {
            groups = groupStorage.getGroups(true, session.getContext());
        } else {
            groups = groupStorage.searchGroups(searchpattern, true, session.getContext());
        }
        final GroupWriter groupWriter = new GroupWriter();
        for (int a = 0; a < groups.length; a++) {
            final JSONObject jsonGroupObj = new JSONObject();
            groupWriter.writeGroup(groups[a], jsonGroupObj);
            if (groups[a].getLastModified().after(timestamp)) {
                timestamp = groups[a].getLastModified();
            }
            jsonResponseArray.put(jsonGroupObj);
        }
        return jsonResponseArray;
    }

    public JSONArray actionAll(final JSONObject jsonObj) throws JSONException, OXException {
        timestamp = new Date(0);

        final String[] sColumns = Strings.splitByComma(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        boolean loadMembers = false;
        final List<Field> fields = new LinkedList<Field>();
        for(final int column: columns){
        	final Field field = Group.Field.getByColumnNumber(column);
        	if(field == Group.Field.MEMBERS){
        		loadMembers = true;
        	}
        	fields.add(field);
        }


        final JSONArray jsonResponseArray = new JSONArray();
        final GroupStorage groupStorage = GroupStorage.getInstance();
        Group[] groups = null;
        groups = groupStorage.getGroups(loadMembers, session.getContext());
        final GroupWriter groupWriter = new GroupWriter();
        for (int a = 0; a < groups.length; a++) {
            final JSONArray row = new JSONArray();
            groupWriter.writeArray(groups[a], row, fields);
            if (groups[a].getLastModified().after(timestamp)) {
                timestamp = groups[a].getLastModified();
            }
            jsonResponseArray.put(row);
        }
        return jsonResponseArray;
    }
}
