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

package com.openexchange.push.udp;

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
                LOG.info("Starting Multicast Socket on Port: {}", multicastPort);
                multicastSocket = new MulticastSocket(multicastPort);
                multicastSocket.joinGroup(multicastAddress);

                final ThreadPoolService threadPool = PushServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class);
                thread = threadPool.submit(ThreadPools.task(this));
            } else {
                LOG.info("Multicast Socket is disabled");
            }
        } catch (final Exception exc) {
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
            } catch (final SocketException e) {
                if (running) {
                    LOG.error("", e);
                }
            } catch (final IOException e) {
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
            } catch (final InterruptedException e) {
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                LOG.error("", e);
            } catch (final ExecutionException e) {
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
