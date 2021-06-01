/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.rest.client.v2.parser;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.v2.RESTMimeType;
import com.openexchange.rest.client.v2.RESTResponse;

/**
 * {@link AbstractRESTResponseParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractRESTResponseParser implements RESTResponseParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRESTResponseParser.class);

    private static final String LAST_MODIFIED_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    private final static ThreadLocal<SimpleDateFormat> LAST_MODIFIED_DATE_PARSER = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(LAST_MODIFIED_DATE_PATTERN, java.util.Locale.US);
        }
    };

    private final Map<String, BiConsumer<RESTResponse, HttpResponse>> headerParsers;
    protected final Map<RESTMimeType, RESTResponseBodyParser> responseBodyParsers;

    /**
     * Initialises a new {@link AbstractRESTResponseParser}.
     */
    public AbstractRESTResponseParser() {
        super();

        //////////////////////////////// RESPONSE HEADER PARSERS ///////////////////
        headerParsers = new HashMap<>(4);
        // Last-Modified header parser
        headerParsers.put(HttpHeaders.LAST_MODIFIED, (response, httpResponse) -> {
            String value = getHeaderValue(httpResponse, HttpHeaders.LAST_MODIFIED);
            if (Strings.isEmpty(value)) {
                return;
            }
            try {
                response.addHeader(HttpHeaders.LAST_MODIFIED, Long.toString(LAST_MODIFIED_DATE_PARSER.get().parse(value).getTime()));
            } catch (ParseException e) {
                LOGGER.debug("Could not parse the value of the 'Last-Modified' header '{}'", value, e);
            }
        });

        // ETag header parser
        headerParsers.put(HttpHeaders.ETAG, (response, httpResponse) -> {
            String value = getHeaderValue(httpResponse, HttpHeaders.ETAG);
            response.addHeader(HttpHeaders.ETAG, value);
        });

        // Content-Type header parser
        headerParsers.put(HttpHeaders.CONTENT_TYPE, (response, httpResponse) -> {
            String value = getHeaderValue(httpResponse, HttpHeaders.CONTENT_TYPE);
            if (Strings.isEmpty(value)) {
                return;
            }
            int indexOf = value.indexOf(';');
            response.addHeader(HttpHeaders.CONTENT_TYPE, indexOf < 0 ? value : value.substring(0, indexOf));
        });

        //////////////////// RESPONSE BODY PARSERS //////////////////////////
        responseBodyParsers = new HashMap<>(4);
        responseBodyParsers.put(RESTMimeType.JSON, new JsonRESTResponseBodyParser());
        responseBodyParsers.put(RESTMimeType.TEXT, new TextRESTResponseBodyParser());
    }

    @Override
    public RESTResponse parse(HttpResponse response) throws OXException, IOException {
        // Get the response code and assert
        int statusCode = assertStatusCode(response);
        if (statusCode == 304) {
            // OK, nothing was modified, no response body, return as is
            return new RESTResponse(statusCode, response.getStatusLine().getReasonPhrase());
        }

        // Prepare the response
        return prepareResponse(response);
    }

    /**
     * Prepares the {@link RESTResponse} from the specified {@link HttpResponse}
     *
     * @param httpResponse The {@link HttpResponse} to extract the content from
     * @return the {@link RESTResponse}
     * @throws IOException if an I/O error is occurred
     * @throws OXException if any other error is occurred
     */
    private RESTResponse prepareResponse(HttpResponse httpResponse) throws OXException {
        RESTResponse response = new RESTResponse(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        parseHeaders(httpResponse, response);

        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            return response;
        }
        parseResponseBody(httpResponse, response);
        return response;
    }

    /**
     * Returns the name of the remote service
     * 
     * @return The name of the remote service
     */
    protected abstract String getRemoteServiceName();

    /**
     * Parses from the specified {@link HttpResponse} the headers that are defined in the {@link #headerParsers}
     * and sets them to the specified {@link RESTResponse}
     *
     * @param httpResponse The {@link HttpResponse}
     * @param restResponse The {@link RESTResponse}
     */
    private void parseHeaders(HttpResponse httpResponse, RESTResponse restResponse) {
        for (Header header : httpResponse.getAllHeaders()) {
            BiConsumer<RESTResponse, HttpResponse> consumer = headerParsers.get(header.getName());
            if (consumer == null) {
                restResponse.addHeader(header.getName(), header.getValue());
                continue;
            }
            consumer.accept(restResponse, httpResponse);
        }
    }

    /**
     * Retrieves the value of the specified header, or <code>null</code> if no such header exists
     * or the value of the header is <code>null</code>
     *
     * @param httpResponse The {@link HttpResponse}
     * @param headerName The header's name
     * @return the value of the specified header, or <code>null</code> if no such header exists or
     *         the value of the header is <code>null</code>.
     */
    private String getHeaderValue(HttpResponse httpResponse, String headerName) {
        Header ctHeader = httpResponse.getFirstHeader(headerName);
        if (ctHeader == null) {
            return null;
        }
        String value = ctHeader.getValue();
        if (Strings.isEmpty(value)) {
            return null;
        }
        return value;
    }

    /**
     * Asserts the status code for any errors
     *
     * @param httpResponse The {@link HttpResponse}'s status code to assert
     * @return The status code
     * @throws OXException if an HTTP error is occurred (4xx or 5xx)
     */
    protected int assertStatusCode(HttpResponse httpResponse) throws OXException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        // Assert the 4xx codes
        switch (statusCode) {
            case 400:
                throw RESTExceptionCodes.BAD_REQUEST.create(httpResponse.getStatusLine().getReasonPhrase());
            case 401:
                throw RESTExceptionCodes.UNAUTHORIZED.create(httpResponse.getStatusLine().getReasonPhrase());
            case 403:
                throw RESTExceptionCodes.FORBIDDEN.create(httpResponse.getStatusLine().getReasonPhrase());
            case 404:
                throw RESTExceptionCodes.PAGE_NOT_FOUND.create();
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw RESTExceptionCodes.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        }

        // Assert the 5xx codes
        switch (statusCode) {
            case 500:
                throw RESTExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(httpResponse.getStatusLine().getReasonPhrase(), getRemoteServiceName());
            case 503:
                throw RESTExceptionCodes.REMOTE_SERVICE_UNAVAILABLE.create(httpResponse.getStatusLine().getReasonPhrase(), getRemoteServiceName());
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw RESTExceptionCodes.REMOTE_SERVER_ERROR.create(httpResponse.getStatusLine(), getRemoteServiceName());
        }
        return statusCode;
    }

    /**
     * Parses the {@link InputStream} from the specified {@link SchedJoulesResponse}
     *
     * @param response The {@link SchedJoulesResponse}
     * @return The parsed {@link R} object
     * @throws OXException if a parsing error occurs
     */
    private void parseResponseBody(HttpResponse httpResponse, RESTResponse response) throws OXException {
        String contentType = response.getHeader(HttpHeaders.CONTENT_TYPE);
        if (Strings.isEmpty(contentType)) {
            throw new IllegalArgumentException("The content type can be neither 'null' nor empty");
        }
        for (RESTResponseBodyParser bodyParser : responseBodyParsers.values()) {
            if (bodyParser.getContentTypes().contains(contentType)) {
                bodyParser.parse(httpResponse, response);
                return;
            }
        }
    }
}
