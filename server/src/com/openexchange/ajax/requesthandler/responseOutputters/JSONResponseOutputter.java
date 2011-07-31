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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.responseOutputters;

import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseOutputter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;


/**
 * {@link JSONResponseOutputter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class JSONResponseOutputter implements ResponseOutputter {
    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(JSONResponseOutputter.class));
    
    @Override
    public int getPriority() {
        return 0;
    }
    
    @Override
    public boolean handles(AJAXRequestData request, AJAXRequestResult result) {
        Object resultObject = result.getResultObject();
        return Response.class.isAssignableFrom(resultObject.getClass());
    }

    @Override
    public void write(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        final Response response = (Response) result.getResultObject();
        writeResponse(response, request.getAction(), req, resp);
    }

    public static void writeResponse(Response response, String action, HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (FileUploadBase.isMultipartContent(new ServletRequestContext(req)) || (req.getParameter("respondWithHTML") != null && req.getParameter("respondWithHTML").equalsIgnoreCase("true")) || req.getParameter("callback") != null ) {
                resp.setContentType(AJAXServlet.CONTENTTYPE_HTML);
                String callback = req.getParameter("callback");
                if(callback == null) {
                    callback = action;
                }
                final StringWriter w = new StringWriter();
                ResponseWriter.write(response, w);
                
                resp.getWriter().print(substituteJS(w.toString(), callback));
            } else {
                ResponseWriter.write(response, resp.getWriter());
            }
        } catch (final JSONException e) {
            final OXException e1 = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error(e1.getMessage(), e1);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (IOException e2) {
                LOG.error(e2.getMessage(),e);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }        
    }

    private static String substituteJS(String json, String action) {
        return AJAXServlet.JS_FRAGMENT.replace("**json**", json).replace("**action**",
            action);    }

}
