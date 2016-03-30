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
        } catch (final Exception e) {
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
