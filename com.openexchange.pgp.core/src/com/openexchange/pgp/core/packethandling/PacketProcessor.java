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

package com.openexchange.pgp.core.packethandling;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGInputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.ContainedPacket;
import org.bouncycastle.bcpg.InputStreamPacket;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.openpgp.PGPUtil;

/**
 * {@link PacketProcessor} allows to process and modify packets within a PGP message
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v2.4.2
 * @see "5. Packet Types" in RFC 4880, "OpenPGP Message Format" (https://www.ietf.org/rfc/rfc4880.txt)
 */
public class PacketProcessor {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PacketProcessor.class);

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
            Packet rawPacket = in.readPacket();
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
     * @throws IOException
     */
    protected void handlePacket(PGPPacket packet, BCPGOutputStream out, PacketProcessorHandler handler) throws Exception {
        if (packet != null) {
            //Handle the packet
            logger.debug("Processing PGP Packet " + packet.getBcPacket());
            PGPPacket[] newPackets = handler.handlePacket(packet);
            if (newPackets != null) {
                for (PGPPacket newPacket : newPackets) {
                    //Writing the modifiedPacket back 
                    Packet rawModifiedPacket = newPacket.getBcPacket();
                    if (rawModifiedPacket != null) {
                        if (rawModifiedPacket instanceof ContainedPacket) {
                            //ContainedPackets are small and can just be re-encoded to the output stream
                            ((ContainedPacket) rawModifiedPacket).encode(out);
                        }
                        else if (rawModifiedPacket instanceof InputStreamPacket) {
                            //InputStreamPackets cannot be re-encoded; 
                            //We need to re-write the already parsed header to the output stream before writing the stream content
                            IOUtils.copy(new ByteArrayInputStream(newPacket.getBcPacketHeader()), out);
                            IOUtils.copy(((InputStreamPacket) rawModifiedPacket).getInputStream(), out);
                        }
                        else {
                            throw new IllegalArgumentException("Cannot handle unknown PGP BC Packet instance: " + rawModifiedPacket.getClass().getCanonicalName());
                        }
                        out.flush();
                    }
                }
            }
        }
    }

    /**
     * Process all packets within a given InputStream and writes modified packets back to the OutputStream
     * 
     * @param in The stream to parse the PGP packets from
     * @param out The stream to write modified packages to
     * @param handler A handler defining how to modify the packages within the InputStream while writing them to the OutputStream
     * @param armored True, if the data should be written as ASCII-Armored to the output stream, false for plain binary
     * @throws IOException
     */
    public void process(InputStream in, OutputStream out, PacketProcessorHandler handler, boolean armored) throws Exception {
        try (BCPGOutputStream bcOut = new BCPGOutputStream(armored ? new ArmoredOutputStream(out) : out);) {

            //We wrap the InputStream in a RememberingInputStream in order to restore packet header later, which has been parsed by BC.
            try (RememberingInputStream rememberingStream = new RememberingInputStream(PGPUtil.getDecoderStream(in));
                BCPGInputStream bcIn = new BCPGInputStream(rememberingStream);) {

                PGPPacket packet = parseNextPackage(bcIn, rememberingStream);
                handlePacket(packet, bcOut, handler);
                while (packet != null) {
                    packet = parseNextPackage(bcIn, rememberingStream);
                    handlePacket(packet, bcOut, handler);
                }
            }
        }
    }
}
