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
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableList;
import com.openexchange.metrics.micrometer.binders.CircuitBreakerMetrics;
import com.openexchange.net.HostList;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInterceptor;
import com.sun.mail.imap.CommandExecutor;
import com.sun.mail.imap.ProtocolAccess;
import com.sun.mail.imap.ResponseEvent.Status;
import com.sun.mail.imap.ResponseEvent.StatusResponse;
import io.micrometer.core.instrument.Metrics;
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
public abstract class AbstractFailsafeCircuitBreakerCommandExecutor implements CommandExecutor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFailsafeCircuitBreakerCommandExecutor.class);

    /** The failure threshold */
    protected final Ratio failureThreshold;

    /** The success threshold */
    protected final Ratio successThreshold;

    /** The delay in milliseconds */
    protected final long delayMillis;

    /** the existent circuit breakers */
    protected final ConcurrentMap<Key, CircuitBreakerInfo> circuitBreakers;

    /** The optional listing of hosts to which the circuit breaker applies */
    protected final Optional<HostList> optionalHostList;

    /** The optional ports of hosts to which the circuit breaker applies */
    protected final Set<Integer> ports;

    /** The ranking for this instance */
    protected final int ranking;

    /** The actual executor to execute commands or read responses */
    private final MonitoringCommandExecutor delegate;

    /**
     * Initializes a new {@link AbstractFailsafeCircuitBreakerCommandExecutor}.
     *
     * @param optHostList The optional hosts to consider
     * @param optPorts The optional ports to consider
     * @param failureThreshold The ratio of successive failures that must occur in order to open the circuit
     * @param successThreshold The ratio of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @param ranking The ranking
     * @param delegate The actual executor to execute commands or read responses
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    protected AbstractFailsafeCircuitBreakerCommandExecutor(Optional<HostList> optHostList, Set<Integer> optPorts, Ratio failureThreshold, Ratio successThreshold, long delayMillis, int ranking, MonitoringCommandExecutor delegate) {
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

        this.ranking = ranking;
        this.optionalHostList = optHostList;
        this.ports = null == optPorts || optPorts.isEmpty() ? null : optPorts;
        this.delegate = delegate;
    }

    /**
     * Gets the circuit breaker for specified IMAP protocol instance.
     *
     * @param protocolAccess The protocol access
     * @return The associated circuit breaker
     */
    protected CircuitBreakerInfo circuitBreakerFor(ProtocolAccess protocolAccess) {
        Key key = getKey(protocolAccess);
        CircuitBreakerInfo breakerInfo = circuitBreakers.get(key);
        if (breakerInfo == null) {
            CircuitBreakerInfo newBreakerInfo = createCircuitBreaker(key);
            breakerInfo = circuitBreakers.putIfAbsent(key, newBreakerInfo);
            if (breakerInfo == null) {
                breakerInfo = newBreakerInfo;
                initMetricsFor(newBreakerInfo);
            }
        }
        return breakerInfo;
    }

    /**
     * Initializes monitoring metrics for this circuit breaker
     *
     * @param newBreakerInfo
     */
    protected void initMetricsFor(CircuitBreakerInfo newBreakerInfo) {
        Key key = newBreakerInfo.getKey();
        Optional<String> targetHost = key.isPerHost() ? Optional.of(key.getHost()) : Optional.empty();
        CircuitBreakerMetrics metrics = new CircuitBreakerMetrics(newBreakerInfo.getCircuitBreaker(), "imap-" + key.getName(), targetHost);
        metrics.bindTo(Metrics.globalRegistry);
        newBreakerInfo.setMetrics(metrics);
    }

    /**
     * Creates a circuit breaker instance.
     *
     * @param key The key identifying the circuit breaker to create
     * @return The circuit breaker
     */
    protected CircuitBreakerInfo createCircuitBreaker(Key key) {
        CircuitBreaker circuitBreaker = new CircuitBreaker();
        CircuitBreakerInfo breakerInfo = new CircuitBreakerInfo(key, circuitBreaker);
        circuitBreaker.withFailureThreshold(failureThreshold.numerator, failureThreshold.denominator).withSuccessThreshold(successThreshold.numerator, successThreshold.denominator).withDelay(delayMillis, TimeUnit.MILLISECONDS).onOpen(new CheckedRunnable() {

            @Override
            public void run() throws Exception {
                onOpen(breakerInfo);
            }
        }).onHalfOpen(new CheckedRunnable() {

            @Override
            public void run() throws Exception {
                onHalfOpen(breakerInfo);
            }
        }).onClose(new CheckedRunnable() {

            @Override
            public void run() throws Exception {
                onClose(breakerInfo);
            }
        });
        return breakerInfo;
    }

    /**
     * Gets a unique key to identify the circuit breaker for specified IMAP protocol instance.
     *
     * @param protocolAccess The protocol access
     * @return The key
     */
    protected abstract Key getKey(ProtocolAccess protocolAccess);

    /**
     * Is called when the circuit breaker is opened: The circuit is opened and not allowing executions to occur.
     *
     * @param breakerInfo The circuit breaker that went into open state
     * @throws Exception If an error occurs
     */
    protected void onOpen(CircuitBreakerInfo breakerInfo) throws Exception {
        LOG.warn("IMAP circuit breaker opened for: {}", breakerInfo.getKey());
        breakerInfo.getMetrics().getOpensCounter().ifPresent(c -> c.increment());
    }

    /**
     * Is called when the circuit breaker is half-opened: The circuit is temporarily allowing executions to occur.
     *
     * @param breakerInfo The circuit breaker that went into half-open state
     * @throws Exception If an error occurs
     */
    protected void onHalfOpen(CircuitBreakerInfo breakerInfo) throws Exception {
        LOG.info("IMAP circuit breaker half-opened for: {}", breakerInfo.getKey());
    }

    /**
     * Is called when the circuit breaker is closed: The circuit is closed and fully functional, allowing executions to occur.
     *
     * @param breakerInfo The circuit breaker that went into closed state
     * @throws Exception If an error occurs
     */
    protected void onClose(CircuitBreakerInfo breakerInfo) throws Exception {
        LOG.info("IMAP circuit breaker closed for: {}", breakerInfo.getKey());
    }

    /**
     * Is called when the circuit breaker denied an access attempt because it is currently open and not allowing executions to occur.
     *
     * @param exception The thrown exception when an execution is attempted while a configured CircuitBreaker is open
     * @param breakerInfo The circuit breaker that denied access attempt
     */
    protected void onDenied(@SuppressWarnings("unused") CircuitBreakerOpenException exception, CircuitBreakerInfo breakerInfo) {
        breakerInfo.getMetrics().getDenialsCounter().ifPresent(c -> c.increment());
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
    public boolean isApplicable(ProtocolAccess protocolAccess) {
        if (!optionalHostList.isPresent()) {
            return true;
        }

        return optionalHostList.get().contains(protocolAccess.getHost()) && (null == ports || ports.contains(I(protocolAccess.getPort())));
    }

    @Override
    public Response[] executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, ProtocolAccess protocolAccess) {
        CircuitBreakerInfo breakerInfo = circuitBreakerFor(protocolAccess);
        try {
            return Failsafe.with(breakerInfo.getCircuitBreaker()).get(new CircuitBreakerCommandCallable(delegate, command, args, optionalInterceptor, protocolAccess));
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
    public Response readResponse(ProtocolAccess protocolAccess) throws IOException {
        CircuitBreakerInfo breakerInfo = circuitBreakerFor(protocolAccess);
        try {
            return Failsafe.with(breakerInfo.getCircuitBreaker()).get(new CircuitBreakerReadResponseCallable(delegate, protocolAccess));
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
    public void authplain(String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(AuthScheme.PLAIN, authzid, u, p, protocolAccess);
    }

    @Override
    public void authlogin(String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(AuthScheme.LOGIN, u, p, protocolAccess);
    }

    @Override
    public void authntlm(String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(AuthScheme.NTLM, authzid, u, p, protocolAccess);
    }

    @Override
    public void authoauth2(String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(AuthScheme.XOAUTH2, u, p, protocolAccess);
    }

    @Override
    public void authoauthbearer(String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(AuthScheme.OAUTHBEARER, u, p, protocolAccess);
    }

    @Override
    public void authsasl(String[] allowed, String realm, String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(AuthScheme.SASL, allowed, realm, authzid, u, p, protocolAccess);
    }

    /**
     * Performs authentication according to given scheme.
     *
     * @param authScheme The authentication scheme
     * @param u The user name
     * @param p The password
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    private void authWithScheme(AuthScheme authScheme, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(authScheme, null, u, p, protocolAccess);
    }


    /**
     * Performs authentication according to given scheme.
     *
     * @param authScheme The authentication scheme
     * @param authzid The authorization identifier
     * @param u The user name
     * @param p The password
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    private void authWithScheme(AuthScheme authScheme, String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        authWithScheme(authScheme, null, null, authzid, u, p, protocolAccess);
    }

    /**
     * Performs authentication according to given scheme.
     *
     * @param authScheme The authentication scheme
     * @param allowed The SASL mechanisms we're allowed to use
     * @param realm The SASL realm
     * @param authzid The authorization identifier
     * @param u The user name
     * @param p The password
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    private void authWithScheme(AuthScheme authScheme, String[] allowed, String realm, String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        CircuitBreakerInfo breakerInfo = circuitBreakerFor(protocolAccess);
        try {
            Optional<ProtocolException> optionalProtocolException = Failsafe.with(breakerInfo.getCircuitBreaker()).get(new CircuitBreakerAuthCallable(delegate, authScheme, allowed, realm, authzid, u, p, protocolAccess));
            if (optionalProtocolException.isPresent()) {
                throw optionalProtocolException.get();
            }
        } catch (CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e, breakerInfo);
            throw new ProtocolException("Denied authenticating against IMAP server since circuit breaker is open.");
        } catch (FailsafeException e) {
            // Runnable failed with a checked exception
            Throwable failure = e.getCause();
            if (failure instanceof ProtocolException) {
                throw (ProtocolException) failure;
            }
            if (failure instanceof Error) {
                throw (Error) failure;
            }
            throw new ProtocolException(failure.getMessage(), failure);
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

    // ------------------------------------------------------------------------------------------------------------------------

    /**
     * {@link Key} to uniquely identify a circuit breaker instance
     */
    protected static final class Key {

        /**
         * Creates a new {@link Key} to uniquely identify a circuit breaker instance
         *
         * @param name The circuit breaker name
         * @param host The target host which is guarded by the circuit breaker. Can be a host name (any end-point of a service,
         *            e.g. {@code imap.example.com(:993)) or IP and optional port combination (per end-point, e.g. {@code 172.16.10.3(:993)})
         *            @param perHost <code>true</code> if the {@code host} parameter specifies a certain end-point, <code>false</code> if not
         * @return The key
         */
        public static Key of(String name, String host, boolean perHost) {
            return new Key(name, host, perHost);
        }

        // ---------------------------------------------------------------------------------------------------------------------------------

        private final String name;
        private final String host;
        private final boolean perHost;

        private Key(String name, String host, boolean perHost) {
            super();
            this.name = name;
            this.host = host;
            this.perHost = perHost;
        }

        /**
         * Gets the name
         *
         * @return The name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the host
         *
         * @return The host
         */
        public String getHost() {
            return host;
        }

        /**
         * Checks if the host information denotes a certain end-point (IP address)-
         *
         * @return <code>true</code> if a certain end-point is specified; otherwise <code>false</code>
         */
        public boolean isPerHost() {
            return perHost;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (perHost ? 1231 : 1237);
            result = prime * result + ((host == null) ? 0 : host.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (perHost != other.perHost) {
                return false;
            }
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new StringBuilder(name).append(" (").append(host).append(')').toString();
        }

    }

    private static class CircuitBreakerAuthCallable implements Callable<Optional<ProtocolException>> {

        private final MonitoringCommandExecutor delegate;
        private final AuthScheme authScheme;
        private final String[] allowed;
        private final String realm;
        private final String authzid;
        private final String u;
        private final String p;
        private final ProtocolAccess protocolAccess;

        /**
         * Initializes a new {@link CircuitBreakerAuthCallable}.
         *
         * @param delegate The delegate
         * @param authScheme The authentication scheme
         * @param allowed The SASL mechanisms we're allowed to use
         * @param realm The SASL realm
         * @param authzid The authorization identifier
         * @param u The user name
         * @param p The password
         * @param protocol The protocol access
         */
        CircuitBreakerAuthCallable(MonitoringCommandExecutor delegate, AuthScheme authScheme, String[] allowed, String realm, String authzid, String u, String p, ProtocolAccess protocolAccess) {
            super();
            this.delegate = delegate;
            this.authScheme = authScheme;
            this.allowed = allowed;
            this.realm = realm;
            this.authzid = authzid;
            this.u = u;
            this.p = p;
            this.protocolAccess = protocolAccess;
        }

        @Override
        public Optional<ProtocolException> call() throws Exception {
            try {
                switch (authScheme) {
                    case LOGIN:
                        delegate.authlogin(u, p, protocolAccess);
                        break;
                    case NTLM:
                        delegate.authntlm(authzid, u, p, protocolAccess);
                        break;
                    case OAUTHBEARER:
                        delegate.authoauthbearer(u, p, protocolAccess);
                        break;
                    case PLAIN:
                        delegate.authplain(authzid, u, p, protocolAccess);
                        break;
                    case XOAUTH2:
                        delegate.authoauth2(u, p, protocolAccess);
                        break;
                    case SASL:
                        delegate.authsasl(allowed, realm, authzid, u, p, protocolAccess);
                        break;
                    default:
                        throw new IllegalArgumentException("No such authentication scheme: " + authScheme);
                }
            } catch (BadCommandException e) {
                // Don't advertise BAD as failure
                return Optional.of(e);
            } catch (CommandFailedException e) {
                // Don't advertise NO as failure
                return Optional.of(e);
            }
            return Optional.empty();
        }

    }

    private static class CircuitBreakerReadResponseCallable implements Callable<Response> {

        private final ProtocolAccess protocolAccess;
        private final MonitoringCommandExecutor delegate;

        /**
         * Initializes a new {@link CircuitBreakerReadResponseCallable}.
         *
         * @param delegate The delegate
         * @param protocolAccess The protocol access
         */
        CircuitBreakerReadResponseCallable(MonitoringCommandExecutor delegate, ProtocolAccess protocolAccess) {
            super();
            this.delegate = delegate;
            this.protocolAccess = protocolAccess;
        }

        @Override
        public Response call() throws Exception {
            return delegate.readResponse(protocolAccess);
        }
    }

    private static class CircuitBreakerCommandCallable implements Callable<Response[]> {

        private static final List<Class<? extends Exception>> NETWORK_COMMUNICATION_ERRORS = ImmutableList.of(com.sun.mail.iap.ByeIOException.class, java.net.SocketTimeoutException.class, java.io.EOFException.class);

        private final MonitoringCommandExecutor delegate;
        private final String command;
        private final Argument args;
        private final Optional<ResponseInterceptor> optionalInterceptor;
        private final ProtocolAccess protocolAccess;

        /**
         * Initializes a new {@link CircuitBreakerCommandCallable}.
         *
         * @param delegate The delegate
         * @param command The command
         * @param args The optional arguments
         * @param optionalInterceptor The optional interceptor
         * @param protocolAccess The protocol access
         */
        CircuitBreakerCommandCallable(MonitoringCommandExecutor delegate, String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, ProtocolAccess protocolAccess) {
            super();
            this.delegate = delegate;
            this.command = command;
            this.args = args;
            this.optionalInterceptor = optionalInterceptor;
            this.protocolAccess = protocolAccess;
        }

        @Override
        public Response[] call() throws Exception {
            // Obtain responses
            ExecutedCommand executedCommand = delegate.executeCommandExtended(command, args, optionalInterceptor, protocolAccess);
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
