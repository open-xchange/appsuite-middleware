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

package com.openexchange.ajax.links;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.TaskFields;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.tools.URLParameter;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class LinkTools extends Assert {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LinkTools.class);

    /**
     * Encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * URL to AJAX link interface.
     */
    private static final String LINK_URL = "/ajax/link";
    
    /**
     * Prevent instantiation
     */
    private LinkTools() {
        super();
    }

    public static Response insertLink(final WebConversation conv,
        final String host, final String session, final LinkObject link)
        throws JSONException, IOException, SAXException {
        LOG.trace("Inserting link.");
        final JSONObject json = toJSON(link);
        
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
        
        final ByteArrayInputStream bais = new ByteArrayInputStream(json
            .toString().getBytes(ENCODING));
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + host + LINK_URL + parameter.getURLParameters(), bais,
            AJAXServlet.CONTENTTYPE_JAVASCRIPT);
        final WebResponse resp = conv.getResponse(req);

        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: " + body);
        return Response.parse(body);
    }

    public static void extractInsertId(final Response response)
        throws JSONException {
        assertFalse(response.getErrorMessage(), response.hasError());
    }

    public static JSONObject toJSON(final LinkObject link) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put("id1", link.getFirstId());
        json.put("module1", link.getFirstType());
        json.put("folder1", link.getFirstFolder());
        json.put("id2", link.getSecondId());
        json.put("module2", link.getSecondType());
        json.put("folder2", link.getSecondFolder());
        return json;
    }
}
