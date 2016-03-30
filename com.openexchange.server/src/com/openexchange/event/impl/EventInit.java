/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        if(started) {
			return;
		}
        started = true;
        final boolean infoEnabled = LOG.isInfoEnabled();
        if (infoEnabled) {
			LOG.info("Parse Event properties");
		}
		final EventConfig eventConfig = new EventConfigImpl(ServerServiceRegistry.getInstance().getService(ConfigurationService.class).getFileByName("event.properties"));
		//final EventQueue eventQueue = new EventQueue(eventConfig);
		EventQueue.init(eventConfig);

		if (infoEnabled) {
			LOG.info("Adding Notification Listener");
		}
        final ParticipantNotify notify = new ParticipantNotify();
		EventQueue.addModernListener((AppointmentEventInterface) notify);
		EventQueue.addModernListener((TaskEventInterface) notify);

		if (infoEnabled) {
			LOG.info("Adding AttachmentCleaner");
		}
        final AttachmentCleaner attCleaner = new AttachmentCleaner();
		EventQueue.addAppointmentEvent(attCleaner);
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
