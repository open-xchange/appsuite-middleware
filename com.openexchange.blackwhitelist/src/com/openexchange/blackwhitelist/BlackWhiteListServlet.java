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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.blackwhitelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.blackwhitelist.osgi.ServletServiceRegistry;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BlackWhiteListServlet}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class BlackWhiteListServlet extends DataServlet {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory.getLog(BlackWhiteListServlet.class);

    private static final String ADD = "add";

    private static final String DELETE = "delete";

    private static final String GET = "get";

    @Override
    protected boolean hasModulePermission(ServerSession session) {
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Response response = new Response();

        try {
            String action = parseMandatoryStringParameter(req, PARAMETER_ACTION);
            String module = parseMandatoryStringParameter(req, PARAMETER_MODULE);

            ListType type = null;
            if (module.equalsIgnoreCase("blacklist")) {
                type = ListType.black;
            } else if (module.equalsIgnoreCase("whitelist")) {
                type = ListType.white;
            } else {
                throw new AjaxException(AjaxException.Code.InvalidParameterValue, PARAMETER_MODULE, module);
            }
            
            ServerSession session = getSessionObject(req);

            if (action.equalsIgnoreCase(ADD)) {
                doAdd(session, type, req, response);
            } else if (action.equalsIgnoreCase(DELETE)) {
                doDelete(session, type, req, response);
            } else if (action.equalsIgnoreCase(GET)) {
                doGet(session, type, response);
            } else {
                throw new AjaxException(AjaxException.Code.InvalidParameterValue, PARAMETER_ACTION, action);
            }
        } catch (AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (JSONException e) {
            LOG.error(e.getMessage(), e);
            response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
        }
        writeResponse(response, resp);
    }

    private void doGet(ServerSession session, ListType type, Response response) throws JSONException, AbstractOXException {
        BlackWhiteListInterface bwService = ServletServiceRegistry.getInstance().getService(BlackWhiteListInterface.class);

        List<String> list = bwService.getList(session, type);

        JSONArray items = new JSONArray();
        for (String item : list) {
            items.put(item);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("items", items);

        response.setData(jsonObject);
    }

    private void doDelete(ServerSession session, ListType type, HttpServletRequest req, Response response) throws JSONException, AbstractOXException {
        JSONObject jsonObject = convertParameter2JSONObject(req);

        String data = jsonObject.getString("data");
        List<String> entries = new ArrayList<String>();
        entries.add(data);

        BlackWhiteListInterface bwService = ServletServiceRegistry.getInstance().getService(BlackWhiteListInterface.class);

        bwService.removeListEntries(session, type, entries);

        response.setData("ok");
    }

    private void doAdd(ServerSession session, ListType type, HttpServletRequest req, Response response) throws JSONException, AbstractOXException {
        JSONObject jsonObject = convertParameter2JSONObject(req);

        String data = jsonObject.getString("data");
        List<String> entries = new ArrayList<String>();
        entries.add(data);

        BlackWhiteListInterface bwService = ServletServiceRegistry.getInstance().getService(BlackWhiteListInterface.class);

        bwService.addListEntries(session, type, entries);

        response.setData("ok");
    }
}
