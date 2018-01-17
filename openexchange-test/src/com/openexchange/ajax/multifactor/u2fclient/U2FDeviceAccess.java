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
