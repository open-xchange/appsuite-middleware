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

import static com.openexchange.file.storage.FileStorageEventConstants.ACCOUNT_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.FOLDER_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.FOLDER_PATH;
import static com.openexchange.file.storage.FileStorageEventConstants.PARENT_FOLDER_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.SERVICE;
import static com.openexchange.file.storage.FileStorageEventConstants.SESSION;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
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

    private static final List<DriveAction<? extends DriveVersion>> SYNC_DIRECTORIES_ACTION;
    static {
        SYNC_DIRECTORIES_ACTION = new ArrayList<DriveAction<? extends DriveVersion>>(1);
        SYNC_DIRECTORIES_ACTION.add(new SyncDirectoriesAction());
    }

    private final List<DriveEventPublisher> publishers;

    public DriveEventServiceImpl() {
        super();
        this.publishers = new CopyOnWriteArrayList<DriveEventPublisher>();
    }

    private static boolean check(Event event) {
        if (null != event &&
            event.containsProperty(SESSION) &&
            event.containsProperty(SERVICE) &&
            event.containsProperty(ACCOUNT_ID) &&
            (event.containsProperty(FOLDER_ID) || event.containsProperty(PARENT_FOLDER_ID))) {
            return true;
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(FileStorageEventHelper.createDebugMessage("event", event));
        }
        if (false == check(event)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to handle incomplete event: " + event);
            }
            return;
        }
        Session session = (Session)event.getProperty(SESSION);
        String service = (String)event.getProperty(SERVICE);
        String accountID = (String)event.getProperty(ACCOUNT_ID);
        Set<String> folderIDs = new HashSet<String>();
        if (event.containsProperty(FOLDER_PATH)) {
            /*
             * prefer folder path if available from event
             */
            folderIDs.addAll(Arrays.asList((String[])event.getProperty(FOLDER_PATH)));
        } else {
            /*
             * determine folder path manually starting with parent/or folder itself
             */
            if (event.containsProperty(PARENT_FOLDER_ID)) {
                String parentFolderID = new FolderID(service, accountID, (String)event.getProperty(PARENT_FOLDER_ID)).toUniqueID();
                folderIDs.add(parentFolderID);
                folderIDs.addAll(resolveToRoot(parentFolderID, session));
            } else if (event.containsProperty(FOLDER_ID)) {
                String folderID = new FolderID(service, accountID, (String)event.getProperty(FOLDER_ID)).toUniqueID();
                folderIDs.add(folderID);
                folderIDs.addAll(resolveToRoot(folderID, session));
            }
        }
        if (null != folderIDs && 0 < folderIDs.size()) {
            DriveEvent driveEvent = new DriveEventImpl(
                session.getContextId(), session.getSessionID(), folderIDs, event, SYNC_DIRECTORIES_ACTION);
            for (DriveEventPublisher publisher : publishers) {
                publisher.publish(driveEvent);
            }
        }
    }

//    private static List<FolderID> resolveToRoot(FolderID folderID, Session session) {
//        List<FolderID> folderIDs = new ArrayList<FolderID>();
//        try {
//            IDBasedFolderAccess folderAccess = DriveEventServiceLookup.getService(IDBasedFolderAccessFactory.class).createAccess(session);
//            FileStorageFolder[] path2DefaultFolder = folderAccess.getPath2DefaultFolder(folderID.toUniqueID());
//            for (FileStorageFolder folder : path2DefaultFolder) {
//                folderIDs.add(new FolderID(folder.getId()));
//            }
//        } catch (OXException e) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Error resolving path to rootfolder from event", e);
//            }
//        }
//        return folderIDs;
//    }

    private static List<String> resolveToRoot(String folderID, Session session) {
        List<String> folderIDs = new ArrayList<String>();
        try {
            IDBasedFolderAccess folderAccess = DriveEventServiceLookup.getService(IDBasedFolderAccessFactory.class).createAccess(session);
            FileStorageFolder[] path2DefaultFolder = folderAccess.getPath2DefaultFolder(folderID);
            for (FileStorageFolder folder : path2DefaultFolder) {
                folderIDs.add(folder.getId());
            }
        } catch (OXException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error resolving path to rootfolder from event", e);
            }
        }
        return folderIDs;
    }

//    private void notifyListeners(int contextID, String folderID, Event event) {
//        List<DriveEventListener> listeners = getListeners(contextID, folderID, false);
//        if (null != listeners && 0 < listeners.size()) {
//            for (DriveEventListener listener : listeners) {
//                SyncDirectoriesAction syncDirectoriesAction = new SyncDirectoriesAction();
//                List<DriveAction<? extends DriveVersion>> actions = new ArrayList<DriveAction<? extends DriveVersion>>(1);
//                actions.add(syncDirectoriesAction);
//                listener.onEvent(new DriveEventImpl(actions, event));
//            }
//        }
//    }
//
//    private List<DriveEventListener> getListeners(int contextID, String folderID, boolean createIfNeeded) {
//        String key = folderID + "@" + contextID;
//        List<DriveEventListener> listeners = driveListeners.get(key);
//        if (null == listeners && createIfNeeded) {
//            listeners = new CopyOnWriteArrayList<DriveEventListener>();
//            List<DriveEventListener> exitingListeners = driveListeners.putIfAbsent(key, listeners);
//            if (null != exitingListeners) {
//                return exitingListeners;
//            }
//        }
//        return listeners;
//    }

    @Override
    public void registerPublisher(DriveEventPublisher publisher) {
        if (publishers.add(publisher) && LOG.isDebugEnabled()) {
            LOG.debug("Added drive event publisher: " + publisher);
        }
    }

    @Override
    public void unregisterPublisher(DriveEventPublisher publisher) {
        if (publishers.remove(publisher) && LOG.isDebugEnabled()) {
            LOG.debug("Removed drive event publisher: " + publisher);
        }
    }

}
