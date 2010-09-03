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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mailfilter.ajax;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mailfilter.ajax.SessionWrapper.Credentials;
import com.openexchange.mailfilter.ajax.actions.AbstractAction;
import com.openexchange.mailfilter.ajax.actions.AbstractRequest;
import com.openexchange.mailfilter.ajax.exceptions.OXMailfilterException;
import com.openexchange.sessiond.SessiondException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AJAXServlet extends HttpServlet {

	private static final long serialVersionUID = 3006497622205429579L;

    private static final Log LOG = LogFactory.getLog(AJAXServlet.class);

    /**
     * The content type if the response body contains javascript data. Set it
     * with <code>resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT)</code>.
     */
    public static final String CONTENTTYPE_JAVASCRIPT = "text/javascript; charset=UTF-8";

    protected AJAXServlet() {
        super();
    }

    protected static void sendError(final HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    protected static SessionWrapper getSession(final HttpServletRequest req) throws SessiondException, OXMailfilterException {
        return new SessionWrapper(req);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Response response = new Response();
        try {
            final AbstractRequest request = createRequest();
            request.setSession(new AbstractRequest.Session() {
                final SessionWrapper session = getSession(req);
                
                public Object getAttribute(final String name) {
                    return session.getParameter(name);
                }
                public void setAttribute(final String name, final Object value) {
                    session.setParameter(name, value);
                }
                public void removeAttribute(final String name) {
                    session.removeParameter(name);
                }
                public Credentials getCredentails() {
                    return session.getCredentials();
                }
            });
            request.setParameters(new AbstractRequest.Parameters() {
                public String getParameter(final Parameter param) throws AjaxException {
                    final String value = req.getParameter(param.getName());
                    if (param.isRequired() && null == value) {
                        throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, param.getName());
                    }
                    return value;
                }
            });
            /*
             * A non-download action
             */
            final AbstractAction action = createAction();
            response.setData(action.action(request));
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        /*
         * Disable browser cache
         */
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, resp.getWriter());
        } catch (final JSONException e) {
            LOG.error("Error while writing JSON.", e);
            sendError(resp);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Response response = new Response();
        try {
            final AbstractRequest request = createRequest();
            request.setSession(new AbstractRequest.Session() {
                final SessionWrapper session = getSession(req);
                
                public Object getAttribute(final String name) {
                    return session.getParameter(name);
                }

                public void setAttribute(final String name, final Object value) {
                    session.setParameter(name, value);
                }

                public void removeAttribute(final String name) {
                    session.removeParameter(name);
                }

                public Credentials getCredentails() {
                    return session.getCredentials();
                }
            });
            request.setParameters(new AbstractRequest.Parameters() {
                public String getParameter(final Parameter param) throws AjaxException {
                    final String value = req.getParameter(param.getName());
                    if (null == value) {
                        throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, param.getName());
                    }
                    return value;
                }
            });
            request.setBody(com.openexchange.ajax.AJAXServlet.getBody(req));
            final AbstractAction action = createAction();
            response.setData(action.action(request));
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        }
        /*
         * Disable browser cache
         */
        Tools.disableCaching(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            ResponseWriter.write(response, resp.getWriter());
        } catch (final JSONException e) {
            LOG.error("Error while writing JSON.", e);
            sendError(resp);
        }
    }

    protected abstract AbstractRequest createRequest();

    protected abstract AbstractAction<?, ? extends AbstractRequest> createAction();
}
