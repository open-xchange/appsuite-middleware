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

import org.opensaml.xml.security.credential.Credential;


/**
 * An implementation of this interface is used at runtime to obtain {@link Credential} instances
 * that are used for signing/decryption and signature validation of SAML objects.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface CredentialProvider {

    /**
     * Checks if this provider contains a credential to validate the signatures of digitally signed SAML objects that have been
     * received from the IDP.
     *
     * @return <code>true</code> if {@link #getValidationCredential()} returns a valid credential
     */
    boolean hasValidationCredential();

    /**
     * Gets the credential that can be used to validate the signatures of digitally signed SAML objects that have been
     * received from the IDP.
     *
     * @return The credential or <code>null</code> if none is available
     */
    Credential getValidationCredential();

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

    /*
     * Using certificates that are part of KeyInfo elements in the signed XML itself would require
     * us to verify if we trust them. In case we need to extract and use those certificates at a later
     * point, that is how it works:
     *
     *     List<KeyInfoProvider> keyInfoProviders = new ArrayList<KeyInfoProvider>(4);
     *     keyInfoProviders.add(new InlineX509DataProvider());
     *     keyInfoProviders.add(new KeyInfoReferenceProvider());
     *     keyInfoProviders.add(new DEREncodedKeyValueProvider());
     *     keyInfoProviders.add(new RSAKeyValueProvider());
     *     keyInfoProviders.add(new DSAKeyValueProvider());
     *     BasicProviderKeyInfoCredentialResolver keyInfoCredentialResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviders);
     *
     *     ChainingCredentialResolver credentialResolver = new ChainingCredentialResolver();
     *     credentialResolver.getResolverChain().add(keyInfoCredentialResolver);
     *     if (idpCertificateCredential != null) {
     *         credentialResolver.getResolverChain().add(new StaticCredentialResolver(idpCertificateCredential));
     *     }
     *
     *     Credential credential;
     *     try {
     *         credential = credentialResolver.resolveSingle(new CriteriaSet(new KeyInfoCriteria(signature.getKeyInfo())));
     *     } catch (SecurityException e) {
     *         throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create(e, e.getMessage());
     *     }
     *
     *     if (credential == null) {
     *         throw SAMLExceptionCode.SIGNATURE_VALIDATION_FAILED.create("Could not find a certificate for signature validation.");
     *     }
     */

}
