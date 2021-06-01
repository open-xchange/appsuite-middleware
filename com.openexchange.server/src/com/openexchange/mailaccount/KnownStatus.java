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

package com.openexchange.mailaccount;

import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link KnownStatus} - An enumeration for known statuses for a mail account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum KnownStatus implements Status {

    /**
     * The "OK" status. All fine.
     */
    OK("ok", KnownStatusMessage.MESSAGE_OK),
    /**
     * Referenced account currently carries invalid credentials and is therefore unable to connect. Credentials are supposed to be corrected by user.
     */
    INVALID_CREDENTIALS("invalid_credentials", KnownStatusMessage.MESSAGE_INVALID_CREDENTIALS),
    /**
     * Account is broken and needs to be re-created
     */
    RECREATION_NEEDED("recreation_needed", KnownStatusMessage.MESSAGE_RECREATION_NEEDED),
    /**
     * Account is disabled.
     */
    DISABLED("disabled", KnownStatusMessage.MESSAGE_DISABLED),
    /**
     * Account is currently being set-up.
     */
    IN_SETUP("in_setup", KnownStatusMessage.MESSAGE_IN_SETUP),
    /**
     * There was an SSL problem.
     */
    INVALID_SSL_CERTIFICATE("invalid_ssl", KnownStatusMessage.MESSAGE_SSL_ERROR),

    ;

    /**
     * Generates an "unknown" status saying "The account status could not be determined.", with an optional root cause.
     *
     * @param e The optional error providing further details
     * @return The status
     */
    public static Status UNKNOWN(OXException e) {
        return new DefaultStatus("unknown", KnownStatusMessage.MESSAGE_UNKNOWN, e);
    }

    /**
     * Generates an "unsupported" status saying "The account is not supported.", with an optional root cause.
     *
     * @param e The optional error providing further details
     * @return The status
     */
    public static Status UNSUPPORTED(OXException e) {
        return new DefaultStatus("unsupported", KnownStatusMessage.MESSAGE_UNSUPPORTED, e);
    }

    /**
     * Generates an "inaccessible" status saying "The account cannot be accessed.", with an optional root cause.
     *
     * @param e The optional error providing further details
     * @return The status
     */
    public static Status INACCESSIBLE(OXException e) {
        return new DefaultStatus("inaccessible", KnownStatusMessage.MESSAGE_INACCESSIBLE, e);
    }

    private final String id;
    private final String message;

    private KnownStatus(String id, String message) {
        this.id = id;
        this.message = message;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getMessage(Locale locale) {
        return StringHelper.valueOf(null == locale ? Locale.US : locale).getString(message);
    }

    @Override
    public OXException getError() {
        return null;
    }

    /**
     * Gets the status for given identifier
     *
     * @param identifier The status' identifier
     * @return The status or <code>null</code>
     */
    public static Status statusFor(String identifier) {
        if (null == identifier) {
            return null;
        }
        for (KnownStatus s : KnownStatus.values()) {
            if (identifier.equalsIgnoreCase(s.id)) {
                return s;
            }
        }
        return null;
    }

}
