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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.bcpg.PublicKeyEncSessionPacket;

/**
 * {@link ExtractSessionProcessorHandler}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class ExtractSessionProcessorHandler implements PacketProcessorHandler {

    private final List<EncryptedSession> encryptedSessions = new ArrayList<EncryptedSession>();

    /**
     * {@link EncryptedSession} represents a "Public-Key Encrypted Session Key Packet" containing the encrypted session key.
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.0
     */
    public static class EncryptedSession {

        private final int version;
        private final int algorithm;
        private final long keyId;
        private final byte[][] encryptedSessionKey;

        /**
         * Initializes a new {@link EncryptedSession}.
         *
         * @param version The version of the packet
         * @param algorithm The asymetric algorithm used to encrypt the session key.
         * @param keyId The ID of the key used to encrypt the session key.
         * @param encryptedSessionKey The encrypted session key.
         */
        private EncryptedSession(int version, int algorithm, long keyId, byte[][] encryptedSessionKey) {
           this.version = version;
           this.algorithm = algorithm;
           this.keyId = keyId;
           this.encryptedSessionKey = encryptedSessionKey;
        }

        /**
         *  The version number of the session packet.
         *
         * @return The version
         */
        public int getVersion() {
            return version;
        }

        /**
         * The ID of the asymmetric algorithm used to encrypt the session data.
         *
         * @return The algorithm
         */
        public int getAlgorithm() {
            return algorithm;
        }

        /**
         * The ID of the key used to encrypt the session data.
         *
         * @return The keyId
         */
        public long getKeyId() {
            return keyId;
        }

        /**
         * Gets the encrypted sessionData
         *
         * @return The encrypted sessionData
         */
        public byte[][] getEncryptedSessionKey() {
            return encryptedSessionKey;
        }

        /**
         * Returns this object as encoded PGP object
         *
         * @return This object as PGP object byte representation
         * @throws IOException
         */
        public byte[] getEncoded() throws IOException {
            return new PublicKeyEncSessionPacket(keyId, algorithm, encryptedSessionKey).getEncoded();
        }
    }

    /**
     * Internal method to convert a {@link PublicKeyEncSessionPacket} to a {@link EncryptedSession} object.
     *
     * @param packet The packet to convert.
     * @return The session object
     */
    private EncryptedSession toEncryptedSession(PublicKeyEncSessionPacket packet) {
        return new EncryptedSession(
            packet.getVersion(),
            packet.getAlgorithm(),
            packet.getKeyID(),
            packet.getEncSessionKey());
    }

    /**
     * Gets all extracted encrypted session
     *
     * @return A list of all extracted encrypted Session packets
     */
    public Collection<EncryptedSession> getEncryptedSessions(){
        return Collections.unmodifiableCollection(this.encryptedSessions);
    }

    /**
     * Gets the extracted session packet for a given key ID.
     *
     * @param keyId The ID of the key to get the session packet for
     * @return The encrypted session packet for the given key ID, or null if no such session packet was found.
     */
    public EncryptedSession getEncryptedSession(long keyId) {
        for (EncryptedSession session : this.encryptedSessions) {
            if (session.getKeyId() == keyId) {
                return session;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.pgp.core.packethandling.PacketProcessorHandler#handlePacket(com.openexchange.pgp.core.packethandling.PGPPacket)
     */
    @Override
    public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
        Packet rawPacket = packet.getBcPacket();
        if(rawPacket instanceof PublicKeyEncSessionPacket) {
           PublicKeyEncSessionPacket sessionPacket = (PublicKeyEncSessionPacket) rawPacket;
           this.encryptedSessions.add(toEncryptedSession(sessionPacket));
        }
        return new PGPPacket[] {packet};
    }

    /* (non-Javadoc)
     * @see com.openexchange.pgp.core.packethandling.PacketProcessorHandler#modifyPacketData(com.openexchange.pgp.core.packethandling.PGPPacket, byte[])
     */
    @Override
    public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
        return packetData;
    }
}
