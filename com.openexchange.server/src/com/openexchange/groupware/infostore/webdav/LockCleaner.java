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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
package com.openexchange.groupware.infostore.webdav;

import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.event.impl.FolderEventInterface;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.impl.FolderLockManager;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class LockCleaner implements FolderEventInterface, EventHandler {

	private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LockCleaner.class));

	private final EntityLockManager infoLockManager;
	private final FolderLockManager folderLockManager;

	public LockCleaner(final FolderLockManager folderLockManager, final EntityLockManager infoLockManager) {
		this.folderLockManager = folderLockManager;
		this.infoLockManager = infoLockManager;
	}


	@Override
    public void folderDeleted(final FolderObject folderObj, final Session session) {
		try {
            final ServerSession sessionObj = ServerSessionAdapter.valueOf(session);
            folderLockManager.removeAll(folderObj.getObjectID(), sessionObj.getContext(), UserStorage.getStorageUser(sessionObj.getUserId(), sessionObj.getContext()));
		} catch (final OXException e) {
			LOG.fatal("Couldn't remove folder locks from folder "+folderObj.getObjectID()+" in context "+session.getContextId()+". Run the consistency tool.");
		}
    }

	@Override
    public void folderCreated(final FolderObject folderObj, final Session sessionObj) {

	}

	@Override
    public void folderModified(final FolderObject folderObj, final Session sessionObj) {

	}

    @Override
    public void handleEvent(Event event) {
        if (FileStorageEventHelper.isInfostoreEvent(event)) {
            if (FileStorageEventHelper.isUpdateEvent(event)) {
                ServerSession session = null;
                int id = 0;
                try {
                    session = ServerSessionAdapter.valueOf(FileStorageEventHelper.extractSession(event));
                    id = Integer.parseInt(FileStorageEventHelper.extractObjectId(event));
                    infoLockManager.removeAll(
                        id,
                        session.getContext(),
                        UserStorage.getStorageUser(session.getUserId(), session.getContext()));
                } catch (OXException e) {
                    LOG.fatal("Couldn't remove locks from infoitem. Run the consistency tool.", e);
                } catch (NumberFormatException e) {
                    LOG.fatal("Couldn't remove locks from infoitem. Run the consistency tool.", e);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(FileStorageEventHelper.createDebugMessage("UpdateEvent", event));
                }
            }
        }
    }
}
