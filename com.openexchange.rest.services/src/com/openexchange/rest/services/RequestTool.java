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
package com.openexchange.rest.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * Provides helpful methods for REST requests.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class RequestTool {

    private RequestTool() {
        super();
    }

    private static final AtomicReference<DispatcherPrefixService> PREFIX_REFERENCE = new AtomicReference<>(null);

    /**
     * Sets the prefix reference to given prefix service
     *
     * @param prefixService The prefix service to set
     */
    public static void setDispatcherPrefixService(DispatcherPrefixService prefixService) {
        PREFIX_REFERENCE.set(prefixService);
    }

    /**
     * Gets the dispatcher path prefix; e.g. <code>"/ajax/"</code>.
     *
     * @return The dispatcher prefix or <code>null</code> if unavailable
     */
    public static String getDispatcherPrefix() {
        DispatcherPrefixService dispatcherPrefixService = PREFIX_REFERENCE.get();
        return null == dispatcherPrefixService ? null : dispatcherPrefixService.getPrefix();
    }

    /**
     * Gets the dispatcher path prefix w/o leading/trailing slash characters; e.g. <code>"ajax"</code>.
     *
     * @return The dispatcher prefix (w/o leading/trailing slash characters) or <code>null</code> if unavailable
     */
    public static String getDispatcherPrefixWithoutSlashes() {
        DispatcherPrefixService dispatcherPrefixService = PREFIX_REFERENCE.get();
        if (null == dispatcherPrefixService) {
            return null;
        }

        String prefix = dispatcherPrefixService.getPrefix();
        if (Strings.isEmpty(prefix)) {
            return null;
        }

        if (prefix.charAt(0) == '/') {
            prefix = prefix.substring(1);
        }

        int length = prefix.length();
        if (length > 0 && prefix.charAt(length - 1) == '/') {
            prefix = prefix.substring(0, length - 1);
        }

        return prefix;
    }

    /**
     * Parses the HTTP request headers and body into an {@link AJAXRequestData} instance.
     *
     * @param context The request headers
     * @return The request data
     * @throws OXException if parsing fails
     */
    public static AJAXRequestData getAJAXRequestData(RequestContext context) throws OXException {
        return getAJAXRequestData(
            context.getHttpHeaders(),
            context.getUriInfo(),
            context.getServletRequest(),
            context.getServletResponse());
    }

    /**
     * Parses the HTTP request headers and body into an {@link AJAXRequestData} instance.
     *
     * @param httpHeaders The headers
     * @param uriInfo The URI info
     * @param servletRequest The Servlet request
     * @param servletResponse The Servlet response
     * @return The request data
     * @throws OXException if parsing fails
     */
    public static AJAXRequestData getAJAXRequestData(HttpHeaders httpHeaders, UriInfo uriInfo, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws OXException {
        AJAXRequestData requestData;
        try {
            boolean multipartFormData = MediaType.MULTIPART_FORM_DATA_TYPE.isCompatible(httpHeaders.getMediaType());
            requestData = AJAXRequestDataTools.getInstance().parseRequest(
                servletRequest,
                multipartFormData,
                multipartFormData,
                null,
                uriInfo.getPathSegments().get(0).toString(),
                servletResponse);
        } catch (IOException e) {
            throw new BadRequestException(e);
        } catch (OXException e) {
            throw new BadRequestException(e);
        }

        MultivaluedMap<String, String> requestHeaders = httpHeaders.getRequestHeaders();
        for (Map.Entry<String, List<String>> headerEntry : requestHeaders.entrySet()) {
            List<String> values = headerEntry.getValue();
            if (!values.isEmpty()) {
                String value = values.get(0);
                if (value != null) {
                    requestData.setHeader(headerEntry.getKey(), value);
                }
            }
        }

        MultivaluedMap<String, String> pathParameters = uriInfo.getPathParameters();
        for (Map.Entry<String, List<String>> paramEntry : pathParameters.entrySet()) {
            List<String> values = paramEntry.getValue();
            if (!values.isEmpty()) {
                String value = values.get(0);
                if (value != null) {
                    requestData.putParameter(paramEntry.getKey(), value);
                }
            }
        }

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        for (Map.Entry<String, List<String>> paramEntry : queryParameters.entrySet()) {
            List<String> values = paramEntry.getValue();
            if (!values.isEmpty()) {
                String value = values.get(0);
                if (value != null) {
                    requestData.putParameter(paramEntry.getKey(), value);
                }
            }
        }

        return requestData;
    }

}
