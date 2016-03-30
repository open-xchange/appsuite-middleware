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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link PushChannels}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PushChannels {

    public static enum ChannelType {
        INTERNAL, EXTERNAL;
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushChannels.class);

    private DatagramSocket internalChannel = null;
    private DatagramSocket externalChannel = null;

    private final List<PushRegistryListenerThread> listeners = new LinkedList<PushRegistryListenerThread>();

    public PushChannels(PushConfiguration config) throws OXException {
        final int serverRegisterPort = config.getRegisterPort();
        final InetAddress senderAddress = config.getSenderAddress();

        final InetAddress internalSenderAddress = config.getHostName();

        try {
            if (config.isPushEnabled()) {
                LOG.info("Starting Push Register Socket on Port: {}", serverRegisterPort);

                if (senderAddress != null) {
                    externalChannel = new DatagramSocket(serverRegisterPort, senderAddress);
                } else {
                    externalChannel = new DatagramSocket(serverRegisterPort);
                }

                if (internalSenderAddress != null && senderAddress != null && !isSameInterface(senderAddress, internalSenderAddress)) {
                    internalChannel = new DatagramSocket(serverRegisterPort, internalSenderAddress);
                }

                listenForRegistrations();

            } else {
                LOG.info("Push Registration is disabled");
            }
        } catch (SocketException e) {
            throw PushUDPExceptionCode.NO_CHANNEL.create(e);
        }
    }

    private boolean isSameInterface(InetAddress senderAddress, InetAddress internalSenderAddress) throws SocketException {
        NetworkInterface senderIface = NetworkInterface.getByInetAddress(senderAddress);
        NetworkInterface internalIface = NetworkInterface.getByInetAddress(internalSenderAddress);

        return senderIface != null && senderIface.equals(internalIface);
    }

    private void listenForRegistrations() {
        listeners.add(new PushRegistryListenerThread(externalChannel));

        if (internalChannel != null) {
            listeners.add(new PushRegistryListenerThread(internalChannel));
        }

        for (Thread t : listeners) {
            t.start();
        }
    }

    public DatagramSocket getInternalChannel() throws OXException {
        if (null == internalChannel) {
            return getExternalChannel();
        }
        return internalChannel;
    }


    public DatagramSocket getExternalChannel() throws OXException {
        if (null == externalChannel) {
            throw PushUDPExceptionCode.NO_CHANNEL.create();
        }
        return externalChannel;
    }

    public void makeAndSendPackage(final byte[] b, final InetAddress host, final int port, ChannelType channel) throws OXException {
        final DatagramPacket datagramPackage = new DatagramPacket(b, b.length, host, port);
        try {
            getSocket(channel).send(datagramPackage);
        } catch (IOException x) {
            LOG.error("Could not send package to {}:{} Using {} socket.", host, port, channel, x);
        }
    }

    public void makeAndSendPackage(final byte[] b, final String host, final int port, ChannelType channel) throws UnknownHostException, OXException {
        makeAndSendPackage(b, InetAddress.getByName(host), port, channel);
    }

    private DatagramSocket getSocket(ChannelType channel) throws OXException {
        if (channel == ChannelType.INTERNAL) {
            return getInternalChannel();
        }
        return getExternalChannel();
    }

    public void shutdown() {
        try {
            if (internalChannel != null) {
                internalChannel.close();
            }
        } catch (Exception x) {
            // Don't care
        }
        try {
            externalChannel.close();
        } catch (Exception x) {
            // Don't care
        }

        for (PushRegistryListenerThread t : listeners) {
            t.stopListening();
        }
    }


}
