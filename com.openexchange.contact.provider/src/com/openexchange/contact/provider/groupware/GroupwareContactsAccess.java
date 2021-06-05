
package com.openexchange.contact.provider.groupware;
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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.GroupwareContactsFolder;
import com.openexchange.contact.common.GroupwareFolderType;
import com.openexchange.contact.provider.folder.FolderContactsAccess;
import com.openexchange.exception.OXException;

/**
 * {@link GroupwareContactsAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface GroupwareContactsAccess extends FolderContactsAccess {

    /**
     * Default implementation delegating to {@link #getVisibleFolders(GroupwareFolderType)} for all types. Override if applicable.
     */
    @Override
    default List<ContactsFolder> getVisibleFolders() throws OXException {
        List<ContactsFolder> folders = new ArrayList<ContactsFolder>();
        for (GroupwareFolderType type : GroupwareFolderType.values()) {
            folders.addAll(getVisibleFolders(type));
        }
        return folders;
    }

    /**
     * Gets a list of all visible contacts folders.
     *
     * @param type The type to get the visible folders for
     * @return A list of all visible contacts folders of the type
     */
    List<GroupwareContactsFolder> getVisibleFolders(GroupwareFolderType type) throws OXException;

}
