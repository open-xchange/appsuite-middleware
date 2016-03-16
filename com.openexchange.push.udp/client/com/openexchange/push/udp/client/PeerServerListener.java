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

package com.openexchange.push.udp.client;

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
        send(PushRequest.REMOTE_HOST_REGISTER, myAddress.getHostAddress(), myPort);
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

        } catch (final Exception x) {
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
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(final String[] args) {
        final String remoteHost = "192.168.33.24"; //args[0];
        final String remotePort = "44335"; //args[1];

        final String myHost = "192.168.33.24"; //args[3];
        final String myPort = "44331"; //args[4];

        try {
            new PeerServerListener(remoteHost, remotePort, myHost, myPort).send(PushRequest.REMOTE_HOST_REGISTER,"fe80::223:32ff:fec8:2bc8", myPort);
        } catch (final Exception e) {
            LOG.error("Unexpected exception.", e);
        }
    }
}
