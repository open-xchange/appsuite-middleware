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

import com.openexchange.config.lean.Property;

/**
 * {@link MailAuthenticityProperty} - Properties for mail authenticity validation.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum MailAuthenticityProperty implements Property {
    /**
     * Defines whether the mail authenticity core feature is enabled
     * <p>
     * Defaults to <code>false</code>
     */
    ENABLED("enabled", Boolean.FALSE),
    /**
     * Defines the date after which the e-mails will be analyzed
     * <p>
     * Defaults to 0
     */
    THRESHOLD("threshold", Long.valueOf(0)),
    /**
     * Defines the MANDATORY <code>authserv-id</code>. It can contain a single arbitrary string
     * or a comma separated list of arbitrary strings
     * <p>
     * Default is empty.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC-7601, Section 2.2</a>
     */
    AUTHSERV_ID("authServId", ""),
    /**
     * <p>Defines whether the raw headers of a message will be logged in DEBUG level.</p>
     * 
     * <p>Defaults to <code>false</code></p>
     */
    LOG_RAW_HEADERS("logRawHeaders", Boolean.FALSE);

    private final Object defaultValue;
    private final String fqn;

    /**
     * Initializes a new {@link MailAuthenticityProperty}.
     */
    private MailAuthenticityProperty(String suffix, Object defaultValue) {
        this.defaultValue = defaultValue;
        fqn = "com.openexchange.mail.authenticity." + suffix;
    }

    /**
     * Gets the fully qualified name for the property
     *
     * @return the fully qualified name for the property
     */
    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    /**
     * Gets the default value of this property
     *
     * @return the default value of this property
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
