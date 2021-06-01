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

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link KnownStatusMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class KnownStatusMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link KnownStatusMessage}.
     */
    private KnownStatusMessage() {
        super();
    }

    // The message advertising that everything is fine with checked account
    public static final String MESSAGE_OK = "All fine";

    // The message advertising that authentication against referenced mail account does not work or stopped working
    public static final String MESSAGE_INVALID_CREDENTIALS = "The entered credential or authentication information does not work or are no longer accepted by provider. Please change them.";

    // The message advertising that affected account is broken and needs to be re-created
    public static final String MESSAGE_RECREATION_NEEDED = "Account is broken and needs to be re-created";

    // The message advertising that affected account has been disabled by administrator
    public static final String MESSAGE_DISABLED = "Account is disabled.";

    // The message advertising that affected account is currently in setup phase and does not yet reflect up-to-date information
    public static final String MESSAGE_IN_SETUP = "Account is currently being set-up.";
    
    // The message advertising that the affected account is facing an SSL problem
    public static final String MESSAGE_SSL_ERROR = "There was an SSL problem.";

    // The message advertising that the status of the account could not be determined.
    public static final String MESSAGE_UNKNOWN = "The account status could not be determined.";

    // The message advertising that the affected account is not supported.
    public static final String MESSAGE_UNSUPPORTED = "The account is not supported.";

    // The message advertising that the affected account cannot be accessed.
    public static final String MESSAGE_INACCESSIBLE = "The account cannot be accessed.";

}
