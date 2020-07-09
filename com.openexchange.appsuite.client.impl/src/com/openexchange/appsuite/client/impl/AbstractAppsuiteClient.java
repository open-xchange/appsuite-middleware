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

package com.openexchange.appsuite.client.impl;

import static com.openexchange.appsuite.client.AppsuiteApiCall.SESSION;
import static com.openexchange.appsuite.client.common.AppsuiteClientUtils.checkJSESSIONCookie;
import static com.openexchange.appsuite.client.common.AppsuiteClientUtils.checkPublicSessionCookie;
import static com.openexchange.appsuite.client.common.AppsuiteClientUtils.checkSameOrigin;
import static com.openexchange.appsuite.client.common.AppsuiteClientUtils.checkSecretCookie;
import static com.openexchange.appsuite.client.common.OXExceptionParser.matches;
import static com.openexchange.appsuite.client.impl.osgi.AppsuiteClientWildcardProvider.HTTP_CLIENT_IDENTIFIER;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.appsuite.client.AppsuiteApiCall;
import com.openexchange.appsuite.client.AppsuiteClient;
import com.openexchange.appsuite.client.AppsuiteClientExceptions;
import com.openexchange.appsuite.client.HttpResponseParser;
import com.openexchange.appsuite.client.LoginInformation;
import com.openexchange.appsuite.client.common.AppsuiteClientUtils;
import com.openexchange.appsuite.client.common.OXExceptionParser;
import com.openexchange.appsuite.client.common.calls.login.LogoutCall;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.ManagedHttpClient;
import com.openexchange.rest.client.httpclient.util.HttpContextUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessionExceptionCodes;

