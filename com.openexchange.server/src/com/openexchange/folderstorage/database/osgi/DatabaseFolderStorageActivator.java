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

package com.openexchange.folderstorage.database.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.database.DatabaseFolderStorage;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link DatabaseFolderStorageActivator} - The activator for database folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DatabaseFolderStorageActivator}.
     */
    public DatabaseFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, UserPermissionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Register folder storage
        Dictionary<String, Object> dictionary = new Hashtable<String, Object>(2);
        dictionary.put("tree", FolderStorage.REAL_TREE_ID);
        registerService(FolderStorage.class, new DatabaseFolderStorage(this), dictionary);

        // Register event handler
        EventHandler eventHandler = event -> {
            if (SessiondEventConstants.TOPIC_LAST_SESSION_CONTEXT.equals(event.getTopic())) {
                Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                if (null != contextId) {
                    DatabaseFolderStorage.clearStampsFor(contextId.intValue());
                }
            }
        };
        dictionary = new Hashtable<String, Object>(2);
        dictionary.put(EventConstants.EVENT_TOPIC, new String[] { SessiondEventConstants.TOPIC_LAST_SESSION_CONTEXT });
        registerService(EventHandler.class, eventHandler, dictionary);
    }

}
