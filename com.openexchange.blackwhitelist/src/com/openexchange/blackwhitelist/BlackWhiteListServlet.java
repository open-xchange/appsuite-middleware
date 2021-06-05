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
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BlackWhiteListServlet}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class BlackWhiteListServlet extends DataServlet {

    /**
     *
     */
    private static final long serialVersionUID = -929748663411398165L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BlackWhiteListServlet.class);

    private static final String ADD = "add";

    private static final String DELETE = "delete";

    private static final String GET = "get";

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        doGet(req, resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Response response = new Response();
        final ServerSession session = getSessionObject(req);

        try {
            final String action = parseMandatoryStringParameter(req, PARAMETER_ACTION);
            final String module = parseMandatoryStringParameter(req, PARAMETER_MODULE);

            ListType type = null;
            if (module.equalsIgnoreCase("blacklist")) {
                type = ListType.black;
            } else if (module.equalsIgnoreCase("whitelist")) {
                type = ListType.white;
            } else {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAMETER_MODULE, module);
            }


            if (action.equalsIgnoreCase(ADD)) {
                doAdd(session, type, req, response);
            } else if (action.equalsIgnoreCase(DELETE)) {
                doDelete(session, type, req, response);
            } else if (action.equalsIgnoreCase(GET)) {
                doGet(session, type, response);
            } else {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(PARAMETER_ACTION, action);
            }
        } catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (JSONException e) {
            LOG.error("", e);
            response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
        }
        writeResponse(response, resp, session);
    }

    private void doGet(final ServerSession session, final ListType type, final Response response) throws JSONException, OXException {
        final BlackWhiteListInterface bwService = ServletServiceRegistry.getInstance().getService(BlackWhiteListInterface.class);

        final List<String> list = bwService.getList(session, type);

        final JSONArray items = new JSONArray();
        for (final String item : list) {
            items.put(item);
        }
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("items", items);

        response.setData(jsonObject);
    }

    private void doDelete(final ServerSession session, final ListType type, final HttpServletRequest req, final Response response) throws JSONException, OXException {
        final JSONObject jsonObject = convertParameter2JSONObject(req);

        final String data = jsonObject.getString("data");
        final List<String> entries = new ArrayList<String>();
        entries.add(data);

        final BlackWhiteListInterface bwService = ServletServiceRegistry.getInstance().getService(BlackWhiteListInterface.class);

        bwService.removeListEntries(session, type, entries);

        response.setData("ok");
    }

    private void doAdd(final ServerSession session, final ListType type, final HttpServletRequest req, final Response response) throws JSONException, OXException {
        final JSONObject jsonObject = convertParameter2JSONObject(req);

        final String data = jsonObject.getString("data");
        final List<String> entries = new ArrayList<String>();
        entries.add(data);

        final BlackWhiteListInterface bwService = ServletServiceRegistry.getInstance().getService(BlackWhiteListInterface.class);

        bwService.addListEntries(session, type, entries);

        response.setData("ok");
    }
}
