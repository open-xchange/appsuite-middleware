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



package com.openexchange.custom.parallels.impl;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * Servlet which manages the POA Black/White Lists for an OX User.
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 *
 */
public final class ParallelsOpenApiServlet extends DataServlet {

    /**
     *
     */
    private static final long serialVersionUID = 7650360590998502303L;
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ParallelsOpenApiServlet.class);

    public ParallelsOpenApiServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }


    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
    throws ServletException, IOException {
        super.doPost(req, resp);
        doGet(req,resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException,
        IOException {

        final Response response = new Response();
        final Session session = getSessionObject(req);

        try {

            final String action = parseMandatoryStringParameter(req,PARAMETER_ACTION);
            final String module = parseMandatoryStringParameter(req,PARAMETER_MODULE);
            JSONObject jsonObj;

            try {
                jsonObj = convertParameter2JSONObject(req);
            } catch (JSONException e) {
                LOG.error("", e);
                response.setException(OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e));
                writeResponse(response, resp, session);
                return;
            }
            final Context ctx = ContextStorage.getInstance().getContext(session);
            final ParallelsOpenApiServletRequest proRequest = new ParallelsOpenApiServletRequest(session, ctx);
            final Object responseObj = proRequest.action(action,module, jsonObj);
            response.setData(responseObj);

        } catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        }

        writeResponse(response, resp, session);

    }

}
