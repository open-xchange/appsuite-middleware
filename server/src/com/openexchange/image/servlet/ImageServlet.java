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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.helper.CombinedInputStream;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataProperties;
import com.openexchange.image.ImageService;
import com.openexchange.image.internal.ImageData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondException;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link ImageServlet} - The servlet serving requests to <i>ajax/image</i>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageServlet extends HttpServlet {

    private static final int BUFLEN = 0xFFFF;

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -3357383590134182460L;

    /**
     * The image servlet's alias
     */
    public static final String ALIAS = "ajax/image";

    /**
     * The <code>"uid"</code> parameter
     */
    public static final String PARAMETER_UID = "uid";

    private CookieHashSource hashSource;

    /**
     * Initializes a new {@link ImageServlet}
     */
    public ImageServlet() {
        super();
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
        try {
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (sessiondService == null) {
                throw new SessiondException(
                    ServiceExceptionCode.SERVICE_UNAVAILABLE.create( SessiondService.class.getName()));
            }
            final String uid = req.getParameter(PARAMETER_UID);
            if (uid == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing URL parameter " + PARAMETER_UID);
            }
            final ImageService imageService = ServerServiceRegistry.getInstance().getService(ImageService.class, true);
            final String sessionId = imageService.getSessionForUID(uid);
            final String errorMsg = "Image not found";
            if(sessionId != null) {
                final Session session = sessiondService.getSession(sessionId);
                final String secret = SessionServlet.extractSecret(hashSource, req, session.getHash(), session.getClient());

                if (session.getSecret().equals(secret)) {
                    final ImageData imageData = imageService.getImageData(session, uid);
                    if(imageData != null) {
                        outputImageData(imageData, session, resp);
                    } else {
                        final String logMsg = "No image found for session " + sessionId + " and uid " + uid;
                        sendErrorAndLog(resp, HttpServletResponse.SC_NOT_FOUND, errorMsg, logMsg);
                    }
                } else {
                    final String logMsg = "Wrong secret " + secret + " for session " + sessionId;
                    sendErrorAndLog(resp, HttpServletResponse.SC_NOT_FOUND, errorMsg, logMsg);
                }
            } else {
                final String logMsg = "No session found for uid " + uid;
                sendErrorAndLog(resp, HttpServletResponse.SC_NOT_FOUND, errorMsg, logMsg);
            }
        } catch (final SessiondException e) {
            sendErrorAndLog(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, null, e.getMessage(), e);
        } catch (final DataException e) {
            sendErrorAndLog(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, e.getMessage(), e);
        } catch (final ServiceException e) {
            sendErrorAndLog(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, null, e.getMessage(), e);
        }
    }
    
    private static void sendErrorAndLog(final HttpServletResponse resp, final int errorCode, final String errorMsg, final String logMsg, final Throwable... throwable) throws IOException {
        if (throwable != null && throwable.length > 0) {
            org.apache.commons.logging.LogFactory.getLog(ImageServlet.class).error(logMsg, throwable[0]);
        } else {
            org.apache.commons.logging.LogFactory.getLog(ImageServlet.class).error(logMsg);
        }
        
        if (errorMsg != null) {
            resp.sendError(errorCode, errorMsg);
        } else {
            resp.sendError(errorCode);
        }        
    }

    private static void outputImageData(final ImageData imageData, final Session session, final HttpServletResponse resp) throws DataException, IOException {
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
             * Reset response header values since we are going to directly write into servlet's output stream and then some browsers do not
             * allow header "Pragma"
             */
            Tools.removeCachingHeader(resp);
            /*
             * Select response's output stream
             */
            final OutputStream out = resp.getOutputStream();
            /*
             * Write from content's input stream to response output stream
             */
            final byte[] buffer = new byte[BUFLEN];
            for (int len; (len = in.read(buffer, 0, buffer.length)) != -1;) {
                out.write(buffer, 0, len);
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
            org.apache.commons.logging.LogFactory.getLog(ImageServlet.class).error(e.getMessage(), e);
        }
    }
}
