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
 *    trademarks of the OX Software GmbH. group of companies.
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

package org.glassfish.grizzly.http.server;

import static org.glassfish.grizzly.http.util.Constants.FORM_POST_CONTENT_TYPE;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.ThreadCache;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.util.Globals;
import org.glassfish.grizzly.localization.LogMessages;
import org.glassfish.grizzly.utils.Charsets;
import com.google.common.collect.ImmutableSet;
import com.openexchange.http.grizzly.GrizzlyConfig;

/**
 * {@link OXRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OXRequest extends Request {

    private static final Logger LOGGER = Grizzly.logger(Request.class);

    private static final ThreadCache.CachedTypeIndex<Request> CACHE_IDX = ThreadCache.obtainIndex(Request.class, 16);

    /**
     * Creates a new request instance
     *
     * @param The effective Grizzly configuration
     * @return The new request instance
     */
    public static Request create(GrizzlyConfig grizzlyConfig) {
        final Request request = ThreadCache.takeFromCache(CACHE_IDX);
        if (request != null) {
            return request;
        }

        return new OXRequest(grizzlyConfig, new OXResponse(grizzlyConfig));
    }

    // ----------------------------------------------------------------------------------------------

    private String xForwardProto = null;
    private int xForwardPort = 0;
    private boolean isConsiderXForwards;
    private boolean isForcedSecurity = false;
    private final GrizzlyConfig grizzlyConfig;

    /**
     * Initializes a new {@link OXRequest}.
     *
     * @param response The associated response instance
     */
    protected OXRequest(GrizzlyConfig grizzlyConfig, Response response) {
        super(response);
        this.grizzlyConfig = grizzlyConfig;
        isConsiderXForwards = grizzlyConfig.isConsiderXForwards();
    }

    @Override
    protected void recycle() {
        xForwardProto = null;
        xForwardPort = 0;
        isConsiderXForwards = grizzlyConfig.isConsiderXForwards();
        isForcedSecurity = false;
        super.recycle();
    }

    /**
     * Gets the XForwardProto e.g. http/s
     *
     * @return The XForwardProto
     */
    public String getXForwardProto() {
        return xForwardProto;
    }

    /**
     * Sets the xForwardProto e.g. http/s
     *
     * @param XForwardProto The XForwardProto to set
     */
    public void setXForwardProto(String XForwardProto) {
        this.xForwardProto = XForwardProto;
    }

    /**
     * Gets the XForwardPort
     *
     * @return The XForwardPort
     */
    public int getXForwardPort() {
        return xForwardPort;
    }

    /**
     * Sets the XForwardPort
     *
     * @param XForwardPort The XForwardPort to set
     */
    public void setXForwardPort(int XForwardPort) {
        this.xForwardPort = XForwardPort;
    }

    @Override
    public String getScheme() {
        if (isConsiderXForwards && xForwardProto != null) {
            return xForwardProto;
        }
        return super.getScheme();
    }

    @Override
    public int getServerPort() {
        if (isConsiderXForwards && xForwardPort > 0) {
            return xForwardPort;
        }
        return super.getServerPort();
    }

    /**
     * Parse request parameters.
     */
    @Override
    protected void parseRequestParameters() {

        // Delay updating requestParametersParsed to TRUE until
        // after getCharacterEncoding() has been called, because
        // getCharacterEncoding() may cause setCharacterEncoding() to be
        // called, and the latter will ignore the specified encoding if
        // requestParametersParsed is TRUE
        requestParametersParsed = true;

        Charset charset = null;

        if (parameters.getEncoding() == null) {
            // getCharacterEncoding() may have been overridden to search for
            // hidden form field containing request encoding
            charset = lookupCharset(getCharacterEncoding());

            parameters.setEncoding(charset);
        }

        if (parameters.getQueryStringEncoding() == null) {
            if (charset == null) {
                // getCharacterEncoding() may have been overridden to search for
                // hidden form field containing request encoding
                charset = lookupCharset(getCharacterEncoding());
            }

            parameters.setQueryStringEncoding(charset);
        }

        parameters.handleQueryParameters();

        if (usingInputStream || usingReader) {
            return;
        }

        if (!Method.POST.equals(getMethod())) {
            return;
        }

        if (!checkPostContentType(getContentType())) {
            return;
        }

        final int maxFormPostSize =
                httpServerFilter.getConfiguration().getMaxFormPostSize();

        int len = getContentLength();
        if (len < 0) {
            if (!request.isChunked()) {
                return;
            }

            len = maxFormPostSize;
        }

        if ((maxFormPostSize > 0) && (len > maxFormPostSize)) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(LogMessages.WARNING_GRIZZLY_HTTP_SERVER_REQUEST_POST_TOO_LARGE());
            }

            throw new IllegalStateException(LogMessages.WARNING_GRIZZLY_HTTP_SERVER_REQUEST_POST_TOO_LARGE());
        }

        int read = 0;
        try {
            final Buffer formData = getPostBody(len);
            read = formData.remaining();
            parameters.processParameters(formData, formData.position(), read);
        } catch (Exception ignored) {
        } finally {
            try {
                skipPostBody(read);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        LogMessages.WARNING_GRIZZLY_HTTP_SERVER_REQUEST_BODY_SKIP(), e);
            }
        }

    }

    private boolean checkPostContentType(final String contentType) {
        return ((contentType != null)
                  && contentType.trim().startsWith(FORM_POST_CONTENT_TYPE));

    }

    /**
     * Lookup a Charset based on a String value.
     * If the lookup fails try to fall back to the default encoding from GrizzlyConfig. If that fails, too use UTF-8.
     * The resulting Charset is used to e.g decode parameters sent via request bodies.
     *
     * @param enc The String representing the encoding
     * @return The Charset loked up, or the default encoding specified in GrizzlyConfig, or UTF-8
     */
    private Charset lookupCharset(final String enc) {
        Charset charset;

        if (enc != null) {
            try {
                charset = Charsets.lookupCharset(enc);
            } catch (Exception e) {
                try {
                    String defaultEncoding = grizzlyConfig.getDefaultEncoding();
                    charset = Charsets.lookupCharset(defaultEncoding);
                } catch (Exception ex) {
                    charset = Charsets.UTF8_CHARSET;
                }
            }
        } else {
            try {
                String defaultEncoding = grizzlyConfig.getDefaultEncoding();
                charset = Charsets.lookupCharset(defaultEncoding);
            } catch (Exception ex) {
                charset = Charsets.UTF8_CHARSET;
            }
        }

        return charset;
    }

    /**
     * Override isSecure by first checking the <code>X-Forward-Proto</code> header. Fall-back is the original implementation of the header wasn't present.
     *
     * @return <code>true</code> if the <code>X-Forward-Proto</code> header indicates a secure connection or a real HTTPS connection was used.
     */
    @Override
    public boolean isSecure() {
        if (isConsiderXForwards && xForwardProto != null) {
            return "https".equals(xForwardProto);
        }
        return super.isSecure();
    }

    private static final Set<String> ATTRS_SSL = ImmutableSet.of(Globals.SSL_CERTIFICATE_ATTR, Globals.CERTIFICATES_ATTR, Globals.CIPHER_SUITE_ATTR, Globals.KEY_SIZE_ATTR);

    /**
     * Return the specified request attribute if it exists; otherwise, return
     * <code>null</code>.
     *
     * @param name Name of the request attribute to return
     * @return the specified request attribute if it exists; otherwise, return
     * <code>null</code>.
     */
    @Override
    public Object getAttribute(final String name) {
        if (ATTRS_SSL.contains(name)) {
            // Check for a forced secure request
            Object attribute = getAttribute("com.openexchange.http.isForcedSecurity");
            if (attribute != null) {
                isForcedSecurity = ((Boolean) attribute).booleanValue();
            }
            if (isForcedSecurity) {
                return null;
            }
        }

        return super.getAttribute(name);
    }

}
