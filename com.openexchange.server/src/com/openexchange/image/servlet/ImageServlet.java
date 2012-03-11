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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceException;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.helper.CombinedInputStream;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.UserService;

/**
 * {@link ImageServlet} - The servlet serving requests to <i>ajax/image</i>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageServlet extends HttpServlet {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ImageServlet.class));

    private static final int BUFLEN = 0xFFFF;

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -3357383590134182460L;

    /**
     * The image servlet's alias
     */
    public static final String ALIAS = ImageDataSource.ALIAS;

    /**
     * The <code>"uid"</code> parameter
     */
    public static final String PARAMETER_UID = "uid";

    private static final ConcurrentMap<String, String> regName2Alias = new ConcurrentHashMap<String, String>(8);

    private static final ConcurrentMap<String, String> alias2regName = new ConcurrentHashMap<String, String>(8);

    /**
     * Adds specified mapping
     * 
     * @param registrationName The registration name
     * @param alias The alias
     */
    public static void addMapping(final String registrationName, final String alias) {
        regName2Alias.put(registrationName, alias);
        alias2regName.put(alias, registrationName);
    }

    /**
     * Gets the registration name for given URL.
     * 
     * @param url The url
     * @return The associated registration name or <code>null</code>
     */
    public static String getRegistrationNameFor(final String url) {
        if (null == url) {
            return null;
        }
        String s = url;
        final int pos = s.indexOf(ALIAS);
        if (pos > 0) {
            s = s.substring(pos + ALIAS.length());
        }
        for (final Entry<String, String> entry : alias2regName.entrySet()) {
            final String alias = entry.getKey();
            if (s.startsWith(alias)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /*-
     * ------------------------------- Member stuff ----------------------------------
     */

    private volatile CookieHashSource hashSource;

    private final String secretPrefix;

    private final String publicSessionCookie;

    /**
     * Initializes a new {@link ImageServlet}
     */
    public ImageServlet() {
        super();
        secretPrefix = Login.SECRET_PREFIX;
        publicSessionCookie = Login.PUBLIC_SESSION_NAME;
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        hashSource = CookieHashSource.parse(config.getInitParameter(Property.COOKIE_HASH.getPropertyName()));
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Tools.disableCaching(resp);
        super.service(req, resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        // Check registration name
        String registrationName = null;
        {
            final String pathInfo = req.getPathInfo();
            for (final Entry<String, String> entry : alias2regName.entrySet()) {
                final String alias = entry.getKey();
                if (pathInfo.startsWith(alias)) {
                    registrationName = entry.getValue();
                    break;
                }
            }
            if (null == registrationName) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown image location.");
                return;
            }
        }
        // Parse path
        final ImageDataSource dataSource;
        try {
            final ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class, true);
            dataSource = (ImageDataSource) conversionService.getDataSource(registrationName);
            if (null == dataSource) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image location.");
                return;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        final ImageLocation imageLocation;
        {
            final StringBuilder sb = new StringBuilder(req.getRequestURI());
            final String queryString = req.getQueryString();
            if (!isEmpty(queryString)) {
                if ('?' != queryString.charAt(0)) {
                    sb.append('?');
                }
                sb.append(queryString);
            }
            imageLocation = dataSource.parseUrl(sb.toString());
        }
        // Now check for appropriate permission
        Session session = null;
        try {
            final Cookie[] cookies = req.getCookies();
            if (null == cookies) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing request cookies.");
                return;
            }
            /*
             * Get user's session
             */
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class, true);
            for (final Cookie cookie : cookies) {
                if (publicSessionCookie.equals(cookie.getName())) {
                    session = sessiondService.getSessionByAlternativeId(cookie.getValue());
                    break;
                }
            }
            if (null == session) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No such session found.");
                return;
            }
            /*
             * Find appropriate secret cookie
             */
            final String cookieName = secretPrefix + SessionServlet.getHash(hashSource, req, session.getHash(), session.getClient());
            String secret = null;
            for (final Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    secret = cookie.getValue();
                    break;
                }
            }
            if (null == secret || !session.getSecret().equals(secret)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid secret cookie.");
                return;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        final Context context = getContext(session.getContextId());
        if (null == context) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to determine context.");
            return;
        }
        final User user = getUser(session.getUserId(), context);
        if (null == user) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to determine user.");
            return;
        }
        if (!context.isEnabled() || !user.isMailEnabled()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials.");
            return;
        }
        /*
         * Output image
         */
        try {
            /*
             * Check for ETag headers
             */
            final String eTag = req.getHeader("If-None-Match");
            if (null != eTag && dataSource.getETag(imageLocation, session).equals(eTag) ) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                final long expires = dataSource.getExpires();
                Tools.setETag(dataSource.getETag(imageLocation, session), expires > 0 ? new Date(System.currentTimeMillis() + expires) : null, resp);
                return;
            }
            outputImageData(dataSource, imageLocation, session, resp);
        } catch (final OXException e) {
            LOG.debug(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

    private static void outputImageData(final ImageDataSource dataSource, final ImageLocation imageLocation, final Session session, final HttpServletResponse resp) throws IOException, OXException {
        final Data<InputStream> data = dataSource.getData(InputStream.class, dataSource.generateDataArgumentsFrom(imageLocation), session);
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
        final InputStream in;
        if (null == ct) {
            /*
             * Get starting byte sequence from image data's input stream
             */
            byte[] sequence = new byte[16];
            final InputStream remainee = data.getData();
            try {
                final int nRead = remainee.read(sequence, 0, sequence.length);
                if (nRead > 0 && nRead < sequence.length) {
                    final byte[] tmp = sequence;
                    sequence = new byte[nRead];
                    System.arraycopy(tmp, 0, sequence, 0, nRead);
                }
            } catch (final IOException e) {
                closeStream(remainee);
                throw e;
            } catch (final Throwable t) {
                closeStream(remainee);
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                if (t instanceof Error) {
                    throw (Error) t;
                }
                final IOException ioe = new IOException(t.getMessage());
                ioe.initCause(t);
                throw ioe;
            }
            /*
             * Detect image content type by starting byte sequence
             */
            in = new CombinedInputStream(sequence, remainee);
            resp.setContentType(ImageTypeDetector.getMimeType(sequence));
        } else {
            in = data.getData();
            resp.setContentType(ct);
        }
        try {
            /*
             * Set ETag
             */
            final long expires = dataSource.getExpires();
            Tools.setETag(dataSource.getETag(imageLocation, session), expires > 0 ? new Date(System.currentTimeMillis() + expires) : null, resp);
            /*
             * Select response's output stream
             */
            final OutputStream out = resp.getOutputStream();
            /*
             * Write from content's input stream to response output stream
             */
            final int len = BUFLEN;
            final byte[] buffer = new byte[len];
            for (int read; (read = in.read(buffer, 0, len)) != -1;) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } finally {
            closeStream(in);
        }
    }

    private static void closeStream(final InputStream in) {
        if (null == in) {
            return;
        }
        try {
            in.close();
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImageServlet.class)).error(e.getMessage(), e);
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static User getUser(final int userId, final Context context) {
        try {
            return ServerServiceRegistry.getInstance().getService(UserService.class, true).getUser(userId, context);
        } catch (final OXException e) {
            LOG.debug("User '" + userId + "' not found.");
            return null;
        }
    }

    private static Context getContext(final int contextId) {
        try {
            return ServerServiceRegistry.getInstance().getService(ContextService.class, true).getContext(contextId);
        } catch (final ServiceException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final OXException e) {
            LOG.error("Can not load context.", e);
            return null;
        }
    }

    private static String getMailAddress(final HttpServletRequest request) {
        final String userName = request.getParameter("username");
        final String serverName = request.getParameter("server");
        if (null == userName || null == serverName) {
            return null;
        }
        return userName + '@' + serverName;
    }

}
