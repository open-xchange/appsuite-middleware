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

package com.openexchange.image.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.openexchange.ajax.Login;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataProperties;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.image.internal.ImageData;
import com.openexchange.image.internal.ImageRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link ImageServlet} - The servlet serving requests to <i>ajax/image</i>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ImageServlet extends HttpServlet {

	private static final long serialVersionUID = -3357383590134182460L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ImageServlet.class);

	/**
	 * The image servlet's alias
	 */
	public static final String ALIAS = "ajax/image";

	/**
	 * The <code>"uid"</code> parameter
	 */
	public static final String PARAMETER_UID = "uid";

	/**
	 * Initializes a new {@link ImageServlet}
	 */
	public ImageServlet() {
		super();
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		Tools.disableCaching(resp);
		super.service(req, resp);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		try {
			final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(
					SessiondService.class);
			if (sessiondService == null) {
				throw new SessiondException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE,
						SessiondService.class.getName()));
			}
			final String uid = req.getParameter(PARAMETER_UID);
			if (uid == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing URL parameter " + PARAMETER_UID);
			}
			final Session[] sessions = getSessions(req, sessiondService);
			for (final Session session : sessions) {
				ImageData imageData = ImageRegistry.getInstance().getImageData(session, uid);
				if (imageData == null) {
					imageData = ImageRegistry.getInstance().getImageData(session.getContextId(), uid);
				}
				if (imageData != null) {
					/*
					 * Output to client
					 */
					outputImageData(imageData, session, resp);
					return;
				}
			}
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No image found for UID " + uid);
		} catch (final SessiondException e) {
			LOG.error(e.getMessage(), e);
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} catch (final ContextException e) {
			LOG.error(e.getMessage(), e);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (final LdapException e) {
			LOG.error(e.getMessage(), e);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} catch (final DataException e) {
			LOG.error(e.getMessage(), e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private static void outputImageData(final ImageData imageData, final Session session, final HttpServletResponse resp)
			throws DataException, IOException {
		final Data<InputStream> data = imageData.getImageData(session);
		final String ct;
		final String fileName;
		{
			final DataProperties dataProperties = data.getDataProperties();
			ct = dataProperties.get(DataProperties.PROPERTY_CONTENT_TYPE);
			fileName = dataProperties.get(DataProperties.PROPERTY_NAME);
		}
		/*
		 * Set header Content-Disposition
		 */
		{
			final String inline = "inline";
			if (fileName != null && fileName.length() > 0) {
				final StringBuilder builder = new StringBuilder(inline);
				builder.append("; filename=").append('"').append(fileName).append('"');
				resp.setHeader("Content-Disposition", builder.toString());
			} else {
				resp.setHeader("Content-Disposition", inline);
			}
		}
		/*
		 * Set header Content-Type
		 */
		resp.setContentType(ct);
		/*
		 * Reset response header values since we are going to directly write
		 * into servlet's output stream and then some browsers do not allow
		 * header "Pragma"
		 */
		Tools.removeCachingHeader(resp);
		/*
		 * Select response's output stream
		 */
		final OutputStream out = resp.getOutputStream();
		/*
		 * Write from content's input stream to response output stream
		 */
		final InputStream in = data.getData();
		try {
			final byte[] buffer = new byte[0xFFFF];
			for (int len; (len = in.read(buffer, 0, buffer.length)) != -1;) {
				out.write(buffer, 0, len);
			}
			out.flush();
		} finally {
			in.close();
		}
	}

	private static Session[] getSessions(final HttpServletRequest req, final SessiondService sessiondService)
			throws ContextException, LdapException {
		final String[] sessionIds = getSessionIds(req);
		final List<Session> sessions = new ArrayList<Session>(4);
		for (int i = 0; i < sessionIds.length; i++) {
			final Session session = sessiondService.getSession(sessionIds[i]);
			if (null != session) {
				final Context ctx = ContextStorage.getStorageContext(session.getContextId());
				if (ctx.isEnabled() && UserStorage.getInstance().getUser(session.getUserId(), ctx).isMailEnabled()) {
					sessions.add(session);
				}
			}
		}
		return sessions.toArray(new Session[sessions.size()]);
	}

	private static String[] getSessionIds(final HttpServletRequest req) {
		final Cookie[] cookies = req.getCookies();
		final List<String> sessionIds = new ArrayList<String>(4);
		if (cookies != null) {
			for (final Cookie cookie : cookies) {
				final String name = cookie.getName();
				if (name != null && name.startsWith(Login.COOKIE_PREFIX, 0)) {
					sessionIds.add(cookie.getValue());
					break;
				}
			}
		}
		return sessionIds.toArray(new String[sessionIds.size()]);
	}
}
