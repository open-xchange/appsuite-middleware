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

package org.glassfish.grizzly.http.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.KeepAlive;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import org.glassfish.grizzly.utils.DelayedExecutor;

/**
 * {@link CustomHttpCodecFilter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class CustomHttpCodecFilter extends org.glassfish.grizzly.http.HttpServerFilter {

    /**
     * Initializes a new {@link CustomHttpCodecFilter}.
     *
     * @deprecated
     */
    @Deprecated
    public CustomHttpCodecFilter(boolean chunkingEnabled, int maxHeadersSize, String defaultResponseContentType, KeepAlive keepAlive, DelayedExecutor executor, int maxRequestHeaders, int maxResponseHeaders) {
        super(chunkingEnabled, maxHeadersSize, defaultResponseContentType, keepAlive, executor, maxRequestHeaders, maxResponseHeaders);
    }


    @Override
    protected void onHttpHeaderError(HttpHeader httpHeader, FilterChainContext ctx, Throwable t) throws IOException {
        if (t instanceof IllegalStateException && "HTTP packet header is too large".equals(t.getMessage())) {
            final HttpRequestPacket request = (HttpRequestPacket) httpHeader;
            final HttpResponsePacket response = request.getResponse();

            sendRequestEntityTooLargeResponse(ctx, response);
        } else {
            super.onHttpHeaderError(httpHeader, ctx, t);
        }
    }

    private void sendRequestEntityTooLargeResponse(final FilterChainContext ctx, final HttpResponsePacket response) {
        if (response.getHttpStatus().getStatusCode() < 400) {
            // 413 - Request entity too large
            HttpStatus.BAD_REQUEST_400.setValues(response);
        }
        commitAndCloseAsError(ctx, response, HttpStatus.BAD_REQUEST_400, "Request headers or cookies too large. Please check your cookies.");
    }

    /*
     * caller has the responsibility to set the status of th response.
     */
    private void commitAndCloseAsError(FilterChainContext ctx, HttpResponsePacket response, HttpStatus httpStatus, String desc) {
        byte[] errorPage = getErrorPage(httpStatus.getStatusCode(), httpStatus.getReasonPhrase(),  desc).getBytes(StandardCharsets.UTF_8);

        response.setContentLength(errorPage.length);
        response.setContentType("text/html; charset=UTF-8");
        HttpContent errorHttpResponse = HttpContent.builder(response).content(new ByteBufferWrapper(ByteBuffer.wrap(errorPage))).last(true).build();

        Buffer resBuf = encodeHttpPacket(ctx, errorHttpResponse);
        ctx.write(resBuf);
        response.getProcessingState().getHttpContext().close();
    }

    /**
     * Generates a simple error page for given arguments.
     *
     * @param statusCode The status code; e.g. <code>404</code>
     * @param msg The optional status message; e.g. <code>"Not Found"</code>
     * @param desc The optional status description; e.g. <code>"The requested URL was not found on this server."</code>
     * @return A simple error page
     */
    private static String getErrorPage(int statusCode, String msg, String desc) {
        StringBuilder sb = new StringBuilder(512);
        String lineSep = System.getProperty("line.separator");
        sb.append("<!DOCTYPE html>").append(lineSep);
        sb.append("<html><head>").append(lineSep);
        {
            sb.append("<title>").append(statusCode);
            if (null != msg) {
                sb.append(' ').append(msg);
            }
            sb.append("</title>").append(lineSep);
        }

        sb.append("</head><body>").append(lineSep);

        sb.append("<h1>");
        if (null == msg) {
            sb.append(statusCode);
        } else {
            sb.append(msg);
        }
        sb.append("</h1>").append(lineSep);

        String desc0 = null == desc ? msg : desc;
        if (null != desc0) {
            sb.append("<p>").append(desc0).append("</p>").append(lineSep);
        }

        sb.append("</body></html>").append(lineSep);
        return sb.toString();
    }

}
