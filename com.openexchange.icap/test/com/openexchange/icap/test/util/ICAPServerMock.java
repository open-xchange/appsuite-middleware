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

package com.openexchange.icap.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.openexchange.tools.io.IOUtils;

/**
 * {@link ICAPServerMock}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPServerMock {

    public static final String SHUT_DOWN_COMMAND = "shutdown";
    private final Executor executor;

    /**
     * Initialises a new {@link ICAPServerMock}.
     */
    public ICAPServerMock() {
        super();
        executor = Executors.newFixedThreadPool(5);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(ICAPTestProperties.ICAP_SERVER_PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(() -> handleRequest(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket socket) {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                String inputLine = in.readLine();
                System.out.println("Client says: " + inputLine);
                out.println("You said: '" + inputLine + '"');
                out.flush();
                if (inputLine == null) {
                    continue;
                }
                if (inputLine.equals(SHUT_DOWN_COMMAND)) {
                    System.out.println("Received the TERMINATE command. Shutting down.");
                    return;
                }
            }
        } catch (IOException e) {
           e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(socket);
        }
    }
}
