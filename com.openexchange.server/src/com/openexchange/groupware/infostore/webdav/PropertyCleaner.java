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

package com.openexchange.groupware.infostore.webdav;

import static com.openexchange.file.storage.FileStorageEventHelper.createDebugMessage;
import static com.openexchange.file.storage.FileStorageEventHelper.extractObjectId;
import static com.openexchange.file.storage.FileStorageEventHelper.extractSession;
import static com.openexchange.file.storage.FileStorageEventHelper.isDeleteEvent;
import static com.openexchange.file.storage.FileStorageEventHelper.isInfostoreEvent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.event.impl.FolderEventInterface;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class PropertyCleaner implements FolderEventInterface, EventHandler {

	private final PropertyStore infoProperties;
	private final PropertyStore folderProperties;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertyCleaner.class);

	public PropertyCleaner(final PropertyStore folderProperties, final PropertyStore infoProperties){
		this.folderProperties = folderProperties;
		this.infoProperties = infoProperties;

	}

	@Override
    public void folderCreated(final FolderObject folderObj, final Session sessionObj) {
        // Nothing to do
	}

	@Override
    public void folderDeleted(final FolderObject folderObj, final Session session) {
		try {
            final ServerSession sessionObj = ServerSessionAdapter.valueOf(session);
            folderProperties.startTransaction();
			folderProperties.removeAll(folderObj.getObjectID(), sessionObj.getContext());
			folderProperties.commit();
		} catch (OXException e) {
			LOG.error("", e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} finally {
			try {
				folderProperties.finish();
			} catch (OXException e) {
			    LOG.error("", e);
			}
		}
	}

	@Override
    public void folderModified(final FolderObject folderObj, final Session sessionObj) {
	    // Nothing to do
	}

    @Override
    public void handleEvent(final Event event) {
        if (isInfostoreEvent(event) && isDeleteEvent(event)) {
            try {
                FileID fileID = new FileID(extractObjectId(event));
                if (FileID.INFOSTORE_SERVICE_ID.equals(fileID.getService()) && FileID.INFOSTORE_ACCOUNT_ID.equals(fileID.getAccountId())) {
                    int objectID = Integer.parseInt(fileID.getFileId());
                    ServerSession session = ServerSessionAdapter.valueOf(extractSession(event));
                    infoProperties.startTransaction();
                    infoProperties.removeAll(objectID, session.getContext());
                    infoProperties.commit();
                }
            } catch (OXException e) {
                LOG.error("Couldn't remove properties from infoitem. Run the consistency tool.", e);
            } catch (NumberFormatException e) {
                // Obviously no numeric identifier; therefore not related to InfoStore file storage
                LOG.debug("", e);
            } finally {
                try {
                    infoProperties.finish();
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
            LOG.debug("{}", new Object() { @Override public String toString() { return createDebugMessage("UpdateEvent", event);}});
        }
    }
}
