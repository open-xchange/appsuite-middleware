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

package com.openexchange.saml.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.saml.SAMLWebSSOProvider;
import com.openexchange.saml.spi.ExceptionHandler;
import com.openexchange.tools.servlet.http.Tools;


/**
 * Makes the service providers metadata XML available via HTTP if configured to do so.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class MetadataService extends SAMLServlet {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataService.class);

    private static final long serialVersionUID = -2932533507293972575L;

    /**
     * Initializes a new {@link MetadataService}.
     * @param provider
     * @param exceptionHandler
     */
    public MetadataService(SAMLWebSSOProvider provider, ExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        Tools.disableCaching(httpResponse);
        httpResponse.setCharacterEncoding(Charsets.UTF_8_NAME);
        try {
            String metadataXML = provider.getMetadataXML();
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setContentType("application/xml");
            httpResponse.setContentLength(metadataXML.getBytes().length);
            httpResponse.getWriter().write(metadataXML);
        } catch (OXException e) {
            LOG.error("Error while generating SAML metadata", e);
            String message = e.getDisplayMessage(Locale.US);
            if (message == null) {
                message = e.getMessage();
                if (message == null) {
                    message = "An internal error occurred, please try again later.";
                }
            }

            StringWriter stWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stWriter) {
                @Override
                public void println() {
                    try {
                        synchronized (lock) {
                            out.write("<br>");
                        }
                    } catch (@SuppressWarnings("unused") InterruptedIOException x) {
                        Thread.currentThread().interrupt();
                    } catch (@SuppressWarnings("unused") IOException x) {
                        setError();
                    }
                }
            });

            String stacktrace = stWriter.toString();

            String response =
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>500 - Internal Server Error</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>500 - Internal Server Error</h1>" +
                "    <p>" + message + "</p>" +
                "    <p>" + stacktrace + "</p>" +
                "  </body>\n" +
                "</html>";
            byte[] responseBytes = response.getBytes();

            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.setContentType("text/html");
            httpResponse.setContentLength(responseBytes.length);
            httpResponse.getWriter().write(response);
        }
    }

}
