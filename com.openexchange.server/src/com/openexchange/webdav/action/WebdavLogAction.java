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

package com.openexchange.webdav.action;

import static com.openexchange.java.Autoboxing.I;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.ImmutableSet;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class WebdavLogAction extends AbstractAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavLogAction.class);
    private static final Set<String> CONFIDENTIAL_HEADERS = ImmutableSet.of("AUTHORIZATION");

	private boolean logBody;
	private boolean logResponse;

    /**
     * Initializes a new {@link WebdavLogAction}.
     */
    public WebdavLogAction() {
        super();
    }

    /**
     * Initializes a new {@link WebdavLogAction}.
     *
     * @param logBody <code>true</code> to log the request body in <code>TRACE</code>-level, <code>false</code>, otherwise
     * @param logResponse <code>true</code> to log the response body in <code>TRACE</code>-level, <code>false</code>, otherwise
     */
    public WebdavLogAction(boolean logBody, boolean logResponse) {
        super();
        this.logBody = logBody;
        this.logResponse = logResponse;
    }

    /**
     * Sets whether to log request body
     *
     * @param b <code>true</code> to log request body; else <code>false</code>
     */
    public void setLogRequestBody(final boolean b) {
        logBody = b;
    }

    /**
     * Sets whether to log response body
     *
     * @param b <code>true</code> to log response body; else <code>false</code>
     */
    public void setLogResponseBody(final boolean b) {
        logResponse = b;
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        try {
            if (false == LOG.isDebugEnabled()) {
                /*
                 * proceed if no logging enabled
                 */
                yield(request, response);
                return;
            }
            /*
             * log headers
             */
            String lineSeparator = Strings.getLineSeparator();
            {
                StringBuilder stringBuilder = new StringBuilder(8192);
                stringBuilder.append("URL: ").append(request.getUrl()).append(lineSeparator);
                for (String header : request.getHeaderNames()) {
                    stringBuilder.append(header).append(": ")
                        .append(CONFIDENTIAL_HEADERS.contains(header.toUpperCase()) ? "***" : request.getHeader(header)).append(lineSeparator);

                }
                WebdavResource resource = request.getResource();
                if (null != resource) {
                    stringBuilder.append("Resource: ").append(resource).append(lineSeparator);
                    stringBuilder.append("exists: ").append(resource.exists()).append(lineSeparator);
                    stringBuilder.append("isCollection: ").append(resource.isCollection()).append(lineSeparator);
                }
                LOG.debug(stringBuilder.toString());
                stringBuilder = null;
            }
            /*
             * log request body & adjust response as needed
             */
            CapturingWebdavResponse capturingResponse = null;
            if (LOG.isTraceEnabled()) {
                if (logBody) {
                    request = new ReplayWebdavRequest(request);
                    logRequestBody(request, lineSeparator);
                }
                if (logResponse) {
                    capturingResponse = new CapturingWebdavResponse(response);
                    response = capturingResponse;
                }
            }
            /*
             * perform request & log captured response if available
             */
            yield(request, response);
            if (null != capturingResponse) {
                LOG.trace(capturingResponse.getBodyAsString(), System.lineSeparator());
            }
        } catch (WebdavProtocolException e) {
            if (HttpServletResponse.SC_INTERNAL_SERVER_ERROR == e.getStatus()) {
                LOG.error("HTTP {} ({}) for request {}", I(e.getStatus()), e.getMessage(), request.getUrl(), e);
            } else {
                LOG.debug("HTTP {} ({}) for request {}", I(e.getStatus()), e.getMessage(), request.getUrl(), e);
            }
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Unexepected runtime excpetion handling request {}: {}", request.getUrl(), e.getMessage(), e);
            throw e;
        }
    }

    private static void logRequestBody(final WebdavRequest req, final String lineSeparator) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(req.getBody(), com.openexchange.java.Charsets.UTF_8));
            String line = null;
            final StringBuilder b = new StringBuilder(65536);
            while ((line = reader.readLine()) != null) {
                b.append(line);
                b.append(lineSeparator);
            }
            LOG.trace(b.toString(), lineSeparator);
        } catch (IOException x) {
            LOG.debug("", x);
        } finally {
            Streams.close(reader);
        }
    }

}
