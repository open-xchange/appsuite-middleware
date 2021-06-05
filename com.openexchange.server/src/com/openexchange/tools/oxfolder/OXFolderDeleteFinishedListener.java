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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFinishedListener;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;

/**
 * {@link OXFolderDeleteFinishedListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.0
 */
public class OXFolderDeleteFinishedListener implements DeleteFinishedListener {

    private static final Logger LOG = LoggerFactory.getLogger(OXFolderDeleteFinishedListener.class);

    /**
     * Initialises a new {@link OXFolderDeleteFinishedListener}.
     */
    public OXFolderDeleteFinishedListener() {
        super();
    }

    @Override
    public void deleteFinished(DeleteEvent event) throws OXException {
        if (event.getType() != DeleteEvent.TYPE_USER) {
            return;
        }

        Context ctx = event.getContext();
        ConditionTreeMapManagement.dropFor(ctx.getContextId());
        if (false == FolderCacheManager.isInitialized()) {
            return;
        }
        // Invalidate cache
        try {
            FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID, ctx);
            FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_LDAP_FOLDER_ID, ctx);
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

}
