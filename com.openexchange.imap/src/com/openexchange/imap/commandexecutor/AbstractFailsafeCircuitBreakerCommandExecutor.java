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
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_DELAY_MILLIS_DESC;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_DELAY_MILLIS_NAME;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_DENIALS_DESC;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_DENIALS_NAME;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_DENIALS_UNITS;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_DIMENSION_ACCOUNT_KEY;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_DIMENSION_PROTOCOL_KEY;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_FAILURE_THRESHOLD_DESC;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_FAILURE_THRESHOLD_NAME;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_GROUP;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_STATUS_NAME;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_SUCCESS_THRESHOLD_DESC;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_SUCCESS_THRESHOLD_NAME;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_TRIP_COUNT_DESC;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_TRIP_COUNT_NAME;
import static com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants.METRICS_TRIP_COUNT_UNITS;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableList;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.MetricService;
import com.openexchange.metrics.MetricType;
import com.openexchange.metrics.circuitbreaker.MetricCircuitBreakerConstants;
import com.openexchange.net.HostList;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInterceptor;
import com.sun.mail.imap.ResponseEvent.Status;
import com.sun.mail.imap.ResponseEvent.StatusResponse;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.function.CheckedRunnable;
import net.jodah.failsafe.util.Ratio;

/**
 * {@link AbstractFailsafeCircuitBreakerCommandExecutor} - An abstract circuit breaker for IMAP end-points.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractFailsafeCircuitBreakerCommandExecutor extends AbstractMetricAwareCommandExecutor {

    /** The failure threshold */
    protected final Ratio failureThreshold;

    /** The success threshold */
    protected final Ratio successThreshold;

    /** The delay in milliseconds */
    protected final long delayMillis;

    /** the existent circuit breakers */
    protected final ConcurrentMap<Object, CircuitBreakerInfo> circuitBreakers;

    /** The optional listing of hosts to which the circuit breaker applies */
    protected final Optional<HostList> optionalHostList;

    /** The optional ports of hosts to which the circuit breaker applies */
    protected final Set<Integer> ports;

    /** The ranking for this instance */
    protected final int ranking;

    /** The registered metric descriptors */
    protected final AtomicReference<List<MetricDescriptor>> metricDescriptors;

    /** The reference for the metric service */
    protected final AtomicReference<MetricService> metricServiceReference;

    /**
     * Initializes a new {@link AbstractFailsafeCircuitBreakerCommandExecutor}.
     *
     * @param optHostList The optional hosts to consider
     * @param optPorts The optional ports to consider
     * @param failureThreshold The ratio of successive failures that must occur in order to open the circuit
     * @param successThreshold The ratio of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @param ranking The ranking
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    protected AbstractFailsafeCircuitBreakerCommandExecutor(Optional<HostList> optHostList, Set<Integer> optPorts, Ratio failureThreshold, Ratio successThreshold, long delayMillis, int ranking) {
        super();
        if (failureThreshold.numerator <= 0) {
            throw new IllegalArgumentException("failureThreshold must be greater than 0 (zero).");
        }
        if (successThreshold.numerator <= 0) {
            throw new IllegalArgumentException("successThreshold must be greater than 0 (zero).");
        }
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be greater than 0 (zero).");
        }

        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.delayMillis = delayMillis;
        circuitBreakers = new ConcurrentHashMap<>(10, 0.9F, 1);

        metricServiceReference = new AtomicReference<>(null);
        metricDescriptors = new AtomicReference<>(null);
        this.ranking = ranking;
        this.optionalHostList = optHostList;
        this.ports = null == optPorts || optPorts.isEmpty() ? null : optPorts;
    }

    /**
     * Creates a circuit breaker instance.
     *
     * @param key The key identifying the circuit breaker to create
     * @return The circuit breaker
     */
    protected CircuitBreakerInfo createCircuitBreaker(String key) {
        CircuitBreaker circuitBreaker = new CircuitBreaker();
        CircuitBreakerInfo breakerInfo = new CircuitBreakerInfo(key, circuitBreaker);
        circuitBreaker.withFailureThreshold(failureThreshold.numerator, failureThreshold.denominator)
            .withSuccessThreshold(successThreshold.numerator, successThreshold.denominator)
            .withDelay(delayMillis, TimeUnit.MILLISECONDS)
            .onOpen(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    Runnable metricTask = breakerInfo.getOnOpenMetricTaskReference().get();
                    if (metricTask != null) {
                        try {
                            metricTask.run();
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    onOpen(breakerInfo);
                }
            })
            .onHalfOpen(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    onHalfOpen(breakerInfo);
                }
            })
            .onClose(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    onClose(breakerInfo);
                }
            });
        return breakerInfo;
    }

    /**
     * Gets the circuit breaker for specified IMAP protocol instance.
     *
     * @param protocol The IMAP protocol
     * @return The associated circuit breaker
     */
    protected abstract CircuitBreakerInfo circuitBreakerFor(Protocol protocol);

    /**
     * Is called when the circuit breaker is opened: The circuit is opened and not allowing executions to occur.
     *
     * @param breakerInfo The circuit breaker that went into open state
     * @throws Exception If an error occurs
     */
    protected abstract void onOpen(CircuitBreakerInfo breakerInfo) throws Exception;

    /**
     * Is called when the circuit breaker is half-opened: The circuit is temporarily allowing executions to occur.
     *
     * @param breakerInfo The circuit breaker that went into half-open state
     * @throws Exception If an error occurs
     */
    protected abstract void onHalfOpen(CircuitBreakerInfo breakerInfo) throws Exception;

    /**
     * Is called when the circuit breaker is closed: The circuit is closed and fully functional, allowing executions to occur.
     *
     * @param breakerInfo The circuit breaker that went into closed state
     * @throws Exception If an error occurs
     */
    protected abstract void onClose(CircuitBreakerInfo breakerInfo) throws Exception;

    /**
     * Is called when the circuit breaker denied an access attempt because it is currently open and not allowing executions to occur.
     *
     * @param exception The thrown exception when an execution is attempted while a configured CircuitBreaker is open
     * @param breakerInfo The circuit breaker that denied access attempt
     */
    protected void onDenied(CircuitBreakerOpenException exception, CircuitBreakerInfo breakerInfo) {
        Runnable metricTask = breakerInfo.getOnDeniedMetricTaskReference().get();
        if (metricTask != null) {
            metricTask.run();
        }
    }

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
    public Response[] executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, Protocol protocol) {
        CircuitBreakerInfo breakerInfo = circuitBreakerFor(protocol);
        try {
            return Failsafe.with(breakerInfo.getCircuitBreaker()).get(new CircuitBreakerCommandCallable(command, args, optionalInterceptor, protocol, metricServiceReference));
        } catch (CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e, breakerInfo);
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
        CircuitBreakerInfo breakerInfo = circuitBreakerFor(protocol);
        try {
            return Failsafe.with(breakerInfo.getCircuitBreaker()).get(new CircuitBreakerReadResponseCallable(protocol, metricServiceReference));
        } catch (CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e, breakerInfo);
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
        for (CircuitBreakerInfo breakerInfo : circuitBreakers.values()) {
            sb.append(", \"").append(breakerInfo.getKey()).append("\" state=").append(breakerInfo.getCircuitBreaker().getState());
        }
        return sb.toString();
    }

    @Override
    protected void addMetricDescriptors(List<MetricDescriptor> descriptors, MetricService metricService) {
        for (CircuitBreakerInfo breakerInfo : circuitBreakers.values()) {
            initMetricsFor(breakerInfo.getKey(), breakerInfo, metricService, descriptors);
        }
    }

    /**
     * Invoked when given metric service appeared.
     *
     * @param metricService The metric service
     * @throws Exception If an error occurs
     */
    @Override
    public void onMetricServiceAppeared(MetricService metricService) throws Exception {
        metricServiceReference.set(metricService);

        List<MetricDescriptor> descriptors = new CopyOnWriteArrayList<MetricDescriptor>();
        for (CircuitBreakerInfo breakerInfo : circuitBreakers.values()) {
            initMetricsFor(breakerInfo.getKey(), breakerInfo, metricService, descriptors);
        }
        this.metricDescriptors.set(descriptors);
    }

    private static final String METRICS_DIMENSION_PROTOCOL_VALUE = "imap";

    /**
     * Invoked when given metric service appeared.
     *
     * @param accountValue The account value; either socket address or host name
     * @param breakerInfo The circuit breaker for which to track metrics
     * @param metricService The metric service
     * @param descriptors The listing of descriptors to add to
     */
    protected void initMetricsFor(String accountValue, CircuitBreakerInfo breakerInfo, MetricService metricService, List<MetricDescriptor> descriptors) {
        if (metricService == null || descriptors == null) {
            return;
        }

        {
            MetricDescriptor breakerStatusGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_STATUS_NAME, MetricType.GAUGE)
                .withDescription(MetricCircuitBreakerConstants.METRICS_STATUS_DESC)
                .withMetricSupplier(() -> {
                    return breakerInfo.getCircuitBreaker().getState().name();
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, accountValue)
                .build();
            metricService.getGauge(breakerStatusGauge);
            descriptors.add(breakerStatusGauge);
        }

        {
            MetricDescriptor failureThresholdGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_FAILURE_THRESHOLD_NAME, MetricType.GAUGE)
                .withDescription(METRICS_FAILURE_THRESHOLD_DESC)
                .withMetricSupplier(() -> {
                    return I(breakerInfo.getCircuitBreaker().getFailureThreshold().numerator);
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, accountValue)
                .build();
            metricService.getGauge(failureThresholdGauge);
            descriptors.add(failureThresholdGauge);
        }

        {
            MetricDescriptor successThresholdGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_SUCCESS_THRESHOLD_NAME, MetricType.GAUGE)
                .withDescription(METRICS_SUCCESS_THRESHOLD_DESC)
                .withMetricSupplier(() -> {
                    return I(breakerInfo.getCircuitBreaker().getSuccessThreshold().numerator);
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, accountValue)
                .build();
            metricService.getGauge(successThresholdGauge);
            descriptors.add(successThresholdGauge);
        }

        {
            MetricDescriptor delayMillisGauge = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_DELAY_MILLIS_NAME, MetricType.GAUGE)
                .withDescription(METRICS_DELAY_MILLIS_DESC)
                .withMetricSupplier(() -> {
                    return L(breakerInfo.getCircuitBreaker().getDelay().toMillis());
                })
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, accountValue)
                .build();
            metricService.getGauge(delayMillisGauge);
            descriptors.add(delayMillisGauge);
        }

        {
            MetricDescriptor tripCounter = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_TRIP_COUNT_NAME, MetricType.COUNTER)
                .withDescription(METRICS_TRIP_COUNT_DESC)
                .withUnit(METRICS_TRIP_COUNT_UNITS)
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, accountValue)
                .build();
            metricService.getCounter(tripCounter);
            breakerInfo.getOnOpenMetricTaskReference().set(new Runnable() {

                @Override
                public void run() {
                    metricService.getCounter(tripCounter).incement();
                }
            });
            descriptors.add(tripCounter);
        }

        {
            MetricDescriptor denialMeter = MetricDescriptor.newBuilder(METRICS_GROUP, METRICS_DENIALS_NAME, MetricType.METER)
                .withDescription(METRICS_DENIALS_DESC)
                .withUnit(METRICS_DENIALS_UNITS)
                .addDimension(METRICS_DIMENSION_PROTOCOL_KEY, METRICS_DIMENSION_PROTOCOL_VALUE)
                .addDimension(METRICS_DIMENSION_ACCOUNT_KEY, accountValue)
                .build();
            metricService.getMeter(denialMeter);
            breakerInfo.getOnDeniedMetricTaskReference().set(new Runnable() {

                @Override
                public void run() {
                    metricService.getMeter(denialMeter).mark();
                }
            });
            descriptors.add(denialMeter);
        }
    }

    /**
     * Invoked when given metric service is about to disappear.
     *
     * @param metricService The metric service
     * @throws Exception If an error occurs
     */
    @Override
    public void onMetricServiceDisppearing(MetricService metricService) throws Exception {
        super.onMetricServiceDisppearing(metricService);

        for (CircuitBreakerInfo breakerInfo : circuitBreakers.values()) {
            breakerInfo.getOnOpenMetricTaskReference().set(null);
            breakerInfo.getOnDeniedMetricTaskReference().set(null);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private static class CircuitBreakerReadResponseCallable implements Callable<Response> {

        private final Protocol protocol;
        private final AtomicReference<MetricService> metricServiceReference;

        /**
         * Initializes a new {@link CircuitBreakerReadResponseCallable}.
         *
         * @param protocol The protocol instance
         * @param metricServiceReference The metric service reference
         */
        CircuitBreakerReadResponseCallable(Protocol protocol, AtomicReference<MetricService> metricServiceReference) {
            super();
            this.protocol = protocol;
            this.metricServiceReference = metricServiceReference;
        }

        @Override
        public Response call() throws Exception {
            MetricService metricService = metricServiceReference.get();
            return MonitoringCommandExecutor.readResponse(protocol, Optional.ofNullable(metricService));
        }
    }

    private static class CircuitBreakerCommandCallable implements Callable<Response[]> {

        private static final List<Class<? extends Exception>> NETWORK_COMMUNICATION_ERRORS = ImmutableList.of(
            com.sun.mail.iap.ByeIOException.class,
            java.net.SocketTimeoutException.class,
            java.io.EOFException.class);

        private final String command;
        private final Argument args;
        private final Optional<ResponseInterceptor> optionalInterceptor;
        private final Protocol protocol;
        private final AtomicReference<MetricService> metricServiceReference;

        /**
         * Initializes a new {@link CircuitBreakerCommandCallable}.
         *
         * @param command The command
         * @param args The optional arguments
         * @param optionalInterceptor The optional interceptor
         * @param protocol The protocol instance
         * @param metricServiceReference The metric service reference
         */
        CircuitBreakerCommandCallable(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, Protocol protocol, AtomicReference<MetricService> metricServiceReference) {
            super();
            this.command = command;
            this.args = args;
            this.optionalInterceptor = optionalInterceptor;
            this.protocol = protocol;
            this.metricServiceReference = metricServiceReference;
        }

        @Override
        public Response[] call() throws Exception {
            MetricService metricService = metricServiceReference.get();

            // Obtain responses
            ExecutedCommand executedCommand = MonitoringCommandExecutor.executeCommand(command, args, optionalInterceptor, protocol, Optional.ofNullable(metricService));
            Response[] responses = executedCommand.responses;

            // Check status response
            Optional<StatusResponse> optionalStatusResponse = executedCommand.optionalStatusResponse;
            StatusResponse statusResponse = optionalStatusResponse.isPresent() ? executedCommand.optionalStatusResponse.get() : StatusResponse.statusResponseFor(responses);
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
