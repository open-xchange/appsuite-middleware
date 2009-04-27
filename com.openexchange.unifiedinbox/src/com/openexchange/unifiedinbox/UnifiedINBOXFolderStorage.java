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

package com.openexchange.unifiedinbox;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.converters.UnifiedINBOXFolderConverter;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;

/**
 * {@link UnifiedINBOXFolderStorage} - The Unified INBOX folder storage implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXFolderStorage extends MailFolderStorage {

    private final UnifiedINBOXAccess access;

    private final Session session;

    private final Context ctx;

    /**
     * Initializes a new {@link UnifiedINBOXFolderStorage}
     * 
     * @param access The Unified INBOX access
     * @param session The session providing needed user data
     * @throws UnifiedINBOXException If context loading fails
     */
    public UnifiedINBOXFolderStorage(final UnifiedINBOXAccess access, final Session session) throws UnifiedINBOXException {
        super();
        this.access = access;
        this.session = session;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new UnifiedINBOXException(e);
        }
    }

    @Override
    public boolean exists(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return true;
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            return true;
        }
        // TODO: Deep check for existence
        return (startsWithKnownFullname(fullname) != null);
    }

    @Override
    public MailFolder getFolder(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return UnifiedINBOXFolderConverter.getRootFolder();
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            return UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(access.getAccountId(), session, fullname, getLocalizedName(fullname));
        }
        final String fn = startsWithKnownFullname(fullname);
        if (null != fn) {
            final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fn);
            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();
            try {
                return mailAccess.getFolderStorage().getFolder(fa.getFullname());
            } finally {
                mailAccess.close(true);
            }
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, fullname);
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullname, final boolean all) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
            final MailFolder[] retval = new MailFolder[5];
            retval[0] = UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(
                access.getAccountId(),
                session,
                UnifiedINBOXAccess.INBOX,
                getLocalizedName(UnifiedINBOXAccess.INBOX));

            retval[1] = UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(
                access.getAccountId(),
                session,
                UnifiedINBOXAccess.DRAFTS,
                getLocalizedName(UnifiedINBOXAccess.DRAFTS));

            retval[2] = UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(
                access.getAccountId(),
                session,
                UnifiedINBOXAccess.SENT,
                getLocalizedName(UnifiedINBOXAccess.SENT));

            retval[3] = UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(
                access.getAccountId(),
                session,
                UnifiedINBOXAccess.SPAM,
                getLocalizedName(UnifiedINBOXAccess.SPAM));

            retval[4] = UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(
                access.getAccountId(),
                session,
                UnifiedINBOXAccess.TRASH,
                getLocalizedName(UnifiedINBOXAccess.TRASH));

            return retval;
        }
        if (UnifiedINBOXAccess.KNOWN_FOLDERS.contains(parentFullname)) {
            final MailAccount[] accounts;
            try {
                final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                    MailAccountStorageService.class,
                    true);
                accounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
            } catch (final ServiceException e) {
                throw new UnifiedINBOXException(e);
            } catch (final MailAccountException e) {
                throw new UnifiedINBOXException(e);
            }
            final int unifiedInboxAccountId = access.getAccountId();
            final List<MailFolder> tmp = new ArrayList<MailFolder>(8);
            for (final MailAccount mailAccount : accounts) {
                if (unifiedInboxAccountId != mailAccount.getId()) {
                    final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
                    boolean close = false;
                    try {
                        mailAccess.connect();
                        close = true;
                        final String accountFullname = UnifiedINBOXUtility.determineAccountFullname(mailAccess, parentFullname);
                        // Check if account fullname is not null
                        if (null != accountFullname) {
                            // Get mail folder
                            final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(accountFullname);
                            mailFolder.setFullname(new StringBuilder(MailFolderUtility.prepareFullname(
                                unifiedInboxAccountId,
                                parentFullname)).append(MailPath.SEPERATOR).append(
                                MailFolderUtility.prepareFullname(mailAccount.getId(), mailFolder.getFullname())).toString());
                            mailFolder.setSubfolders(false);
                            mailFolder.setSubscribedSubfolders(false);
                            mailFolder.setName(mailAccount.getName());
                            tmp.add(mailFolder);
                        }
                    } finally {
                        if (close) {
                            mailAccess.close(true);
                        }
                    }
                }
            }
            return tmp.toArray(new MailFolder[tmp.size()]);
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, parentFullname);
    }

    @Override
    public MailFolder getRootFolder() throws MailException {
        return UnifiedINBOXFolderConverter.getRootFolder();
    }

    @Override
    public void checkDefaultFolders() throws MailException {
        // Nothing to do
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_CREATION_FAILED);
    }

    @Override
    public String moveFolder(final String fullname, final String newFullname) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.MOVE_DENIED);
    }

    @Override
    public String updateFolder(final String fullname, final MailFolderDescription toUpdate) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.UPDATE_DENIED);
    }

    @Override
    public String deleteFolder(final String fullname, final boolean hardDelete) throws MailException {
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.DELETE_DENIED);
    }

    @Override
    public void clearFolder(final String fullname, final boolean hardDelete) throws MailException {
        // Shall we support clear() ? ? ?
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.CLEAR_NOT_SUPPORTED);
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullname) throws MailException {
        if (DEFAULT_FOLDER_ID.equals(fullname)) {
            return EMPTY_PATH;
        }
        if (!UnifiedINBOXAccess.KNOWN_FOLDERS.contains(fullname)) {
            throw new UnifiedINBOXException(UnifiedINBOXException.Code.FOLDER_NOT_FOUND, fullname);
        }
        return new MailFolder[] {
            UnifiedINBOXFolderConverter.getUnifiedINBOXFolder(access.getAccountId(), session, fullname, getLocalizedName(fullname)),
            UnifiedINBOXFolderConverter.getRootFolder() };
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
        return UnifiedINBOXAccess.DRAFTS;
    }

    @Override
    public String getSentFolder() throws MailException {
        return UnifiedINBOXAccess.SENT;
    }

    @Override
    public String getSpamFolder() throws MailException {
        return UnifiedINBOXAccess.SPAM;
    }

    @Override
    public String getTrashFolder() throws MailException {
        return UnifiedINBOXAccess.TRASH;
    }

    @Override
    public void releaseResources() throws UnifiedINBOXException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws MailException {
        return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
    }

    private static String getLocalizedName(final String fullname) throws UnifiedINBOXException {
        // TODO: Return real localized name
        if (UnifiedINBOXAccess.INBOX.equals(fullname)) {
            return UnifiedINBOXAccess.INBOX;
        }
        if (UnifiedINBOXAccess.DRAFTS.equals(fullname)) {
            return UnifiedINBOXAccess.DRAFTS;
        }
        if (UnifiedINBOXAccess.SENT.equals(fullname)) {
            return UnifiedINBOXAccess.SENT;
        }
        if (UnifiedINBOXAccess.SPAM.equals(fullname)) {
            return UnifiedINBOXAccess.SPAM;
        }
        if (UnifiedINBOXAccess.TRASH.equals(fullname)) {
            return UnifiedINBOXAccess.TRASH;
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.UNKNOWN_DEFAULT_FOLDER_INDEX, fullname);
    }

    private static String startsWithKnownFullname(final String fullname) {
        for (final Iterator<String> iter = UnifiedINBOXAccess.KNOWN_FOLDERS.iterator(); iter.hasNext();) {
            final String knownFullname = iter.next();
            if (fullname.startsWith(knownFullname)) {
                // Cut off starting known fullname AND separator character
                return fullname.substring(knownFullname.length() + 1);
            }
        }
        return null;
    }
}
