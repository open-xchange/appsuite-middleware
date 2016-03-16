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
        } catch (final JSONException e) {
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
        resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, resp.getWriter());
        } catch (final JSONException e) {
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
