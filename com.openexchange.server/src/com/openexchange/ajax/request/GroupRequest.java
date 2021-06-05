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
import com.openexchange.group.GroupService;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerRequestHandlerRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
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
        final GroupService groupService = ServerServiceRegistry.getServize(GroupService.class, true);
        final Date modifiedSince = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_TIMESTAMP);

        final Group[] modifiedGroups = groupService.listModifiedGroups(session.getContext(), modifiedSince);
        final Group[] deletedGroups = groupService.listDeletedGroups(session.getContext(), modifiedSince);
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
        final GroupService groupService = ServerServiceRegistry.getServize(GroupService.class, true);
        final GroupWriter groupWriter = new GroupWriter();
        for (int a = 0; a < jsonArray.length(); a++) {
            final JSONObject jData = jsonArray.getJSONObject(a);
            final Group group = groupService.getGroup(session.getContext(), DataParser.checkInt(jData, DataFields.ID));
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
        final GroupService groupService = ServerServiceRegistry.getServize(GroupService.class, true);
        final Group group = groupService.getGroup(session.getContext(), groupId);
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
        final GroupService groupService = ServerServiceRegistry.getServize(GroupService.class, true);
        Group[] groups = null;
        if ("*".equals(searchpattern)) {
            groups = groupService.getGroups(session, true);
        } else {
            groups = groupService.searchGroups(session, searchpattern, true);
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

    public JSONArray actionAll(final JSONObject jsonObj) throws OXException {
        timestamp = new Date(0);

        final String[] sColumns = Strings.splitByComma(DataParser.checkString(jsonObj, AJAXServlet.PARAMETER_COLUMNS));
        final int[] columns = StringCollection.convertStringArray2IntArray(sColumns);
        boolean loadMembers = false;
        final List<Field> fields = new LinkedList<Field>();
        for(final int column: columns){
        	final Field field = Group.Field.getByColumnNumber(column);
        	if (field == Group.Field.MEMBERS){
        		loadMembers = true;
        	}
        	fields.add(field);
        }


        final JSONArray jsonResponseArray = new JSONArray();
        final GroupService groupService = ServerServiceRegistry.getServize(GroupService.class, true);
        Group[] groups = null;
        groups = groupService.getGroups(session, loadMembers);
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
