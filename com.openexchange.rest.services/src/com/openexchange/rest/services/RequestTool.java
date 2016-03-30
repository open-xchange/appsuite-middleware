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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;

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
     * @param uriInfo The uri info
     * @param servletRequest The servlet request
     * @param servletResponse The servlet response
     * @return The request data
     * @throws OXException if parsing fails
     */
    public static AJAXRequestData getAJAXRequestData(HttpHeaders httpHeaders, UriInfo uriInfo, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws OXException {
        AJAXRequestData requestData;
        try {
            requestData = AJAXRequestDataTools.getInstance().parseRequest(
                servletRequest,
                false,
                false,
                null,
                uriInfo.getPathSegments().get(0).toString(),
                servletResponse);
        } catch (IOException e) {
            throw new BadRequestException(e);
        } catch (OXException e) {
            throw new BadRequestException(e);
        }

        for (String header : httpHeaders.getRequestHeaders().keySet()) {
            String value = httpHeaders.getRequestHeaders().getFirst(header);
            if (value != null) {
                requestData.setHeader(header, value);
            }
        }

        for (String param : uriInfo.getPathParameters().keySet()) {
            String value = uriInfo.getPathParameters().getFirst(param);
            if (value != null) {
                requestData.putParameter(param, value);
            }
        }

        for (String param : uriInfo.getQueryParameters().keySet()) {
            String value = uriInfo.getQueryParameters().getFirst(param);
            if (value != null) {
                requestData.putParameter(param, value);
            }
        }

        return requestData;
    }

}
