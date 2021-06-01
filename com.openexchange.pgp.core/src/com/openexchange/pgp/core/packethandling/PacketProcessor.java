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

package com.openexchange.pgp.core.packethandling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGInputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.ContainedPacket;
import org.bouncycastle.bcpg.InputStreamPacket;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.openpgp.PGPUtil;
import com.openexchange.java.Streams;

/**
 * {@link PacketProcessor} allows to process and modify packets within a PGP message
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 * @see "5. Packet Types" in RFC 4880, "OpenPGP Message Format" (https://www.ietf.org/rfc/rfc4880.txt)
 */
public class PacketProcessor {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PacketProcessor.class);
    private static final int STREAM_BUFFER_SIZE = 1024 * 500 /*500 kB*/ ;
    private static final byte[] GARBAGE_SINK = new byte[STREAM_BUFFER_SIZE];

    /**
     * Parses the next packet from a given stream
     *
     * @param bcIn The stream to parse a packet from
     * @param t
     * @return
     * @throws IOException
     */
    private PGPPacket parseNextPackage(BCPGInputStream in, RememberingInputStream rememberInpuStream) throws IOException {
        try {
            //Resetting the buffer
            //Remember the header parsed by BC so that we can write them back to the output later on
            rememberInpuStream.resetBuffer();
            rememberInpuStream.startRemembering(); //only remember the header ...
            Packet rawPacket = in.readPacket();
            rememberInpuStream.stopRemembering(); //...not the whole package
            if (rawPacket != null) {
                return new PGPPacket(rawPacket, rememberInpuStream.getBuffer());
            }
            else {
                return null;
            }
        } finally {
            //Resetting the buffer
            rememberInpuStream.resetBuffer();
        }
    }

    /**
     * Internal method to handle a parsed packet
     *
     * @param packet The parsed packet to handle
     * @param out The output to write the modified packet to
     * @param handler The handler which want't to modify the parsed package
     * @param rememberInpuStream The underlying stream containing the whole PGP Message
     * @throws IOException
     */
    protected void handlePacket(PGPPacket packet, BCPGOutputStream out, PacketProcessorHandler handler, RememberingInputStream rememberInpuStream) throws Exception {
        if (packet != null) {
            //Handle the packet
            logger.debug("Processing PGP Packet {}", packet.getBcPacket());
            PGPPacket[] newPackets = handler.handlePacket(packet);
            if (out != null && newPackets != null) {
                for (PGPPacket newPacket : newPackets) {
                    //Writing the modifiedPacket back
                    Packet rawModifiedPacket = newPacket.getBcPacket();
                    if (rawModifiedPacket != null) {
                        if (rawModifiedPacket instanceof ContainedPacket) {
                            //ContainedPackets are small and can just be re-encoded to the output stream
                            ((ContainedPacket) rawModifiedPacket).encode(out);
                        }
                        else if (rawModifiedPacket instanceof InputStreamPacket) {
                            BCPGInputStream inputStream = ((InputStreamPacket) rawModifiedPacket).getInputStream();
                            try {
                                //InputStreamPackets cannot be re-encoded;
                                //We need to re-write the already parsed header to the output stream before writing the stream content
                                out.write(newPacket.getBcPacketHeader());

                                //The content-stream might also contain header information (PGP partial streams) which will be parsed out by
                                //Bouncy Castle. Since we do not want to loose this information, which would create
                                //a corrupt data stream, we remember every byte read from the stream and write it to the output
                                rememberInpuStream.resetBuffer();
                                rememberInpuStream.startRemembering();
                                while (inputStream.read(GARBAGE_SINK, 0, STREAM_BUFFER_SIZE) > 0) {
                                    final byte[] buffer = rememberInpuStream.getBuffer();
                                    byte[] modified = handler.handlePacketData(packet, buffer);
                                    if (modified != null) {
                                        out.write(modified, 0, modified.length);
                                    }
                                    rememberInpuStream.resetBuffer();
                                }
                            } finally {
                                Streams.close(inputStream);
                                rememberInpuStream.stopRemembering();
                                rememberInpuStream.resetBuffer();
                            }
                        }
                        else {
                            throw new IllegalArgumentException("Cannot handle unknown PGP BC Packet instance: " + rawModifiedPacket.getClass().getCanonicalName());
                        }
                        out.flush();
                    }
                }
            }
            else {
                //The handler or the caller don't want the package to be written into the output stream;
                //we skip in case the handler did not consumed the data.
                Packet rawPacket = packet.getBcPacket();
                if (rawPacket instanceof InputStreamPacket) {
                    InputStream streamToSkip = ((InputStreamPacket)rawPacket).getInputStream();
                    try {
                        Streams.consume(streamToSkip);
                    } finally {
                        Streams.close(streamToSkip);
                    }
                }
            }
        }
    }

    /**
     * Processes all PGP packets within a given InputStream
     *
     * @param in The stream to parse the PGP packets from
     * @param handler A handler called for each iterated PGP packet
     * @throws Exception
     */
    public void process(InputStream in, PacketProcessorHandler handler) throws Exception {
        process(in, null, handler, false);
    }

    /**
     * Processes all PGP packets within a given InputStream and writes modified packets back to the OutputStream.
     *
     * @param in The stream to parse the PGP packets from
     * @param out The stream to write modified packages to, or <code>null</code> for not writing any data.
     * @param handler A handler defining how to modify the packages within the InputStream while writing them to the OutputStream
     * @param armored True, if the data should be written as ASCII-Armored to the output stream, false for plain binary
     * @throws IOException
     */
    public void process(InputStream in, OutputStream out, PacketProcessorHandler handler, boolean armored) throws Exception {
        try (BCPGOutputStream bcOut = out == null ? null : new BCPGOutputStream(armored ? new ArmoredOutputStream(out) : out);) {

            //We wrap the InputStream in a RememberingInputStream in order to restore packet header later, which has been parsed by BC.
            try (RememberingInputStream rememberingStream = new RememberingInputStream(
                PGPUtil.getDecoderStream(in)); BCPGInputStream bcIn = new BCPGInputStream(rememberingStream);) {

                PGPPacket packet = parseNextPackage(bcIn, rememberingStream);
                handlePacket(packet, bcOut, handler, rememberingStream);
                while (packet != null) {
                    packet = parseNextPackage(bcIn, rememberingStream);
                    handlePacket(packet, bcOut, handler, rememberingStream);
                }
            }
        }
    }
}
