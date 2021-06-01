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

package com.openexchange.mail.api;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.Quota;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;

/**
 * {@link MailFolderStorage} - Abstract implementation of {@link IMailFolderStorage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailFolderStorage implements IMailFolderStorage {

    /**
     * Initializes a new {@link MailFolderStorage}.
     */
    protected MailFolderStorage() {
        super();
    }

    @Override
    public <T> T supports(Class<T> iface) throws OXException {
        if (iface.isInstance(this)) {
            return (T) this;
        }

        if (IMailFolderStorageDelegator.class.isInstance(this)) {
            return ((IMailFolderStorageDelegator) this).getDelegateFolderStorage().supports(iface);
        }

        return null;
    }

    @Override
    public abstract boolean exists(String fullName) throws OXException;

    @Override
    public abstract MailFolder getFolder(String fullName) throws OXException;

    @Override
    public abstract MailFolder[] getSubfolders(String parentFullName, boolean all) throws OXException;

    /**
     * Gets the mailbox's root folder.
     * <p>
     * This is a convenience method that invokes {@link #getFolder(String)} with its parameter set to {@link MailFolder#DEFAULT_FOLDER_ID}.
     * It may be overridden if a faster way can be achieved by specific implementation.
     *
     * @return The mailbox's root folder
     * @throws OXException If mailbox's default folder cannot be delivered
     */
    @Override
    public MailFolder getRootFolder() throws OXException {
        return getFolder(MailFolder.ROOT_FOLDER_ID);
    }

    @Override
    public abstract void checkDefaultFolders() throws OXException;

    @Override
    public abstract String createFolder(MailFolderDescription toCreate) throws OXException;

    @Override
    public abstract String updateFolder(String fullName, MailFolderDescription toUpdate) throws OXException;

    @Override
    public abstract String moveFolder(String fullName, String newFullName) throws OXException;

    /**
     * Renames the folder identified through given full name to the specified new name.
     * <p>
     * Since a rename is a move operation in the same (parent) folder, this is only a convenience method that may be overridden if
     * necessary.
     * <p>
     * E.g.:
     *
     * <pre>
     * my.path.to.folder -&gt; my.path.to.newfolder
     * </pre>
     *
     * @param fullName The folder full name
     * @param newName The new name
     * @return The new full name
     * @throws OXException If either folder does not exist or cannot be renamed
     */
    @Override
    public String renameFolder(String fullName, String newName) throws OXException {
        final MailFolder folder = getFolder(fullName);
        if (com.openexchange.java.Strings.isEmpty(newName)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        if (newName.indexOf(folder.getSeparator()) != -1) {
            throw MailExceptionCode.INVALID_FOLDER_NAME2.create(newName, Character.toString(folder.getSeparator()));
        }
        final String newPath;
        if (MailFolder.ROOT_FOLDER_ID.equals(folder.getParentFullname())) {
            newPath = newName;
        } else {
            newPath = new StringBuilder(folder.getParentFullname()).append(folder.getSeparator()).append(newName).toString();
        }
        return moveFolder(fullName, newPath);
    }

    @Override
    public String deleteFolder(String fullName) throws OXException {
        return deleteFolder(fullName, false);
    }

    @Override
    public abstract String deleteFolder(String fullName, boolean hardDelete) throws OXException;

    @Override
    public void clearFolder(String fullName) throws OXException {
        clearFolder(fullName, false);
    }

    @Override
    public abstract void clearFolder(String fullName, boolean hardDelete) throws OXException;

    @Override
    public MailFolder[] getPath2DefaultFolder(String fullName) throws OXException {
        if (fullName.equals(MailFolder.ROOT_FOLDER_ID)) {
            return new MailFolder[0];
        }
        MailFolder f = getFolder(fullName);
        final List<MailFolder> list = new ArrayList<MailFolder>();
        do {
            list.add(f);
            f = getFolder(f.getParentFullname());
        } while (!f.getFullname().equals(MailFolder.ROOT_FOLDER_ID));
        return list.toArray(new MailFolder[list.size()]);
    }

    private static final Quota.Type[] STORAGE = { Quota.Type.STORAGE };

    @Override
    public Quota getStorageQuota(String fullName) throws OXException {
        return getQuotas(fullName, STORAGE)[0];
    }

    private static final Quota.Type[] MESSAGE = { Quota.Type.MESSAGE };

    @Override
    public Quota getMessageQuota(String fullName) throws OXException {
        return getQuotas(fullName, MESSAGE)[0];
    }

    @Override
    public abstract Quota[] getQuotas(String fullName, Quota.Type[] types) throws OXException;

    /**
     * Gets the prefix (incl. separator character) for default folders.
     * <p>
     * By now a compound full name is assumed. Override if not appropriate.
     *
     * @return The prefix
     * @throws OXException If a mail error occurs
     */
    @Override
    public String getDefaultFolderPrefix() throws OXException {
        checkDefaultFolders();
        final String trashFullName = getTrashFolder();
        final char separator = getFolder(trashFullName).getSeparator();
        final int pos = trashFullName.lastIndexOf(separator);
        return pos < 0 ? "" : trashFullName.substring(0, pos + 1);
    }

    @Override
    public abstract String getConfirmedHamFolder() throws OXException;

    @Override
    public abstract String getConfirmedSpamFolder() throws OXException;

    @Override
    public abstract String getDraftsFolder() throws OXException;

    @Override
    public abstract String getSpamFolder() throws OXException;

    @Override
    public abstract String getSentFolder() throws OXException;

    @Override
    public abstract String getTrashFolder() throws OXException;

    @Override
    public abstract void releaseResources() throws OXException;

}
