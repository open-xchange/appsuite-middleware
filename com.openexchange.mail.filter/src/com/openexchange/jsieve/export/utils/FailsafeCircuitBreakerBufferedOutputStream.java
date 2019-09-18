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

package com.openexchange.jsieve.export.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;

/**
 * {@link FailsafeCircuitBreakerBufferedOutputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class FailsafeCircuitBreakerBufferedOutputStream extends BufferedOutputStream {

    private final CircuitBreaker circuitBreaker;

    /**
     * Initializes a new {@link FailsafeCircuitBreakerBufferedOutputStream}.
     *
     * @param out The underlying output stream
     * @param circuitBreaker The circuit breaker to use
     */
    public FailsafeCircuitBreakerBufferedOutputStream(OutputStream out, CircuitBreaker circuitBreaker) {
        super(out);
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Initializes a new {@link FailsafeCircuitBreakerBufferedOutputStream}.
     *
     * @param out The underlying output stream
     * @param size The buffer size
     * @param circuitBreaker The circuit breaker to use
     */
    public FailsafeCircuitBreakerBufferedOutputStream(OutputStream out, int size, CircuitBreaker circuitBreaker) {
        super(out, size);
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            Failsafe.with(circuitBreaker).get(new NetworkCommunicationErrorAdvertisingCallable<Void>() {

                @Override
                protected Void performIOOperation() throws IOException {
                    FailsafeCircuitBreakerBufferedOutputStream.super.write(b);
                    return null;
                }
            }).getCheckedResult();
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            IOException ioe = new IOException("Denied SIEVE write access since circuit breaker is open.");
            throw ioe;
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
            Failsafe.with(circuitBreaker).get(new NetworkCommunicationErrorAdvertisingCallable<Void>() {

                @Override
                protected Void performIOOperation() throws IOException {
                    FailsafeCircuitBreakerBufferedOutputStream.super.write(b, off, len);
                    return null;
                }
            }).getCheckedResult();
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            IOException ioe = new IOException("Denied SIEVE write access since circuit breaker is open.");
            throw ioe;
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
            Failsafe.with(circuitBreaker).get(new NetworkCommunicationErrorAdvertisingCallable<Void>() {

                @Override
                protected Void performIOOperation() throws IOException {
                    FailsafeCircuitBreakerBufferedOutputStream.super.flush();
                    return null;
                }
            }).getCheckedResult();
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            IOException ioe = new IOException("Denied SIEVE write access since circuit breaker is open.");
            throw ioe;
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
