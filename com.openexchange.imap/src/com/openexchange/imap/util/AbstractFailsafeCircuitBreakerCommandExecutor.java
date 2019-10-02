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

package com.openexchange.imap.util;

import static com.openexchange.exception.ExceptionUtils.isEitherOf;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.net.HostList;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.CommandExecutor;
import com.sun.mail.imap.ResponseEvent.Status;
import com.sun.mail.imap.ResponseEvent.StatusResponse;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.CircuitBreaker.State;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.function.CheckedRunnable;

/**
 * {@link AbstractFailsafeCircuitBreakerCommandExecutor} - An abstract circuit breaker for IMAP end-points.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractFailsafeCircuitBreakerCommandExecutor implements CommandExecutor {

    private static final String METRICS_DIMENSION_PROTOCOL_KEY = "protocol";
    private static final String METRICS_DIMENSION_PROTOCOL_VALUE = "imap";

    private static final String METRICS_DIMENSION_ACCOUNT_KEY = "account";

    private static final String METRICS_GROUP = "circuit-breakers";

    private static final String METRICS_STATUS_NAME = "status";
    private static final String METRICS_STATUS_DESC = "The current status of the IMAP circuit breaker";

    private static final String METRICS_FAILURE_THRESHOLD_NAME = "failureThreshold";
    private static final String METRICS_FAILURE_THRESHOLD_DESC = "The number of successive failures that must occur in order to open the circuit";

    private static final String METRICS_SUCCESS_THRESHOLD_NAME = "successThreshold";
    private static final String METRICS_SUCCESS_THRESHOLD_DESC = "The number of successive successful executions that must occur when in a half-open state in order to close the circuit";

    private static final String METRICS_DELAY_MILLIS_NAME = "delayMillis";
    private static final String METRICS_DELAY_MILLIS_DESC = "The number of milliseconds to wait in open state before transitioning to half-open";

    private static final String METRICS_TRIP_COUNT_NAME = "tripCount";
    private static final String METRICS_TRIP_COUNT_DESC = "The number representing how often the circuit breaker tripped";
    private static final String METRICS_TRIP_COUNT_UNITS = "trips";

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The circuit breaker instance */
    protected final CircuitBreaker circuitBreaker;

    /** The optional listing of hosts to which the circuit breaker applies */
    protected final Optional<HostList> optionalHostList;

    /** The optional ports of hosts to which the circuit breaker applies */
    protected final Set<Integer> ports;

    /** The ranking for this instance */
    protected final int ranking;

    /** The registered metric descriptors */
    protected final AtomicReference<List<MetricDescriptor>> metricDescriptors;

    private final AtomicReference<Runnable> onOpenTask;

    /**
     * Initializes a new {@link AbstractFailsafeCircuitBreakerCommandExecutor}.
     *
     * @param optHostList The optional hosts to consider
     * @param optPorts The optional ports to consider
     * @param failureThreshold The number of successive failures that must occur in order to open the circuit
     * @param successThreshold The number of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @param ranking The ranking
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    protected AbstractFailsafeCircuitBreakerCommandExecutor(Optional<HostList> optHostList, Set<Integer> optPorts, int failureThreshold, int successThreshold, long delayMillis, int ranking) {
        super();
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be greater than 0 (zero).");
        }
        if (successThreshold <= 0) {
            throw new IllegalArgumentException("successThreshold must be greater than 0 (zero).");
        }
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be greater than 0 (zero).");
        }

        AtomicReference<Runnable> onOpenTask = new AtomicReference<>(null);
        this.onOpenTask = onOpenTask;
        metricDescriptors = new AtomicReference<>(null);
        this.ranking = ranking;
        this.optionalHostList = optHostList;
        this.ports = null == optPorts || optPorts.isEmpty() ? null : optPorts;

        CircuitBreaker circuitBreaker = new CircuitBreaker()
            .withFailureThreshold(failureThreshold)
            .withSuccessThreshold(successThreshold)
            .withDelay(delayMillis, TimeUnit.MILLISECONDS)
            .onOpen(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    Runnable task = onOpenTask.get();
                    if (task != null) {
                        try {
                            task.run();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    onOpen();
                }
            })
            .onHalfOpen(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    onHalfOpen();
                }
            })
            .onClose(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    onClose();
                }
            });
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Is called when the circuit breaker is opened: The circuit is opened and not allowing executions to occur.
     *
     * @throws Exception If an error occurs
     */
    protected abstract void onOpen() throws Exception;

    /**
     * Is called when the circuit breaker is half-opened: The circuit is temporarily allowing executions to occur.
     *
     * @throws Exception If an error occurs
     */
    protected abstract void onHalfOpen() throws Exception;

    /**
     * Is called when the circuit breaker is closed: The circuit is closed and fully functional, allowing executions to occur.
     *
     * @throws Exception If an error occurs
     */
    protected abstract void onClose() throws Exception;

    /**
     * Gets a short description for this circuit breaker.
     *
     * @return The description
     */
    public abstract String getDescription();

    /**
     * Gets the optional listing of IMAP hosts to which this circuit breaker applies.
     *
     * @return The host list
     */
    public Optional<HostList> getHostList() {
        return optionalHostList;
    }

    @Override
    public int getRanking() {
        return ranking;
    }

    @Override
    public boolean isApplicable(Protocol protocol) {
        if (!optionalHostList.isPresent()) {
            return true;
        }

        return optionalHostList.get().contains(protocol.getHost()) && (null == ports || ports.contains(I(protocol.getPort())));
    }

    @Override
    public Response[] executeCommand(String command, Argument args, Protocol protocol) {
        try {
            return Failsafe.with(circuitBreaker).get(new CircuitBreakerCommandCallable(command, args, protocol));
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            IOException ioe = new IOException("Denied IMAP command since circuit breaker is open.");
            return new Response[] { Response.byeResponse(ioe) };
        } catch (FailsafeException e) {
            // Runnable failed with a checked exception
            Throwable failure = e.getCause();
            if (failure instanceof CircuitBreakerCommandFailedException) {
                return ((CircuitBreakerCommandFailedException) failure).getResponses();
            }
            if (failure instanceof Exception) {
                return new Response[] { Response.byeResponse((Exception) failure) };
            }
            if (failure instanceof Error) {
                throw (Error) failure;
            }
            return new Response[] { Response.byeResponse(e) };
        }
    }

    @Override
    public Response readResponse(Protocol protocol) throws IOException {
        try {
            return Failsafe.with(circuitBreaker).get(new CircuitBreakerReadResponseCallable(protocol));
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            throw new IOException("Denied reading from IMAP server since circuit breaker is open.");
        } catch (FailsafeException e) {
            // Runnable failed with a checked exception
            Throwable failure = e.getCause();
            if (failure instanceof IOException) {
                throw (IOException) failure;
            }
            if (failure instanceof Error) {
                throw (Error) failure;
            }
            throw new IOException(failure);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        if (optionalHostList.isPresent()) {
            sb.append("hosts=").append(optionalHostList.get().getHostString());
        } else {
            sb.append("hosts=none");
        }
        if (ports != null) {
            sb.append(", ports=").append(ports);
        }
        State state = circuitBreaker.getState();
        sb.append(", state=").append(state);
        switch (state) {
            case CLOSED:
                sb.append(" (The circuit is closed and fully functional, allowing executions to occur)");
                break;
            case HALF_OPEN:
                sb.append(" (The circuit is temporarily allowing executions to occur)");
                break;
            case OPEN:
                sb.append(" (The circuit is opened and not allowing executions to occur.)");
                break;
            default:
                break;

        }
        return super.toString();
    }

    /**
     * Invoked when given metric service appeared.
     *
     * @param metricService The metric service
     * @throws Exception If an error occurs
     */
    public void onMetricServiceAppeared(MetricService metricService) throws Exception {
        List<MetricDescriptor> descriptors= new ArrayList<MetricDescriptor>();

        {
            MetricDescriptor breakerStatusGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_STATUS_NAME, MetricType.GAUGE)
                .withDescription(METRICS_STATUS_DESC)
                .withMetricSupplier(() -> {
                    return circuitBreaker.getState().name();
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, getDescription())
                .build();
            metricService.getGauge(breakerStatusGauge);
            descriptors.add(breakerStatusGauge);
        }

        {
            MetricDescriptor failureThresholdGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_FAILURE_THRESHOLD_NAME, MetricType.GAUGE)
                .withDescription(METRICS_FAILURE_THRESHOLD_DESC)
                .withMetricSupplier(() -> {
                    return I(circuitBreaker.getFailureThreshold().numerator);
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, getDescription())
                .build();
            metricService.getGauge(failureThresholdGauge);
            descriptors.add(failureThresholdGauge);
        }

        {
            MetricDescriptor successThresholdGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_SUCCESS_THRESHOLD_NAME, MetricType.GAUGE)
                .withDescription(METRICS_SUCCESS_THRESHOLD_DESC)
                .withMetricSupplier(() -> {
                    return I(circuitBreaker.getSuccessThreshold().numerator);
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, getDescription())
                .build();
            metricService.getGauge(successThresholdGauge);
            descriptors.add(successThresholdGauge);
        }

        {
            MetricDescriptor delayMillisGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_DELAY_MILLIS_NAME, MetricType.GAUGE)
                .withDescription(METRICS_DELAY_MILLIS_DESC)
                .withMetricSupplier(() -> {
                    return L(circuitBreaker.getDelay().toMillis());
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, getDescription())
                .build();
            metricService.getGauge(delayMillisGauge);
            descriptors.add(delayMillisGauge);
        }

        {
            MetricDescriptor tripCounter = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_TRIP_COUNT_NAME, MetricType.COUNTER)
                .withDescription(METRICS_TRIP_COUNT_DESC)
                .withUnit(METRICS_TRIP_COUNT_UNITS)
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, getDescription())
                .build();
            metricService.getCounter(tripCounter);
            onOpenTask.set(new Runnable() {

                @Override
                public void run() {
                    metricService.getCounter(tripCounter).incement();
                }
            });
            descriptors.add(tripCounter);
        }

        this.metricDescriptors.set(descriptors);
    }

    /**
     * Invoked when given metric service is about to disappear.
     *
     * @param metricService The metric service
     * @throws Exception If an error occurs
     */
    public void onMetricServiceDisppearing(MetricService metricService) throws Exception {
        List<MetricDescriptor> descriptors = this.metricDescriptors.getAndSet(null);
        if (descriptors != null) {
            for (MetricDescriptor metricDescriptor : descriptors) {
                metricService.removeMetric(metricDescriptor);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private static class CircuitBreakerReadResponseCallable implements Callable<Response> {

        private final Protocol protocol;

        /**
         * Initializes a new {@link CircuitBreakerReadResponseCallable}.
         *
         * @param protocol The protocol instance
         */
        CircuitBreakerReadResponseCallable(Protocol protocol) {
            super();
            this.protocol = protocol;
        }

        @Override
        public Response call() throws Exception {
            return protocol.readResponse();
        }
    }

    private static class CircuitBreakerCommandCallable implements Callable<Response[]> {

        private static final List<Class<? extends Exception>> NETWORK_COMMUNICATION_ERRORS = ImmutableList.of(
            com.sun.mail.iap.ByeIOException.class,
            java.net.SocketTimeoutException.class,
            java.io.EOFException.class);

        private final String command;
        private final Argument args;
        private final Protocol protocol;

        /**
         * Initializes a new {@link CircuitBreakerCommandCallable}.
         *
         * @param command The command
         * @param args The optional arguments
         * @param protocol The protocol instance
         */
        CircuitBreakerCommandCallable(String command, Argument args, Protocol protocol) {
            super();
            this.command = command;
            this.args = args;
            this.protocol = protocol;
        }

        @Override
        public Response[] call() throws Exception {
            // Obtain responses
            Response[] responses = protocol.executeCommand(command, args);

            // Check status response
            StatusResponse statusResponse = StatusResponse.statusResponseFor(responses);
            if (statusResponse != null && Status.BYE == statusResponse.getStatus()) {
                Response response = statusResponse.getResponse();
                // Command failed. Check for a synthetic BYE response providing the causing I/O error
                Exception byeException = response.getByeException();
                if (isEitherOf(byeException, NETWORK_COMMUNICATION_ERRORS)) {
                    // Command failed due to a network communication error. Signal I/O error as failure to circuit breaker
                    // System.err.println("Failed command: " + command + " (" + statusResponse + ")");
                    throw new CircuitBreakerCommandFailedException(responses, byeException);
                }
            }

            // Command succeeded or failed for any other reason than a network communication error
            // System.out.println("Succeeded command: " + command + " (" + statusResponse + ")");
            return responses;
        }
    }

    private static class CircuitBreakerCommandFailedException extends Exception {

        private static final long serialVersionUID = 9014458066755839452L;

        private final Response[] responses;

        /**
         * Initializes a new {@link CircuitBreakerCommandFailedException}.
         *
         * @param responses The responses advertised from IMAP server
         * @param byeException The BYE exception
         */
        public CircuitBreakerCommandFailedException(Response[] responses, Exception byeException) {
            super(byeException);
            this.responses = responses;
        }

        /**
         * Gets the responses
         *
         * @return The responses
         */
        public Response[] getResponses() {
            return responses;
        }
    }

}
