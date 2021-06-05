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

package com.openexchange.mail.service;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.session.Session;

/**
 * {@link EncryptedMailService}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v2.8.0
 */
public interface EncryptedMailService {

    /**
     * Encrypts a draft message before saving to the draft folder
     *
     * @param draft
     * @return a ComposedMailMessage containing the encrypted message
     * @throws OXException
     */
    public ComposedMailMessage encryptDraftEmail(ComposedMailMessage draft, Session session, String cryptoAuthentication) throws OXException;

    /**
     * Encrypts a {@link MailMessage} for autosave a draft
     *
     * @param message The message to encrypt
     * @param session The session The session of the user
     * @param securitySettings The settings used for encryption/signing
     * @return The encrypted {@link MailMessage}
     * @throws OXException
     */
    public MailMessage encryptAutosaveDraftEmail(MailMessage message, Session session, SecuritySettings securitySettings) throws OXException;
}
