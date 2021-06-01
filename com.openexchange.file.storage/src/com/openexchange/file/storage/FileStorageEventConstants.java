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

package com.openexchange.file.storage;


/**
 * {@link FileStorageEventConstants}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileStorageEventConstants {

    public static final String UPDATE_TOPIC = "com/openexchange/groupware/infostore/update";

    public static final String CREATE_TOPIC = "com/openexchange/groupware/infostore/insert";

    public static final String DELETE_TOPIC = "com/openexchange/groupware/infostore/delete";

    public static final String ACCESS_TOPIC = "com/openexchange/groupware/infostore/access";

    public static final String ALL_TOPICS = "com/openexchange/groupware/infostore/*";

    public static final String UPDATE_FOLDER_TOPIC = "com/openexchange/groupware/fsfolder/update";

    public static final String CREATE_FOLDER_TOPIC = "com/openexchange/groupware/fsfolder/insert";

    public static final String DELETE_FOLDER_TOPIC = "com/openexchange/groupware/fsfolder/delete";

    public static final String ALL_FOLDER_TOPICS = "com/openexchange/groupware/fsfolder/*";

    public static final String SESSION = "session";

    public static final String SERVICE = "service";

    public static final String ACCOUNT_ID = "accountId";

    /**
     * The folder ID in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String FOLDER_ID = "folderId";

    /**
     * The parent folder ID in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String PARENT_FOLDER_ID = "parentFolderId";

    /**
     * The old parent folder ID (in case of moves) in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String OLD_PARENT_FOLDER_ID = "oldParentFolderId";

    /**
     * The path to the default folder in an array of folder IDs in their absolute/unique form, i.e. all containing the service/account
     * information.
     */
    public static final String FOLDER_PATH = "folderPath";

    /**
     * The object ID in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String OBJECT_ID = "objectId";

    public static final String FILE_NAME = "fileName";

    public static final String VERSIONS = "versions";

    public static final String E_TAG = "eTag";

    public static final String HARD_DELETE = "hardDelete";

    /**
     * Indicates that any share-related cleanup has already been processed.
     */
    public static final String SHARE_CLEANUP_DONE = "shareCleanupDone";

}
