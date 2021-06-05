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

package com.openexchange.tools.oxfolder.property.impl;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderDeleteListenerService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link FolderUserPropertyDeleteListener}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class FolderUserPropertyDeleteListener implements FolderDeleteListenerService, DeleteListener {

    private final RdbFolderUserPropertyStorage storage;

    /**
     * Initializes a new {@link FolderUserPropertyDeleteListener}.
     */
    public FolderUserPropertyDeleteListener(RdbFolderUserPropertyStorage storage) {
        super();
        this.storage = storage;
    }

    @Override
    public void onFolderDelete(int folderId, Context context) throws OXException {
       storage.deleteFolderProperties(context.getContextId(), folderId);
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        switch(event.getType()) {
            case DeleteEvent.TYPE_CONTEXT:
                storage.deleteContextProperties(event.getId(), writeCon);
                return;
            case DeleteEvent.TYPE_USER:
                storage.deleteUserProperties(event.getContext().getContextId(), event.getId(), writeCon);
                return;
            default:
                // ignore other events
                return;
        }

    }

}
