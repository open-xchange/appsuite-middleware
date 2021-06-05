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

package com.openexchange.ajax.multifactor.u2fclient.impl;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.openexchange.ajax.multifactor.u2fclient.U2FClientCrypto;
import com.openexchange.ajax.multifactor.u2fclient.U2FClientException;

/**
 * {@link BouncyCastleU2FClientCrypto} - The default implementation for {@link BouncyCastleU2FClientCrypto} using Bouncy Castle
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class BouncyCastleU2FClientCrypto implements U2FClientCrypto {

    public BouncyCastleU2FClientCrypto(){
        //Adding BouncyCastleProvider if not already added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    @Override
    public byte[] sha256(byte[] data) throws U2FClientException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new U2FClientException(e.getMessage(), e);
        }
    }

    @Override
    public byte[] sign(byte[] data, PrivateKey key) throws U2FClientException {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(key);
            signature.update(data);
            return signature.sign();

        } catch (NoSuchAlgorithmException   |
                 NoSuchProviderException    |
                 InvalidKeyException        |
                 SignatureException e) {
            throw new U2FClientException(e.getMessage(), e);
        }
    }

}
