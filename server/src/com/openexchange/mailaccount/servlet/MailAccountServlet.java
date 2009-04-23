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

package com.openexchange.mailaccount.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXMandatoryFieldException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mailaccount.servlet.request.MailAccountRequest;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailAccountServlet} - The servlet handling requests to mail account module.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountServlet extends PermissionServlet {

    private static final long serialVersionUID = 6467520155761361011L;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailAccountServlet.class);

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserConfiguration().hasWebMail();
    }

    @Override
    protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        final Response response = new Response();
        try {
            final String action = DataServlet.parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
            final ServerSession session = getSessionObject(httpServletRequest);
            JSONObject jsonObj;

            try {
                jsonObj = DataServlet.convertParameter2JSONObject(httpServletRequest);
            } catch (final JSONException e) {
                LOG.error(e.getMessage(), e);
                response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
                writeResponse(response, httpServletResponse);
                return;
            }

            final MailAccountRequest accountRequest = new MailAccountRequest(session);

            final Object responseObj = accountRequest.action(action, jsonObj);
            response.setTimestamp(accountRequest.getTimestamp());
            response.setData(responseObj);
        } catch (final OXMandatoryFieldException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final OXConflictException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final SearchIteratorException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final AjaxException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final OXJSONException exc) {
            LOG.error(exc.getMessage(), exc);
            response.setException(exc);
        } catch (final JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        } catch (final AbstractOXException exc) {
            LOG.error(exc.getMessage(), exc);
            response.setException(exc);
        }
        writeResponse(response, httpServletResponse);
    }

    @Override
    protected void doPut(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        final Response response = new Response();
        try {
            final String action = DataServlet.parseMandatoryStringParameter(httpServletRequest, PARAMETER_ACTION);
            final ServerSession session = getSessionObject(httpServletRequest);

            final String data = getBody(httpServletRequest);
            JSONObject jsonObj;

            try {
                jsonObj = DataServlet.convertParameter2JSONObject(httpServletRequest);
            } catch (final JSONException e) {
                LOG.error(e.getMessage(), e);
                response.setException(new OXJSONException(OXJSONException.Code.JSON_BUILD_ERROR, e));
                writeResponse(response, httpServletResponse);
                return;
            }

            final MailAccountRequest accountRequest = new MailAccountRequest(session);

            final Object responseObj;
            if (data.charAt(0) == '[') {
                final JSONArray jData = new JSONArray(data);

                jsonObj.put(PARAMETER_DATA, jData);

                responseObj = accountRequest.action(action, jsonObj);
            } else {
                final JSONObject jData = new JSONObject(data);
                jsonObj.put(PARAMETER_DATA, jData);

                responseObj = accountRequest.action(action, jsonObj);
            }
            response.setTimestamp(accountRequest.getTimestamp());
            response.setData(responseObj);
        } catch (final OXMandatoryFieldException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final OXConflictException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final OXJSONException exc) {
            LOG.error(exc.getMessage(), exc);
            response.setException(exc);
        } catch (final JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final SearchIteratorException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final AjaxException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final AbstractOXException exc) {
            LOG.error(exc.getMessage(), exc);
            response.setException(exc);
        }

        writeResponse(response, httpServletResponse);
    }
}
