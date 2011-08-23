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
        connect();
        try {
            return delegateMailAccess.getFolderStorage().exists(fullName);
        } finally {
            close();
        }
    }

    @Override
    public MailFolder getFolder(final String fullName) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getFolder(fullName);
        } finally {
            close();
        }
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException {
        connect();
        try {
            System.out.println("SMALFolderStorage.getSubfolders()");
            return delegateMailAccess.getFolderStorage().getSubfolders(parentFullName, all);
        } finally {
            close();
        }
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        connect();
        try {
            delegateMailAccess.getFolderStorage().checkDefaultFolders();
        } finally {
            close();
        }
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().createFolder(toCreate);
        } finally {
            close();
        }
    }

    @Override
    public String updateFolder(final String fullName, final MailFolderDescription toUpdate) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().updateFolder(fullName, toUpdate);
        } finally {
            close();
        }
    }

    @Override
    public String moveFolder(final String fullName, final String newFullName) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().moveFolder(fullName, newFullName);
        } finally {
            close();
        }
    }

    @Override
    public String deleteFolder(final String fullName, final boolean hardDelete) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().deleteFolder(fullName, hardDelete);
        } finally {
            close();
        }
    }

    @Override
    public void clearFolder(final String fullName, final boolean hardDelete) throws OXException {
        connect();
        try {
            delegateMailAccess.getFolderStorage().clearFolder(fullName, hardDelete);
        } finally {
            close();
        }
    }

    @Override
    public Quota[] getQuotas(final String fullName, final Type[] types) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getQuotas(fullName, types);
        } finally {
            close();
        }
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getConfirmedHamFolder();
        } finally {
            close();
        }
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getConfirmedSpamFolder();
        } finally {
            close();
        }
    }

    @Override
    public String getDraftsFolder() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getDraftsFolder();
        } finally {
            close();
        }
    }

    @Override
    public String getSpamFolder() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getSpamFolder();
        } finally {
            close();
        }
    }

    @Override
    public String getSentFolder() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getSentFolder();
        } finally {
            close();
        }
    }

    @Override
    public String getTrashFolder() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getTrashFolder();
        } finally {
            close();
        }
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getFolder(MailFolder.DEFAULT_FOLDER_ID);
        } finally {
            close();
        }
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getDefaultFolderPrefix();
        } finally {
            close();
        }
    }

    @Override
    public String renameFolder(final String fullName, final String newName) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().renameFolder(fullName, newName);
        } finally {
            close();
        }
    }

    @Override
    public String deleteFolder(final String fullName) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().deleteFolder(fullName);
        } finally {
            close();
        }
    }

    @Override
    public void clearFolder(final String fullName) throws OXException {
        connect();
        try {
            delegateMailAccess.getFolderStorage().clearFolder(fullName);
        } finally {
            close();
        }
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getPath2DefaultFolder(fullName);
        } finally {
            close();
        }
    }

    @Override
    public Quota getStorageQuota(final String fullName) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getStorageQuota(fullName);
        } finally {
            close();
        }
    }

    @Override
    public Quota getMessageQuota(final String fullName) throws OXException {
        connect();
        try {
            return delegateMailAccess.getFolderStorage().getMessageQuota(fullName);
        } finally {
            close();
        }
    }

}
