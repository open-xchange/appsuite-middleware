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

package com.openexchange.chronos.provider.internal;

import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum Constants {
    ;

    /** The static identifier of the internal calendar provider */
    public static final String PROVIDER_ID = CalendarProviders.ID_CHRONOS;

    /** The static identifier of the single default account in the internal calendar provider */
    public static final int ACCOUNT_ID = 0;

    /** The static qualified identifier of the single default account in the internal calendar provider */
    public static final String QUALIFIED_ACCOUNT_ID = "cal://0";

    /** The identifier of the folder tree the calendar provider is using */
    public static final String TREE_ID = com.openexchange.folderstorage.FolderStorage.REAL_TREE_ID;

    /** The used folder storage content type */
    public static final ContentType CONTENT_TYPE = com.openexchange.folderstorage.database.contentType.CalendarContentType.getInstance();

    /** The identifier of the "private" root folder */
    static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /** The identifier of the "public" root folder */
    static final String PUBLIC_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);

    /** The identifier of the "shared" root folder */
    static final String SHARED_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID);

    /** The prefix used for stored user properties of folders */
    static final String USER_PROPERTY_PREFIX = "cal/";

}
