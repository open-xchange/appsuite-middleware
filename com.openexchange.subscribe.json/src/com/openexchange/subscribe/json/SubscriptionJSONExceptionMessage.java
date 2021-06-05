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

package com.openexchange.subscribe.json;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SubscriptionJSONExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SubscriptionJSONExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link SubscriptionJSONExceptionMessage}.
     */
    private SubscriptionJSONExceptionMessage() {
        super();
    }

    // The message displayed if the requested subscription cannot be found from the server.
    public static final String UNKNOWN_SUBSCRIPTION_DISPLAY = "The subscription you requested is unkonwn!";
    
    // The operation is forbidden
    public static final String FORBIDDEN_CREATE_MODIFY_MESSAGE = "The operation is forbidden.";
}
