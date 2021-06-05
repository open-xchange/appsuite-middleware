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

import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link MailAuthenticityResultKey} - Defines the {@link MailAuthenticityResultKey}s
 * used in the core/default implementation of the {@link MailAuthenticityHandler} to record
 * the results of the evaluation of an e-mail and compile the {@link MailAuthenticityResult}.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum MailAuthenticityResultKey {

    /**
     * Defines the overall status of the result object
     */
    STATUS("status"),
    /**
     * Defines the domain of the 'From' header from the e-mail
     * which the authenticity was evaluated.
     */
    FROM_HEADER_DOMAIN("from_header_domain", false),
    /**
     * Defines the domain of the '*.from' attribute of a mechanism
     * result that is part of the response.
     */
    FROM_DOMAIN("from_domain"),
    /**
     * Defines if there is a domain mismatch between the domain of the
     * mechanism and the domain from the 'From' header
     */
    DOMAIN_MISMATCH("domain_mismatch"),
    /**
     * Defines the mechanism from which the authenticated from domain ({@link #FROM_DOMAIN})
     * was determined.
     */
    DOMAIN_MECH("domain_mech"),
    /**
     * Defines the {@link MailAuthenticityMechanismResult}s that were
     * collected during the evaluation process of the e-mail.
     */
    MAIL_AUTH_MECH_RESULTS("mailAuthenticityMechanismResults"),
    /**
     * Defines the unknown/unconsidered mail authenticity mechanism results
     * that were collected during the evaluation process of the e-mail.
     */
    UNCONSIDERED_AUTH_MECH_RESULTS("unconsideredAuthenticityMechanismResults"),
    /**
     * Defines the complete address of the trusted sender of the e-mail
     */
    TRUSTED_SENDER("trustedSender", false),
    /**
     * Contains a map of custom properties
     */
    CUSTOM_PROPERTIES("custom", false),
    /**
     * Defines the 'image' key for the response object
     */
    IMAGE("image");

    private final String key;
    private final boolean isVisible;

    /**
     * Initialises a new {@link DefaultMailAuthenticityResultKey}.
     */
    private MailAuthenticityResultKey(String key) {
        this(key, true);
    }

    /**
     * Initialises a new {@link DefaultMailAuthenticityResultKey}.
     */
    private MailAuthenticityResultKey(String key, boolean isVisible) {
        this.key = key;
        this.isVisible = isVisible;
    }

    /**
     * Returns the key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * A flag indicating whether this attribute is visible or not
     *
     * @return
     */
    public boolean isVisible() {
        return isVisible;
    }
}
