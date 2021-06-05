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

package com.openexchange.share.subscription;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ShareSubscriptionExceptionMessages}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class ShareSubscriptionExceptionMessages implements LocalizableStrings {

    /** Unable to find a subscription for \"%1$s\" */
    public static final String MISSING_SUBSCRIPTION_MSG = "Unable to find a subscription for \"%1$s\"";

    /** The given link \"%1$s\" can't be resolved to a share. */
    public static final String NOT_USABLE_MSG = "The given link \"%1$s\" can't be resolved to a share.";

    /** You don't have enough permissions to perform the operation. */
    public static final String MISSING_PERMISSIONS_MSG = "You don't have enough permissions to perform the operation.";

    /** The folder %1$s belongs to a folder tree that is unsubscribed. */
    public static final String UNSUBSCRIEBED_FOLDER_MSG = "The folder %1$s belongs to a folder tree that is unsubscribed.";

    /** After unsubscribing from \"%1$s\", all folders from the account \"%2$s\" will be removed. */
    public static final String ACCOUNT_WILL_BE_REMOVED_MSG = "After unsubscribing from \"%1$s\", all folders from the account \"%2$s\" will be removed.";

    /**
     * Initializes a new {@link OXExceptionMessages}.
     */
    private ShareSubscriptionExceptionMessages() {
        super();
    }

}
