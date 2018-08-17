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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.microsoft.graph.api.client;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.client.auxiliary.MicrosoftGraphResponseParser;
import com.openexchange.rest.client.RESTResponse;
import com.openexchange.rest.client.RESTResponseParser;
import com.openexchange.rest.client.exception.RESTExceptionCodes;

/**
 * {@link MicrosoftGraphRESTResponseParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphRESTResponseParser implements RESTResponseParser {

    private static final String REMOTE_SERVICE = "Microsoft Graph";

    /**
     * Initialises a new {@link MicrosoftGraphRESTResponseParser}.
     */
    public MicrosoftGraphRESTResponseParser() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.RESTResponseParser#parse(com.openexchange.rest.client.CloseableHttpResponse)
     */
    @Override
    public RESTResponse parse(CloseableHttpResponse response) throws OXException, IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw RESTExceptionCodes.PARSE_ERROR.create("The response entity is 'null'");
        }
        int statusCode = assertStatusCode(response);
        // TODO: assert the code

        return prepareResponse(response);
    }

    /**
     * Prepares the {@link SchedJoulesResponse} from the specified {@link HttpResponse}
     *
     * @param httpResponse The {@link HttpResponse} to extract the content from
     * @return the {@link SchedJoulesResponse}
     * @throws IOException if an I/O error is occurred
     * @throws OXException if any other error is occurred
     */
    private MicrosoftGraphResponse prepareResponse(HttpResponse httpResponse) throws IOException, OXException {
        MicrosoftGraphResponse response = new MicrosoftGraphResponse(httpResponse.getStatusLine().getStatusCode());
        String value = getHeaderValue(httpResponse, HttpHeaders.CONTENT_TYPE);
        if (Strings.isNotEmpty(value)) {
            int indexOf = value.indexOf(';');
            response.addHeader(HttpHeaders.CONTENT_TYPE, indexOf < 0 ? value : value.substring(0, indexOf));
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            return response;
        }
        response.setStream(entity.getContent());
        response.setResponseBody(MicrosoftGraphResponseParser.parse(response));
        return response;
    }
    
    

    /**
     * @param httpResponse
     * @param contentType
     * @return
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
    private int assertStatusCode(HttpResponse httpResponse) throws OXException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        // Assert the 4xx codes
        switch (statusCode) {
            case 401:
                throw RESTExceptionCodes.UNAUTHORIZED.create(httpResponse.getStatusLine().getReasonPhrase());
            case 404:
                throw RESTExceptionCodes.PAGE_NOT_FOUND.create();
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw RESTExceptionCodes.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        }

        // Assert the 5xx codes
        switch (statusCode) {
            case 500:
                throw RESTExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(httpResponse.getStatusLine().getReasonPhrase(), REMOTE_SERVICE);
            case 503:
                throw RESTExceptionCodes.REMOTE_SERVICE_UNAVAILABLE.create(httpResponse.getStatusLine().getReasonPhrase(), REMOTE_SERVICE);
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw RESTExceptionCodes.REMOTE_SERVER_ERROR.create(httpResponse.getStatusLine(), REMOTE_SERVICE);
        }
        return statusCode;
    }

}
