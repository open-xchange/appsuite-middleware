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

package com.openexchange.folderstorage.cache;

import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFinishedListener;

/**
 * {@link CacheFolderStorageDeleteFinishedListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.0
 */
public class CacheFolderStorageDeleteFinishedListener implements DeleteFinishedListener {

    /**
     * Initialises a new {@link CacheFolderStorageDeleteFinishedListener}.
     */
    public CacheFolderStorageDeleteFinishedListener() {
        super();
    }

    @Override
    public void deleteFinished(DeleteEvent event) throws OXException {
        if (event.getType() != DeleteEvent.TYPE_USER) {
            return;
        }
        int userId = event.getId();
        List<String> ids = Arrays.asList(Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID), Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID));
        CacheFolderStorage.getInstance().removeSingleFromCache(ids, OutlookFolderStorage.OUTLOOK_TREE_ID, userId, event.getContext().getContextId(), true, false, null);
    }
}
