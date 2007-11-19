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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.event;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.attach.AttachmentCleaner;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.LockCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.links.LinksEventHandler;
import com.openexchange.groupware.notify.ParticipantNotify;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.server.Initialization;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * EventInit
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class EventInit implements Initialization {
	
	private static final Log LOG = LogFactory.getLog(EventInit.class);
    private boolean started;



    public EventInit() {
		super();
	}

    @Deprecated
    public static void init() {

	}

    public void start() throws AbstractOXException {
        if(started)
            return;
        started = true;
        if (LOG.isInfoEnabled()) {
			LOG.info("Parse Event properties");
		}
		final EventConfig eventConfig = new EventConfigImpl(SystemConfig.getProperty("EVENTPROPERTIES"));
		final EventQueue eventQueue = new EventQueue(eventConfig);

		if (LOG.isInfoEnabled()) {
			LOG.info("Adding Notification Listener");
		}
        ParticipantNotify notify = new ParticipantNotify();
		EventQueue.addAppointmentEvent(notify);
		EventQueue.addTaskEvent(notify);

		if (LOG.isInfoEnabled()) {
			LOG.info("Adding LinkEventHandler");
		}
        LinksEventHandler linkHandler = new LinksEventHandler();
		EventQueue.addAppointmentEvent(linkHandler);
		EventQueue.addContactEvent(linkHandler);
		EventQueue.addTaskEvent(linkHandler);
		EventQueue.addInfostoreEvent(linkHandler);

		if (LOG.isInfoEnabled()) {
			LOG.info("Adding AttachmentCleaner");
		}
        AttachmentCleaner attCleaner = new AttachmentCleaner();
		EventQueue.addAppointmentEvent(attCleaner);
		EventQueue.addContactEvent(attCleaner);
		EventQueue.addTaskEvent(attCleaner);

		if (LOG.isInfoEnabled()) {
			LOG.info("Adding PropertiesCleaner");
		}
        PropertyCleaner propertyCleaner = new PropertyCleaner(new PropertyStoreImpl(new DBPoolProvider(), "oxfolder_property"), new PropertyStoreImpl(new DBPoolProvider(), "infostore_property"));
		EventQueue.addFolderEvent(propertyCleaner);
		EventQueue.addInfostoreEvent(propertyCleaner);

		if (LOG.isInfoEnabled()) {
			LOG.info("Adding LockCleaner");
		}
        LockCleaner lockCleaner = new LockCleaner(new FolderLockManagerImpl(new DBPoolProvider()), new EntityLockManagerImpl(new DBPoolProvider(), "infostore_lock"));
		EventQueue.addFolderEvent(lockCleaner);
		EventQueue.addInfostoreEvent(lockCleaner);
    }

    public void stop() throws AbstractOXException {
        EventQueue.stop();
        EventQueue.clearAllListeners();
    }
}
