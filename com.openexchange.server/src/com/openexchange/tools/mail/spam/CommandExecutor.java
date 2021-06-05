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

package com.openexchange.tools.mail.spam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * CommandExecutor - executes given command in a separate process and supports possibility to additionally send data to running process and
 * reading its output and/or error data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CommandExecutor {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CommandExecutor.class);

    private final Process process;

    private final InputStreamSucker errSucker;

    private final InputStreamSucker outSucker;

    private final OutputStream inputStream;

    /**
     * Creates and starts a process that executes given command
     */
    public CommandExecutor(final String cmd) throws IOException {
        if (cmd == null) {
            throw new IllegalArgumentException("command is null");
        }
        process = Runtime.getRuntime().exec(cmd);
        errSucker = new InputStreamSucker(process.getErrorStream());
        outSucker = new InputStreamSucker(process.getInputStream());
        inputStream = process.getOutputStream();
        errSucker.start();
        outSucker.start();
    }

    /**
     * Writes given string into process' input
     *
     * @throws IOException
     */
    public void send(final String in) throws IOException {
        inputStream.write(in.getBytes(StandardCharsets.ISO_8859_1));
        inputStream.flush();
        inputStream.close();
    }

    /**
     * Turns given input stream to process' input
     *
     * @throws IOException
     */
    public void send(final InputStream in) throws IOException {
        streamCopy(in, inputStream);
        inputStream.flush();
        inputStream.close();
    }

    /**
     * Causes the curretn thread to wait until background process has terminated.
     *
     * @return process' exit code
     */
    public int waitFor() throws InterruptedException {
        final int exitCode = process.waitFor();
        waitForThreads();
        return exitCode;
    }

    /**
     * @return error
     */
    public String getErrorString() {
        return errSucker.getBuffer();
    }

    /**
     * @return output
     */
    public String getOutputString() {
        return outSucker.getBuffer();
    }

    /**
     * Wait until all output and/or error data have been read
     */
    private void waitForThreads() throws InterruptedException {
        if (outSucker.isAlive()) {
            outSucker.join();
        }
        if (errSucker.isAlive()) {
            errSucker.join();
        }
    }

    /**
     * InputStreamSucker - writes content of an <code>java.io.InputStream</code> into a <code>java.lang.StringBuilder</code> in a separate
     * thread. The content is then accessible through method <code>getBuffer()</code>.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    public static class InputStreamSucker extends Thread {

        private final InputStream is;

        private final StringBuilder buf;

        public InputStreamSucker(final InputStream is) {
            this.is = is;
            buf = new StringBuilder();
        }

        @Override
        public void run() {
            try {
                buf.append(Streams.stream2string(is, Charsets.UTF_8_NAME));
            } catch (IOException ioe) {
                LOG.error("", ioe);
            }
        }

        public final String getBuffer() {
            return buf.toString();
        }
    }

    private static final int BUFFERSIZE = 8192;

    private static long streamCopy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[BUFFERSIZE];
        int read;
        long copied = 0;
        while ((read = in.read(buffer)) > 0) {
            out.write(buffer, 0, read);
            copied += read;
        }
        return copied;
    }
}
