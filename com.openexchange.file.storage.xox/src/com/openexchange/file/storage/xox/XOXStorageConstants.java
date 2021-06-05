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

package com.openexchange.file.storage.xox;

import com.openexchange.annotation.NonNull;
import com.openexchange.file.storage.FileStorageConstants;
import com.openexchange.groupware.modules.Module;

/**
 * {@link XOXStorageConstants}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public final class XOXStorageConstants implements FileStorageConstants {

    /**
     * The unique ID of the OX share file storage implementation
     */
    public static final String ID = "xox" + Module.INFOSTORE.getFolderConstant();

    /**
     * The display name of the the OX share file storage implementation
     */
    @NonNull
    public static final String DISPLAY_NAME = "OX AppSuite Share";

    /**
     * The share link to the other OX
     */
    @NonNull
    public static final String SHARE_URL = "url";

    /**
     * The password to the user's share if any
     */
    @NonNull
    public static final String PASSWORD = "password";
}
