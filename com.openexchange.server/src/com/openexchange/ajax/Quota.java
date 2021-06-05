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

package com.openexchange.ajax;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.request.QuotaRequest;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class Quota extends SessionServlet {

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Quota.class);

    private static final long serialVersionUID = 6477434510302882905L;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        final ServerSession session = getSessionObject(req);
        final Response response = new Response(session);
        try {
            final String action = req.getParameter(PARAMETER_ACTION);
            if (null == action) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_ACTION);
            }
            final QuotaRequest fsReq = new QuotaRequest(session);
            final JSONValue responseObj = fsReq.action(action);

            // response.setTimestamp(fsReq.getTimestamp());
            response.setData(responseObj);
        } catch (JSONException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                /*
                 * Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                 * throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                 */
                throw (IOException) cause;
            }
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error("", oje);
            response.setException(oje);
        } catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
        }
        writeResponse(response, res, session);
    }

}
