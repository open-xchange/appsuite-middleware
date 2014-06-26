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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;


/**
 * {@link MailNotifyPushUdpSocketListener} - A socket listener which receives
 * UDP packets and generates events
 *
 */
public class MailNotifyPushUdpSocketListener implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailNotifyPushUdpSocketListener.class);

    private static final int MAX_UDP_PACKET_SIZE = 4+64+1;

    private static DatagramSocket datagramSocket = null;

    private final String imapLoginDelimiter;

    private final MailNotifyPushListenerRegistry registry;

    public MailNotifyPushUdpSocketListener(MailNotifyPushListenerRegistry registry, final String udpListenHost, final int udpListenPort, final String imapLoginDelimiter, final boolean multicast) throws OXException, IOException {
        super();
        this.registry = registry;
        final InetAddress senderAddress = InetAddress.getByName(udpListenHost);

        this.imapLoginDelimiter = imapLoginDelimiter;
        if (senderAddress != null) {
            if (multicast) {
                datagramSocket = new MulticastSocket(udpListenPort);
                ((MulticastSocket)datagramSocket).joinGroup(senderAddress);
            } else {
                datagramSocket = new DatagramSocket(udpListenPort, senderAddress);
            }
        } else {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Can't get internet addres to given hostname " + udpListenHost);
        }
    }

    private void start() {
        while (true) {
            final DatagramPacket datagramPacket = new DatagramPacket(new byte[MAX_UDP_PACKET_SIZE], MAX_UDP_PACKET_SIZE);
            try {
                datagramSocket.receive(datagramPacket);

                if (datagramPacket.getLength() > 0) {
                    // Packet received
                    final String mailboxName = getMailboxName(datagramPacket);
                    registry.scheduleEvent(mailboxName);
                } else {
                    LOG.warn("recieved empty udp package: {}", datagramSocket);
                }
            } catch (final IOException e) {
                LOG.error("Receiving of UDP packet failed", e);
            }
        }
    }

    private String getMailboxName(final DatagramPacket datagramPacket) {
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
        // user name at position 3, see above
        packetDataString = packetDataString.split("\0")[3];
        if (null != imapLoginDelimiter) {
        	final int idx;
        	idx = packetDataString.indexOf(imapLoginDelimiter);
        	if (idx != -1) {
        		packetDataString = packetDataString.substring(0, idx);
        	}
        }
        LOG.debug("Username={}", packetDataString);
        if (null != packetDataString && packetDataString.length() > 0) {
            return packetDataString;
        } else {
            return null;
        }
    }

    @Override
    public void run() {
        start();
    }
}
