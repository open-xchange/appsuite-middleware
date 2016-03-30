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

package com.openexchange.pop3;

import com.openexchange.exception.OXException;
import com.openexchange.mail.Quota;
import com.openexchange.mail.Quota.Type;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.pop3.storage.POP3Storage;

/**
 * {@link POP3FolderStorage} - The POP3 folder storage implementation.
 * <p>
 * POP3 folder structure only consists of the INBOX folder with its parental root folder:
 *
 * <pre>
 * &lt;default&gt;
 *      |
 *      |-- INBOX
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3FolderStorage extends MailFolderStorage {

    private final IMailFolderStorage pop3FolderStorage;

    /**
     * Initializes a new {@link POP3FolderStorage}
     *
     * @param pop3Storage The POP3 storage
     * @throws OXException If initialization fails
     */
    public POP3FolderStorage(final POP3Storage pop3Storage) throws OXException {
        super();
        pop3FolderStorage = pop3Storage.getFolderStorage();
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        pop3FolderStorage.checkDefaultFolders();
    }

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws OXException {
        pop3FolderStorage.clearFolder(fullname, hardDelete);
    }

    @Override
    public void clearFolder(final String fullname) throws OXException {
        pop3FolderStorage.clearFolder(fullname);
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        return pop3FolderStorage.createFolder(toCreate);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws OXException {
        return pop3FolderStorage.deleteFolder(fullname, hardDelete);
    }

    @Override
    public String deleteFolder(final String fullname) throws OXException {
        return pop3FolderStorage.deleteFolder(fullname);
    }

    @Override
    public boolean exists(final String fullname) throws OXException {
        return pop3FolderStorage.exists(fullname);
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        return pop3FolderStorage.getDefaultFolderPrefix();
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return pop3FolderStorage.getConfirmedHamFolder();
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return pop3FolderStorage.getConfirmedSpamFolder();
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return pop3FolderStorage.getDraftsFolder();
    }

    @Override
    public MailFolder getFolder(final String fullname) throws OXException {
        return pop3FolderStorage.getFolder(fullname);
    }

    @Override
    public Quota getMessageQuota(final String folder) throws OXException {
        return pop3FolderStorage.getMessageQuota(folder);
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws OXException {
        return pop3FolderStorage.getPath2DefaultFolder(fullname);
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        return pop3FolderStorage.getQuotas(folder, types);
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return pop3FolderStorage.getRootFolder();
    }

    @Override
    public String getSentFolder() throws OXException {
        return pop3FolderStorage.getSentFolder();
    }

    @Override
    public String getSpamFolder() throws OXException {
        return pop3FolderStorage.getSpamFolder();
    }

    @Override
    public Quota getStorageQuota(final String folder) throws OXException {
        return pop3FolderStorage.getStorageQuota(folder);
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws OXException {
        return pop3FolderStorage.getSubfolders(parentFullname, all);
    }

    @Override
    public String getTrashFolder() throws OXException {
        return pop3FolderStorage.getTrashFolder();
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws OXException {
        return pop3FolderStorage.moveFolder(fullname, newFullname);
    }

    @Override
    public void releaseResources() throws OXException {
        pop3FolderStorage.releaseResources();
    }

    @Override
    public String renameFolder(final String fullname, final String newName) throws OXException {
        return pop3FolderStorage.renameFolder(fullname, newName);
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws OXException {
        return pop3FolderStorage.updateFolder(fullname, toUpdate);
    }

}
