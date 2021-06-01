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

package com.openexchange.jsieve.export.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import com.openexchange.mailfilter.internal.CircuitBreakerInfo;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;

/**
 * {@link FailsafeCircuitBreakerBufferedOutputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
@SuppressWarnings("synthetic-access")
public class FailsafeCircuitBreakerBufferedOutputStream extends BufferedOutputStream {

    private final CircuitBreakerInfo circuitBreakerInfo;

    /**
     * Initializes a new {@link FailsafeCircuitBreakerBufferedOutputStream}.
     *
     * @param out The underlying output stream
     * @param circuitBreakerInfo The circuit breaker to use
     */
    public FailsafeCircuitBreakerBufferedOutputStream(OutputStream out, CircuitBreakerInfo circuitBreakerInfo) {
        super(out);
        this.circuitBreakerInfo = circuitBreakerInfo;
    }

    /**
     * Initializes a new {@link FailsafeCircuitBreakerBufferedOutputStream}.
     *
     * @param out The underlying output stream
     * @param size The buffer size
     * @param circuitBreakerInfo The circuit breaker to use
     */
    public FailsafeCircuitBreakerBufferedOutputStream(OutputStream out, int size, CircuitBreakerInfo circuitBreakerInfo) {
        super(out, size);
        this.circuitBreakerInfo = circuitBreakerInfo;
    }

    /**
     * Is called when the circuit breaker denied an access attempt because it is currently open and not allowing executions to occur.
     *
     * @param exception The thrown exception when an execution is attempted while a configured CircuitBreaker is open
     */
    private void onDenied(CircuitBreakerOpenException exception) {
        circuitBreakerInfo.incrementDenials();
    }

    @Override
    public void write(int b) throws IOException {
        try {
            Failsafe.with(circuitBreakerInfo.getCircuitBreaker()).get(new NetworkCommunicationErrorAdvertisingCallable<Void>() {

                @Override
                protected Void performIOOperation() throws IOException {
                    FailsafeCircuitBreakerBufferedOutputStream.super.write(b);
                    return null;
                }
            }).getCheckedResult();
        } catch (CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e);
            throw new IOException("Denied SIEVE write access since circuit breaker is open.");
        } catch (FailsafeException e) {
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
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            Failsafe.with(circuitBreakerInfo.getCircuitBreaker()).get(new NetworkCommunicationErrorAdvertisingCallable<Void>() {

                @Override
                protected Void performIOOperation() throws IOException {
                    FailsafeCircuitBreakerBufferedOutputStream.super.write(b, off, len);
                    return null;
                }
            }).getCheckedResult();
        } catch (CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e);
            throw new IOException("Denied SIEVE write access since circuit breaker is open.");
        } catch (FailsafeException e) {
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
    public void flush() throws IOException {
        try {
            Failsafe.with(circuitBreakerInfo.getCircuitBreaker()).get(new NetworkCommunicationErrorAdvertisingCallable<Void>() {

                @Override
                protected Void performIOOperation() throws IOException {
                    FailsafeCircuitBreakerBufferedOutputStream.super.flush();
                    return null;
                }
            }).getCheckedResult();
        } catch (CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e);
            throw new IOException("Denied SIEVE write access since circuit breaker is open.");
        } catch (FailsafeException e) {
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

}
