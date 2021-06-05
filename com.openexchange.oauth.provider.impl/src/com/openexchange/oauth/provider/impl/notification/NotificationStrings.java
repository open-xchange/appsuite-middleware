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

package com.openexchange.oauth.provider.impl.notification;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link NotificationStrings}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class NotificationStrings implements LocalizableStrings {

    // Successfully connected ExampleApp with your account
    public static final String SUBJECT = "Successfully connected %1$s with your account";

    // Salutation with name: "Hello Peter,"
    public static final String SALUTATION = "Hello %1$s,";

    // your account peter@example.com has been successfully connected to ExampleApp.
    public static final String APP_CONNECTED = "your account %1$s has been successfully connected to %2$s.";

    // You may revoke access for this application at any time, by visiting your settings page.
    public static final String REVOKE_ACCESS = "You may revoke access for this application at any time, by visiting your settings page.";

    // Go to settings
    public static final String GO_TO_SETTINGS = "Go to settings";

}
