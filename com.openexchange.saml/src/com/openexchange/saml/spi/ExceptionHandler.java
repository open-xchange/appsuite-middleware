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

package com.openexchange.saml.spi;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.authorization.AuthorizationExceptionCodes;
import com.openexchange.context.ContextExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.saml.SAMLWebSSOProvider;
import com.openexchange.tools.servlet.http.Tools;


/**
 * An extension point to define how exceptions thrown during SAML authentication flows shall be handled.
 * Default implementations basically respond with a not-so-pretty error page, displaying a technical error
 * message. For customer plug-ins it is advised to override these methods in a ways that they return
 * pretty error pages with proper theming or redirect users to according exit locations.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface ExceptionHandler {

    /**
     * This method is called when {@link SAMLWebSSOProvider#handleAuthnResponse(HttpServletRequest, HttpServletResponse, com.openexchange.saml.SAMLConfig.Binding)}
     * failed with an exception. An exception handler is responsible for answering the HTTP request then. Normally the request is a redirect, triggered by the
     * IdP and the result is directly visible to the user. I.e. this method is responsible to help the user out of this messed up situation.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param exception The thrown exception
     */
    void handleAuthnResponseFailed(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OXException exception);

    /**
     * This method is called when {@link SAMLWebSSOProvider#handleLogoutRequest(HttpServletRequest, HttpServletResponse, com.openexchange.saml.SAMLConfig.Binding)}
     * failed with an exception. An exception handler is responsible for answering the HTTP request then. Normally the request is a redirect, triggered by the
     * IdP and the result is directly visible to the user. I.e. this method is responsible to help the user out of this messed up situation.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param exception The thrown exception
     */
    void handleLogoutResponseFailed(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OXException e);

    /**
     * This method is called when redeeming the token from SAML login request does not yield a valid/existing session reservation; e.g. the
     * token is expired.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param token The token that could not be redeemed
     */
    default void handleSessionReservationExpired(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String token) {
        int statusCode = HttpServletResponse.SC_FORBIDDEN;
        String statusMessage = "Forbidden";
        String message = "Authentication failed, please try again.";
        sendErrorPage(httpResponse, statusCode, statusMessage, message);
    }

    /**
     * This method is called when the context associated with the session of a SAML login request is disabled.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param contextId The context identifier
     */
    default void handleContextDisabled(HttpServletRequest httpRequest, HttpServletResponse httpResponse, int contextId) {
        int statusCode = HttpServletResponse.SC_FORBIDDEN;
        String statusMessage = "Forbidden";
        String message = "You are currently not authorized, please try again later.";
        sendErrorPage(httpResponse, statusCode, statusMessage, message);
    }

    /**
     * This method is called when the user associated with the session of a SAML login request is disabled.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    default void handleUserDisabled(HttpServletRequest httpRequest, HttpServletResponse httpResponse, int userId, int contextId) {
        int statusCode = HttpServletResponse.SC_FORBIDDEN;
        String statusMessage = "Forbidden";
        String message = "You are currently not authorized, please try again later.";
        sendErrorPage(httpResponse, statusCode, statusMessage, message);
    }

    /**
     * This method is called when the SAML login request could not be processed since update tasks are running or pending.
     *
     * @param httpRequest The servlet request
     * @param httpResponse The servlet response
     * @param contextId The context identifier
     */
    default void handleUpdateTasksRunningOrPending(HttpServletRequest httpRequest, HttpServletResponse httpResponse, int contextId) {
        int statusCode = HttpServletResponse.SC_FORBIDDEN;
        String statusMessage = "Forbidden";
        String message = "You are currently not authorized, please try again later.";
        sendErrorPage(httpResponse, statusCode, statusMessage, message);
    }

    /**
     * Sends a simple HTML error page derived from specified exception instance.
     * <p>
     * By default <code>"500 - Internal Server Error"</code> is used to indicate an error which prevented from processing the SAML request.
     * However, special error codes are interpreted as <code>"403 - Forbidden"</code> to signal that SAML request was refused due to non-
     * authorized user; such as disabled context and/or user.
     *
     * @param httpResponse The HTTP response to use to send the HTML page
     * @param exception The exception to advertise
     */
    static void sendErrorPage(HttpServletResponse httpResponse, OXException exception) {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String statusMessage = "Internal Server Error";
        String fallbackMessage = "An internal error occurred, please try again later.";

        if (ContextExceptionCodes.CONTEXT_DISABLED.equals(exception)) {
            statusCode = HttpServletResponse.SC_FORBIDDEN;
            statusMessage = "Forbidden";
            fallbackMessage = "You are currently not authorized, please try again later..";
        } else if (AuthorizationExceptionCodes.USER_DISABLED.equals(exception)) {
            statusCode = HttpServletResponse.SC_FORBIDDEN;
            statusMessage = "Forbidden";
            fallbackMessage = "You are currently not authorized, please try again later.";
        }

        String message = exception.getDisplayMessage(Locale.US);
        if (message == null) {
            message = exception.getMessage();
            if (message == null) {
                message = fallbackMessage;
            }
        }

        sendErrorPage(httpResponse, statusCode, statusMessage, message);
    }

    /**
     * Sends a simple HTML error page for specified status code & message and display message
     *
     * @param httpResponse The HTTP response to use to send the HTML page
     * @param statusCode The status code to advertise
     * @param statusMessage The accompanying status message for given status code
     * @param message The display message
     */
    static void sendErrorPage(HttpServletResponse httpResponse, int statusCode, String statusMessage, String message) {
        String response =
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <title>" + statusCode + " - " + statusMessage + "</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>" + statusCode + " - " + statusMessage + "</h1>" +
            "    <p>" + message + "</p>" +
            "  </body>\n" +
            "</html>";
        int responseLength = response.getBytes(com.openexchange.java.Charsets.UTF_8).length;

        Tools.disableCaching(httpResponse);
        httpResponse.setStatus(statusCode);
        httpResponse.setCharacterEncoding(Charsets.UTF_8_NAME);
        httpResponse.setContentType("text/html");
        httpResponse.setContentLength(responseLength);
        try {
            httpResponse.getWriter().write(response);
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
            logger.trace("I/O error", e);
            try {
                httpResponse.sendError(statusCode);
            } catch (IOException | IllegalStateException x) {
                // nothing to do here
                logger.trace("Unable to send response", x);
            }
        } catch (IllegalStateException e) {
            // response already commited
            LoggerFactory.getLogger(ExceptionHandler.class).trace("Unable to send response", e);
        }
    }

}
