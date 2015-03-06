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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.saml.spi;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;


/**
 * A {@link CredentialProvider} that uses {@link Key} instances directly to provide
 * the credentials. The keys are generated on provided {@link KeySpec} instances.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class KeySpecCredentialProvider extends AbstractCredentialProvider {

    public static enum Algorithm {
        RSA,
        DSA
    }

    protected KeySpecCredentialProvider(Credential idpPublicKeyCredential, Credential signingPrivateKeyCredential, Credential decryptionPrivateKeyCredential) {
        super(idpPublicKeyCredential, signingPrivateKeyCredential, decryptionPrivateKeyCredential);
    }

    /**
     * Initializes a new instance of {@link KeySpecCredentialProvider}.
     *
     * @param idpPublicKeySpec The spec of the IDPs public key used for validating signatures or <code>null</code>
     * @param idpPublicKeyAlgorithm The specs algorithm or <code>null</code>
     * @param signingKeySpec The spec of the private key used for signing SP requests or <code>null</code>
     * @param signingKeyAlgorithm The specs algorithm or <code>null</code>
     * @param decryptionKeySpec The spec of the private key used to decrypt encrypted data or encryption keys or <code>null</code>
     * @param decryptionKeyAlgorithm The specs algorithm or <code>null</code>
     */
    public static KeySpecCredentialProvider newInstance(KeySpec idpPublicKeySpec, Algorithm idpPublicKeyAlgorithm, KeySpec signingKeySpec, Algorithm signingKeyAlgorithm, KeySpec decryptionKeySpec, Algorithm decryptionKeyAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BasicCredential idpPublicKeyCredential = null;
        BasicCredential signingPrivateKeyCredential = null;
        BasicCredential decryptionPrivateKeyCredential = null;
        if (idpPublicKeySpec != null) {
            KeyFactory keyFactory = KeyFactory.getInstance(idpPublicKeyAlgorithm.name());
            PublicKey idpPublicKey = keyFactory.generatePublic(idpPublicKeySpec);
            idpPublicKeyCredential = new BasicCredential();
            idpPublicKeyCredential.setUsageType(UsageType.SIGNING);
            idpPublicKeyCredential.setPublicKey(idpPublicKey);
        }

        if (signingKeySpec != null) {
            KeyFactory keyFactory = KeyFactory.getInstance(signingKeyAlgorithm.name());
            PrivateKey signingKey = keyFactory.generatePrivate(signingKeySpec);
            signingPrivateKeyCredential = new BasicCredential();
            signingPrivateKeyCredential.setUsageType(UsageType.SIGNING);
            signingPrivateKeyCredential.setPrivateKey(signingKey);
        }

        if (decryptionKeySpec != null) {
            KeyFactory keyFactory = KeyFactory.getInstance(decryptionKeyAlgorithm.name());
            PrivateKey decryptionKey = keyFactory.generatePrivate(decryptionKeySpec);
            decryptionPrivateKeyCredential = new BasicCredential();
            decryptionPrivateKeyCredential.setUsageType(UsageType.ENCRYPTION);
            decryptionPrivateKeyCredential.setPrivateKey(decryptionKey);
        }

        return new KeySpecCredentialProvider(idpPublicKeyCredential, signingPrivateKeyCredential, decryptionPrivateKeyCredential);
    }
}
