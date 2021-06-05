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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;

/**
 * {@link AuthenticityMechanismResult}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface AuthenticityMechanismResult {

    static final Logger LOGGER = LoggerFactory.getLogger(AuthenticityMechanismResult.class);

    /**
     * Returns the display name of the mechanism's result
     * 
     * @return the display name of the mechanism's result
     */
    String getDisplayName();

    /**
     * Returns the technical name of the mechanism's result
     * 
     * @return the technical name of the mechanism's result
     */
    String getTechnicalName();

    /**
     * Returns the ordinal
     * 
     * @return The ordinal of the enum
     */
    int getCode();

    /**
     * Converts the specified {@link AuthenticityMechanismResult} to {@link MailAuthenticityStatus}
     *
     * @param mechanismResult The {@link AuthenticityMechanismResult} to convert
     * @return The converted {@link MailAuthenticityStatus}. The status {@link MailAuthenticityStatus#NEUTRAL} might
     *         also get returned if the specified {@link AuthenticityMechanismResult} does not map to a valid {@link MailAuthenticityStatus}.
     */
    default MailAuthenticityStatus convert() {
        try {
            return MailAuthenticityStatus.valueOf(getTechnicalName().toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unknown mail authenticity status '{}'", getTechnicalName(), e);
            return MailAuthenticityStatus.NEUTRAL;
        }
    }
}
