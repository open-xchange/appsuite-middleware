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

package com.openexchange.ajax.helper;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Contains methods for sending responses.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Send {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Send.class);

    /**
     * Prevent instantiation.
     */
    private Send() {
        super();
    }

    /**
     * Sends the GUI the html callback page if an upload has been done.
     * @param response Response to send to the GUI.
     * @param module Module string for the callback method.
     * @param resp http servlet response.
     * @throws IOException if sending fails in some way.
     */
    public static void sendCallbackResponse(final Response response, final String module, final HttpServletResponse resp) throws IOException {
        final AllocatingStringWriter sWriter = new AllocatingStringWriter();
        try {
            ResponseWriter.write(response, sWriter);
        } catch (JSONException e) {
            LOG.error("", e);
            sendError(resp);
        }
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(AJAXServlet.CONTENTTYPE_HTML);
		resp.getWriter().write(
				AJAXServlet.substituteJS(sWriter.toString(), module));
    }

    public static void sendResponse(final Response response, final HttpServletResponse resp) throws IOException {
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        AJAXServlet.setDefaultContentType(resp);
        try {
            ResponseWriter.write(response, resp.getWriter());
        } catch (JSONException e) {
            LOG.error("", e);
            sendError(resp);
        }
    }

    /**
     * Method for sending an internal server error. This should be only used if
     * everything else fails.
     * @param resp http servlet response.
     * @throws IOException if sending fails in some way.
     */
    public static void sendError(final HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
