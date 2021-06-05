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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link UpdatedFolderHandler} - May be passed to {@link OXFolderManager#getInstance(com.openexchange.session.Session, java.util.Collection, Connection, Connection)}
 * and gets called-back in case a folder has been modified.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface UpdatedFolderHandler {

    /**
     * Handles specified updated folder.
     *
     * @param updatedFolder The updated folder
     * @param con The connection to use
     */
    void onFolderUpdated(FolderObject updatedFolder, Connection con);
}
