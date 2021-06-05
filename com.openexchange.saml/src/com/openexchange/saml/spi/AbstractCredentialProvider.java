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

package com.openexchange.saml.spi;

import java.util.List;
import org.opensaml.security.credential.Credential;


/**
 * Abstract class for {@link CredentialProvider} implementations.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public abstract class AbstractCredentialProvider implements CredentialProvider {

    private final List<Credential> idpCertificateCredential;

    private final Credential signingCredential;

    private final Credential decryptionCredential;

    /**
     * Initializes a new {@link KeyStoreCredentialProvider}.
     *
     * @param idpPublicKeyCredentials The credential list containing the IDPs public keys used for validating signatures or <code>null</code>
     * @param signingPrivateKeyCredential The credential containing the SPs private key used for signing requests or <code>null</code>
     * @param decryptionPrivateKeyCredential The credential containing the private key used to decrypt encrypted data or encryption keys or <code>null</code>
     */
    protected AbstractCredentialProvider(List<Credential> idpPublicKeyCredentials, Credential signingPrivateKeyCredential, Credential decryptionPrivateKeyCredential) {
        super();
        this.idpCertificateCredential = idpPublicKeyCredentials;
        this.signingCredential = signingPrivateKeyCredential;
        this.decryptionCredential = decryptionPrivateKeyCredential;
    }
    
    @Override
    public boolean hasValidationCredential() {
        return hasValidationCredentials();
    }
    
    @Override
    public Credential getValidationCredential() {
        List<Credential> validationCredentials = getValidationCredentials();
        return validationCredentials != null && validationCredentials.isEmpty() == false ? validationCredentials.get(0) : null;
    }

    @Override
    public boolean hasValidationCredentials() {
        return idpCertificateCredential != null;
    }

    @Override
    public List<Credential> getValidationCredentials() {
        return idpCertificateCredential;
    }

    @Override
    public boolean hasSigningCredential() {
        return signingCredential != null;
    }

    @Override
    public Credential getSigningCredential() {
        return signingCredential;
    }

    @Override
    public boolean hasDecryptionCredential() {
        return decryptionCredential != null;
    }

    @Override
    public Credential getDecryptionCredential() {
        return decryptionCredential;
    }
}
