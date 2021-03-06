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

package com.openexchange.kerberos;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link KerberosExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum KerberosExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Verification of client ticket failed: %1$s
     */
    TICKET_WRONG("Verification of client ticket failed: %1$s", Category.CATEGORY_PERMISSION_DENIED, 1,
        KerberosExceptionMessages.TICKET_WRONG_MSG),

    /**
     * Unknown problem: "%1$s".
     */
    UNKNOWN("Unknown problem: \"%1$s\".", Category.CATEGORY_ERROR, 2, null),

    /**
     * Failed to get a delegate ticket for %1$s.
     */
    DELEGATE_FAILED("Failed to get a delegate ticket for %1$s.", Category.CATEGORY_PERMISSION_DENIED, 3,
        KerberosExceptionMessages.DELEGATE_FAILED_MSG),

    /**
     * Communication to Kerberos server failed: %1$s
     */
    COMM_FAILED("Communication to Kerberos server failed: %1$s", Category.CATEGORY_CONFIGURATION, 4,
        KerberosExceptionMessages.COMM_FAILED_MSG),

    /**
     * Authentication this service failed: %1$s
     */
    LOGIN_FAILED("Authenticating this service against the Kerberos server failed: %1$s", Category.CATEGORY_CONFIGURATION, 5,
        KerberosExceptionMessages.LOGIN_FAILED_MSG),

    /**
     * Problem while terminating service ticket: %1$s
     */
    LOGOUT_FAILED("Problem while terminating service ticket: %1$s", Category.CATEGORY_SERVICE_DOWN, 6,
        KerberosExceptionMessages.LOGOUT_FAILED_MSG),

    /**
     * Can not find credentials in subject %1$s that need a renewal.
     */
    NO_CREDENTIALS("Can not find credentials in subject %1$s that need a renewal.", Category.CATEGORY_ERROR, 7,
        KerberosExceptionMessages.NO_CREDENTIALS_MSG),

    /**
     * No Kerberos delegation ticket found in session %1$s.
     */
    TICKET_MISSING("No Kerberos delegation ticket found in session %1$s.", Category.CATEGORY_ERROR, 8,
        KerberosExceptionMessages.TICKET_MISSING_MSG);

    final String message;

    final Category category;

    final int number;

    final String displayMessage;

    private KerberosExceptionCodes(String message, Category category, int detailNumber, String displayMessage) {
        this.message = message;
        this.category = category;
        number = detailNumber;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public String getPrefix() {
        return "KER";
    }

    public OXException create(Object... messageArgs) {
        return OXExceptionFactory.getInstance().create(this, messageArgs);
    }

    public OXException create(Throwable cause, Object... messageArgs) {
        return OXExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
