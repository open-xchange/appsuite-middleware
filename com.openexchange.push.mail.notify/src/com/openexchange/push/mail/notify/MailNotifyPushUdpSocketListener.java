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

package com.openexchange.push.mail.notify;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;


/**
 * {@link MailNotifyPushUdpSocketListener} - A socket listener which receives
 * UDP packets and generates events
 *
 */
public class MailNotifyPushUdpSocketListener implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailNotifyPushUdpSocketListener.class);

    private static final int MAX_UDP_PACKET_SIZE = 4 + 64 + 1; // 69

    // --------------------------------------------------------------------------------------------------------------------------------

    private final DatagramSocket datagramSocket;
    private final String imapLoginDelimiter;
    private final MailNotifyPushListenerRegistry registry;
    private final AtomicBoolean stopped;
    private volatile Future<Object> future;

    /**
     * Initializes a new {@link MailNotifyPushUdpSocketListener}.
     */
    public MailNotifyPushUdpSocketListener(MailNotifyPushListenerRegistry registry, String udpListenHost, int udpListenPort, String imapLoginDelimiter, boolean multicast) throws OXException, IOException {
        super();
        stopped = new AtomicBoolean(false);
        InetAddress senderAddress = InetAddress.getByName(udpListenHost);
        if (senderAddress == null) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Can't get Internet Protocol (IP) addres for given hostname " + udpListenHost);
        }

        this.registry = registry;
        this.imapLoginDelimiter = imapLoginDelimiter;
        if (multicast) {
            datagramSocket = new MulticastSocket(udpListenPort);
            ((MulticastSocket)datagramSocket).joinGroup(senderAddress);
        } else {
            datagramSocket = new DatagramSocket(udpListenPort, senderAddress);
        }
    }

    /**
     * Sets the associated <code>Future</code> instance
     *
     * @param future The <code>Future</code> instance
     */
    public void setFuture(Future<Object> future) {
        this.future = future;
    }

    /**
     * Closes this UDP listener.
     */
    public void close() {
        if (stopped.compareAndSet(false, true)) {
            datagramSocket.close();
        }

        Future<Object> future = this.future;
        if (null != future) {
            future.cancel(true);
            this.future = null;
        }
    }

    @Override
    public void run() {
        while (!stopped.get()) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[MAX_UDP_PACKET_SIZE], MAX_UDP_PACKET_SIZE);
            try {
                datagramSocket.receive(datagramPacket);

                // Check packet length
                if (datagramPacket.getLength() <= 0) {
                    LOG.warn("recieved empty udp package: {}", datagramSocket);
                    return;
                }

                // Valid packet received - Schedule an event for extracted mailbox name
                registry.scheduleEvent(getMailboxNameFromPacket(datagramPacket));
            } catch (SocketException e) {
                LOG.info("UDP socket closed");
            } catch (InterruptedIOException e) {
                LOG.error("Receiving of UDP packet interrupted", e);
            } catch (IOException e) {
                LOG.error("Receiving of UDP packet failed", e);
            }
        }
    }

    private String getMailboxNameFromPacket(DatagramPacket datagramPacket) {
        /* TODO: this currently works with cyrus notify must be configurable somehow later
         *
         * Format:
         *   notifyd/notifyd.c:
         *   method NUL class NUL priority NUL user NUL mailbox NUL
         *   nopt NUL N(option NUL) message NUL
         *
         * Example:
         *
         *  log\0MAIL\0\0postmaster\0INBOX\00\0From: root@oxsles11.example.com (root)
         *  Subject: asdf To: postmaster@example.com
         */

        String packetDataString = new String(datagramPacket.getData());
        // User name at position 3, see above
        packetDataString = packetDataString.split("\0")[3];

        String delimiter = imapLoginDelimiter;
        if (null != delimiter) {
        	int idx = packetDataString.indexOf(delimiter);
        	if (idx >= 0) {
        		packetDataString = packetDataString.substring(0, idx);
        	}
        }

        LOG.debug("Username={}", packetDataString);
        return null != packetDataString && packetDataString.length() > 0 ? packetDataString : null;
    }

}
