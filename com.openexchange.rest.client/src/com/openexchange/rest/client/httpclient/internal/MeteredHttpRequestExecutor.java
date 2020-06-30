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

package com.openexchange.rest.client.httpclient.internal;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.noop.NoopTimer;
import com.openexchange.metrics.types.Timer;
import com.openexchange.rest.client.osgi.RestClientServices;


/**
 * A {@link HttpRequestExecutor} that monitors executed requests in terms of method, response time and response status.
 *
 * This was inspired by io.micrometer.core.instrument.binder.httpcomponents.MicrometerHttpRequestExecutor.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class MeteredHttpRequestExecutor extends HttpRequestExecutor {

    private final MonitoringId monitoringId;

    /**
     * Initializes a new {@link MeteredHttpRequestExecutor}.
     *
     * @param monitoringId The monitoring identifier
     */
    public MeteredHttpRequestExecutor(MonitoringId monitoringId) {
        super();
        this.monitoringId = monitoringId;
    }

    @Override
    public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
        if (monitoringId == MonitoringId.getNoop()) {
            return super.execute(request, conn, context);
        }

        long start = System.nanoTime();
        String status = "UNKNOWN";
        try {
            HttpResponse response = super.execute(request, conn, context);
            status = response != null ? Integer.toString(response.getStatusLine().getStatusCode()) : "CLIENT_ERROR";
            return response;
        } catch (IOException | HttpException | RuntimeException e) {
            status = "IO_ERROR";
            throw e;
        } finally {
             getTimer(request.getRequestLine().getMethod(), status).update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    private Timer getTimer(String method, String status) {
        MetricService metrics = RestClientServices.getOptionalService(MetricService.class);
        if (metrics == null || monitoringId == MonitoringId.getNoop()) {
            return NoopTimer.getInstance();
        }

        return metrics.getTimer(monitoringId.newMetricBuilder("httpclient", "RequestTimes", MetricType.TIMER)
            .withRate(TimeUnit.SECONDS)
            .withUnit("requests")
            .withDescription("Duration of Apache HttpClient request execution")
            .addDimension("method", method)
            .addDimension("status", status)
            .build());
    }

}
