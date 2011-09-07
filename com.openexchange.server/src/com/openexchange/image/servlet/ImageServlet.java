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

package com.openexchange.image.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.ServiceException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.helper.CombinedInputStream;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.crypto.CryptoService;
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

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImageServlet.class));

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

    private volatile CookieHashSource hashSource;

    private final String secretPrefix;

    /**
     * Initializes a new {@link ImageServlet}
     */
    public ImageServlet() {
        super();
        secretPrefix = Login.SECRET_PREFIX;
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
        final String registrationName = req.getParameter("source");
        if (null == registrationName) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing source parameter.");
            return;
        }
        final String signParam;
        try {
            final CryptoService cryptoService = ServerServiceRegistry.getInstance().getService(CryptoService.class);
            final String param = req.getParameter("signature");
            if (null == param) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing signature parameter.");
                return;
            }
            signParam = cryptoService.decrypt(param, registrationName);
        } catch (final OXException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature parameter.");
            return;
        }
        if (null == signParam) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing signature parameter.");
            return;
        }
        int beginIndex = 0;
        int endIndex = signParam.indexOf('.');
        if (endIndex <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature parameter.");
            return;
        }
        final Context context = getContext(signParam.substring(beginIndex, endIndex));
        if (null == context) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to determine context.");
            return;
        }
        beginIndex = endIndex;
        endIndex = signParam.indexOf('.', beginIndex + 1);
        if (endIndex <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature parameter.");
            return;
        }
        final String signature = signParam.substring(beginIndex + 1, endIndex);
        final User user = getUser(signParam.substring(endIndex + 1), context);
        if (null == user) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to determine user.");
            return;
        }
        if (!context.isEnabled() || !user.isMailEnabled()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials.");
            return;
        }
        /*
         * Look-up up cookies
         */
        String secret = null;
        String secretCookieName = null;
        Session session = null;
        try {
            final Cookie[] cookies = req.getCookies();
            if (null == cookies) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing request cookies.");
                return;
            }
            /*
             * Get user's sessions
             */
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class, true);
            final Collection<Session> sessions = sessiondService.getSessions(user.getId(), context.getContextId());
            /*
             * Find secret cookie
             */
            NextCookie: for (final Cookie cookie : cookies) {
                final String cookieName = cookie.getName();
                if (cookieName.startsWith(secretPrefix)) {
                    secret = cookie.getValue();
                    /*
                     * Find an appropriate session for secret
                     */
                    for (final Session ses : sessions) {
                        if (secret.equals(ses.getSecret())) {
                            secretCookieName = cookieName;
                            session = ses;
                            break NextCookie;
                        }
                    }
                }
            }
            if (null == session) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid secret cookie.");
                return;
            }
            /*
             * Verify hash
             */
            final String expectedSecretCookieName = secretPrefix + SessionServlet.getHash(hashSource, req, session.getHash(), session.getClient());
            if (null == secretCookieName || !secretCookieName.equals(expectedSecretCookieName)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid secret cookie.");
                return;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        /*
         * Get image data source
         */
        final ImageDataSource dataSource;
        try {
            final ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class, true);
            dataSource = (ImageDataSource) conversionService.getDataSource(registrationName);
            if (null == dataSource) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid source parameter.");
                return;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        /*
         * Generate image location
         */
        final ImageLocation imageLocation;
        {
            final String folder = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
            final String id = req.getParameter(AJAXServlet.PARAMETER_ID);
            final String imageId = req.getParameter(AJAXServlet.PARAMETER_UID);
            imageLocation = new ImageLocation.Builder(imageId).folder(folder).id(id) .build();
        }
        /*
         * Check signature equality
         */
        if (!signature.equals(dataSource.getSignature(imageLocation, session))) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Signature does not match.");
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
                return;
            }
            outputImageData(dataSource, imageLocation, session, resp);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

    private static User getUser(final String sUserId, final Context context) {
        final int userId;
        try {
            userId = Integer.parseInt(sUserId.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Unable to parse user identifier.", e);
            return null;
        }
        try {
            return ServerServiceRegistry.getInstance().getService(UserService.class, true).getUser(userId, context);
        } catch (final OXException e) {
            LOG.debug("User '" + sUserId + "' not found.");
            return null;
        }
    }

    private static Context getContext(final String sContextId) {
        final int contextId;
        try {
            contextId = Integer.parseInt(sContextId.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Unable to parse context identifier.", e);
            return null;
        }
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
                if (nRead < sequence.length) {
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
            if (expires > 0) {
                final long millis = System.currentTimeMillis() + (expires);
                Tools.setETag(dataSource.getETag(imageLocation, session), new Date(millis), resp);
            } else {
                Tools.setETag(dataSource.getETag(imageLocation, session), resp);
            }
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
        } catch (final IOException e) {
            com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImageServlet.class)).error(e.getMessage(), e);
        }
    }
}
