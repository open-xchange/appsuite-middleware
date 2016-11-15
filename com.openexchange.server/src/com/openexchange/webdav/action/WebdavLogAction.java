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

package com.openexchange.webdav.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.ImmutableSet;
import com.openexchange.java.Streams;
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
            String lineSeparator = System.getProperty("line.separator");
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
                LOG.trace(capturingResponse.getBodyAsString());
            }
        } catch (WebdavProtocolException e) {
            if (HttpServletResponse.SC_INTERNAL_SERVER_ERROR == e.getStatus()) {
                LOG.error("HTTP {} ({}) for request {}", e.getStatus(), e.getMessage(), request.getUrl(), e);
            } else {
                LOG.debug("HTTP {} ({}) for request {}", e.getStatus(), e.getMessage(), request.getUrl(), e);
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
            LOG.trace(b.toString());
        } catch (final IOException x) {
            LOG.debug("", x);
        } finally {
            Streams.close(reader);
        }
    }

}
