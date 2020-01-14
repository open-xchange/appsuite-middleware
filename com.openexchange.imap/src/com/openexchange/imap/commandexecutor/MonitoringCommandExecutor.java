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

package com.openexchange.imap.commandexecutor;

import static com.openexchange.exception.ExceptionUtils.isEitherOf;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.ImmutableList;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.types.Meter;
import com.openexchange.metrics.types.Timer;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInterceptor;
import com.sun.mail.imap.ResponseEvent.Status;
import com.sun.mail.imap.ResponseEvent.StatusResponse;

/**
 * {@link MonitoringCommandExecutor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MonitoringCommandExecutor extends AbstractMetricAwareCommandExecutor {

    private static final List<Class<? extends Exception>> NETWORK_COMMUNICATION_ERRORS = ImmutableList.of(
        com.sun.mail.iap.ConnectionException.class,
        com.sun.mail.iap.ByeIOException.class,
        java.net.SocketTimeoutException.class,
        java.io.EOFException.class);

    /**
     * Initializes a new {@link MonitoringCommandExecutor}.
     */
    public MonitoringCommandExecutor() {
        super();
    }

    @Override
    public String getDescription() {
        return "monitoring";
    }

    @Override
    public boolean isApplicable(Protocol protocol) {
        return true;
    }

    @Override
    protected void addMetricDescriptors(List<MetricDescriptor> descriptors, MetricService metricService) {
        // Nothing
    }

    @Override
    public Response readResponse(Protocol protocol) throws IOException {
        MetricService metricService = metricServiceReference.get();
        return readResponse(protocol, Optional.ofNullable(metricService));
    }

    @Override
    public Response[] executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, Protocol protocol) {
        MetricService metricService = metricServiceReference.get();
        return executeCommand(command, args, optionalInterceptor, protocol, Optional.ofNullable(metricService)).responses;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The group name for IMAP metrics */
    private static final String METRICS_GROUP = "imap";

    /** The name for request rate metric */
    private static final String METRICS_REQUEST_RATE_NAME = "requestRate";

    /** The name for error rate metric */
    private static final String METRICS_ERROR_RATE_NAME = "errorRate";

    /** The key for server dimension */
    private static final String METRICS_DIMENSION_SERVER_KEY = "server";

    /**
     * Reads a single IMAP response.
     *
     * @param protocol The protocol to read from
     * @param optionalMetricService The optional metric service
     * @return The IMAP response
     * @throws IOException If IMAP response cannot be returned due to an I/O error
     */
    public static Response readResponse(Protocol protocol, Optional<MetricService> optionalMetricService) throws IOException {
        if (!optionalMetricService.isPresent()) {
            try {
                return protocol.readResponse();
            } catch (ProtocolException e) {
                // Cannot occur
                throw new IOException(e);
            }
        }

        Metrics metrics = initMetrics(protocol, optionalMetricService.get());
        Timer requestTimer = metrics.requestTimer;
        Meter errorMeter = metrics.errorMeter;

        long duration = -1;
        long start = System.nanoTime();
        try {
            // Measure command execution
            Response response = protocol.readResponse();
            duration = System.nanoTime() - start;
            return response;
        } catch (IOException e) {
            duration = System.nanoTime() - start;
            if (isEitherOf(e, NETWORK_COMMUNICATION_ERRORS)) {
                // Command failed due to a network communication error.
                errorMeter.mark();
            }
            throw e;
        } catch (ProtocolException e) {
            // Cannot occur
            duration = System.nanoTime() - start;
            throw new IOException(e);
        } catch (RuntimeException e) {
            // Should not occur
            duration = System.nanoTime() - start;
            throw new IOException(e);
        } finally {
            if (duration >= 0) {
                requestTimer.update(duration, TimeUnit.NANOSECONDS);
            }
        }
    }

    /**
     * Executes given command with specified arguments using passed protocol instance while adding metrics in case optional metric service
     * is present.
     *
     * @param command The command
     * @param args The arguments
     * @param optionalInterceptor The optional interceptor
     * @param protocol The protocol instance
     * @param optionalMetricService The optional metric service
     * @return The response array
     */
    public static ExecutedCommand executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, Protocol protocol, Optional<MetricService> optionalMetricService) {
        if (!optionalMetricService.isPresent()) {
            return new ExecutedCommand(protocol.executeCommand(command, args, optionalInterceptor));
        }

        Metrics metrics = initMetrics(protocol, optionalMetricService.get());
        Timer requestTimer = metrics.requestTimer;
        Meter errorMeter = metrics.errorMeter;

        long duration = -1;
        try {
            // Measure command execution
            long start = System.nanoTime();
            Response[] responses = protocol.executeCommand(command, args, optionalInterceptor);
            duration = System.nanoTime() - start;

            // Check responses if command failed
            StatusResponse statusResponse = StatusResponse.statusResponseFor(responses);
            if (statusResponse != null && Status.BYE == statusResponse.getStatus()) {
                Response response = statusResponse.getResponse();
                // Command failed. Check for a synthetic BYE response providing the causing I/O error
                Exception byeException = response.getByeException();
                if (isEitherOf(byeException, NETWORK_COMMUNICATION_ERRORS)) {
                    // Command failed due to a network communication error.
                    errorMeter.mark();
                }
            }

            return new ExecutedCommand(statusResponse, responses);
        } finally {
            if (duration >= 0) {
                requestTimer.update(duration, TimeUnit.NANOSECONDS);
            }
        }
    }

    private static Metrics initMetrics(Protocol protocol, MetricService metricService) {
        String serverInfo = new StringBuilder(protocol.getHost()).append('@').append(protocol.getPort()).toString();

        Timer requestTimer = metricService.getTimer(MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_REQUEST_RATE_NAME, MetricType.TIMER)
            .withDescription("Overall IMAP request timer per target server")
            .addDimension(METRICS_DIMENSION_SERVER_KEY, serverInfo)
            .build());

        Meter errorMeter = metricService.getMeter(MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_ERROR_RATE_NAME, MetricType.METER)
            .withDescription("Failed IMAP request meter per target server")
            .addDimension(METRICS_DIMENSION_SERVER_KEY, serverInfo)
            .build());

        return new Metrics(requestTimer, errorMeter);
    }

    private static class Metrics {

        final Timer requestTimer;
        final Meter errorMeter;

        Metrics(Timer requestTimer, Meter errorMeter) {
            super();
            this.requestTimer = requestTimer;
            this.errorMeter = errorMeter;
        }
    }

}
