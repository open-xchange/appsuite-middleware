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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import com.openexchange.mailfilter.internal.CircuitBreakerInfo;
import net.jodah.failsafe.CircuitBreakerOpenException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;

/**
 * {@link FailsafeCircuitBreakerBufferedReader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class FailsafeCircuitBreakerBufferedReader extends BufferedReader {

    private final CircuitBreakerInfo circuitBreakerInfo;

    /**
     * Initializes a new {@link FailsafeCircuitBreakerBufferedReader}.
     *
     * @param in A reader
     * @param circuitBreakerInfo The circuit breaker to use
     */
    public FailsafeCircuitBreakerBufferedReader(Reader in, CircuitBreakerInfo circuitBreakerInfo) {
        super(in);
        this.circuitBreakerInfo = circuitBreakerInfo;
    }

    /**
     * Initializes a new {@link FailsafeCircuitBreakerBufferedReader}.
     *
     * @param in A reader
     * @param sz The buffer size
     * @param circuitBreakerInfo The circuit breaker to use
     */
    public FailsafeCircuitBreakerBufferedReader(Reader in, int sz, CircuitBreakerInfo circuitBreakerInfo) {
        super(in, sz);
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
    public int read() throws IOException {
        try {
            return Failsafe.with(circuitBreakerInfo.getCircuitBreaker()).get(new NetworkCommunicationErrorAdvertisingCallable<Integer>() {

                @Override
                protected Integer performIOOperation() throws IOException {
                    return Integer.valueOf(FailsafeCircuitBreakerBufferedReader.super.read());
                }
            }).getCheckedResult().intValue();
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e);
            IOException ioe = new IOException("Denied SIEVE read access since circuit breaker is open.");
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
    public int read(char[] cbuf, int off, int len) throws IOException {
        try {
            return Failsafe.with(circuitBreakerInfo.getCircuitBreaker()).get(new NetworkCommunicationErrorAdvertisingCallable<Integer>() {

                @Override
                protected Integer performIOOperation() throws IOException {
                    return Integer.valueOf(FailsafeCircuitBreakerBufferedReader.super.read(cbuf, off, len));
                }
            }).getCheckedResult().intValue();
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e);
            IOException ioe = new IOException("Denied SIEVE read access since circuit breaker is open.");
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
    public String readLine() throws IOException {
        try {
            return Failsafe.with(circuitBreakerInfo.getCircuitBreaker()).get(new NetworkCommunicationErrorAdvertisingCallable<String>() {

                @Override
                protected String performIOOperation() throws IOException {
                    return FailsafeCircuitBreakerBufferedReader.super.readLine();
                }
            }).getCheckedResult();
        } catch (@SuppressWarnings("unused") CircuitBreakerOpenException e) {
            // Circuit is open
            onDenied(e);
            IOException ioe = new IOException("Denied SIEVE read access since circuit breaker is open.");
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
