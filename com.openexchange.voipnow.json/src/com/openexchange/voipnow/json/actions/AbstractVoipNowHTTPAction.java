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

package com.openexchange.voipnow.json.actions;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import com.openexchange.exception.OXException;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;
import com.openexchange.voipnow.json.http.TrustAllAdapter;

/**
 * {@link AbstractVoipNowHTTPAction} - Abstract action for HTTP requests..
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractVoipNowHTTPAction<M extends HttpMethod> extends AbstractVoipNowAction {

    /**
     * The path to VoipNow server's call API: <code><i>/callapi/callapi.php</i></code>.
     */
    protected static final String CALLAPI_PATH = "/callapi/callapi.php";

    /**
     * The HTTP protocol constant.
     */
    private static final Protocol PROTOCOL_HTTP = Protocol.getProtocol("http");

    /**
     * Initializes a new {@link AbstractVoipNowHTTPAction}.
     */
    protected AbstractVoipNowHTTPAction() {
        super();
    }

    /**
     * Gets a newly created {@link HttpMethod HTTP method} instance.
     *
     * @return A newly created {@link HttpMethod HTTP method} instance
     */
    protected abstract M newHttpMethod();

    /**
     * Gets the request path.
     *
     * @return The request path
     */
    protected abstract String getPath();

    /**
     * Gets the time out.
     *
     * @return The time out
     */
    protected abstract int getTimeout();

    /**
     * Creates a new VoipNow exception for a failed request to VoipNow server.
     *
     * @param code The error code
     * @param message The error message or <code>null</code> if none available
     * @return A new VoipNow exception for failed request
     */
    protected static OXException newRequestFailedException(final String code, final String message) {
        return VoipNowExceptionCodes.VOIPNOW_REQUEST_FAILED.create(code == null ? "" : code, message == null ? "" : message);
    }

    /**
     * Configures and creates a new HTTP call.
     *
     * @param setting The VoipNow server setting
     * @param queryString The query string to apply to HTTP method
     * @return The executed HTTP method
     * @throws OXException If executing HTTP method fails
     */
    protected M configure(final VoipNowServerSetting setting, final String queryString) throws OXException {
        M httpMethod = null;
        try {
            final HttpClient client = new HttpClient();
            final int httpTimeout = getTimeout();
            client.getParams().setSoTimeout(httpTimeout);
            client.getParams().setIntParameter("http.connection.timeout", httpTimeout);
            client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            /*
             * Create host configuration or URI
             */
            final String host = setting.getHost();
            final HostConfiguration hostConfiguration;
            if (setting.isSecure()) {
                int port = setting.getPort();
                if (port == -1) {
                    port = 443;
                }
                /*
                 * Own HTTPS host configuration and relative URI
                 */
                final Protocol httpsProtocol = new Protocol(HTTPS, ((ProtocolSocketFactory) new TrustAllAdapter()), port);
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(host, port, httpsProtocol);
            } else {
                int port = setting.getPort();
                if (port == -1) {
                    port = 80;
                }
                /*
                 * HTTP host configuration and relative URI
                 */
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(host, port, PROTOCOL_HTTP);
            }
            httpMethod = newHttpMethod();
            String uri = getPath();
            /*
             * Create a URI and allow for null/empty URI values
             */
            if (uri == null || uri.equals("")) {
                uri = "/";
            } else {
                uri = uri.trim();
                if (uri.endsWith("/")) {
                    uri = uri.substring(0, uri.length() - 1);
                }
            }
            final HttpMethodParams params = httpMethod.getParams();
            httpMethod.setURI(new URI(uri, true, params.getUriCharset()));
            params.setSoTimeout(httpTimeout);
            httpMethod.setQueryString(queryString);
            /*
             * Fire request
             */
            final int responseCode = client.executeMethod(hostConfiguration, httpMethod);
            /*
             * Check response code
             */
            if (200 != responseCode) {
                // GET request failed
                throw VoipNowExceptionCodes.HTTP_REQUEST_FAILED.create(host, httpMethod.getStatusLine().toString());
            }
            return httpMethod;
        } catch (final OXException e) {
            if (null != httpMethod) {
                closeResponse(httpMethod);
                httpMethod.releaseConnection();
            }
            throw e;
        } catch (final URIException e) {
            if (null != httpMethod) {
                closeResponse(httpMethod);
                httpMethod.releaseConnection();
            }
            throw VoipNowExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final NullPointerException e) {
            if (null != httpMethod) {
                closeResponse(httpMethod);
                httpMethod.releaseConnection();
            }
            throw VoipNowExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final HttpException e) {
            if (null != httpMethod) {
                closeResponse(httpMethod);
                httpMethod.releaseConnection();
            }
            throw VoipNowExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            if (null != httpMethod) {
                closeResponse(httpMethod);
                httpMethod.releaseConnection();
            }
            throw VoipNowExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            if (null != httpMethod) {
                closeResponse(httpMethod);
                httpMethod.releaseConnection();
            }
            throw VoipNowExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    protected static <M extends HttpMethod> void closeResponse(final M httpMethod) {
        if (null != httpMethod) {
            try {
                final InputStream stream = httpMethod.getResponseBodyAsStream();
                if (null != stream) {
                    stream.close();
                }
            } catch (final IOException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractVoipNowHTTPAction.class)).error(e.getMessage(), e);
            }
        }
    }

}
