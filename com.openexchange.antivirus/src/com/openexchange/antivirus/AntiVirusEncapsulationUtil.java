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

package com.openexchange.antivirus;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.ImmutableList;
import com.openexchange.antivirus.internal.DefaultAntiVirusEncapsulatedContent;
import com.openexchange.antivirus.internal.DefaultAntiVirusEncapsulatedContent.Builder;
import com.openexchange.servlet.HttpStatusCode;

/**
 * {@link AntiVirusEncapsulationUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class AntiVirusEncapsulationUtil {

    private static final List<String> BLACK_LISTED_HEADERS = ImmutableList.of("cookie");

    /**
     * Creates an {@link AntiVirusEncapsulatedContent} from the specified request and response
     *
     * @param request The request
     * @param response The response
     * @return The {@link AntiVirusEncapsulatedContent}
     */
    public static final AntiVirusEncapsulatedContent encapsulate(HttpServletRequest request, HttpServletResponse response) {
        Builder builder = DefaultAntiVirusEncapsulatedContent.builder();
        encapsulateRequest(request, builder);
        encapsulateResponse(response, builder);
        return builder.build();
    }

    /**
     * Encapsulates the optional request and request headers to the builder
     *
     * @param request The request
     * @param builder The builder
     */
    private static void encapsulateRequest(HttpServletRequest request, Builder builder) {
        if (request == null) {
            return;
        }
        StringBuilder b = new StringBuilder(128);
        b.append(request.getMethod()).append(" ").append(request.getRequestURI()).append("?").append(request.getQueryString()).append(" HTTP/1.1");
        builder.withOriginalRequest(b.toString());
        
        Enumeration<String> enumeration = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>(4);
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            headers.put(key, request.getHeader(key));
        }
        BLACK_LISTED_HEADERS.stream().forEach(header -> headers.remove(header));
        builder.withOriginalRequestHeaders(headers);
    }

    /**
     * Encapsulates the optional response and response headers to the builder
     * 
     * @param response The response
     * @param builder The builder
     */
    private static void encapsulateResponse(HttpServletResponse response, Builder builder) {
        if (response == null) {
            return;
        }
        int httpStatus = response.getStatus();
        StringBuilder b = new StringBuilder(128);
        b.append("HTTP/1.1 ").append(httpStatus).append(" ").append(HttpStatusCode.getHttpStatus(httpStatus).getReasonPhrase());
        builder.withOriginalResponseLine(b.toString());
        
        Map<String, String> headers = new HashMap<>(4);
        for (String name : response.getHeaderNames()) {
            headers.put(name, response.getHeader(name));
        }
        builder.withOriginalResponseHeaders(headers);
    }
}
