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

package com.openexchange.chronos.provider.google;

/**
 * {@link GoogleCalendarConfigField}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarConfigField {

    public static final String OAUTH_ID = "oauthId";
    public static final String FOLDER = "folder";
    public static final String PRIMARY = "isPrimary";
    public static final String SYNC_TOKEN = "syncToken";
    /**
     * In case the account was migrated. This field contains the folder of the former subscription
     */
    public static final String OLD_FOLDER = "oldSubscriptionFolder";

    public static final String COLOR = "color";
    public static final String DEFAULT_REMINDER = "default_reminders";
    public static final String DESCRIPTION = "description";
    public static final String NAME = "name";

}