/**
 * {@link AbstractAppsuiteClient}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public abstract class AbstractAppsuiteClient implements AppsuiteClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAppsuiteClient.class);

    /** The default maximum number of retry attempts */
    private static final int DEFAULT_RETRIES = 5;

    /** The base number of milliseconds to wait until retrying */
    private static final int RETRY_BASE_DELAY = 500;

    protected final ServiceLookup services;

    protected final int contextId;
    protected final int userId;
    protected final URL loginLink;

    protected final BasicHttpContext httpContext;
    protected final CookieStore cookieStore;

    private final AtomicBoolean isClosed;

    /**
     * Initializes a new {@link AbstractAppsuiteClient}.
     * 
     * @param services The service lookup
     * @param contextId The context identifier of this local OX node for logging
     * @param userId The user identifier of this local OX node for logging
     * @param loginLink The link to the target to log in into
     */
    public AbstractAppsuiteClient(ServiceLookup services, int contextId, int userId, URL loginLink) {
        super();

        this.services = services;
        this.loginLink = loginLink;
        this.contextId = contextId;
        this.userId = userId;

        this.httpContext = new BasicHttpContext();
        this.cookieStore = HttpContextUtils.createCookieStore();
        HttpContextUtils.addCookieStore(httpContext, cookieStore);

        this.isClosed = new AtomicBoolean(false);
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public URL getLoginLink() {
        return loginLink;
    }

    @Override
    public <T> T execute(AppsuiteApiCall<T> call) throws OXException {
        return execute(buildRequest(call), (r, hc) -> call.parse(r, hc));
    }

    @Override
    public <T> T execute(HttpRequestBase request, HttpResponseParser<T> parser) throws OXException {
        if (isClosed.get()) {
            throw AppsuiteClientExceptions.UNEXPECTED_ERROR.create("Client is closing.");
        }
        checkSameOrigin(request, loginLink);

        HttpResponse response = null;
        try {
            /*
             * Execute the request, log if necessary
             */
            response = getHttpClient().execute(request, httpContext);
            response = ensureRepeatability(response);
            logResponse(response);

            /*
             * Check that cookies are set
             */
            checkJSESSIONCookie(cookieStore, loginLink);
            LoginInformation infos = getLoginInformation();
            if (null != infos && Strings.isNotEmpty(infos.getRemoteSessionId())) {
                checkPublicSessionCookie(cookieStore, loginLink);
                checkSecretCookie(cookieStore, loginLink);
            }

            /*
             * Check status code
             */
            handleStatusError(response, response.getStatusLine().getStatusCode());

            /*
             * Check response for exceptions, try to re-login if session expired
             */
            OXException oxException = getNestedOXException(response);
            if (null != oxException) {
                if (matches(SessionExceptionCodes.SESSION_EXPIRED, oxException)) {
                    reLogin();
                    return execute(request, parser);
                }
                throw AppsuiteClientExceptions.REMOTE_OX_EXCEPTION.create(oxException, oxException.getMessage());
            }

            /*
             * Finally parse object
             */
            return parser.parse(response, httpContext);
        } catch (IOException e) {
            throw AppsuiteClientExceptions.IO_ERROR.create(e, e.getMessage());
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void logout() {
        if (isClosed.getAndSet(true)) {
            // Is already being closed
            return;
        }

        try {
            /*
             * Check if there is a session to remove on the remote system
             */
            LoginInformation infos = getLoginInformation();
            if (null == infos || Strings.isEmpty(infos.getRemoteSessionId())) {
                LOGGER.trace("Unable to logout client due missing session ID.");
                return;
            }
            LOGGER.trace("Client is closed. Logging out user {} in context {} on host {}", I(userId), I(contextId), loginLink.getHost());

            /*
             * Execute the logout request manually to avoid constrains from execute method (cookies, shutdown flag, etc.)
             */
            HttpResponse response = getHttpClient().execute(buildRequest(new LogoutCall()));
            int status = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != status) {
                LOGGER.info("Unable to logout user {} in context {} from host {} with error code", I(userId), I(contextId), loginLink.getHost(), I(status));
            }
        } catch (Exception e) {
            LOGGER.error("Unable to logout client", e);
        } finally {
            clean();
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed.get();
    }

    /*
     * ---------------------- HELPERS ----------------------
     */

    private ManagedHttpClient getHttpClient() throws OXException {
        return services.getServiceSafe(HttpClientService.class).getHttpClient(HTTP_CLIENT_IDENTIFIER + loginLink.getHost());
    }

    /**
     * Builds the request based on the given call
     *
     * @param <T> The class of the response object
     * @param call The call to translate into an HTTP request
     * @return The request
     * @throws OXException In case request can't be build
     */
    private <T> HttpRequestBase buildRequest(AppsuiteApiCall<T> call) throws OXException {
        /*
         * Build the URI based on the host of the login link, the prefix parsed from the login
         * and the path announced by the call.
         * Add parameters to that URI in one process
         */
        URI uri;
        try {
            URIBuilder builder = new URIBuilder(loginLink.toString());
            builder.setPath(buildPath(call));
            builder.clearParameters();
            for (Entry<String, String> entry : call.getPathParameters().entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
            LoginInformation infos = getLoginInformation();
            if (call.appendSessionToPath() && null != infos) {
                String remoteSessionId = infos.getRemoteSessionId();
                if (Strings.isNotEmpty(remoteSessionId)) {
                    builder.addParameter(SESSION, remoteSessionId);
                }
            }
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw AppsuiteClientExceptions.INVALIDE_TARGET.create(e, loginLink);
        }

        HttpRequestBase request;
        switch (call.getHttpMehtod()) {
            case DELETE:
                request = new HttpDelete(uri);
                break;
            case GET:
                request = new HttpGet(uri);
                break;
            case PATCH:
                request = new HttpPatch(uri);
                break;
            case PUT:
                request = new HttpPut(uri);
                break;
            case POST:
                request = new HttpPost(uri);
                break;
            case OPTIONS:
                request = new HttpOptions(uri);
                break;
            default:
                throw AppsuiteClientExceptions.UNEXPECTED_ERROR.create("Unkown HTTP method used.");
        }

        /*
         * Set body if necessary
         */
        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntity body = call.getBody();
            if (null != body) {
                ((HttpEntityEnclosingRequestBase) request).setEntity(body);
            }
        }

        return request;
    }

    /**
     * Builds the path for the request based on the prefix and
     * the actual API path for the original targeted host.
     * <p>
     * If the login information are not yet set, a login request is
     * assumed and the path is returned as-is
     *
     * @param call The call to get the information from
     * @return the full qualified path
     * @throws OXException In case path prefix can't be added
     */
    private <T> String buildPath(AppsuiteApiCall<T> call) throws OXException {
        if (false == call.appendPathPrefix()) {
            return call.getPath();
        }
        String prefix = AppsuiteClientUtils.parsePathPrefix(loginLink.getPath());
        if (call.getPath().startsWith("/")) {
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        } else {
            if (false == prefix.endsWith("/")) {
                prefix = prefix + "/";
            }
        }
        return prefix + call.getPath();
    }

    /**
     * Handles a status error by generating a appropriated exception
     * <p>
     * If the remote server provided an exception of the cause, the exception
     * will be set as cause of the exception
     *
     * @param response The response
     * @param statusCode The current status code
     * @throws OXException The OXException to throw for the status code
     */
    private void handleStatusError(HttpResponse response, int statusCode) throws OXException {
        if (statusCode < HttpStatus.SC_BAD_REQUEST) {
            // Nothing to do, pass response to caller
            return;
        }
        OXException parsedException = null;
        try {
            JSONObject jsonObject = AppsuiteClientUtils.parseJSONObject(response);
            parsedException = OXExceptionParser.parseException(jsonObject);
        } catch (Exception e) {
            LOGGER.debug("Error while getting error stack", e);
        }
        if (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw AppsuiteClientExceptions.CLIENT_ERROR.create(parsedException, I(statusCode));
        }
        throw AppsuiteClientExceptions.REMOTE_SERVER_ERROR.create(parsedException, I(statusCode));
    }

    /**
     * Ensures that the HTTP entity within the response is repeatable to enable
     * multiple consumption of the response
     *
     * @param response The response
     * @return A response that has a repeatable entity as per {@link HttpEntity#isRepeatable()}
     * @throws OXException In case of an I/O error
     */
    private static HttpResponse ensureRepeatability(HttpResponse response) throws OXException {
        if (response.getEntity().isRepeatable()) {
            return response;
        }
        try {
            BufferedHttpEntity httpEntity = new BufferedHttpEntity(response.getEntity());
            response.setEntity(httpEntity);
            return response;
        } catch (IOException e) {
            throw AppsuiteClientExceptions.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks if there is a nested {@link OXException} within the response and returns it
     * <p>
     * Content will be checked if the content type is
     * <li>application/json</li>
     * <li>text/javascript</li>
     * <li>text/html</li>
     * <p>
     * Content will not be checked for other known and used content types of the OX, namely:
     * <li>application/octet-stream</li>
     * <li>application/zip</li>
     * 
     * @param response The response to check
     * @return A {@link OXException} In case a exception is found in the response or <code>null</code>
     */
    private static OXException getNestedOXException(HttpResponse response) {
        String contentType = AppsuiteClientUtils.getHeaderValue(response, "Content-Type");
        if (Strings.isEmpty(contentType)) {
            return null;
        }
        try {
            JSONObject jsonObject = null;
            if (contentType.indexOf("text/javascript") > -1 || contentType.indexOf("application/json") > -1) {
                jsonObject = AppsuiteClientUtils.parseJSONObject(response);
            }
            if (contentType.indexOf("text/html") > -1) {
                String json = AppsuiteClientUtils.getJSONFromBody(response);
                if (Strings.isNotEmpty(json)) {
                    jsonObject = AppsuiteClientUtils.parseJSONObject(json);
                }
            }
            if (null != jsonObject) {
                return OXExceptionParser.parseException(jsonObject);
            }
        } catch (OXException | JSONException e) {
            LOGGER.debug("Unable to parse content", e);
        }
        return null;
    }

    /**
     * Receives the response and logs it if the level is set to <code>TRACE</code>
     *
     * @param response The actual response
     */
    private static void logResponse(HttpResponse response) {
        if (false == LOGGER.isTraceEnabled()) {
            return;
        }
        String body = AppsuiteClientUtils.getBody(response);
        if (Strings.isNotEmpty(body)) {
            LOGGER.trace("Recieved following content:\n\n{}\n", body);
        }
    }

    /**
     * Tries to login the client again. Will wait a certain time after
     * a login request failed.
     * <p>
     * If the login constantly fails, the client will be closed as per
     * {@link #isClosed}
     *
     * @throws OXException In case a valid session can't be obtained, or the login fails because of another reason then {@link SessionExceptionCodes#SESSION_EXPIRED}
     */
    private synchronized void reLogin() throws OXException {
        for (int retryCount = 0; retryCount <= DEFAULT_RETRIES; retryCount++) {
            try {
                clean();
                login();
            } catch (OXException e) {
                if (false == matches(AppsuiteClientExceptions.NO_ACCESS, e)) {
                    throw e;
                }

                if (retryCount == DEFAULT_RETRIES) {
                    /*
                     * Login attempts failed. Throw error and close client. Logout doen't need to be performed, as no valid session is available
                     */
                    LOGGER.info("Can't obtain a valid session at host {} for user {} in context {}", I(contextId), I(userId), loginLink.getHost(), e);
                    clean();
                    isClosed.set(true);
                    throw AppsuiteClientExceptions.NO_ACCESS.create(e, loginLink);
                }
                /*
                 * Try login after a certain amount of time again
                 */
                int delay = RETRY_BASE_DELAY * retryCount;
                LOGGER.debug("Error during automatic login (\"{}\"), trying again in {}ms ({}/{})...", e.getMessage(), I(delay), I(retryCount), I(DEFAULT_RETRIES));
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(delay));
            }
        }
    }

    /**
     * Cleans up any kind of HTTP related data. Removes
     * <li>cookies</li>
     * <li>HTTP context attributes</li>
     * Afterwards the client is back to an "uninitialized" state
     */
    private void clean() {
        this.cookieStore.clear();
        this.httpContext.clear();
    }
}
