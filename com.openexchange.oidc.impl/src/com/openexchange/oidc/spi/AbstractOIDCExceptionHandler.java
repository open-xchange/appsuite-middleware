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

package com.openexchange.oidc.spi;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCExceptionHandler;
import com.openexchange.tools.servlet.http.Tools;

public abstract class AbstractOIDCExceptionHandler implements OIDCExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOIDCExceptionHandler.class);

    protected String getDefaultErrorResponsePage(OXException exception, HttpServletRequest request) {
        String result = "";

        boolean isOauthFailure = exception == null;
        String headTitle = "";
        String category = "error_category: ";
        String errorType = "error_type: ";
        String errorDescription = "error_description: ";
        if (isOauthFailure) {
            headTitle = "Authentication provider error";
            category += "oauth-error";
            String error = request.getParameter("error");
            errorType += error != null ? StringEscapeUtils.escapeHtml4(error) : "";
            String errorDesc = request.getParameter("error_description");
            errorDescription += errorDesc != null ? StringEscapeUtils.escapeHtml4(errorDesc) : "";
        } else {
            headTitle = "500 - Internal Server Error";
            category += "ox-error";
            errorType += "internal server error";
            errorDescription += "An internal error occurred, please try again later.";
        }

        result += "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <title>"+ headTitle + "</title>\n" +
            "  </head>\n" +
            "<body>\n" +
            "<h1>An error occured</h1>\n" +
            "<p>" + category + "</p>\n" +
            "<p>" + errorType + "</p>\n" +
            "<p>" + errorDescription + "</p>\n" +
            "</body></html>\n";
        return result;
    }

    @Override
    public void handleAuthenticationFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException {
        LOG.trace("handleAuthenticationFailed(request: {}, HttpServletResponse response, OXException: {})", request.getRequestURI(), exception.getExceptionCode());
        this.handleResponseException(request, response, exception);
    }

    @Override
    public void handleLogoutFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException {
        LOG.trace("handleAuthenticationFailed(request: {}, HttpServletResponse response, OXException: {})", request.getRequestURI(), exception.getExceptionCode());
        this.handleResponseException(request, response, exception);
    }

    @Override
    public void handleResponseException(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException {
        LOG.trace("handleException(request: {}, HttpServletResponse response, OXException: {})", request.getRequestURI(), exception.getMessage());
        LOG.error(exception.getMessage(), exception);
        String content = this.getDefaultErrorResponsePage(exception, request);
        if (Strings.isEmpty(content)) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        Tools.disableCaching(response);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setCharacterEncoding(Charsets.UTF_8_NAME);
        response.setContentType("text/html");
        response.setContentLength(content.getBytes(Charsets.UTF_8).length);
        response.getWriter().write(content);
    }
}
