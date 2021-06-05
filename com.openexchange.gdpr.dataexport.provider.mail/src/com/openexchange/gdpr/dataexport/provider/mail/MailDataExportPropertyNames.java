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

package com.openexchange.gdpr.dataexport.provider.mail;


/**
 * {@link MailDataExportPropertyNames}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MailDataExportPropertyNames {

    /**
     * Initializes a new {@link MailDataExportPropertyNames}.
     */
    private MailDataExportPropertyNames() {
        super();
    }

    public static final String PROP_ENABLED = "enabled";

    public static final String PROP_INCLUDE_PUBLIC_FOLDERS = "includePublic";

    public static final String PROP_INCLUDE_SHARED_FOLDERS = "includeShared";

    public static final String PROP_INCLUDE_TRASH_FOLDER = "includeTrash";

    public static final String PROP_INCLUDE_UNSUBSCRIBED = "includeUnsubscribed";

    public static final String PROP_SESSION_GENERATOR = "sessionGenerator";

    public static final String PROP_PASSWORD = "password";

    public static final String PROP_LOGIN_NAME = "loginName";

    public static final String PROP_REFRESH_TOKEN = "refreshToken";

    public static final String PROP_ACCESS_TOKEN = "accessToken";

}
