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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.events.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.events.DriveEventListener;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.session.Session;

/**
 * {@link DriveEventServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventServiceImpl implements org.osgi.service.event.EventHandler, DriveEventService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveEventServiceImpl.class);

    private final ConcurrentMap<String, List<DriveEventListener>> driveListeners;

    public DriveEventServiceImpl() {
        super();
        this.driveListeners = new ConcurrentHashMap<String, List<DriveEventListener>>();
    }

    private static boolean check(Event event) {
        if (null != event &&
            event.containsProperty(FileStorageEventConstants.SESSION) &&
            event.containsProperty(FileStorageEventConstants.SERVICE) &&
            event.containsProperty(FileStorageEventConstants.ACCOUNT_ID) &&
            (event.containsProperty(FileStorageEventConstants.FOLDER_ID) || event.containsProperty(FileStorageEventConstants.PARENT_FOLDER_ID))) {
            return true;
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got event: " + event);
        }
        if (false == check(event)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to handle incomplete event: " + event);
            }
            return;
        }
        Session session = (Session)event.getProperty(FileStorageEventConstants.SESSION);
        String service = (String)event.getProperty(FileStorageEventConstants.SERVICE);
        String accountID = (String)event.getProperty(FileStorageEventConstants.ACCOUNT_ID);
        Set<FolderID> folderIDs = new HashSet<FolderID>();
        if (event.containsProperty(FileStorageEventConstants.FOLDER_PATH)) {
            String[] path = (String[])event.getProperty(FileStorageEventConstants.FOLDER_PATH);
            for (String id : path) {
                folderIDs.add(new FolderID(service, accountID, id));
            }
        }

        if (event.containsProperty(FileStorageEventConstants.PARENT_FOLDER_ID)) {
            FolderID parentFolderID = new FolderID(service, accountID, (String)event.getProperty(FileStorageEventConstants.PARENT_FOLDER_ID));
            folderIDs.add(parentFolderID);
            folderIDs.addAll(resolveToRoot(parentFolderID, session));
        } else if (event.containsProperty(FileStorageEventConstants.FOLDER_ID)) {
            FolderID folderID = new FolderID(service, accountID, (String)event.getProperty(FileStorageEventConstants.FOLDER_ID));
            folderIDs.add(folderID);
            folderIDs.addAll(resolveToRoot(folderID, session));
        }
        if (0 < folderIDs.size()) {
            for (FolderID folderID : folderIDs) {
                notifyListeners(session.getContextId(), folderID.toUniqueID(), event);
            }
        }
    }

    private static List<FolderID> resolveToRoot(FolderID folderID, Session session) {
        List<FolderID> folderIDs = new ArrayList<FolderID>();
        try {
            IDBasedFolderAccess folderAccess = DriveEventServiceLookup.getService(IDBasedFolderAccessFactory.class).createAccess(session);
            FileStorageFolder[] path2DefaultFolder = folderAccess.getPath2DefaultFolder(folderID.toUniqueID());
            for (FileStorageFolder folder : path2DefaultFolder) {
                folderIDs.add(new FolderID(folderID.getService(), folderID.getAccountId(), folder.getId()));
            }
        } catch (OXException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error resolving path to rootfolder from event", e);
            }
        }
        return folderIDs;
    }

    private void notifyListeners(int contextID, String folderID, Event event) {
        List<DriveEventListener> listeners = getListeners(contextID, folderID, false);
        if (null != listeners && 0 < listeners.size()) {
            for (DriveEventListener listener : listeners) {
                SyncDirectoriesAction syncDirectoriesAction = new SyncDirectoriesAction();
                List<DriveAction<? extends DriveVersion>> actions = new ArrayList<DriveAction<? extends DriveVersion>>(1);
                actions.add(syncDirectoriesAction);
                listener.onEvent(new DriveEventImpl(actions, event));
            }
        }
    }

    private List<DriveEventListener> getListeners(int contextID, String folderID, boolean createIfNeeded) {
        String key = folderID + "@" + contextID;
        List<DriveEventListener> listeners = driveListeners.get(key);
        if (null == listeners && createIfNeeded) {
            listeners = new CopyOnWriteArrayList<DriveEventListener>();
            List<DriveEventListener> exitingListeners = driveListeners.putIfAbsent(key, listeners);
            if (null != exitingListeners) {
                return exitingListeners;
            }
        }
        return listeners;
    }

    @Override
    public void registerListener(DriveEventListener listener, String rootFolderID, int contextID) {
        if (getListeners(contextID, rootFolderID, true).add(listener) && LOG.isDebugEnabled()) {
            LOG.debug("Added drive listener for folder '" + rootFolderID + "': " + listener);
        }
    }

    @Override
    public void unregisterListener(DriveEventListener listener, String rootFolderID, int contextID) {
        List<DriveEventListener> folderListeners = getListeners(contextID, rootFolderID, false);
        if (null != folderListeners && folderListeners.remove(listener)) {
            LOG.debug("Removed drive listener for folder '" + rootFolderID + "': " + listener);
        } else {
            LOG.warn("Listener not found, unable to unregister drive listener for folder '" + rootFolderID + "': " + listener);
        }
    }

}
