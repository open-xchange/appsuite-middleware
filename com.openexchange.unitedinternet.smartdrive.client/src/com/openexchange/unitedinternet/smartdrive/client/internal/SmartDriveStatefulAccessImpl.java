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

package com.openexchange.unitedinternet.smartdrive.client.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveExceptionCodes;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveResponse;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveStatefulAccess;

/**
 * {@link SmartDriveStatefulAccessImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmartDriveStatefulAccessImpl implements SmartDriveStatefulAccess {

    private final String requestPrefix;

    private final HttpClient client;

    private String sessionId;

    /**
     * Initializes a new {@link SmartDriveStatefulAccessImpl}.
     */
    public SmartDriveStatefulAccessImpl(final String userName, final HttpClient client) {
        super();
        this.client = client;
        requestPrefix = new StringBuilder(16).append("/op/").append(userName).append('/').toString();
        ensureStatefulCookies();
    }

    private String getSessionId() {
        if (null == sessionId) {
            /*
             * TODO: Obtain proper session identifier
             */
            sessionId = null;
        }
        return sessionId;
    }

    public SmartDriveResponse list(final String pathOfDirectory) throws SmartDriveException {
        try {
            final String uriStr = getURI("list", pathOfDirectory);
            final GetMethod getMethod = new GetMethod(uriStr);
            setStatefulHeaders(getMethod);
            try {
                final long start = System.currentTimeMillis();
                client.executeMethod(getMethod);
                final long duration = System.currentTimeMillis() - start;
                final SmartDriveResponse errorResp = handleForErrorResponse(getMethod, duration);
                if (null != errorResp) {
                    /*
                     * Not 200 (OK).
                     */
                    return errorResp;
                }
                /*
                 * Expect response to be JSON array
                 */
                final JSONArray jsonArray = new JSONArray(suckMethodResponse(getMethod));
                /*
                 * Create appropriate response
                 */
                final int status = getMethod.getStatusCode();
                final SmartDriveResponseImpl response = new SmartDriveResponseImpl();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                response.setResponseObject(jsonArray);
                return response;
            } finally {
                getMethod.releaseConnection();
            }
        } catch (final SmartDriveException e) {
            throw e;
        } catch (final HttpException e) {
            throw SmartDriveExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SmartDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw SmartDriveExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public SmartDriveResponse extendedList(final String pathOfDirectory) throws SmartDriveException {
        try {
            final String uriStr = getURI("extendedList", pathOfDirectory);
            final GetMethod getMethod = new GetMethod(uriStr);
            setStatefulHeaders(getMethod);
            try {
                final long start = System.currentTimeMillis();
                client.executeMethod(getMethod);
                final long duration = System.currentTimeMillis() - start;
                final SmartDriveResponse errorResp = handleForErrorResponse(getMethod, duration);
                if (null != errorResp) {
                    /*
                     * Not 200 (OK).
                     */
                    return errorResp;
                }
                /*
                 * Expect response to be JSON array
                 */
                final JSONArray jsonArray = new JSONArray(suckMethodResponse(getMethod));
                /*
                 * Create appropriate response
                 */
                final int status = getMethod.getStatusCode();
                final SmartDriveResponseImpl response = new SmartDriveResponseImpl();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                response.setResponseObject(jsonArray);
                return response;
            } finally {
                getMethod.releaseConnection();
            }
        } catch (final SmartDriveException e) {
            throw e;
        } catch (final HttpException e) {
            throw SmartDriveExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SmartDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw SmartDriveExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /*-
     * ---------------------------- HELPER METHODS ----------------------------
     */

    private static SmartDriveResponse handleForErrorResponse(final HttpMethodBase method, final long duration) {
        final int status = method.getStatusCode();
        if (SC_NOT_FOUND != status) {
            final SmartDriveResponseImpl response = new SmartDriveResponseImpl();
            response.setDuration(duration);
            response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)).setErrorMessage(method.getStatusText()));
            return response;
        }
        if (SC_GENERAL_ERROR != status) {
            final SmartDriveResponseImpl response = new SmartDriveResponseImpl();
            response.setDuration(duration);
            response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)).setErrorMessage(method.getStatusText()));
            return response;
        }
        if (SC_OK != status) {
            final SmartDriveResponseImpl response = new SmartDriveResponseImpl();
            response.setDuration(duration);
            response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)).setErrorMessage(method.getStatusText()));
            return response;
        }
        return null;
    }

    /**
     * Sucks method's response input stream using direct buffering.
     * 
     * @param method The method
     * @return The read string
     * @throws IOException If an I/O error occurs
     */
    private static String suckMethodResponse(final HttpMethodBase method) throws IOException {
        final Reader reader = new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8");
        try {
            final StringBuilder sb = new StringBuilder(8192);
            final char[] buf = new char[2048];
            int read;
            while ((read = reader.read(buf, 0, buf.length)) >= 0) {
                sb.append(buf, 0, read);
            }
            return sb.toString();
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                org.apache.commons.logging.LogFactory.getLog(SmartDriveStatefulAccessImpl.class).error(e.getMessage(), e);
            }
        }
    }

    private String getURI(final String methodName, final String... pathElements) throws SmartDriveException {
        try {
            final URI uri = new URI(requestPrefix, false, "ISO-8859-1");
            final StringBuilder sb = new StringBuilder(uri.getPath()).append(methodName);
            for (final String pathElement : pathElements) {
                sb.append('/').append(pathElement);
            }
            uri.setPath(sb.toString());
            return uri.toString();
        } catch (final URIException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void ensureStatefulCookies() {
        final String sessionId = getSessionId();
        final HttpState httpState = client.getState();
        /*
         * Set stateful cookies if absent
         */
        final Cookie[] cookies = httpState.getCookies();
        boolean addJSessionId = false;
        boolean addAuthentication = false;
        for (final Cookie cookie : cookies) {
            if (COOKIE_JSESSIONID.equals(cookie.getName())) {
                addJSessionId = !sessionId.equals(cookie.getValue());
            } else if (COOKIE_AUTHENTICATION.equals(cookie.getName())) {
                addAuthentication = !sessionId.equals(cookie.getValue());
            }
        }
        /*
         * JSESSIONID
         */
        if (addJSessionId) {
            final Cookie jsessionidCookie = new Cookie();
            jsessionidCookie.setName(COOKIE_JSESSIONID);
            jsessionidCookie.setValue(sessionId);
            /*
             * Default path
             */
            jsessionidCookie.setPath("/");
            /*
             * The cookie is not stored persistently and will be deleted when the Web browser exits
             */
            jsessionidCookie.setExpiryDate(new Date(0));
            httpState.addCookie(jsessionidCookie);
        }
        /*
         * Authentication
         */
        if (addAuthentication) {
            final Cookie authenticationCookie = new Cookie();
            authenticationCookie.setName(COOKIE_AUTHENTICATION);
            authenticationCookie.setValue(sessionId);
            /*
             * Default path
             */
            authenticationCookie.setPath("/");
            /*
             * The cookie is not stored persistently and will be deleted when the Web browser exits
             */
            authenticationCookie.setExpiryDate(new Date(0));
            httpState.addCookie(authenticationCookie);
        }
    }

    private void setStatefulHeaders(final HttpMethodBase method) {
        method.setRequestHeader("User-Agent", CLIENT_NAME);
    }

}
