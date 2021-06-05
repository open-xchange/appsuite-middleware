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

package com.openexchange.client.onboarding.mail.custom;

import com.openexchange.client.onboarding.mail.MailOnboardingProvider;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CustomLoginSource} - Provides the IMAP and SMTP login name for the {@link MailOnboardingProvider}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public interface CustomLoginSource {

    /**
     * Provides the IMAP login for the {@link MailOnboardingProvider}.
     *
     * @param optSession The session (if available); otherwise <code>null</code>
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return the IMAP login
     * @throws OXException If IMAP login cannot be returned
     */
    String getImapLogin(Session optSession, int userId, int contextId) throws OXException;

    /**
     * Provides the SMTP login for the {@link MailOnboardingProvider}.
     *
     * @param optSession The session (if available); otherwise <code>null</code>
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return the SMTP login
     * @throws OXException If SMTP login cannot be returned
     */
    String getSmtpLogin(Session optSession, int userId, int contextId) throws OXException;

}
