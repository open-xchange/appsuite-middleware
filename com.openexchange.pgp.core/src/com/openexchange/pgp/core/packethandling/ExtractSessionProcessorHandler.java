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
    @SuppressWarnings("synthetic-access")
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

    @Override
    public PGPPacket[] handlePacket(PGPPacket packet) throws Exception {
        Packet rawPacket = packet.getBcPacket();
        if (rawPacket instanceof PublicKeyEncSessionPacket) {
           PublicKeyEncSessionPacket sessionPacket = (PublicKeyEncSessionPacket) rawPacket;
           this.encryptedSessions.add(toEncryptedSession(sessionPacket));
        }
        return new PGPPacket[] {packet};
    }

    @Override
    public byte[] handlePacketData(PGPPacket packet, byte[] packetData) {
        return packetData;
    }
}
