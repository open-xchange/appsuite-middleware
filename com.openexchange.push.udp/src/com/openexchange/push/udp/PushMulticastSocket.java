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

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import com.openexchange.push.udp.registry.PushServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * MultiCastPushSocket
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class PushMulticastSocket implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMulticastSocket.class);

    private Future<Object> thread;

    private static volatile MulticastSocket multicastSocket;

    private boolean running = true;

    private final int multicastPort;

    private final InetAddress multicastAddress;

    public PushMulticastSocket(final PushConfiguration config) {
        multicastPort = config.getMultiCastPort();
        multicastAddress = config.getMultiCastAddress();

        try {
            if (config.isMultiCastEnabled()) {
                LOG.info("Starting Multicast Socket on Port: {}", I(multicastPort));
                multicastSocket = new MulticastSocket(multicastPort);
                multicastSocket.joinGroup(multicastAddress);

                final ThreadPoolService threadPool = PushServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class);
                thread = threadPool.submit(ThreadPools.task(this));
            } else {
                LOG.info("Multicast Socket is disabled");
            }
        } catch (Exception exc) {
            LOG.error("MultiCastPushSocket", exc);
        }
    }

    @Override
    public void run() {
        while (running) {
            final DatagramPacket datagramPacket = new DatagramPacket(new byte[2048], 2048);
            try {
                multicastSocket.receive(datagramPacket);

                if (datagramPacket.getLength() > 0) {
                    final PushRequest serverRegisterRequest = new PushRequest();
                    serverRegisterRequest.init(datagramPacket);
                } else {
                    LOG.warn("recieved empty multicast package: {}", datagramPacket);
                }
            } catch (SocketException e) {
                if (running) {
                    LOG.error("", e);
                }
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
    }

    public void close() {
        running = false;
        if (null != multicastSocket) {
            multicastSocket.close();
            multicastSocket = null;
        }
        if (null != thread) {
            try {
                thread.get();
            } catch (InterruptedException e) {
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                LOG.error("", e);
            } catch (ExecutionException e) {
                final Throwable cause = e.getCause();
                LOG.error("", cause);
            }
            thread = null;
        }
    }

    public static MulticastSocket getPushMulticastSocket() {
        return multicastSocket;
    }
}
