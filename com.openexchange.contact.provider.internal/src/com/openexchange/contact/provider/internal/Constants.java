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

package com.openexchange.contact.provider.internal;

import com.openexchange.contact.provider.ContactsProviders;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
class Constants {

    /** The static identifier of the internal contacts provider */
    static final String PROVIDER_ID = ContactsProviders.ID_CONTACTS;

    /** The static identifier of the single default account in the internal contacts provider */
    static final int ACCOUNT_ID = 0;

    /** The identifier of the folder tree the contacts provider is using */
    static final String TREE_ID = com.openexchange.folderstorage.FolderStorage.REAL_TREE_ID;

    /** The static qualified identifier of the single default account in the internal contacts provider */
    static final String QUALIFIED_ACCOUNT_ID = "con://0";

    /** The used folder storage content type */
    public static final ContentType CONTENT_TYPE = com.openexchange.folderstorage.database.contentType.ContactsContentType.getInstance();

    /** The identifier of the "private" root folder */
    static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /** The identifier of the "public" root folder */
    static final String PUBLIC_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);

    /** The identifier of the "shared" root folder */
    static final String SHARED_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID);

    /** The prefix used for stored user properties of folders */
    static final String USER_PROPERTY_PREFIX = "con/";
}
