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

    @Override
    public abstract boolean exists(final String fullName) throws OXException;

    @Override
    public abstract MailFolder getFolder(final String fullName) throws OXException;

    @Override
    public abstract MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException;

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
        return getFolder(MailFolder.DEFAULT_FOLDER_ID);
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
    public String renameFolder(final String fullName, final String newName) throws OXException {
        final MailFolder folder = getFolder(fullName);
        if (com.openexchange.java.Strings.isEmpty(newName)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        if (newName.indexOf(folder.getSeparator()) != -1) {
            throw MailExceptionCode.INVALID_FOLDER_NAME2.create(newName, Character.toString(folder.getSeparator()));
        }
        final String newPath;
        if (MailFolder.DEFAULT_FOLDER_ID.equals(folder.getParentFullname())) {
            newPath = newName;
        } else {
            newPath = new StringBuilder(folder.getParentFullname()).append(folder.getSeparator()).append(newName).toString();
        }
        return moveFolder(fullName, newPath);
    }

    @Override
    public String deleteFolder(final String fullName) throws OXException {
        return deleteFolder(fullName, false);
    }

    @Override
    public abstract String deleteFolder(String fullName, boolean hardDelete) throws OXException;

    @Override
    public void clearFolder(final String fullName) throws OXException {
        clearFolder(fullName, false);
    }

    @Override
    public abstract void clearFolder(String fullName, boolean hardDelete) throws OXException;

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        if (fullName.equals(MailFolder.DEFAULT_FOLDER_ID)) {
            return new MailFolder[0];
        }
        MailFolder f = getFolder(fullName);
        final List<MailFolder> list = new ArrayList<MailFolder>();
        do {
            list.add(f);
            f = getFolder(f.getParentFullname());
        } while (!f.getFullname().equals(MailFolder.DEFAULT_FOLDER_ID));
        return list.toArray(new MailFolder[list.size()]);
    }

    private static final Quota.Type[] STORAGE = { Quota.Type.STORAGE };

    @Override
    public Quota getStorageQuota(final String fullName) throws OXException {
        return getQuotas(fullName, STORAGE)[0];
    }

    private static final Quota.Type[] MESSAGE = { Quota.Type.MESSAGE };

    @Override
    public Quota getMessageQuota(final String fullName) throws OXException {
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
