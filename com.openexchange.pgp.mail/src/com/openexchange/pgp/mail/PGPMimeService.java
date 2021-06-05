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

package com.openexchange.pgp.mail;

import java.util.List;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import com.openexchange.exception.OXException;

/**
 * {@link PGPMimeService} - Service for encrypting and/or singing mails using PGP/MIME.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public interface PGPMimeService {

    /**
     * Encrypts given MimeMessage for a set of recipients
     *
     * @param mimeMessage The MimeMessage to encrypt
     * @param recipientsKeys The recipients
     * @return The PGP/MIME encrypted MimeMessage
     * @throws OXException
     */
    MimeMessage encrypt(MimeMessage mimeMessage, List<PGPPublicKey> recipientsKeys) throws OXException;

    /**
     * Encrypts given MimeMessage for a set of recipients and adds additional clear text BodyParts to the message.
     *
     * @param mimeMessage The MimeMessage to encrypt
     * @param recipientsKeys The recipients
     * @param additionalClearTextParts A list of additional clear text parts to add to the encrypted message, or null
     * @return The PGP/MIME encrypted MimeMessage
     * @throws OXException
     */
    MimeMessage encrypt(MimeMessage mimeMessage, List<PGPPublicKey> recipientsKeys, List<BodyPart> additionalClearTextParts) throws OXException;

    /**
     * Encrypts and signs a given MimeMessage for a set of recipients
     *
     * @param mimeMessage The MimeMessage to encrypt and sign
     * @param signingKey The key used for signing
     * @param password The password for the given signingKey
     * @param recipientsKeys The recipients
     * @return The PGP/MIME encrypted and signed MimeMessage
     * @throws OXException
     */
    MimeMessage encryptSigned(MimeMessage mimeMessage, PGPSecretKey signingKey, char[] password, List<PGPPublicKey> recipientsKeys) throws OXException;

    /**
     * Encrypts and signs a given MimeMessage for a set of recipients and adds additional clear text BodyParts to the message.
     *
     * @param mimeMessage The MimeMessage to encrypt and sign
     * @param signingKey The key used for signing
     * @param password The password for the given signingKey
     * @param recipientsKeys The recipients
     * @param additionalClearTextParts A list of additional clear text parts to add to the encrypted message, or null
     * @return The PGP/MIME encrypted and signed MimeMessage
     * @throws OXException
     */
    MimeMessage encryptSigned(MimeMessage mimeMessage, PGPSecretKey signingKey, char[] password, List<PGPPublicKey> recipientsKeys, List<BodyPart> additionalClearTextParts) throws OXException;

    /**
     * Signs a given MimeMessage
     *
     * @param mimeMessage The MimeMessage to sign
     * @param signingKey The key used for signing
     * @param password The password of the given signing key
     * @return The signed MimeMessage
     * @throws OXException
     */
    MimeMessage sign(MimeMessage mimeMessage, PGPSecretKey signingKey, char[] password) throws OXException;
}
