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

package com.openexchange.mail.authenticity.mechanism.dmarc;

import com.openexchange.mail.authenticity.mechanism.AbstractAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;

/**
 * {@link DMARCAuthMechResult}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DMARCAuthMechResult extends AbstractAuthMechResult {

    /**
     * Initialises a new {@link DMARCAuthMechResult}.
     * 
     * @param domain The domain for which this mail authentication mechanism was applied to
     * @param result The {@link DMARCResult}
     */
    public DMARCAuthMechResult(String domain, DMARCResult result) {
        super(domain, null, result);
    }

    /**
     * Initialises a new {@link DMARCAuthMechResult}.
     * 
     * @param domain The domain for which this mail authentication mechanism was applied to
     * @param clientIP The optional client IP used to send the e-mail
     * @param result The {@link DMARCResult}
     */
    public DMARCAuthMechResult(String domain, String clientIP, DMARCResult result) {
        super(domain, clientIP, result);
    }

    @Override
    public MailAuthenticityMechanism getMechanism() {
        return DefaultMailAuthenticityMechanism.DMARC;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DMARCAuthMechResult [mechanism=").append(getMechanism()).append(", domain=").append(getDomain()).append(", clientIP=").append(getClientIP()).append(", result=").append(getResult()).append("]");
        return builder.toString();
    }
}
