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

package com.openexchange.ajax.multifactor.u2fclient;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * {@link U2FDeviceAccess} provides access to the underlaying, connected, physical U2F device
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public interface U2FDeviceAccess {

    /**
     * {@link AttestationCertificate} represents an attestation certificate used for U2F
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.1
     */
    public class AttestationCertificate{
        private final PrivateKey privateKey;
        private final X509Certificate certificate;

        public AttestationCertificate(PrivateKey privateKey, X509Certificate certificate) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }
        public PrivateKey getPrivateKey() {
            return privateKey;
        }
    }

    /**
     * Represent a key pair containing public and private key
     *
     * {@link U2FKeyPair}
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.1
     */
    public class U2FKeyPair {

        private final byte[]  keyHandle;
        private final KeyPair keyPair;
        private int counter = 0;

        /**
         * Initializes a new {@link U2FKeyPair}.
         *
         * @param keyHandle The unique handle of the key
         * @param keyPair The KeyPair
         */
        public U2FKeyPair(byte[] keyHandle, KeyPair keyPair) {
            this.keyHandle = keyHandle;
            this.keyPair = keyPair;
        }

        /**
         * Gets the unique handle of the KeyPair
         *
         * @return The unique handle of the key pair
         */
        public byte[] getKeyHandle() {
            return keyHandle;
        }

        /**
         * Gets the actual key pair
         *
         * @return The key pair
         */
        public KeyPair getKeyPair() {
            return keyPair;
        }

        /**
         * The counter
         *
         * @return The counter
         */
        public int getCounter() {
            return counter;
        }

        /**
         * Increments the counter
         */
        public void incrementCounter() {
            this.counter++;
        }
    }

    /**
     * Gets the unique certificate of the U2F devicecertificate of the U2F device.
     *
     * @return The certificate
     * @throws U2FClientException
     */
    AttestationCertificate getCertificate() throws U2FClientException;

    /**
     * "Generates" a "new" key pair.
     * <br>
     * <br>
     * Due to physical storage limitations and practical reasons, U2F devices usally do not store keys.
     * Instead keys are derived from the device's secret master key and given server paramters.
     * <br>
     * See https://developers.yubico.com/U2F/Protocol_details/Key_generation.html for further information.
     *
     * @param appId The App ID
     * @param challenge The challenge
     * @return The U2F
     * @throws U2FClientException
     */
    public U2FKeyPair getKeyPair(String appId, String challenge) throws U2FClientException;

    /**
     * Gets a key by it's handle.
     * <br>
     * <br>
     * Due to physical storage limitations and practical reasons, U2F devices usally do not store keys.
     * Instead keys are derived from the device's secret master key and given server paramters.
     * <br>
     * See https://developers.yubico.com/U2F/Protocol_details/Key_generation.html for further information.
     *
     * @param keyHandle The handle to get the corresponding key for
     * @return The key pair with the given handle or null if the handle was invalid
     */
    public U2FKeyPair getKeyPair(byte[] keyHandle) throws U2FClientException;

    /**
     * Encodes the public key of the given key pair.
     * "This is the (uncompressed) x,y-representation of a curve point on the P-256 NIST elliptic curve" (https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#registration-response-message-success)
     *
     * @param key The key pair containing the public key.
     * @return The raw, encoded representation of the public key
     * @throws U2FClientException
     */
    public byte[] encodePublicKey(U2FKeyPair key) throws U2FClientException;
}
