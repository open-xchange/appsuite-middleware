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

package com.openexchange.ajax.requesthandler.converters;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.UnhandledException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.java.UnsynchronizedStringWriter;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DebugConverter} - Special converter that outputs request/response information as an HTML page.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DebugConverter implements ResultConverter {

    /**
     * Initializes a new {@link DebugConverter}.
     */
    public DebugConverter() {
        super();
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        StringBuilder out = new StringBuilder(1024);
        UnsynchronizedStringWriter writer = new UnsynchronizedStringWriter(out);

        out.append("<!DOCTYPE html><head><title>").append(escapeHtml(requestData.getAction(), writer)).append(" Response").append("</title></head>");
        out.append("<body><h1>Request with action ").append(escapeHtml(requestData.getAction(), writer)).append("</h1>");
        out.append("<h2>Parameters:</h2>");
        out.append("<table>");
        for (Map.Entry<String, String> entry : requestData.getParameters().entrySet()) {
            out.append("<tr><th>").append(escapeHtml(entry.getKey(), writer)).append("</th><td>").append(escapeHtml(entry.getValue(), writer)).append("</td></tr>");
        }
        out.append("</table>");

        Object data = requestData.getData();
        if (data != null) {
            if (data instanceof JSONObject) {
                try {
                    out.append("<h2>Body:</h2><pre>").append(escapeHtml(((JSONObject) data).toString(4), writer));
                } catch (JSONException e) {
                    out.append("Error rendering body: ").append(escapeHtml(e.toString(), writer));
                }
            } else if (data instanceof JSONArray) {
                try {
                    out.append("<h2>Body:</h2><pre>").append(escapeHtml(((JSONArray) data).toString(4), writer));
                } catch (JSONException e) {
                    out.append("Error rendering body: ").append(escapeHtml(e.toString(), writer));
                }
            }
        }
        out.append("<h1>Response</h1>");

        try {
            converter.convert(result.getFormat(), "apiResponse", requestData, result, session);
            Response response = (Response) result.getResultObject();
            JSONObject json = new JSONObject();
            ResponseWriter.write(response, json);
            out.append("<h2>Response:</h2><pre>").append(escapeHtml(json.toString(4), writer));


        } catch (Exception e) {
            out.append("Can't render response: ").append(escapeHtml(e.toString(), writer));
        }
        out.append("</body></html>");

        result.setHeader("Content-Type", "text/html");
        result.setResultObject(out.toString());
    }

    private static String escapeHtml(String str, UnsynchronizedStringWriter writer) {
        if (str != null) {
            try {
                StringEscapeUtils.escapeHtml(writer, str);
            } catch (IOException ioe) {
                // Should be impossible
                throw new UnhandledException(ioe);
            }
        }
        return "";
    }

    @Override
    public String getInputFormat() {
        return "json";
    }

    @Override
    public String getOutputFormat() {
        return "debug";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

}
