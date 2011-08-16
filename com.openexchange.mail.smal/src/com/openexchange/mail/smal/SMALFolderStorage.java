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
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.session.Session;


/**
 * {@link SMALFolderStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMALFolderStorage extends MailFolderStorage {

    private final Session session;

    private final int accountId;

    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> realMailAccess;

    /**
     * Initializes a new {@link SMALFolderStorage}.
     */
    public SMALFolderStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> realMailAccess) {
        super();
        this.accountId = accountId;
        this.realMailAccess = realMailAccess;
        this.session = session;
    }

    @Override
    public boolean exists(final String fullname) throws OXException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname)) {
            return true;
        }
        realMailAccess.connect(false);
        try {
            return realMailAccess.getFolderStorage().exists(fullname);
        } finally {
            realMailAccess.close(true);
        }
    }

    @Override
    public MailFolder getFolder(final String fullname) throws OXException {
        realMailAccess.connect(false);
        try {
            return realMailAccess.getFolderStorage().getFolder(fullname);
        } finally {
            realMailAccess.close(true);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getSubfolders(java.lang.String, boolean)
     */
    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#checkDefaultFolders()
     */
    @Override
    public void checkDefaultFolders() throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#createFolder(com.openexchange.mail.dataobjects.MailFolderDescription)
     */
    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#updateFolder(java.lang.String, com.openexchange.mail.dataobjects.MailFolderDescription)
     */
    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#moveFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(final String fullname, final String newFullname) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#deleteFolder(java.lang.String, boolean)
     */
    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#clearFolder(java.lang.String, boolean)
     */
    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getQuotas(java.lang.String, com.openexchange.mail.Quota.Type[])
     */
    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getConfirmedHamFolder()
     */
    @Override
    public String getConfirmedHamFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getConfirmedSpamFolder()
     */
    @Override
    public String getConfirmedSpamFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getDraftsFolder()
     */
    @Override
    public String getDraftsFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getSpamFolder()
     */
    @Override
    public String getSpamFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getSentFolder()
     */
    @Override
    public String getSentFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#getTrashFolder()
     */
    @Override
    public String getTrashFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.api.MailFolderStorage#releaseResources()
     */
    @Override
    public void releaseResources() throws OXException {
        // TODO Auto-generated method stub

    }

}
