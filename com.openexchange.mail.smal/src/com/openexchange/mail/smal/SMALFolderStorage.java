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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.Quota;
import com.openexchange.mail.Quota.Type;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.session.Session;

/**
 * {@link SMALFolderStorage} - The SMAL folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMALFolderStorage extends AbstractSMALStorage implements IMailFolderStorage {

    /**
     * Initializes a new {@link SMALFolderStorage}.
     */
    public SMALFolderStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess) {
        super(session, accountId, delegateMailAccess);
    }

    @Override
    public boolean exists(final String fullName) throws OXException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullName)) {
            return true;
        }
        return delegateMailAccess.getFolderStorage().exists(fullName);
    }

    @Override
    public MailFolder getFolder(final String fullName) throws OXException {
        return delegateMailAccess.getFolderStorage().getFolder(fullName);
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException {
        System.out.println("SMALFolderStorage.getSubfolders()");
        return delegateMailAccess.getFolderStorage().getSubfolders(parentFullName, all);
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        delegateMailAccess.getFolderStorage().checkDefaultFolders();
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        return delegateMailAccess.getFolderStorage().createFolder(toCreate);
    }

    @Override
    public String updateFolder(final String fullName, final MailFolderDescription toUpdate) throws OXException {
        return delegateMailAccess.getFolderStorage().updateFolder(fullName, toUpdate);
    }

    @Override
    public String moveFolder(final String fullName, final String newFullName) throws OXException {
        return delegateMailAccess.getFolderStorage().moveFolder(fullName, newFullName);
    }

    @Override
    public String deleteFolder(final String fullName, final boolean hardDelete) throws OXException {
        return delegateMailAccess.getFolderStorage().deleteFolder(fullName, hardDelete);
    }

    @Override
    public void clearFolder(final String fullName, final boolean hardDelete) throws OXException {
        delegateMailAccess.getFolderStorage().clearFolder(fullName, hardDelete);
    }

    @Override
    public Quota[] getQuotas(final String fullName, final Type[] types) throws OXException {
        return delegateMailAccess.getFolderStorage().getQuotas(fullName, types);
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return delegateMailAccess.getFolderStorage().getConfirmedHamFolder();
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return delegateMailAccess.getFolderStorage().getConfirmedSpamFolder();
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return delegateMailAccess.getFolderStorage().getDraftsFolder();
    }

    @Override
    public String getSpamFolder() throws OXException {
        return delegateMailAccess.getFolderStorage().getSpamFolder();
    }

    @Override
    public String getSentFolder() throws OXException {
        return delegateMailAccess.getFolderStorage().getSentFolder();
    }

    @Override
    public String getTrashFolder() throws OXException {
        return delegateMailAccess.getFolderStorage().getTrashFolder();
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return delegateMailAccess.getFolderStorage().getFolder(MailFolder.DEFAULT_FOLDER_ID);
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        return delegateMailAccess.getFolderStorage().getDefaultFolderPrefix();
    }

    @Override
    public String renameFolder(final String fullName, final String newName) throws OXException {
        return delegateMailAccess.getFolderStorage().renameFolder(fullName, newName);
    }

    @Override
    public String deleteFolder(final String fullName) throws OXException {
        return delegateMailAccess.getFolderStorage().deleteFolder(fullName);
    }

    @Override
    public void clearFolder(final String fullName) throws OXException {
        delegateMailAccess.getFolderStorage().clearFolder(fullName);
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        return delegateMailAccess.getFolderStorage().getPath2DefaultFolder(fullName);
    }

    @Override
    public Quota getStorageQuota(final String fullName) throws OXException {
        return delegateMailAccess.getFolderStorage().getStorageQuota(fullName);
    }

    @Override
    public Quota getMessageQuota(final String fullName) throws OXException {
        return delegateMailAccess.getFolderStorage().getMessageQuota(fullName);
    }

    @Override
    public void releaseResources() throws OXException {
        delegateMailAccess.getFolderStorage().releaseResources();
    }

}
