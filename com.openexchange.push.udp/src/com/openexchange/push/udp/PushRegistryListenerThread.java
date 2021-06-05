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

package com.openexchange.push.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link PushRegistryListenerThread}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
// Refactored from PushSocket
public class PushRegistryListenerThread extends Thread {

    private static final AtomicInteger instances = new AtomicInteger(0);

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushRegistryListenerThread.class);

    private final DatagramSocket datagramSocket;

    private boolean running;

    public PushRegistryListenerThread(DatagramSocket socket) {
        this.datagramSocket = socket;
        setName("PushRegistryListenerThread-"+instances.getAndIncrement());
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            final DatagramPacket datagramPacket = new DatagramPacket(new byte[2048], 2048);
            try {
                datagramSocket.receive(datagramPacket);

                if (datagramPacket.getLength() > 0) {
                    final PushRequest serverRegisterRequest = new PushRequest();
                    serverRegisterRequest.init(datagramPacket);
                } else {
                    LOG.warn("received empty udp package: {}", datagramSocket);
                }
            } catch (SocketException e) {
                LOG.error("", e);
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
    }

    public void stopListening() {
        running = false;
    }
}
