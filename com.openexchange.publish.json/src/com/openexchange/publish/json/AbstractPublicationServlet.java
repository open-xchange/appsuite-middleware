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

package com.openexchange.publish.json;

import static com.openexchange.publish.json.PublicationJSONErrorMessage.THROWABLE;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractPublicationServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractPublicationServlet extends PermissionServlet {

    private static final long serialVersionUID = 5880236559668102629L;

    /**
     * Initializes a new {@link AbstractPublicationServlet}.
     */
    protected AbstractPublicationServlet() {
        super();
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserPermissionBits().isPublication();
    }

    protected void writeOXException(final OXException e, final HttpServletRequest req, final HttpServletResponse resp) {
        switch (e.getCategories().get(0).getLogLevel()) {
            case TRACE:
                getLog().trace("", e);
                break;
            case DEBUG:
                getLog().debug("", e);
                break;
            case INFO:
                getLog().info("", e);
                break;
            case WARNING:
                getLog().warn("", e);
                break;
            case ERROR:
                getLog().error("", e);
                break;
            default:
                break;
        }
        final Response response = new Response(getSessionObject(req));
        response.setException(e);
        writeResponseSafely(response, resp, getSessionObject(req));
    }

    protected void writeData(final Object data, final HttpServletResponse resp, final Session session) {
        final Response response = new Response();
        response.setData(data);
        writeResponseSafely(response, resp, session);
    }

    protected OXException wrapThrowable(final Throwable t) {
        ExceptionUtils.handleThrowable(t);
        return t instanceof OXException ? (OXException) t : THROWABLE.create(t, t.getMessage());
    }

    protected void writeResponseSafely(final Response response, final HttpServletResponse resp, final Session session) {
        try {
            writeResponse(response, resp, session);
        } catch (final IOException e) {
            getLog().error("", e);
        }
    }

    protected abstract Logger getLog();
}
