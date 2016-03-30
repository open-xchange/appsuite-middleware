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

import java.util.Iterator;
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
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DebugConverter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DebugConverter implements ResultConverter {

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        StringBuilder out = new StringBuilder("<!DOCTYPE html><head><title>").append(requestData.getAction()+" Response").append("</title></head><body><h1>Request with action ").append(requestData.getAction()).append("</h1>");
        out.append("<h2>Parameters:</h2>");
        out.append("<table>");
        Iterator<String> parameterNames = requestData.getParameterNames();
        while(parameterNames.hasNext()) {
            String paramName = parameterNames.next();
            out.append("<tr><th>").append(paramName).append("</th><td>").append(requestData.getParameter(paramName)).append("</td></tr>");
        }
        out.append("</table>");

        Object data = requestData.getData();
        if (data != null) {
            if (data instanceof JSONObject) {
                try {
                    out.append("<h2>Body:</h2><pre>").append(((JSONObject) data).toString(4));
                } catch (JSONException e) {
                    out.append("Error rendering body: ").append(e.toString());
                }
            } else if (data instanceof JSONArray) {
                try {
                    out.append("<h2>Body:</h2><pre>").append(((JSONArray) data).toString(4));
                } catch (JSONException e) {
                    out.append("Error rendering body: ").append(e.toString());
                }
            }
        }
        out.append("<h1>Response</h1>");

        try {
            converter.convert(result.getFormat(), "apiResponse", requestData, result, session);
            Response response = (Response) result.getResultObject();
            JSONObject json = new JSONObject();
            ResponseWriter.write(response, json);
            out.append("<h2>Response:</h2><pre>").append((json).toString(4));


        } catch (Exception e) {
            out.append("Can't render response: "+e.toString());
        }
        out.append("</body></html>");

        result.setHeader("Content-Type", "text/html");
        result.setResultObject(out.toString());
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
