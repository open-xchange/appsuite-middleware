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

package com.openexchange.saml.tools;

import java.util.Arrays;
import java.util.List;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.ChainingEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.SimpleKeyInfoReferenceEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import com.openexchange.saml.spi.CredentialProvider;

/**
 * Tools to en-/decrypt SAML XML elements.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class CryptoHelper {

    /**
     * Gets a {@link Decrypter} instance for decrypting encrypted assertions, subject
     * IDs etc. Currently this decrypter is only able to decrypt elements that come along
     * with their symmetric encrytion keys which are in turn encrypted with the public key
     * the passed {@link CredentialProvider}s decryption credential.
     *
     * @param credentialProvider The credential provider
     * @return The decrypter
     */
    public static Decrypter getDecrypter(CredentialProvider credentialProvider) {
        List<EncryptedKeyResolver> keyResolver = Arrays.asList(
            new InlineEncryptedKeyResolver(), 
            new EncryptedElementTypeEncryptedKeyResolver(), 
            new SimpleRetrievalMethodEncryptedKeyResolver(), 
            new SimpleKeyInfoReferenceEncryptedKeyResolver());
        ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver(keyResolver);
        KeyInfoCredentialResolver kekCredentialResolver = new StaticKeyInfoCredentialResolver(credentialProvider.getDecryptionCredential());

        Decrypter decrypter = new Decrypter(null, kekCredentialResolver, encryptedKeyResolver);
        decrypter.setRootInNewDocument(true);
        return decrypter;
    }

}
