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
