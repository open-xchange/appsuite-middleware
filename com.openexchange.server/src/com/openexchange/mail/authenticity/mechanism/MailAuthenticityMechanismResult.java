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

package com.openexchange.mail.authenticity.mechanism;

import java.util.Map;

/**
 * {@link MailAuthenticityMechanismResult} - Defines the methods of the mail authentication
 * mechanism result dataobject
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MailAuthenticityMechanismResult {

    /**
     * Returns the domain for which this mechanism was applied
     *
     * @return the domain for which this mechanism was applied
     */
    String getDomain();

    /**
     * Returns the (optional) client IP which was used to send the e-mail
     *
     * @return the (optional) client IP which was used to send the e-mail;
     *         <code>null</code> if none available
     */
    String getClientIP();

    /**
     * Returns the {@link DefaultMailAuthenticityMechanism} used for this result
     *
     * @return the {@link DefaultMailAuthenticityMechanism} used for this result
     */
    MailAuthenticityMechanism getMechanism();

    /**
     * Returns the result of the authentication mechanism
     *
     * @return the result of the authentication mechanism
     */
    AuthenticityMechanismResult getResult();

    /**
     * Returns the reason of the result
     *
     * @return the reason of the result
     */
    String getReason();

    /**
     * Returns a {@link Map} with the properties for the mechanism result
     * 
     * @return a {@link Map} with the properties for the mechanism result
     */
    Map<String, String> getProperties();

    /**
     * Gets the domainMatch
     *
     * @return The domainMatch
     */
    boolean isDomainMatch();
}
