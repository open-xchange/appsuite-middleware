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

package com.openexchange.file.storage.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.java.StringAllocator;
import com.openexchange.session.Session;

/**
 * {@link WrappedFileStorageFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class WrappedFileStorageFolderAccess implements FileStorageFolderAccess {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(WrappedFileStorageFolderAccess.class);

    private final FileStorageFolderAccess delegate;
    private final FileStorageAccountAccess parent;
    private final Session session;

    public WrappedFileStorageFolderAccess(FileStorageFolderAccess delegate, FileStorageAccountAccess parent, Session session) {
        super();
        this.delegate = delegate;
        this.parent = parent;
        this.session = session;
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        return delegate.exists(folderId);
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        return delegate.getFolder(folderId);
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        return delegate.getPersonalFolder();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return delegate.getPublicFolders();
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        return delegate.getSubfolders(parentIdentifier, all);
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return delegate.getRootFolder();
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        String newId = delegate.createFolder(toCreate);
        fire(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(newId)));
        return newId;
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        String newId = delegate.updateFolder(identifier, toUpdate);
        fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(newId)));
        return newId;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        String newId = delegate.moveFolder(folderId, newParentId);
        fire(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, getEventProperties(folderId)));
        fire(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(newId)));
        return newId;
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        String newId = delegate.renameFolder(folderId, newName);
        fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(newId)));
        return newId;
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        String oldId = delegate.deleteFolder(folderId);
        fire(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, getEventProperties(oldId)));
        return oldId;
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        String oldId = delegate.deleteFolder(folderId, hardDelete);
        fire(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, getEventProperties(oldId)));
        return oldId;
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        delegate.clearFolder(folderId);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        delegate.clearFolder(folderId, hardDelete);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        return delegate.getPath2DefaultFolder(folderId);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return delegate.getStorageQuota(folderId);
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return delegate.getFileQuota(folderId);
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        return delegate.getQuotas(folder, types);
    }

    private Dictionary<String, Object> getEventProperties(String folderId) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(5);
        properties.put(FileStorageEventConstants.SESSION, session);
        properties.put(FileStorageEventConstants.FOLDER_ID, folderId);
        properties.put(FileStorageEventConstants.ACCOUNT_ID, parent.getAccountId());
        properties.put(FileStorageEventConstants.SERVICE, parent.getService().getId());
        try {
            FileStorageFolder[] folders = delegate.getPath2DefaultFolder(folderId);
            if (null != folders) {
                String[] folderPath = new String[folders.length];
                for (int i = 0; i < folders.length; i++) {
                    folderPath[i] = folders[i].getId();
                }
                properties.put(FileStorageEventConstants.FOLDER_PATH, folderPath);
            }
        } catch (OXException e) {
            LOG.warn("Error getting path to default folder for event", e);
        }
        return properties;
    }

    private static void fire(Event event) {
        EventAdmin eventAdmin = FileStorageServiceLookup.getService(EventAdmin.class);
        if (null != eventAdmin) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Publishing: " + dump(event));
            }
            eventAdmin.postEvent(event);
        } else if (LOG.isWarnEnabled()) {
            LOG.warn("Unable to access event admin, unable to publish event " + dump(event));
        }
    }

    private static String dump(Event event) {
        if (null != event) {
            return new StringAllocator().append(event.getTopic())
                .append(": folderId=").append(event.getProperty(FileStorageEventConstants.FOLDER_ID))
                .append(", folderPath=").append(event.getProperty(FileStorageEventConstants.FOLDER_PATH))
                .append(", service=").append(event.getProperty(FileStorageEventConstants.SERVICE))
                .append(", accountId=").append(event.getProperty(FileStorageEventConstants.ACCOUNT_ID))
                .append(", session=").append(event.getProperty(FileStorageEventConstants.SESSION))
                .toString();
        }
        return null;
    }

}
