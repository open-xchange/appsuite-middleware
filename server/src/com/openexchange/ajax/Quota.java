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

package com.openexchange.ajax;

import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.request.QuotaRequest;
import com.openexchange.ajax.request.ServletRequestAdapter;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.AbstractOXException;

public class Quota extends SessionServlet {

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(Quota.class);

    private static final transient LoggingLogic LL = LoggingLogic.getLoggingLogic(Quota.class, LOG);

    private static final long serialVersionUID = 6477434510302882905L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException,
			IOException {

		final String action = req.getParameter(PARAMETER_ACTION);
		if (action == null) {
			missingParameter(PARAMETER_ACTION, res, false, null);
			return;
		}

        final ServerSession session;
        try {
            session = new ServerSessionAdapter(getSessionObject(req));
        } catch (ContextException e) {
            handle(res, e, action, JS_FRAGMENT_POPUP);
            return;
        }


        final OXJSONWriter writer = new OXJSONWriter();
		final QuotaRequest fsReq = new QuotaRequest(session, writer);
		if (!fsReq.action(action, new ServletRequestAdapter(req, res))) {
			unknownAction("GET", action, res, false);
			return;
		}
		try {
			Response.write(new Response((JSONObject) writer.getObject()), res.getWriter());
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}


    private void handle(final HttpServletResponse res, final AbstractOXException t, final String action, final String fragmentOverride) {
		LL.log(t);

		final Response resp = new Response();
		resp.setException(t);


		try {
			Writer writer = res.getWriter();
			Response.write(resp, writer);

		} catch (JSONException e) {
			LOG.error("",t);
		} catch (IOException e) {
			LOG.error("",e);
		}
	}
}
