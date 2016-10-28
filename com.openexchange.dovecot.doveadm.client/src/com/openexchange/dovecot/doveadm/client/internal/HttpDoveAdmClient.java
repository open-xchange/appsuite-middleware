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

package com.openexchange.dovecot.doveadm.client.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.dovecot.doveadm.client.DoveAdmClientExceptionCodes;
import com.openexchange.dovecot.doveadm.client.DoveAdmCommand;
import com.openexchange.dovecot.doveadm.client.DoveAdmResponse;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.endpointpool.Endpoint;

/**
 * {@link HttpDoveAdmClient} - The REST client for DoveAdm interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v1.0.0
 */
public class HttpDoveAdmClient implements DoveAdmClient {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpDoveAdmClient.class);

    // -------------------------------------------------------------------------------------------------------------- //

    /** The status code policy to obey */
    public static interface StatusCodePolicy {

        /**
         * Examines given status line
         *
         * @param httpResponse The HTTP response
         * @throws OXException If an Open-Xchange error is yielded from status
         * @throws HttpResponseException If status is interpreted as an error
         */
        void handleStatusCode(HttpResponse httpResponse) throws OXException, HttpResponseException;
    }

    /** The default status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_DEFAULT = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws OXException, HttpResponseException {
            final StatusLine statusLine = httpResponse.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                if (404 == statusCode) {
                    throw DoveAdmClientExceptionCodes.NOT_FOUND_SIMPLE.create();
                }
                String reason;
                try {
                    InputStreamReader reader = new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8);
                    String sResponse = Streams.reader2string(reader);
                    JSONObject jsonObject = new JSONObject(sResponse);
                    reason = jsonObject.getString("reason");
                } catch (final Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }
        }
    };

    /** The status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> while ignoring <code>404</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_IGNORE_NOT_FOUND = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws HttpResponseException {
            final StatusLine statusLine = httpResponse.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            if ((statusCode < 200 || statusCode >= 300) && statusCode != 404) {
                String reason;
                try {
                    final JSONObject jsonObject = new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = jsonObject.getJSONObject("error").getString("message");
                } catch (final Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }
        }
    };

    // -------------------------------------------------------------------------------------------------------------- //

    private final HttpDoveAdmEndpointManager endpointManager;
    private final BasicHttpContext localcontext;
    private final String authorizationHeaderValue;

    /**
     * Initializes a new {@link HttpDoveAdmClient}.
     */
    public HttpDoveAdmClient(String apiKey, HttpDoveAdmEndpointManager endpointManager) {
        super();
        this.endpointManager = endpointManager;

        // Generate BASIC scheme object and stick it to the local execution context
        final BasicHttpContext context = new BasicHttpContext();
        final BasicScheme basicAuth = new BasicScheme();
        context.setAttribute("preemptive-auth", basicAuth);
        this.localcontext = context;
        String encodedApiKey = BaseEncoding.base64().encode(apiKey.getBytes(Charsets.UTF_8));
        authorizationHeaderValue = "X-Dovecot-API " + encodedApiKey;
    }

    private CallProperties getCallProperties(HttpDoveAdmCall call) throws OXException {
        HttpClientAndEndpoint clientAndUri = endpointManager.getHttpClientAndUri(call);
        Endpoint endpoint = clientAndUri.endpoint;
        String sUrl = endpoint.getBaseUri();
        try {
            URI uri = new URI(sUrl);
            HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            return new CallProperties(uri, clientAndUri.httpClient, targetHost, endpoint);
        } catch (final URISyntaxException e) {
            throw DoveAdmClientExceptionCodes.INVALID_DOVECOT_URL.create(null == sUrl ? "<empty>" : sUrl);
        }
    }

    /**
     * Shuts-down this instance.
     */
    public void shutDown() {
        endpointManager.shutDown();
    }

    /**
     * Builds the JSON request body from specified command.
     * <pre>
     * [
     *   ["command", {"parameter":"value"}, "optional identifier"]
     * ]
     * </pre>
     *
     * @param command The command to build from
     * @return The resulting JSON request body
     */
    private JSONArray buildRequestBody(DoveAdmCommand command) {
        return buildRequestBody(Collections.singletonList(command));
    }

    /**
     * Builds the JSON request body from specified commands collection.
     * <pre>
     * [
     *   ["command1", {"parameter1":"value1"}, "optional identifier"],
     *   ...
     *   ["commandN", {"parameterN":"valueN"}, "optional identifier"]
     * ]
     * </pre>
     *
     * @param commands The commands to build from
     * @return The resulting JSON request body
     */
    private JSONArray buildRequestBody(Collection<DoveAdmCommand> commands) {
        JSONArray jCommands = new JSONArray(commands.size());
        for (DoveAdmCommand command : commands) {
            String optionalIdentifier = command.getOptionalIdentifier();
            if (Strings.isEmpty(optionalIdentifier)) {
                jCommands.put(new JSONArray(2).put(command.getCommand()).put(new JSONObject(command.getParameters())));
            } else {
                jCommands.put(new JSONArray(3).put(command.getCommand()).put(new JSONObject(command.getParameters())).put(optionalIdentifier));
            }
        }
        return jCommands;
    }

    @Override
    public DoveAdmResponse executeCommand(DoveAdmCommand command) throws OXException {
        if (null == command) {
            return null;
        }

        // Build JSON request body & execute command
        JSONValue jRetval = executePost(HttpDoveAdmCall.DEFAULT, null, null, buildRequestBody(command), ResultType.JSON);

        // Check result (should be a JSON array)
        if (!jRetval.isArray()) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create("Expected a JSON array as return value, but is a JSON object: " + jRetval);
        }

        JSONArray jResponses = jRetval.toArray();
        if (jResponses.length() != 1) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create("Unexpected number of responses: " + jRetval);
        }

        ParsedResponses responses = ParsedResponses.valueFor(jResponses);
        if (responses.isEmpty()) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create("Empty or invalid responses: " + jRetval);
        }

        if (Strings.isEmpty(command.getOptionalIdentifier())) {
            // Grab first response
            return responses.getResponses().get(0);
        }

        DoveAdmResponse response = responses.getTaggedResponse(command.getOptionalIdentifier());
        if (null == response) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create("No such response: " + command.getOptionalIdentifier());
        }
        return response;
    }

    @Override
    public List<DoveAdmResponse> executeCommands(List<DoveAdmCommand> commands) throws OXException {
        if (null == commands || commands.isEmpty()) {
            return Collections.emptyList();
        }

        checkOptionalIdentifiers(commands);

        // Build JSON request body & execute command
        JSONValue jRetval = executePost(HttpDoveAdmCall.DEFAULT, null, null, buildRequestBody(commands), ResultType.JSON);

        // Check result (should be a JSON array)
        if (!jRetval.isArray()) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create("Expected a JSON array as return value, but is a JSON object: " + jRetval);
        }

        JSONArray jResponses = jRetval.toArray();
        if (jResponses.length() != commands.size()) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create("Unexpected number of responses: " + jRetval);
        }

        ParsedResponses responses = ParsedResponses.valueFor(jResponses);
        if (responses.isEmpty()) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create("Empty or invalid responses: " + jRetval);
        }

        List<DoveAdmResponse> doveAdmDataResponses = new ArrayList<>(commands.size());
        int i = 0;
        for (DoveAdmCommand command : commands) {
            if (Strings.isEmpty(command.getOptionalIdentifier())) {
                // Grab matching response
                doveAdmDataResponses.add(responses.getResponses().get(i));
            } else {
                DoveAdmResponse response = responses.getTaggedResponse(command.getOptionalIdentifier());
                if (null == response) {
                    throw DoveAdmClientExceptionCodes.JSON_ERROR.create("No such response: " + command.getOptionalIdentifier());
                }
                doveAdmDataResponses.add(response);
            }
            i++;
        }

        return doveAdmDataResponses;
    }

    private void checkOptionalIdentifiers(List<DoveAdmCommand> commands) throws OXException {
        Set<String> oids = new HashSet<>(commands.size());
        String oid;
        for (DoveAdmCommand command : commands) {
            oid = command.getOptionalIdentifier();
            if (Strings.isNotEmpty(oid) && false == oids.add(oid)) {
                throw DoveAdmClientExceptionCodes.DUPLICATE_OPTIONAL_IDENTIFIER.create(oid);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void setCommonHeaders(HttpRequestBase request) {
        request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    }

    private <R> R executePost(HttpDoveAdmCall call, String path, Map<String, String> parameters, JSONValue jBody, ResultType<R> resultType) throws OXException {
        CallProperties callProperties = getCallProperties(call);

        HttpPost post = null;
        try {
            URI uri = buildUri(callProperties.uri, toQueryString(parameters), path);
            post = new HttpPost(uri);
            setCommonHeaders(post);
            post.setEntity(new InputStreamEntity(new JSONInputStream(jBody, "UTF-8"), -1L, ContentType.APPLICATION_JSON));

            return handleHttpResponse(execute(post, callProperties.targetHost, callProperties.httpClient), resultType);
        } catch (final HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed
                throw DoveAdmClientExceptionCodes.AUTH_ERROR.create(e, e.getMessage());
            }
            throw handleHttpResponseError(null, e);
        } catch (final IOException e) {
            throw handleIOError(e, callProperties.endpoint, call);
        } catch (final RuntimeException e) {
            throw DoveAdmClientExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            reset(post);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    /**
     * Builds the URI from given arguments
     *
     * @param baseUri The base URI
     * @param queryString The query string parameters
     * @return The built URI string
     * @throws IllegalArgumentException If the given string violates RFC 2396
     */
    protected static URI buildUri(URI baseUri, List<NameValuePair> queryString, String optPath) {
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(baseUri.getScheme()).setHost(baseUri.getHost()).setPort(baseUri.getPort()).setPath(null == optPath ? baseUri.getPath() : optPath).setQuery(null == queryString ? null : URLEncodedUtils.format(queryString, "UTF-8"));
            return builder.build();
        } catch (final URISyntaxException x) {
            throw new IllegalArgumentException("Failed to build URI", x);
        }
    }

    /**
     * Gets a (parameters) map for specified arguments.
     *
     * @param args The arguments
     * @return The resulting map
     */
    protected static Map<String, String> mapFor(String... args) {
        if (null == args) {
            return null;
        }

        int length = args.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }

        Map<String, String> map = new LinkedHashMap<String, String>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(args[i], args[i+1]);
        }
        return map;
    }

    /**
     * Turns specified JSON value into an appropriate HTTP entity.
     *
     * @param jValue The JSON value
     * @return The HTTP entity
     * @throws JSONException If a JSON error occurs
     * @throws IOException If an I/O error occurs
     */
    protected InputStreamEntity asHttpEntity(JSONValue jValue) throws JSONException, IOException {
        if (null == jValue) {
            return null;
        }

        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean error = true;
        try {
            final OutputStreamWriter osw = new OutputStreamWriter(sink.asOutputStream(), Charsets.UTF_8);
            jValue.write(osw);
            osw.flush();
            final InputStreamEntity entity = new InputStreamEntity(sink.getStream(), sink.getLength(), ContentType.APPLICATION_JSON);
            error = false;
            return entity;
        } catch (final OXException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(null == cause ? e : cause);
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    /**
     * Gets the appropriate query string for given parameters
     *
     * @param parameters The parameters
     * @return The query string
     */
    protected static List<NameValuePair> toQueryString(Map<String, String> parameters) {
        if (null == parameters || parameters.isEmpty()) {
            return null;
        }
        final List<NameValuePair> l = new LinkedList<NameValuePair>();
        for (final Map.Entry<String, String> e : parameters.entrySet()) {
            l.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }
        return l;
    }

    /**
     * Executes specified HTTP method/request using given HTTP client instance.
     *
     * @param method The method/request to execute
     * @param targetHost The target host
     * @param httpClient The HTTP client to use
     * @return The HTTP response
     * @throws ClientProtocolException If client protocol error occurs
     * @throws IOException If an I/O error occurs
     */
    protected HttpResponse execute(HttpRequestBase method, HttpHost targetHost, DefaultHttpClient httpClient) throws ClientProtocolException, IOException {
        return execute(method, targetHost, httpClient, localcontext);
    }

    /**
     * Executes specified HTTP method/request using given HTTP client instance.
     *
     * @param method The method/request to execute
     * @param targetHost The target host
     * @param httpClient The HTTP client to use
     * @param context The context
     * @return The HTTP response
     * @throws ClientProtocolException If client protocol error occurs
     * @throws IOException If an I/O error occurs
     */
    protected HttpResponse execute(HttpRequestBase method, HttpHost targetHost, DefaultHttpClient httpClient, BasicHttpContext context) throws ClientProtocolException, IOException {
        return httpClient.execute(targetHost, method, context);
    }

    /**
     * Resets given HTTP request
     *
     * @param request The HTTP request
     */
    protected static void reset(HttpRequestBase request) {
        if (null != request) {
            try {
                request.reset();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists, is closed silently.
     *
     * @param response The HTTP response to consume and close
     */
    protected static void consume(HttpResponse response) {
        if (null != response) {
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Closes the supplied HTTP request & response resources silently.
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    protected static void close(HttpRequestBase request, HttpResponse response) {
        consume(response);
        reset(request);
    }

    /**
     * Handles given HTTP response while expecting <code>200 (Ok)</code> status code.
     *
     * @param httpResponse The HTTP response
     * @param type The type of the result object
     * @return The result object
     * @throws OXException If an Open-Xchange error occurs
     * @throws ClientProtocolException If a client protocol error occurs
     * @throws IOException If an I/O error occurs
     */
    protected <R> R handleHttpResponse(HttpResponse httpResponse, ResultType<R> type) throws OXException, ClientProtocolException, IOException {
        return handleHttpResponse(httpResponse, STATUS_CODE_POLICY_DEFAULT, type);
    }

    /**
     * Handles given HTTP response while expecting given status code.
     *
     * @param httpResponse The HTTP response
     * @param policy The status code policy to obey
     * @param type The type of the result object
     * @return The result object
     * @throws OXException If an Open-Xchange error occurs
     * @throws ClientProtocolException If a client protocol error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If content stream cannot be created
     */
    protected <R> R handleHttpResponse(HttpResponse httpResponse, StatusCodePolicy policy, ResultType<R> type) throws OXException, ClientProtocolException, IOException {
        policy.handleStatusCode(httpResponse);

        // OK, continue
        if (ResultType.JSON == type) {
            try {
                return (R) JSONObject.parse(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
            } catch (final JSONException e) {
                throw DoveAdmClientExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            } finally {
                consume(httpResponse);
            }
        }

        if (ResultType.VOID == type) {
            consume(httpResponse);
            return null;
        }

        if (ResultType.INPUT_STREAM == type) {
            return (R) httpResponse.getEntity().getContent();
        }

        R retval = parseIntoObject(httpResponse.getEntity().getContent(), type.getType());
        consume(httpResponse);
        return retval;
    }

    /**
     * The Jackson object mapper instance.
     */
    private static final ObjectMapper MAPPER;
    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER = objectMapper;
    }

    /**
     * Gets the object mapper.
     *
     * @return The object mapper
     */
    protected static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    /**
     * Parses the JSON data provided by given input stream to its Java object representation.
     *
     * @param inputStream The input stream to read JSON data from
     * @param clazz The type of the Java representation
     * @return The Java object representation
     * @throws OXException If Java object representation cannot be returned
     */
    protected static <T> T parseIntoObject(InputStream inputStream, Class<T> clazz) throws OXException {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jp = jsonFactory.createParser(inputStream);
            return getObjectMapper().readValue(jp, clazz);
        } catch (JsonGenerationException | JsonMappingException | JsonParseException e) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw DoveAdmClientExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw DoveAdmClientExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles given I/O error.
     *
     * @param e The I/O error
     * @param endpoint The end-point for which an I/O error occurred
     * @param call The associated call
     * @return The resulting exception
     */
    protected OXException handleIOError(IOException e, Endpoint endpoint, HttpDoveAdmCall call) {
        final Throwable cause = e.getCause();
        if (cause instanceof AuthenticationException) {
            return DoveAdmClientExceptionCodes.AUTH_ERROR.create(cause, cause.getMessage());
        }

        LOG.info("Encountered I/O error \"{}\" ({}) while trying to access DoveAdm end-point {}. End-point will therefore be added to black-list until re-available", e.getMessage(), e.getClass().getName(), endpoint.getBaseUri());
        endpointManager.blacklist(call, endpoint);
        return DoveAdmClientExceptionCodes.IO_ERROR.create(e, e.getMessage());
    }

    /** Status code (401) indicating that the request requires HTTP authentication. */
    private static final int SC_UNAUTHORIZED = 401;

    /** Status code (404) indicating that the requested resource is not available. */
    private static final int SC_NOT_FOUND = 404;

    /**
     * Handles given HTTP response error.
     *
     * @param identifier The optional identifier for associated Microsoft OneDrive resource
     * @param e The HTTP error
     * @return The resulting exception
     */
    protected OXException handleHttpResponseError(String identifier, HttpResponseException e) {
        if (null != identifier && SC_NOT_FOUND == e.getStatusCode()) {
            return DoveAdmClientExceptionCodes.NOT_FOUND.create(e, identifier);
        }
        if (SC_UNAUTHORIZED == e.getStatusCode()) {
            return DoveAdmClientExceptionCodes.AUTH_ERROR.create();
        }
        return DoveAdmClientExceptionCodes.DOVEADM_SERVER_ERROR.create(e, Integer.valueOf(e.getStatusCode()), e.getMessage());
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static final class CallProperties {

        final URI uri;
        final HttpHost targetHost;
        final DefaultHttpClient httpClient;
        final Endpoint endpoint;

        CallProperties(URI uri, DefaultHttpClient httpClient, HttpHost targetHost, Endpoint endpoint) {
            super();
            this.uri = uri;
            this.httpClient = httpClient;
            this.targetHost = targetHost;
            this.endpoint = endpoint;
        }
    }

}
