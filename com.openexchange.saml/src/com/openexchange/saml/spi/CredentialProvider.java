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
 * An implementation of this interface is used at runtime to obtain {@link Credential} instances
 * that are used for signing/decryption and signature validation of SAML objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see KeyStoreCredentialProvider
 * @see KeySpecCredentialProvider
 */
public interface CredentialProvider {

    /**
     * Checks if this provider contains a credential to validate the signatures of digitally signed SAML objects that have been
     * received from the IDP.
     *
     * @return <code>true</code> if {@link #getValidationCredential()} returns a valid credential
     * @deprecated Use {@link CredentialProvider#hasValidationCredentials} instead
     */
    @Deprecated
    boolean hasValidationCredential();

    /**
     * Gets the credential that can be used to validate the signatures of digitally signed SAML objects that have been
     * received from the IDP.
     *
     * @return The credential or <code>null</code> if none is available
     * @deprecated Use {@link CredentialProvider#getValidationCredentials} instead
     */
    @Deprecated
    Credential getValidationCredential();
    
    /**
     * Checks if this provider contains a credential list to validate the signatures of digitally signed SAML objects that have been
     * received from the IDP.
     *
     * @return <code>true</code> if {@link #getValidationCredentials()} returns a valid credential
     */
    boolean hasValidationCredentials();

    /**
     * Gets the credential list that can be used to validate the signatures of digitally signed SAML objects that have been
     * received from the IDP.
     *
     * @return The List of credentials or an empty list if none are available
     */
    List<Credential> getValidationCredentials();
    
    /**
     * Checks if this provider contains a credential to sign SAML objects that are to be sent to the IDP.
     *
     * @return <code>true</code> if {@link #getSigningCredential()} returns a valid credential
     */
    boolean hasSigningCredential();

    /**
     * Gets the credential used for signing SAML objects that are to be sent to the IDP.
     *
     * @return The credential or <code>null</code> if none is available
     */
    Credential getSigningCredential();

    /**
     * Checks if this provider contains a credential to decrypt encrypted SAML objects or according key encryption keys that have
     * been received from the IDP.
     *
     * @return <code>true</code> if {@link #getDecryptionCredential()} returns a valid credential
     */
    boolean hasDecryptionCredential();

    /**
     * Gets the credential used to decrypt encrypted SAML objects or according key encryption keys that have
     * been received from the IDP.
     *
     * @return The credential or <code>null</code> if none is available
     */
    Credential getDecryptionCredential();

}
