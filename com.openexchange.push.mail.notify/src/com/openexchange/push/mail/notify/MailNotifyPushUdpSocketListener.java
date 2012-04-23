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

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailNotifyPushUdpSocketListener.class));

    private static final int MAX_UDP_PACKET_SIZE = 4+64+1;

    private static DatagramSocket datagramSocket = null;

    private final String imapLoginDelimiter;

    public MailNotifyPushUdpSocketListener(final String udpListenHost, final int udpListenPort, final String imapLoginDelimiter, final boolean multicast) throws OXException, IOException {
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
                    MailNotifyPushListenerRegistry.getInstance().fireEvent(mailboxName);
                } else {
                    LOG.warn("recieved empty udp package: " + datagramSocket);
                }
            } catch (final IOException e) {
                LOG.error("Receiving of UDP packet failed: " + e.getMessage(), e);
            } catch (final OXException e) {
                LOG.error("Failed to create push event: " + e.getMessage(), e);
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
        LOG.debug("Username=" + packetDataString);
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
