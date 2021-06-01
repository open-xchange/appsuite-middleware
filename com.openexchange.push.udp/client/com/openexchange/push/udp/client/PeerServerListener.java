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

package com.openexchange.push.udp.client;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import com.openexchange.java.Charsets;
import com.openexchange.push.udp.PushRequest;

/**
 * {@link PeerServerListener}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PeerServerListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PeerServerListener.class);

    private final InetAddress remoteAddress;
    private final int remotePort;

    private final InetAddress myAddress;
    private final int myPort;


    private final DatagramSocket mySocket;

    public PeerServerListener(final String remoteHost, final String remotePort, final String myHost, final String myPort) throws SocketException, UnknownHostException {

        this.remoteAddress = InetAddress.getByName(remoteHost);
        this.remotePort = Integer.parseInt(remotePort);

        this.myAddress = InetAddress.getByName(myHost);
        this.myPort = Integer.parseInt(myPort);


        mySocket = new DatagramSocket(this.myPort, myAddress);
    }

    public void run() throws IOException {
        register();

        while (true) {
            receiveAndPrint();
        }
    }

    private void register() throws IOException {
        send(I(PushRequest.REMOTE_HOST_REGISTER), myAddress.getHostAddress(), I(myPort));
    }

    private void receiveAndPrint() {
        final DatagramPacket datagramPacket = new DatagramPacket(new byte[2048], 2048);
        LOG.info("I'm listenin'");
        try {
            mySocket.receive(datagramPacket);
            final String[] event = new String(datagramPacket.getData(), com.openexchange.java.Charsets.UTF_8).split("\1");

            final StringBuilder b = new StringBuilder();
            for (final String string : event) {
                b.append(string).append('\t');
            }

            LOG.info(b.toString());

        } catch (Exception x) {
            LOG.error("Unexpected exception.", x);
        }
    }

    private void send(final Object... args) throws IOException {
        final StringBuilder b = new StringBuilder();
        byte[] data;
        try {
            for(final Object o : args) {
                b.append(o.toString()).append('\1');
            }
            b.setLength(b.length()-1);
            final StringBuilder b2 = new StringBuilder().append(PushRequest.MAGIC).append('\1').append(b.length()).append('\1').append(b);
            data = Charsets.toAsciiBytes(b2);
            final DatagramPacket datagramPackage = new DatagramPacket(data, data.length, remoteAddress, remotePort);
            mySocket.send(datagramPackage);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(final String[] args) {
        final String remoteHost = "192.168.33.24"; //args[0];
        final String remotePort = "44335"; //args[1];

        final String myHost = "192.168.33.24"; //args[3];
        final String myPort = "44331"; //args[4];

        try {
            new PeerServerListener(remoteHost, remotePort, myHost, myPort).send(I(PushRequest.REMOTE_HOST_REGISTER),"fe80::223:32ff:fec8:2bc8", myPort);
        } catch (Exception e) {
            LOG.error("Unexpected exception.", e);
        }
    }
}
