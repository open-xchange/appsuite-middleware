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
                LOG.info("Starting Push Register Socket on Port: {}", I(serverRegisterPort));

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
            LOG.error("Could not send package to {}:{} Using {} socket.", host, I(port), channel, x);
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
