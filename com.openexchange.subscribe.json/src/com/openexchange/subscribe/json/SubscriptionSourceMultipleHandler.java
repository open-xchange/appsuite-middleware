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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.subscribe.json;

import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_PARAMETER;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.UNKNOWN_ACTION;
import static com.openexchange.subscribe.json.MultipleHandlerTools.response;
import static com.openexchange.subscribe.json.MultipleHandlerTools.wrapThrowable;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SubscriptionSourceMultipleHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SubscriptionSourceMultipleHandler implements MultipleHandler {

    private SubscriptionSourceDiscoveryService discoverer;
    private SubscriptionSourceJSONWriterInterface writer = new SubscriptionSourceJSONWriter();
    
    private SubscriptionSourceMultipleHandler(SubscriptionSourceDiscoveryService discoverer) {
        super();
        this.discoverer = discoverer;
    }

    public void close() {

    }

    public Date getTimestamp() {
        return null;
    }

    public JSONValue performRequest(String action, JSONObject request, ServerSession session) throws AbstractOXException, JSONException {
        try {
            if(null == action) {
                MISSING_PARAMETER.throwException("action");
                return null;
            } else if(action.equals("listSources") || action.equals("all")) {
                return listSources(request, session);
            } else if (action.equals("getSource") || action.equals("get")) {
                return getSource(request, session);
            } else {
                UNKNOWN_ACTION.throwException(action);
                return null;
            }
        } catch (AbstractOXException x) {
            throw x;
        } catch (JSONException x) {
            throw x;
        } catch (Throwable t) {
            throw wrapThrowable(t);
        }
    }

    protected JSONValue listSources(JSONObject req, ServerSession session) throws AbstractOXException, JSONException  {
        int module = getModule(req);
        List<SubscriptionSource> sources = discoverer.getSources(module);
        String[] columns = getColumns(req);
        JSONArray json = writer.writeJSONArray(sources, columns);
        return response(json);
    }
    
    private String[] getColumns(JSONObject req) {
        String columns = req.optString("columns");
        if(columns == null) {
            return new String[]{"id", "displayName", "module", "icon",  "formDescription"};
        }
        return columns.split("\\s*,\\s*"); 
    }

    protected JSONValue getSource(JSONObject req, ServerSession session) throws AbstractOXException, JSONException {
        String identifier = req.getString("id");
        if(identifier == null) {
            MISSING_PARAMETER.throwException("id");
        }
        SubscriptionSource source = discoverer.getSource(identifier);
        JSONObject data = writer.writeJSON(source);
        return response(data);
    }
    
    protected int getModule(JSONObject req) throws AbstractOXException {
        String moduleAsString = req.optString("module");
        if(moduleAsString == null) {
            return -1;
        }
        if(moduleAsString.equals("contacts")) {
            return FolderObject.CONTACT;
        } else if (moduleAsString.equals("calendar")) {
            return FolderObject.CALENDAR;
        } else if (moduleAsString.equals("tasks")) {
            return FolderObject.TASK;
        } else if (moduleAsString.equals("infostore")) {
            return FolderObject.INFOSTORE;
        }
        return -1;
    }


}
