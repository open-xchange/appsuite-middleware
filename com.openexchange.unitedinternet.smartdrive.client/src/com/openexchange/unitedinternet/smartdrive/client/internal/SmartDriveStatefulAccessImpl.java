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
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveCollision;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveDeadProperty;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveExceptionCodes;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveQuery;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveResource;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveResponse;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveStatefulAccess;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveUserInfo;

/**
 * {@link SmartDriveStatefulAccessImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmartDriveStatefulAccessImpl implements SmartDriveStatefulAccess {

    private final String requestPrefix;

    private final HttpClient client;
    
    private final String smartDriveServerUrl;

    private String sessionId;

    /**
     * Initializes a new {@link SmartDriveStatefulAccessImpl}.
     */
    public SmartDriveStatefulAccessImpl(final String userName, final String smartDriveServerUrl, final HttpClient client) {
        super();
        this.client = client;
        this.smartDriveServerUrl = smartDriveServerUrl;
        requestPrefix = new StringBuilder(16).append("/op/").append(userName).append('/').toString();
        ensureStatefulCookies();
    }

    private String getSessionId() {
        if (null == sessionId) {
            /*
             * Obtain proper session identifier
             */
            sessionId = (String) client.getParams().getParameter(HTTP_CLIENT_PARAM_SESSION_ID);
        }
        return sessionId;
    }

    public String obtainDownloadToken() throws SmartDriveException {
        try {
            final String uriStr = getURI("list");
            final GetMethod method = new GetMethod(uriStr);
            setStatefulHeaders(method);
            try {
                client.executeMethod(method);
                final int status = method.getStatusCode();
                if (SC_NOT_FOUND == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(smartDriveServerUrl);
                }
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * Parse response
                 */
                final SmartDriveResource firstResource;
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    firstResource = SmartDriveCoercion.parseResourceResponse(new JSONObject(body));
                } else {
                    /*
                     * Expect the body to be a JSON array
                     */
                    firstResource = SmartDriveCoercion.parseResourcesResponse(new JSONArray(body)).get(0);
                }
                final String downloadToken = firstResource.getDownloadToken();
                client.getParams().setParameter(HTTP_CLIENT_PARAM_DOWNLOAD_TOKEN, downloadToken);
                return downloadToken;
            } finally {
                method.releaseConnection();
            }
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
    
    public SmartDriveResponse<List<SmartDriveResource>> list(final String pathOfDirectory) throws SmartDriveException {
        try {
            final String uriStr = getURI("list", pathOfDirectory);
            final GetMethod method = new GetMethod(uriStr);
            setStatefulHeaders(method);
            try {
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_NOT_FOUND == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(pathOfDirectory);
                }
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * Create appropriate response
                 */
                final SmartDriveListResponse response = new SmartDriveListResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                /*
                 * Parse body
                 */
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    response.setList(Collections.singletonList(SmartDriveCoercion.parseResourceResponse(new JSONObject(body))));
                } else if (startsWith('[', body, true)) {
                    /*
                     * Expect the body to be a JSON array
                     */
                    response.setList(SmartDriveCoercion.parseResourcesResponse(new JSONArray(body)));
                } else {
                    response.setList(null);
                }
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<List<SmartDriveResource>> extendedList(final String pathOfDirectory) throws SmartDriveException {
        try {
            final String uriStr = getURI("extendedList", pathOfDirectory);
            final GetMethod method = new GetMethod(uriStr);
            setStatefulHeaders(method);
            try {
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_NOT_FOUND == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(pathOfDirectory);
                }
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * Create appropriate response
                 */
                final SmartDriveListResponse response = new SmartDriveListResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                /*
                 * Parse body
                 */
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    response.setList(Collections.singletonList(SmartDriveCoercion.parseResourceResponse(new JSONObject(body))));
                } else if (startsWith('[', body, true)) {
                    /*
                     * Expect the body to be a JSON array
                     */
                    response.setList(SmartDriveCoercion.parseResourcesResponse(new JSONArray(body)));
                } else {
                    response.setList(null);
                }
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<Object> mkcol(final String pathOfDirectory) throws SmartDriveException {
        try {
            final String uriStr = getURI("mkcol", pathOfDirectory);
            final GetMethod method = new GetMethod(uriStr);
            setStatefulHeaders(method);
            try {
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(pathOfDirectory);
                }
                if (SC_FORBIDDEN == status) {
                    throw SmartDriveExceptionCodes.ILLEGAL_CHARS_OR_READ_ONLY.create(pathOfDirectory);
                }
                if (SC_METHOD_NOT_ALLOWED == status) {
                    throw SmartDriveExceptionCodes.EQUAL_NAME.create(pathOfDirectory);
                }
                if (SC_CONFLICT == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(pathOfDirectory);
                }
                if (SC_INSUFFICIENT_STORAGE == status) {
                    throw SmartDriveExceptionCodes.INSUFFICIENT_STORAGE.create();
                }
                if (SC_CREATED != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 201 (Created): Create appropriate response
                 */
                final DefaultSmartDriveResponse response = new DefaultSmartDriveResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                response.setJSONResponseObject(null);
                return response;
            } finally {
                method.releaseConnection();
            }
        } catch (final HttpException e) {
            throw SmartDriveExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SmartDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public SmartDriveResponse<Object> rename(final String srcPath, final String newName) throws SmartDriveException {
        try {
            final String uriStr = getURI("rename", srcPath);
            final PostMethod method = new PostMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Add request body
                 */
                {
                    final JSONObject jsonRequestBody = new JSONObject();
                    jsonRequestBody.put(JSON_NEW_NAME, newName);
                    method.setRequestEntity(new ByteArrayRequestEntity(jsonRequestBody.toString().getBytes("US-ASCII"), MIME_JSON));
                }
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(srcPath);
                }
                if (SC_FORBIDDEN == status) {
                    throw SmartDriveExceptionCodes.EQUAL_NAME.create(srcPath);
                }
                if (SC_PRECONDITION_FAILED == status) {
                    throw SmartDriveExceptionCodes.EQUAL_NAME.create(srcPath);
                }
                if (SC_CONFLICT == status) {
                    throw SmartDriveExceptionCodes.CONFLICT.create(srcPath);
                }
                if (SC_NOT_FOUND == status) {
                    final DefaultSmartDriveResponse response = new DefaultSmartDriveResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)).setErrorMessage(
                        method.getStatusText()));
                    return response;
                }
                if (SC_CREATED != status && SC_NO_CONTENT != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 201 (Created): Create appropriate response
                 */
                final DefaultSmartDriveResponse response = new DefaultSmartDriveResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                response.setJSONResponseObject(null);
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<List<SmartDriveCollision>> copy(final String srcPath, final String destPath, final String[] fileNames, final boolean overwrite) throws SmartDriveException {
        return copyMove("copy", srcPath, destPath, fileNames, overwrite);
    }

    public SmartDriveResponse<List<SmartDriveCollision>> move(final String srcPath, final String destPath, final String[] fileNames, final boolean overwrite) throws SmartDriveException {
        return copyMove("move", srcPath, destPath, fileNames, overwrite);
    }

    private SmartDriveResponse<List<SmartDriveCollision>> copyMove(final String methodName, final String srcPath, final String destPath, final String[] fileNames, final boolean overwrite) throws SmartDriveException {
        try {
            final String uriStr = getURI(methodName, destPath);
            final PostMethod method = new PostMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Add request body
                 */
                {
                    final JSONObject jsonRequestBody = new JSONObject();
                    jsonRequestBody.put(JSON_SRC_PATH, srcPath);

                    final JSONArray names = new JSONArray();
                    for (final String fileName : fileNames) {
                        names.put(fileName);
                    }
                    jsonRequestBody.put(JSON_NAMES, names);

                    jsonRequestBody.put(JSON_OVER_WRITE, overwrite);

                    method.setRequestEntity(new ByteArrayRequestEntity(jsonRequestBody.toString().getBytes("US-ASCII"), MIME_JSON));
                }
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(srcPath);
                }
                if (SC_NOT_FOUND == status) {
                    final SmartDriveCollisionResponse response = new SmartDriveCollisionResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)).setErrorMessage(
                        method.getStatusText()));
                    return response;
                }
                if (SC_OK == status) {
                    /*
                     * All file copied successfully
                     */
                    final SmartDriveCollisionResponse response = new SmartDriveCollisionResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                    return response;
                }
                if (SC_MULTI_STATUS == status) {
                    /*
                     * Collisions
                     */
                    final SmartDriveCollisionResponse response = new SmartDriveCollisionResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                    /*
                     * Parse body
                     */
                    final String body = suckMethodResponse(method);
                    if (startsWith('{', body, true)) {
                        /*
                         * Expect the body to be a JSON object
                         */
                        response.setList(Collections.singletonList(SmartDriveCoercion.parseCollisionResponse(new JSONObject(body))));
                    } else if (startsWith('[', body, true)) {
                        /*
                         * Expect the body to be a JSON array
                         */
                        response.setList(SmartDriveCoercion.parseCollisionsResponse(new JSONArray(body)));
                    } else {
                        response.setList(null);
                    }
                    return response;
                }
                /*
                 * Unexpected status code
                 */
                throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<List<SmartDriveCollision>> delete(final String pathOfDirectory, final List<String> relativePaths) throws SmartDriveException {
        try {
            final String uriStr = null == pathOfDirectory ? getURI("delete") : getURI("delete", pathOfDirectory);
            final PostMethod method = new PostMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Add request body
                 */
                {
                    final JSONObject jsonRequestBody = new JSONObject();

                    final JSONArray names = new JSONArray();
                    for (final String fileName : relativePaths) {
                        names.put(fileName);
                    }
                    jsonRequestBody.put(JSON_NAMES, names);

                    method.setRequestEntity(new ByteArrayRequestEntity(jsonRequestBody.toString().getBytes("US-ASCII"), MIME_JSON));
                }
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(pathOfDirectory);
                }
                if (SC_NOT_FOUND == status) {
                    final SmartDriveCollisionResponse response = new SmartDriveCollisionResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)).setErrorMessage(
                        method.getStatusText()));
                    return response;
                }
                if (SC_OK == status) {
                    /*
                     * All resources deleted successfully
                     */
                    final SmartDriveCollisionResponse response = new SmartDriveCollisionResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                    return response;
                }
                if (SC_MULTI_STATUS == status) {
                    /*
                     * Collisions
                     */
                    final SmartDriveCollisionResponse response = new SmartDriveCollisionResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                    /*
                     * Parse body
                     */
                    final String body = suckMethodResponse(method);
                    if (startsWith('{', body, true)) {
                        /*
                         * Expect the body to be a JSON object
                         */
                        response.setList(Collections.singletonList(SmartDriveCoercion.parseCollisionResponse(new JSONObject(body))));
                    } else if (startsWith('[', body, true)) {
                        /*
                         * Expect the body to be a JSON array
                         */
                        response.setList(SmartDriveCoercion.parseCollisionsResponse(new JSONArray(body)));
                    } else {
                        response.setList(null);
                    }
                    return response;
                }
                /*
                 * Unexpected status code
                 */
                throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<Object> proppatch(final String srcPath, final String newName, final List<SmartDriveDeadProperty> deadProperties) throws SmartDriveException {
        try {
            final String uriStr = getURI("proppatch", srcPath);
            final PostMethod method = new PostMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Add request body
                 */
                {
                    final JSONObject jsonRequestBody = new JSONObject();
                    if (null != newName) {
                        jsonRequestBody.put(JSON_NEW_NAME, newName);
                    }
                    for (final SmartDriveDeadProperty deadProperty : deadProperties) {
                        jsonRequestBody.put(
                            checkDeadPropertyName(deadProperty.getPropertyName()),
                            checkDeadPropertyValue(deadProperty.getValue()));
                    }
                    if (0 == jsonRequestBody.length()) {
                        /*
                         * Nothing to do
                         */
                        final DefaultSmartDriveResponse response = new DefaultSmartDriveResponse();
                        response.setDuration(0L);
                        response.setStatus(new ResponseStatusImpl().setHttpStatusCode(SC_OK).setStatusCode(String.valueOf(SC_OK)));
                        response.setJSONResponseObject(null);
                        return response;
                    }
                    method.setRequestEntity(new ByteArrayRequestEntity(jsonRequestBody.toString().getBytes("US-ASCII"), MIME_JSON));
                }
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(srcPath);
                }
                if (SC_NOT_FOUND == status) {
                    final DefaultSmartDriveResponse response = new DefaultSmartDriveResponse();
                    response.setDuration(duration);
                    response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)).setErrorMessage(
                        method.getStatusText()));
                    return response;
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 200 (Ok)
                 */
                final DefaultSmartDriveResponse response = new DefaultSmartDriveResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                response.setJSONResponseObject(null);
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<List<SmartDriveResource>> propget(final String resourcePath, final int[] thumbNailFormatIds) throws SmartDriveException {
        try {
            final String uriStr = getURI("propget", resourcePath);
            final GetMethod method = new GetMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Set query string
                 */
                final NameValuePair nvp = new NameValuePair("thumbNailFormatIds", toString(thumbNailFormatIds, "1"));
                method.setQueryString(new NameValuePair[] { nvp });
                /*
                 * Execute
                 */
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_NOT_FOUND == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(resourcePath);
                }
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 200 (OK): Expect response to be JSON array
                 */
                final SmartDriveListResponse response = new SmartDriveListResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                /*
                 * Parse body
                 */
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    response.setList(Collections.singletonList(SmartDriveCoercion.parseResourceResponse(new JSONObject(body))));
                } else if (startsWith('[', body, true)) {
                    /*
                     * Expect the body to be a JSON array
                     */
                    response.setList(SmartDriveCoercion.parseResourcesResponse(new JSONArray(body)));
                } else {
                    response.setList(null);
                }
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<List<SmartDriveResource>> extendedPropget(final String resourcePath, final int[] thumbNailFormatIds) throws SmartDriveException {
        try {
            final String uriStr = getURI("extendedPropget", resourcePath);
            final GetMethod method = new GetMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Set query string
                 */
                final NameValuePair nvp = new NameValuePair("thumbNailFormatIds", toString(thumbNailFormatIds, "1"));
                method.setQueryString(new NameValuePair[] { nvp });
                /*
                 * Execute
                 */
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_NOT_FOUND == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(resourcePath);
                }
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 200 (OK): Expect response to be JSON array
                 */
                final SmartDriveListResponse response = new SmartDriveListResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                /*
                 * Parse body
                 */
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    response.setList(Collections.singletonList(SmartDriveCoercion.parseResourceResponse(new JSONObject(body))));
                } else if (startsWith('[', body, true)) {
                    /*
                     * Expect the body to be a JSON array
                     */
                    response.setList(SmartDriveCoercion.parseResourcesResponse(new JSONArray(body)));
                } else {
                    response.setList(null);
                }
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<List<SmartDriveResource>> search(final String pathOfDirectory, final SmartDriveQuery query, final int[] thumbNailFormatIds) throws SmartDriveException {
        try {
            final String uriStr = getURI("search", pathOfDirectory);
            final PostMethod method = new PostMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Add request body
                 */
                {
                    final JSONObject jsonRequestBody = new JSONObject();

                    jsonRequestBody.put(JSON_QUERY_TYPE, query.getQueryType().toString());
                    jsonRequestBody.put(JSON_QUERY_TEXT, query.getQueryText());

                    method.setRequestEntity(new ByteArrayRequestEntity(jsonRequestBody.toString().getBytes("US-ASCII"), MIME_JSON));
                }
                /*
                 * Execute
                 */
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_NOT_FOUND == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(pathOfDirectory);
                }
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 200 (OK): Expect response to be JSON array
                 */
                final SmartDriveListResponse response = new SmartDriveListResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                /*
                 * Parse body
                 */
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    response.setList(Collections.singletonList(SmartDriveCoercion.parseResourceResponse(new JSONObject(body))));
                } else if (startsWith('[', body, true)) {
                    /*
                     * Expect the body to be a JSON array
                     */
                    response.setList(SmartDriveCoercion.parseResourcesResponse(new JSONArray(body)));
                } else {
                    response.setList(null);
                }
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<List<SmartDriveResource>> extendedSearch(final String pathOfDirectory, final SmartDriveQuery query, final int[] thumbNailFormatIds) throws SmartDriveException {
        try {
            final String uriStr = getURI("extendedSearch", pathOfDirectory);
            final PostMethod method = new PostMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Add request body
                 */
                {
                    final JSONObject jsonRequestBody = new JSONObject();

                    jsonRequestBody.put(JSON_QUERY_TYPE, query.getQueryType().toString());
                    jsonRequestBody.put(JSON_QUERY_TEXT, query.getQueryText());

                    method.setRequestEntity(new ByteArrayRequestEntity(jsonRequestBody.toString().getBytes("US-ASCII"), MIME_JSON));
                }
                /*
                 * Execute
                 */
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_NOT_FOUND == status) {
                    throw SmartDriveExceptionCodes.NOT_FOUND.create(pathOfDirectory);
                }
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 200 (OK): Expect response to be JSON array
                 */
                final SmartDriveListResponse response = new SmartDriveListResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                /*
                 * Parse body
                 */
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    response.setList(Collections.singletonList(SmartDriveCoercion.parseResourceResponse(new JSONObject(body))));
                } else if (startsWith('[', body, true)) {
                    /*
                     * Expect the body to be a JSON array
                     */
                    response.setList(SmartDriveCoercion.parseResourcesResponse(new JSONArray(body)));
                } else {
                    response.setList(null);
                }
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<SmartDriveUserInfo> userInfo() throws SmartDriveException {
        try {
            final String uriStr = getURI("user/userInfo");
            final GetMethod method = new GetMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Execute
                 */
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 200 (OK): Expect response to be JSON array
                 */
                final SmartDriveUserResponse response = new SmartDriveUserResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                /*
                 * Parse body
                 */
                final String body = suckMethodResponse(method);
                if (startsWith('{', body, true)) {
                    /*
                     * Expect the body to be a JSON object
                     */
                    response.setUserInfo(SmartDriveCoercion.parseUserInfoResponse(new JSONObject(body)));
                } else {
                    response.setUserInfo(null);
                }
                return response;
            } finally {
                method.releaseConnection();
            }
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

    public SmartDriveResponse<Object> uploadByUrl(final String targetDirPath, final String targetFileName, final String uploadUrl, final boolean overwrite, final Map<String, String> properties) throws SmartDriveException {
        try {
            /*
             * TODO: Understand this request from spec
             */
            final String uriStr = getURI("uploadByUrl", targetDirPath, targetFileName);
            final PostMethod method = new PostMethod(uriStr);
            setStatefulHeaders(method);
            try {
                /*
                 * Add request body
                 */
                {
                    final JSONObject jsonRequestBody = new JSONObject();

                    jsonRequestBody.put(JSON_URL, uploadUrl);
                    jsonRequestBody.put(JSON_OVER_WRITE, overwrite);

                    if (null != properties && !properties.isEmpty()) {
                        for (final Entry<String, String> entry : properties.entrySet()) {
                            jsonRequestBody.put(entry.getKey(), entry.getValue());
                        }
                    }

                    method.setRequestEntity(new ByteArrayRequestEntity(jsonRequestBody.toString().getBytes("US-ASCII"), MIME_JSON));
                }
                /*
                 * Execute
                 */
                final long start = System.currentTimeMillis();
                client.executeMethod(method);
                final long duration = System.currentTimeMillis() - start;
                final int status = method.getStatusCode();
                if (SC_GENERAL_ERROR == status) {
                    throw SmartDriveExceptionCodes.GENERAL_ERROR.create(method.getStatusText());
                }
                if (SC_PRECONDITION_FAILED == status) {
                    throw SmartDriveExceptionCodes.PRECONDITION_FAILED.create(targetDirPath + "/" + targetFileName);
                }
                if (SC_CONFLICT == status) {
                    final String body = suckMethodResponse(method);
                    if (startsWith('{', body, true)) {
                        /*
                         * Expect the body to be a JSON object
                         */
                        final JSONObject errorDesc = new JSONObject(body);
                        throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(errorDesc.getInt("errorCode")), errorDesc.getString("errorMessage"));
                    }
                    throw SmartDriveExceptionCodes.CONFLICT.create(targetDirPath + "/" + targetFileName);
                }
                if (SC_OK != status) {
                    throw SmartDriveExceptionCodes.UNEXPECTED_STATUS.create(Integer.valueOf(status), method.getStatusText());
                }
                /*
                 * 200 (OK): Expect response to be JSON array
                 */
                final DefaultSmartDriveResponse response = new DefaultSmartDriveResponse();
                response.setDuration(duration);
                response.setStatus(new ResponseStatusImpl().setHttpStatusCode(status).setStatusCode(String.valueOf(status)));
                response.setJSONResponseObject(null);
                return response;
            } finally {
                method.releaseConnection();
            }
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

    private static String checkDeadPropertyName(final String propertyName) {
        if (null == propertyName) {
            throw new IllegalArgumentException("Dead property name is null.");
        }
        final String tmp = propertyName.trim();
        return tmp.startsWith("WEBDE:") ? tmp : new StringBuilder(16).append("WEBDE:").append(tmp).toString();
    }

    private static String checkDeadPropertyValue(final String value) throws SmartDriveException {
        try {
            if (null != value && value.getBytes("UTF-8").length > 512) {
                throw new IllegalArgumentException("Dead property value exceeds 512 bytes.");
            }
            return value;
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static String toString(final int[] arr, final String defaultVal) {
        if (null == arr || arr.length == 0) {
            return defaultVal;
        }
        final StringBuilder sb = new StringBuilder(16);
        sb.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            sb.append(',').append(arr[i]);
        }
        return sb.toString();
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
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
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
            httpState.addCookie(new Cookie(null, COOKIE_JSESSIONID, sessionId, "/", null, false));
        }
        /*
         * Authentication
         */
        if (addAuthentication) {
            httpState.addCookie(new Cookie(null, COOKIE_AUTHENTICATION, sessionId, "/", null, false));
        }
    }

    private static void setStatefulHeaders(final HttpMethodBase method) {
        method.setRequestHeader("User-Agent", CLIENT_NAME);
    }

    private static boolean startsWith(final char startingChar, final String toCheck, final boolean ignoreHeadingWhitespaces) {
        if (null == toCheck) {
            return false;
        }
        final int len = toCheck.length();
        if (len <= 0) {
            return false;
        }
        if (!ignoreHeadingWhitespaces) {
            return startingChar == toCheck.charAt(0);
        }
        int i = 0;
        if (Character.isWhitespace(toCheck.charAt(i))) {
            do {
                i++;
            } while (i < len && Character.isWhitespace(toCheck.charAt(i)));
        }
        if (i >= len) {
            return false;
        }
        return startingChar == toCheck.charAt(i);
    }

}
