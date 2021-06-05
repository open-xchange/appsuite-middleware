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
    public static TracerState enableTrace(com.sun.mail.iap.Protocol protocol, OutputStream outputStream) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
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
    public static void restoreTraceState(com.sun.mail.iap.Protocol protocol, TracerState tracerState) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
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

        public TracerStateImpl(boolean trace, OutputStream out) {
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
