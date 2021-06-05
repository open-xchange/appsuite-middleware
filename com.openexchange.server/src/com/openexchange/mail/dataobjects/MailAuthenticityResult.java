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

package com.openexchange.mail.dataobjects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;

/**
 * {@link MailAuthenticityResult} - The result of the overall mail authenticity validation.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class MailAuthenticityResult {

    /** The 'none' result */
    //public static final MailAuthenticityResult NONE_RESULT = new MailAuthenticityResult(MailAuthenticityStatus.NONE, Collections.emptyMap());

    /** The default neutral result */
    public static final MailAuthenticityResult NEUTRAL_RESULT = new MailAuthenticityResult(MailAuthenticityStatus.NEUTRAL, Collections.emptyMap());

    /** The 'not_analyzed' result (used for case where an error occurred during the analysis or for e-mails before the cuf-off-date) */
    public static final MailAuthenticityResult NOT_ANALYZED_RESULT = new MailAuthenticityResult(MailAuthenticityStatus.NOT_ANALYZED, Collections.emptyMap());

    /** Map holding information about the result that does not accept <code>null</code> values */
    private final Map<MailAuthenticityResultKey, Object> attributes;

    /** The overall status of the result */
    private MailAuthenticityStatus status;

    /**
     * Initialises a new {@link MailAuthenticityResult}.
     */
    private MailAuthenticityResult(MailAuthenticityStatus status, Map<MailAuthenticityResultKey, Object> attributes) {
        super();
        this.status = status;
        this.attributes = attributes;
    }

    /**
     * Initialises a new {@link MailAuthenticityResult}.
     */
    public MailAuthenticityResult(MailAuthenticityStatus status) {
        super();
        this.status = status;
        attributes = new HashMap<>();
    }

    /**
     * Adds the specified key with the specified value
     *
     * @param key The {@link MailAuthenticityResultKey}
     * @param value The value to add
     * @throws IllegalArgumentException if the value is <code>null</code>
     */
    public void addAttribute(MailAuthenticityResultKey key, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("The value cannot be 'null'");
        }
        attributes.put(key, value);
    }

    /**
     * Returns the value of the attribute that is stored under the
     * specified {@link MailAuthenticityResultKey}
     *
     * @param key The {@link MailAuthenticityResultKey}
     * @return The value of the attribute
     */
    public Object getAttribute(MailAuthenticityResultKey key) {
        return attributes.get(key);
    }

    /**
     * Returns the value of the attribute that is stored under the
     * specified {@link MailAuthenticityResultKey}, casted to the specified type
     *
     * @param key The {@link MailAuthenticityResultKey}
     * @param type The type to cast the value of the attribute to
     * @return The casted value of the attribute or <code>null</code> if no such attribute exists or
     *         if the value is not of the specified type.
     */
    public <T> T getAttribute(MailAuthenticityResultKey key, Class<T> type) {
        if (!attributes.containsKey(key)) {
            return null;
        }
        Object o = attributes.get(key);
        if (o == null) {
            return null;
        }
        try {
            return type.cast(attributes.get(key));
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Returns an unmodifiable {@link Map} with the attributes of the result
     *
     * @return an unmodifiable {@link Map} with the attributes of the result
     */
    public Map<MailAuthenticityResultKey, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Gets the overall status of the result
     *
     * @return The status
     */
    public MailAuthenticityStatus getStatus() {
        return status;
    }

    /**
     * Sets the overall status of the result
     *
     * @param status The status to set
     */
    public void setStatus(MailAuthenticityStatus status) {
        this.status = status;
    }
}
