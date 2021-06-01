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
