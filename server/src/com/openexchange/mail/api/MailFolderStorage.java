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

package com.openexchange.mail.api;

import static com.openexchange.mail.utils.MailFolderUtility.isEmpty;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.mail.MailException;
import com.openexchange.mail.Quota;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;

/**
 * {@link MailFolderStorage} - Abstract implementation of {@link IMailFolderStorage}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailFolderStorage implements IMailFolderStorage {

    public abstract boolean exists(final String fullname) throws MailException;

    public abstract MailFolder getFolder(final String fullname) throws MailException;

    public abstract MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException;

    /**
     * Gets the mailbox's root folder.
     * <p>
     * This is a convenience method that invokes {@link #getFolder(String)} with its parameter set to {@link MailFolder#DEFAULT_FOLDER_ID}.
     * It may be overridden if a faster way can be achieved by specific implementation.
     * 
     * @return The mailbox's root folder
     * @throws MailException If mailbox's default folder cannot be delivered
     */
    public MailFolder getRootFolder() throws MailException {
        return getFolder(MailFolder.DEFAULT_FOLDER_ID);
    }

    public abstract void checkDefaultFolders() throws MailException;

    public abstract String createFolder(MailFolderDescription toCreate) throws MailException;

    public abstract String updateFolder(String fullname, MailFolderDescription toUpdate) throws MailException;

    public abstract String moveFolder(String fullname, String newFullname) throws MailException;

    /**
     * Renames the folder identified through given fullname to the specified new name.
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
     * @param fullname The folder fullname
     * @param newName The new name
     * @return The new fullname
     * @throws MailException If either folder does not exist or cannot be renamed
     */
    public String renameFolder(final String fullname, final String newName) throws MailException {
        final MailFolder folder = getFolder(fullname);
        if (isEmpty(newName)) {
            throw new MailException(MailException.Code.INVALID_FOLDER_NAME_EMPTY);
        } else if (newName.indexOf(folder.getSeparator()) != -1) {
            throw new MailException(MailException.Code.INVALID_FOLDER_NAME, String.valueOf(folder.getSeparator()));
        }
        final String newPath;
        if (MailFolder.DEFAULT_FOLDER_ID.equals(folder.getParentFullname())) {
            newPath = newName;
        } else {
            newPath = new StringBuilder(folder.getParentFullname()).append(folder.getSeparator()).append(newName).toString();
        }
        return moveFolder(fullname, newPath);
    }

    public String deleteFolder(final String fullname) throws MailException {
        return deleteFolder(fullname, false);
    }

    public abstract String deleteFolder(String fullname, boolean hardDelete) throws MailException;

    public void clearFolder(final String fullname) throws MailException {
        clearFolder(fullname, false);
    }

    public abstract void clearFolder(String fullname, boolean hardDelete) throws MailException;

    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
        if (fullname.equals(MailFolder.DEFAULT_FOLDER_ID)) {
            return new MailFolder[0];
        }
        MailFolder f = getFolder(fullname);
        final List<MailFolder> list = new ArrayList<MailFolder>();
        do {
            list.add(f);
            f = getFolder(f.getParentFullname());
        } while (!f.getFullname().equals(MailFolder.DEFAULT_FOLDER_ID));
        return list.toArray(new MailFolder[list.size()]);
    }

    private static final Quota.Type[] STORAGE = { Quota.Type.STORAGE };

    public Quota getStorageQuota(final String folder) throws MailException {
        return getQuotas(folder, STORAGE)[0];
    }

    private static final Quota.Type[] MESSAGE = { Quota.Type.MESSAGE };

    public Quota getMessageQuota(final String folder) throws MailException {
        return getQuotas(folder, MESSAGE)[0];
    }

    public abstract Quota[] getQuotas(String folder, Quota.Type[] types) throws MailException;

    public abstract String getConfirmedHamFolder() throws MailException;

    public abstract String getConfirmedSpamFolder() throws MailException;

    public abstract String getDraftsFolder() throws MailException;

    public abstract String getSpamFolder() throws MailException;

    public abstract String getSentFolder() throws MailException;

    public abstract String getTrashFolder() throws MailException;

    public abstract void releaseResources() throws MailException;

}
