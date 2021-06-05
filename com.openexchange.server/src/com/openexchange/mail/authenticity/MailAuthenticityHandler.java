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

package com.openexchange.mail.authenticity;

import java.util.Collection;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityHandler} - Checks the authenticity of a given mail message.
 * <p>
 * Results are stored to {@link MailMessage#getAuthenticityResult()}.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MailAuthenticityHandler {

    /**
     * Handles the specified mail message. Extracts the mail headers from the mail message
     * and checks if the 'Authentication-Results' header is present. If it is, then parses that header
     * and collects the results of the different {@link MailAuthenticityMechanism}s that might be present
     * in a {@link MailAuthenticityResult} object.
     *
     * @param session The session providing user data
     * @param mailMessage The mail message to handle
     */
    void handle(Session session, MailMessage mailMessage);

    /**
     * Returns an unmodifiable collection with all additionally required mail fields beside mandatory {@link MailField#AUTHENTICATION_OVERALL_RESULT} or {@link MailField#AUTHENTICATION_MECHANISM_RESULTS}
     *
     * @return an unmodifiable collection with all additionally required mail fields
     */
    Collection<MailField> getRequiredFields();

    /**
     * Returns an unmodifiable collection with all additionally required mail headers
     *
     * @return an unmodifiable collection with all additionally required mail headers
     */
    Collection<String> getRequiredHeaders();

    /**
     * Determines whether this handler is enabled for the user that is associated with specified session.
     *
     * @param session The session providing user data
     * @return <code>true</code> if this handler is enabled; <code>false</code> otherwise
     */
    boolean isEnabled(Session session);

    /**
     * Gets the ranking of this handler. A higher number in ranking means a higher priority.
     *
     * @return The ranking of the {@link MailAuthenticityHandler}
     */
    int getRanking();
}
