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

package com.openexchange.event.impl;

import com.openexchange.config.ConfigurationService;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentCleaner;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.LockCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.notify.ParticipantNotify;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * EventInit
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class EventInit implements Initialization {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventInit.class);
    private boolean started;



    public EventInit() {
		super();
	}

    @Override
    public void start() throws OXException {
        if (started) {
			return;
		}
        started = true;
        final boolean infoEnabled = LOG.isInfoEnabled();
        if (infoEnabled) {
			LOG.info("Parse Event properties");
		}
		final EventConfig eventConfig = new EventConfigImpl(ServerServiceRegistry.getInstance().getService(ConfigurationService.class).getFile("event.properties"));
		//final EventQueue eventQueue = new EventQueue(eventConfig);
		EventQueue.init(eventConfig);

		if (infoEnabled) {
			LOG.info("Adding Notification Listener");
		}
        final ParticipantNotify notify = new ParticipantNotify();
		EventQueue.addModernListener(notify);

		if (infoEnabled) {
			LOG.info("Adding AttachmentCleaner");
		}
        final AttachmentCleaner attCleaner = new AttachmentCleaner();
		EventQueue.addContactEvent(attCleaner);
		EventQueue.addTaskEvent(attCleaner);

		if (infoEnabled) {
			LOG.info("Adding PropertiesCleaner");
		}
        final PropertyCleaner propertyCleaner = new PropertyCleaner(new PropertyStoreImpl(new DBPoolProvider(), "oxfolder_property"), new PropertyStoreImpl(new DBPoolProvider(), "infostore_property"));
		EventQueue.addFolderEvent(propertyCleaner);

		if (infoEnabled) {
			LOG.info("Adding LockCleaner");
		}
        final LockCleaner lockCleaner = new LockCleaner(new FolderLockManagerImpl(new DBPoolProvider()), new EntityLockManagerImpl(new DBPoolProvider(), "infostore_lock"));
		EventQueue.addFolderEvent(lockCleaner);
    }

    @Override
    public void stop() throws OXException {
        EventQueue.stop();
        EventQueue.clearAllListeners();
    }
}
