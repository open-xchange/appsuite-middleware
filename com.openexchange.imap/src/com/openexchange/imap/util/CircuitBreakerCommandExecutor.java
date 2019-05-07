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
import java.util.concurrent.atomic.AtomicReference;
import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerException;
import org.fishwife.jrugged.CircuitBreakerExceptionMapper;
import org.fishwife.jrugged.PercentErrPerTimeFailureInterpreter;
import org.fishwife.jrugged.RequestCounter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.CommandEvent;
import com.sun.mail.imap.CommandExecutor;

/**
 * {@link CircuitBreakerCommandExecutor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CircuitBreakerCommandExecutor implements CommandExecutor {

    private static Set<String> lowerCaseSetFor(Set<String> set) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (String elem : set) {
            builder.add(Strings.asciiLowerCase(elem));
        }
        return builder.build();
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final CircuitBreaker circuitBreaker;
    private final HostList hostList;
    private final Set<Integer> ports;

    /**
     * Initializes a new {@link CircuitBreakerCommandExecutor}.
     *
     * @param hostList The hosts to consider
     * @param optPorts The optional ports to consider
     * @param percent The whole number percentage of failures that will be tolerated (i.e. the percentage of failures has to be strictly greater than this number in order to trip the breaker).
     *            For example, if the percentage is <code>3</code>, any calculated failure percentage above that number during the window will cause the breaker to trip.
     * @param windowMillis The length of the window in milliseconds
     * @throws IllegalArgumentException If invalid/arguments are passed
     */
    public CircuitBreakerCommandExecutor(HostList hostList, Set<Integer> optPorts, int percent, long windowMillis) {
        super();
        if (null == hostList) {
            throw new IllegalArgumentException("hosts must not be null or empty.");
        }
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent is required to be between 0 and 100 (inclusive).");
        }
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be greater than 0 (zero).");
        }
        this.hostList = hostList;
        this.ports = null == optPorts || optPorts.isEmpty() ? null : optPorts;
        CircuitBreaker circuitBreaker = new CircuitBreaker("IMAP Circuit Breaker for " + hostList.getHostString());
        PercentErrPerTimeFailureInterpreter failureInterpreter = new PercentErrPerTimeFailureInterpreter(new RequestCounter(), percent, windowMillis);
        circuitBreaker.setFailureInterpreter(failureInterpreter);
        CircuitBreakerExceptionMapper<IOException> exceptionMapper = new CircuitBreakerExceptionMapper<IOException>() {

            @Override
            public IOException map(CircuitBreaker breaker, CircuitBreakerException e) {
                Throwable tripException = breaker.getTripException();
                if (null == tripException) {
                    return new java.net.SocketTimeoutException("Read timed out");
                }
                return (tripException instanceof IOException) ? (IOException) tripException : new IOException(tripException.getMessage(), tripException);
            }
        };
        circuitBreaker.setExceptionMapper(exceptionMapper);
        this.circuitBreaker = circuitBreaker;
    }

    private boolean isApplicable(CommandEvent event) {
        return hostList.contains(event.getHost()) && (null == ports || ports.contains(Integer.valueOf(event.getPort())));
    }

    @Override
    public Response[] executeCommand(String command, Argument args, Protocol protocol) {
        AtomicReference<Response[]> responeReference = new AtomicReference<>(null);
        try {
            return circuitBreaker.invoke(new CircuitBreakerCommandCallable(command, args, protocol, responeReference));
        } catch (Exception e) {
            // Check if exception occurred because circuit breaker was tripped or not
            Response[] responses = responeReference.get();
            if (responses != null) {
                return responses;
            }

            // Exception occurred because circuit breaker was tripped (OPEN)
            return new Response[] { Response.byeResponse(e) };
        }
    }

    @Override
    public boolean onBeforeCommandIssued(CommandEvent event) {
        return isApplicable(event);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private static class CircuitBreakerCommandCallable implements Callable<Response[]> {

        private static final List<Class<? extends Exception>> NETWORK_COMMUNICATION_ERRORS = ImmutableList.of(
            com.sun.mail.iap.ByeIOException.class,
            java.net.SocketTimeoutException.class,
            java.io.EOFException.class);

        private final String command;
        private final Argument args;
        private final Protocol protocol;
        private final AtomicReference<Response[]> responeReference;

        /**
         * Initializes a new {@link CircuitBreakerCommandCallable}.
         */
        CircuitBreakerCommandCallable(String command, Argument args, Protocol protocol, AtomicReference<Response[]> responeReference) {
            super();
            this.command = command;
            this.args = args;
            this.protocol = protocol;
            this.responeReference = responeReference;
        }

        @Override
        public Response[] call() throws Exception {
            // Obtain responses
            Response[] responses = protocol.executeCommand(command, args);

            // Check last response
            Response response = responses[responses.length - 1];
            if (response.isBYE() && response.isTagged()) {
                // Command failed. Check if for a synthetic BYE response providing the causing I/O error
                responeReference.set(responses);
                Exception byeException = response.getByeException();
                if (isEitherOf(byeException, NETWORK_COMMUNICATION_ERRORS)) {
                    // Command failed due to a network communication error. Signal I/O error as failure to circuit breaker
                    throw byeException;
                }
            }

            // Command succeeded or failed for any other reason than a network communication error
            return responses;
        }
    }

}
