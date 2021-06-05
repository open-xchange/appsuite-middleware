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

package com.openexchange.monitoring.impl.sockets.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import com.openexchange.monitoring.sockets.failure.IOFailure;
import com.openexchange.monitoring.sockets.failure.TimeoutFailure;

/**
 * {@link SocketMonitoringInputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SocketMonitoringInputStream extends InputStream {

    private final Socket socket;
    private final InputStream in;

    /**
     * Initializes a new {@link SocketMonitoringInputStream}.
     *
     * @param socket The associated socket
     * @param in The socket input stream to delegate to
     */
    public SocketMonitoringInputStream(Socket socket, InputStream in) {
        super();
        this.socket = socket;
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        long st = System.currentTimeMillis();
        try {
            int result = in.read();
            if (result < 0) {
                SocketMonitoringSystem.getInstance().eof(socket);
            } else {
                SocketMonitoringSystem.getInstance().read(socket, result);
            }
            return result;
        } catch (java.net.SocketTimeoutException e) {
            // Read timed out
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readTimedOut(socket, new TimeoutFailure(e, millis));
            throw e;
        } catch (IOException e) {
            // Read failed
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readError(socket, new IOFailure(e, millis));
            throw e;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long st = System.currentTimeMillis();
        try {
            int length = in.read(b, off, len);
            if (length < 0) {
                SocketMonitoringSystem.getInstance().eof(socket);
            } else {
                SocketMonitoringSystem.getInstance().read(socket, b, off, length);
            }
            return length;
        } catch (java.net.SocketTimeoutException e) {
            // Read timed out
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readTimedOut(socket, new TimeoutFailure(e, millis));
            throw e;
        } catch (IOException e) {
            // Read failed
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readError(socket, new IOFailure(e, millis));
            throw e;
        }
    }
}
