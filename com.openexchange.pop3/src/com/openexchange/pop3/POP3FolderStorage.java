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

package com.openexchange.pop3;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import javax.mail.MessagingException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.pop3.config.POP3Config;
import com.openexchange.pop3.converters.POP3FolderConverter;
import com.openexchange.session.Session;
import com.sun.mail.pop3.POP3Store;

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

    private final POP3Store pop3Store;

    private final POP3Access pop3Access;

    private final Session session;

    private final POP3Config pop3Config;

    /**
     * Initializes a new {@link POP3FolderStorage}
     * 
     * @param pop3Store The POP3 store
     * @param pop3Access The POP3 access
     * @param session The session providing needed user data
     * @throws POP3Exception If context loading fails
     */
    public POP3FolderStorage(final POP3Store pop3Store, final POP3Access pop3Access, final Session session) throws POP3Exception {
        super();
        this.pop3Store = pop3Store;
        this.pop3Access = pop3Access;
        this.session = session;
        pop3Config = pop3Access.getPOP3Config();
    }

    @Override
    public boolean exists(final String fullname) throws MailException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullname)) {
                return true;
            }
            return pop3Store.getFolder(fullname).exists();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e, pop3Config);
        }
    }

    @Override
    public MailFolder getFolder(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return POP3FolderConverter.getRootFolder();
        }
        if ("INBOX".equals(fullname)) {
            // INBOX
            return POP3FolderConverter.getINBOXFolder(pop3Access.getInboxFolder(), session);
        }
        throw new POP3Exception(POP3Exception.Code.FOLDER_NOT_FOUND, fullname);
    }

    private static final String PATTERN_ALL = "%";

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
            // INBOX
            return new MailFolder[] { POP3FolderConverter.getINBOXFolder(pop3Access.getInboxFolder(), session) };
        }
        return EMPTY_PATH;
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        return POP3FolderConverter.getRootFolder();
    }

    @Override
    public void checkDefaultFolders() throws MailException {
        // POP3 mailbox only contains ONE folder: The INBOX folder
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws MailException {
        throw new POP3Exception(POP3Exception.Code.FOLDER_CREATION_FAILED);
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws MailException {
        throw new POP3Exception(POP3Exception.Code.MOVE_DENIED);
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
        throw new POP3Exception(POP3Exception.Code.UPDATE_DENIED);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
        throw new POP3Exception(POP3Exception.Code.DELETE_DENIED);
    }

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.UPDATE_DENIED);
        }
        if (!"INBOX".equals(fullname)) {
            throw new POP3Exception(POP3Exception.Code.FOLDER_NOT_FOUND, fullname);
        }
        final POP3InboxFolder pop3InboxFolder = pop3Access.getInboxFolder();
        pop3InboxFolder.clear();
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return EMPTY_PATH;
        }
        return new MailFolder[] {
            POP3FolderConverter.getINBOXFolder(pop3Access.getInboxFolder(), session), POP3FolderConverter.getRootFolder() };
    }

    @Override
    public String getConfirmedHamFolder() throws MailException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws MailException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws MailException {
        return null;
    }

    @Override
    public String getSentFolder() throws MailException {
        return null;
    }

    @Override
    public String getSpamFolder() throws MailException {
        return null;
    }

    @Override
    public String getTrashFolder() throws MailException {
        return null;
    }

    @Override
    public void releaseResources() throws POP3Exception {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws MailException {
        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
    }

}
