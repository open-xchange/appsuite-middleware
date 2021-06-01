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
