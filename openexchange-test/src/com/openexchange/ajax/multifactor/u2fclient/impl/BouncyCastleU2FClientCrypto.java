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
    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.ajax.multifactor.u2fclient.U2FClientCrypto#sha256(byte)
     */
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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.ajax.multifactor.u2fclient.U2FClientCrypto#sign(byte[], java.security.PrivateKey)
     */
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
