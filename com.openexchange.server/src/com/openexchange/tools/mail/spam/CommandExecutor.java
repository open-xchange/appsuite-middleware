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

package com.openexchange.tools.mail.spam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        inputStream.write(in.getBytes());
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
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    buf.append(line).append('\n');
                }
            } catch (final IOException ioe) {
                LOG.error("", ioe);
            } finally {
                Streams.close(br);
                Streams.close(isr);
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
