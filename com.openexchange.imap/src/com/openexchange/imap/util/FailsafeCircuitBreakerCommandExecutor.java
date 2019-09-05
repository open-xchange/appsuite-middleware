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
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableList;
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
 * {@link FailsafeCircuitBreakerCommandExecutor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class FailsafeCircuitBreakerCommandExecutor implements CommandExecutor {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FailsafeCircuitBreakerCommandExecutor.class);

    private final CircuitBreaker circuitBreaker;
    private final HostList hostList;
    private final Set<Integer> ports;

    /**
     * Initializes a new {@link FailsafeCircuitBreakerCommandExecutor}.
     *
     * @param hostList The hosts to consider
     * @param optPorts The optional ports to consider
     * @param failureThreshold The number of successive failures that must occur in order to open the circuit
     * @param successThreshold The number of successive successful executions that must occur when in a half-open state in order to close the circuit
     * @param delayMillis The number of milliseconds to wait in open state before transitioning to half-open
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    public FailsafeCircuitBreakerCommandExecutor(HostList hostList, Set<Integer> optPorts, int failureThreshold, int successThreshold, long delayMillis) {
        super();
        if (null == hostList || hostList.isEmpty()) {
            throw new IllegalArgumentException("hostList must not be null or empty.");
        }
        if (failureThreshold < 0) {
            throw new IllegalArgumentException("failureThreshold must be greater than 0 (zero).");
        }
        if (successThreshold < 0) {
            throw new IllegalArgumentException("successThreshold must be greater than 0 (zero).");
        }
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be greater than 0 (zero).");
        }

        this.hostList = hostList;
        this.ports = null == optPorts || optPorts.isEmpty() ? null : optPorts;

        CircuitBreaker circuitBreaker = new CircuitBreaker()
            .withFailureThreshold(failureThreshold)
            .withSuccessThreshold(successThreshold)
            .withDelay(delayMillis, TimeUnit.MILLISECONDS)
            .onOpen(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    LOG.info("Circuit breaker opened for: {}", hostList.getHostString());
                }
            })
            .onHalfOpen(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    LOG.info("Circuit breaker half-opened for: {}", hostList.getHostString());
                }
            })
            .onClose(new CheckedRunnable() {

                @Override
                public void run() throws Exception {
                    LOG.info("Circuit breaker closed for: {}", hostList.getHostString());
                }
            });
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public boolean isApplicable(Protocol protocol) {
        return hostList.contains(protocol.getHost()) && (null == ports || ports.contains(Integer.valueOf(protocol.getPort())));
    }

    @Override
    public Response[] executeCommand(String command, Argument args, Protocol protocol) {
        try {
            return Failsafe.with(circuitBreaker).get(new CircuitBreakerCommandCallable(command, args, protocol));
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            IOException ioe = new IOException("Denied IMAP command since circuit is open.");
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
        sb.append("hosts=").append(hostList.getHostString());
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
