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

package com.openexchange.groupware.infostore;

/**
 * WebdavFolderAliases allow overriding folder names for certain folders in
 * the infostores Webdav interface.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface WebdavFolderAliases {

    /**
     * Magic number that #getId(String,int) returns, if no alias matching the given criteria has not been found.
     */
    public static int NOT_REGISTERED = -1;

    /**
     * Add an alias for "folderName" for a certain id under a certain parent
     */
    void registerNameWithIDAndParent(String folderName, int id, int parent);

    /**
     * Look up an alias for the given id. If no alias was registered, this returns null.
     */
    String getAlias(int id);

    /**
     * Lookup up an id by alias and parent. Returns WebdavFolderAliases.NOT_REGISTERED when no alias was registered or
     * if the parents did not match.
     */
    int getId(String alias, int parent);
}
