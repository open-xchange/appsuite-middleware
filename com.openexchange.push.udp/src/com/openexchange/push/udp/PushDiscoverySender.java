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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import com.openexchange.java.Charsets;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * PushOutputQueue
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushDiscoverySender implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushDiscoverySender.class);

    private final int multicastPort;
    private final InetAddress multicastAddress;
    private final int remoteHostFresh;
    private final String packetData;
    private final byte[] packetBytes;
    private volatile ScheduledTimerTask task;

    public PushDiscoverySender(final PushConfiguration pushConfigInterface) {
        super();

        InetAddress hostname = pushConfigInterface.getHostName();

        final String discoveryData = String.valueOf(PushRequest.REMOTE_HOST_REGISTER) + '\1' + hostname.getCanonicalHostName()
            + '\1' + String.valueOf(pushConfigInterface.getRegisterPort());
        packetData = String.valueOf(PushRequest.MAGIC) + '\1' + String.valueOf(discoveryData.length()) + '\1' + discoveryData;
        packetBytes = Charsets.toAsciiBytes(packetData);

        multicastPort = pushConfigInterface.getMultiCastPort();
        multicastAddress = pushConfigInterface.getMultiCastAddress();
        remoteHostFresh = pushConfigInterface.getRemoteHostRefresh();
    }

    @Override
    public void run() {
        try {
            LOG.debug("Sending multicast discovery package: \"{}\".", packetData);
            final MulticastSocket multicastSocket = PushMulticastSocket.getPushMulticastSocket();
            final DatagramPacket datagramPacket = new DatagramPacket(packetBytes, packetBytes.length, multicastAddress, multicastPort);
            multicastSocket.send(datagramPacket);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public void startSender(final TimerService service) {
        ScheduledTimerTask task = this.task;
        if (null == task) {
            task = service.scheduleAtFixedRate(this, 0, remoteHostFresh);
            this.task = task;
        }
    }

    public void stopSender() {
        final ScheduledTimerTask task = this.task;
        if (null != task) {
            task.cancel();
            this.task = null;
        }
    }
}
