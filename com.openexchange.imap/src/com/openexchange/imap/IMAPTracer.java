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

package com.openexchange.imap;

import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * {@link IMAPTracer} - Utility methods to enable and restore IMAP protocol's {@link com.sun.mail.util.TraceInputStream TraceInputStream}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPTracer {

    /**
     * Initializes a new {@link IMAPTracer}.
     */
    private IMAPTracer() {
        super();
    }

    /**
     * Enables the {@link com.sun.mail.util.TraceInputStream TraceInputStream} of specified protocol.
     *
     * @param protocol The protocol
     * @param outputStream The output stream to apply to protocol's {@link com.sun.mail.util.TraceInputStream TraceInputStream}
     * @return The previous state of protocol's {@link com.sun.mail.util.TraceInputStream TraceInputStream}
     * @throws SecurityException If a security error occurs
     * @throws NoSuchFieldException If a field does not exist
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field
     *             (or a subclass or implementor thereof)
     * @throws IllegalAccessException If the underlying field is inaccessible
     */
    public static TracerState enableTrace(final com.sun.mail.iap.Protocol protocol, final OutputStream outputStream) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field traceInputField = com.sun.mail.iap.Protocol.class.getDeclaredField("traceInput");
        traceInputField.setAccessible(true);
        /*
         * Fetch trace input stream
         */
        final com.sun.mail.util.TraceInputStream tracer = (com.sun.mail.util.TraceInputStream) traceInputField.get(protocol);
        /*
         * Fetch tracer's flag
         */
        final Field traceField = com.sun.mail.util.TraceInputStream.class.getDeclaredField("trace");
        traceField.setAccessible(true);
        /*
         * Backup old
         */
        final boolean oldTrace = traceField.getBoolean(tracer);
        /*
         * Set new
         */
        tracer.setTrace(true);
        /*
         * Fetch tracer's stream
         */
        final Field outField = com.sun.mail.util.TraceInputStream.class.getDeclaredField("traceOut");
        outField.setAccessible(true);
        /*
         * Backup old
         */
        final OutputStream oldOut = (OutputStream) outField.get(tracer);
        /*
         * Set new
         */
        outField.set(tracer, outputStream);
        /*
         * Return old state
         */
        return new TracerStateImpl(oldTrace, oldOut);
    }

    /**
     * Restores specified trace state for given protocol's {@link com.sun.mail.util.TraceInputStream TraceInputStream}.
     *
     * @param protocol The protocol whose {@link com.sun.mail.util.TraceInputStream TraceInputStream} shall be restored
     * @param tracerState The trace state to restore
     * @throws SecurityException If a security error occurs
     * @throws NoSuchFieldException If a field does not exist
     * @throws IllegalArgumentException If the specified object is not an instance of the class or interface declaring the underlying field
     *             (or a subclass or implementor thereof)
     * @throws IllegalAccessException If the underlying field is inaccessible
     */
    public static void restoreTraceState(final com.sun.mail.iap.Protocol protocol, final TracerState tracerState) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field traceInputField = com.sun.mail.iap.Protocol.class.getDeclaredField("traceInput");
        traceInputField.setAccessible(true);
        /*
         * Fetch trace input stream
         */
        final com.sun.mail.util.TraceInputStream tracer = (com.sun.mail.util.TraceInputStream) traceInputField.get(protocol);
        /*
         * Restore flag
         */
        tracer.setTrace(tracerState.isTrace());
        /*
         * Fetch tracer's stream
         */
        final Field outField = com.sun.mail.util.TraceInputStream.class.getDeclaredField("traceOut");
        outField.setAccessible(true);
        /*
         * Restore out
         */
        outField.set(tracer, tracerState.getOut());
    }

    /**
     * Helper to store the state of a {@link com.sun.mail.util.TraceInputStream TraceInputStream} instance.
     */
    public static interface TracerState {

        /**
         * Gets the trace flag.
         *
         * @return The trace flag
         */
        public boolean isTrace();

        /**
         * Gets the tracer's output stream.
         *
         * @return The tracer's output stream
         */
        public OutputStream getOut();
    }

    private static final class TracerStateImpl implements TracerState {

        private final boolean trace;

        private final java.io.OutputStream out;

        public TracerStateImpl(final boolean trace, final OutputStream out) {
            super();
            this.trace = trace;
            this.out = out;
        }

        @Override
        public boolean isTrace() {
            return trace;
        }

        @Override
        public OutputStream getOut() {
            return out;
        }

    }

}
