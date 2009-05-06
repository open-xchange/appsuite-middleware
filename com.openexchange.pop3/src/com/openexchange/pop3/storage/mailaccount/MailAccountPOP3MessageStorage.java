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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.pop3.storage.mailaccount.util.Utility.prependPath2Fullname;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.storage.FullnameUIDPair;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.session.Session;

/**
 * {@link MailAccountPOP3MessageStorage} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountPOP3MessageStorage implements IMailMessageStorage {

    private final IMailMessageStorage delegatee;

    private final MailAccountPOP3Storage storage;

    private MailAccountPOP3FolderStorage folderStorage;

    private final Session session;

    private final int accountId;

    private final String path;

    private final char separator;

    private final POP3StorageUIDLMap uidlMap;

    private final POP3StorageTrashContainer trashContainer;

    MailAccountPOP3MessageStorage(final IMailMessageStorage delegatee, final MailAccountPOP3Storage storage, final POP3Access pop3Access) throws MailException {
        super();
        this.delegatee = delegatee;
        this.storage = storage;
        this.folderStorage = (MailAccountPOP3FolderStorage) storage.getFolderStorage();
        this.path = storage.getPath();
        this.separator = storage.getSeparator();
        this.session = pop3Access.getSession();
        this.accountId = pop3Access.getAccountId();
        this.uidlMap = storage.getUIDLMap();
        this.trashContainer = storage.getTrashContainer();
    }

    private MailAccountPOP3FolderStorage getFolderStorage() throws MailException {
        if (null == folderStorage) {
            folderStorage = (MailAccountPOP3FolderStorage) storage.getFolderStorage();
        }
        return folderStorage;
    }

    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws MailException {
        /*
         * Append to mail account storage and return storage's IDs, NOT UIDLS!!!
         */
        return delegatee.appendMessages(getRealFullname(destFolder), msgs);
    }

    /**
     * Appends specified POP3 messages fetched from POP3 account to this storage's INBOX folder.
     * <p>
     * The new UIDLs are automatically added to used {@link POP3StorageUIDLMap UIDL map}.
     * 
     * @param pop3Messages The POP3 messages
     * @return The
     * @throws MailException
     */
    public void appendPOP3Messages(final MailMessage[] pop3Messages) throws MailException {
        /*
         * This method has a special meaning since it's called during synchronization of actual POP3 content with storage content
         */
        final String[] uidls = new String[pop3Messages.length];
        for (int i = 0; i < uidls.length; i++) {
            uidls[i] = pop3Messages[i].getMailId();
        }
        /*
         * Append to mail account storage
         */
        final String[] uids = delegatee.appendMessages(getRealFullname("INBOX"), pop3Messages);
        /*
         * Add mappings
         */
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uidls.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = FullnameUIDPair.newINBOXInstance(uids[i]);
        }
        uidlMap.addMappings(uidls, pairs);
    }

    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIDs, final boolean fast) throws MailException {
        return delegatee.copyMessages(getRealFullname(sourceFolder), getRealFullname(destFolder), mailIDs, fast);
    }

    public void deleteMessages(final String folder, final String[] mailIDs, final boolean hardDelete) throws MailException {
        if (hardDelete) {
            // Clean from storage
            delegatee.deleteMessages(getRealFullname(folder), mailIDs, true);
            // Look-up UIDLs for mail IDs
            final Set<String> cleanedUIDLs = getContainedUIDLs(folder, mailIDs);
            if (!cleanedUIDLs.isEmpty()) {
                // Clean from UIDL map
                uidlMap.deleteUIDLMappings(cleanedUIDLs.toArray(new String[cleanedUIDLs.size()]));
                trashContainer.addAllUIDL(cleanedUIDLs);
            }
        } else {
            // Move to trash
            final String realFullname = getRealFullname(folder);
            final String trashFullname = getFolderStorage().getTrashFolder();
            final String realTrashFullname = getRealFullname(trashFullname);

            final String[] newMailIds = delegatee.moveMessages(realFullname, realTrashFullname, mailIDs, false);

            // Update UIDL map
            final List<String> uidls = new ArrayList<String>(mailIDs.length);
            final List<FullnameUIDPair> pairs = new ArrayList<FullnameUIDPair>(mailIDs.length);
            for (int i = 0; i < mailIDs.length; i++) {
                final String mailID = mailIDs[i];
                final String uidl = uidlMap.getUIDL(new FullnameUIDPair(folder, mailID));
                if (null != uidl) {
                    uidls.add(uidl);
                    pairs.add(new FullnameUIDPair(trashFullname, newMailIds[i]));
                }
            }
            uidlMap.addMappings(uidls.toArray(new String[uidls.size()]), pairs.toArray(new FullnameUIDPair[pairs.size()]));
        }
    }

    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.getAllMessages(getRealFullname(folder), indexRange, sortField, order, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
        }
        return mails;
    }

    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws MailException {
        return delegatee.getAttachment(getRealFullname(folder), mailId, sequenceId);
    }

    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws MailException {
        return delegatee.getImageAttachment(getRealFullname(folder), mailId, contentId);
    }

    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws MailException {
        final MailMessage mail = delegatee.getMessage(getRealFullname(folder), mailId, markSeen);
        if (mail.containsFolder() && null != mail.getFolder()) {
            mail.setFolder(folder);
        }
        return mail;
    }

    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.getMessages(getRealFullname(folder), mailIds, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
        }
        return mails;
    }

    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.getThreadSortedMessages(getRealFullname(folder), indexRange, searchTerm, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
        }
        return mails;
    }

    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        final MailMessage[] mails = delegatee.getUnreadMessages(getRealFullname(folder), sortField, order, fields, limit);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
        }
        return mails;
    }

    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIDs, final boolean fast) throws MailException {
        // Move to destination folder
        final String realSourceFullname = getRealFullname(sourceFolder);
        final String realDestFullname = getRealFullname(destFolder);
        // Invoke with fast=false to be able to update UIDLs
        final String[] newMailIds = delegatee.moveMessages(realSourceFullname, realDestFullname, mailIDs, false);
        // Update UIDL map
        final List<String> uidls = new ArrayList<String>(mailIDs.length);
        final List<FullnameUIDPair> pairs = new ArrayList<FullnameUIDPair>(mailIDs.length);
        for (int i = 0; i < mailIDs.length; i++) {
            final String mailID = mailIDs[i];
            final String uidl = uidlMap.getUIDL(new FullnameUIDPair(sourceFolder, mailID));
            if (null != uidl) {
                uidls.add(uidl);
                pairs.add(new FullnameUIDPair(destFolder, newMailIds[i]));
            }
        }
        uidlMap.addMappings(uidls.toArray(new String[uidls.size()]), pairs.toArray(new FullnameUIDPair[pairs.size()]));
        // Return
        return fast ? new String[0] : newMailIds;
    }

    public void releaseResources() throws MailException {
        delegatee.releaseResources();
    }

    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws MailException {
        return delegatee.saveDraft(getRealFullname(draftFullname), draftMail);
    }

    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.searchMessages(getRealFullname(folder), indexRange, sortField, order, searchTerm, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
        }
        return mails;
    }

    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws MailException {
        delegatee.updateMessageColorLabel(getRealFullname(folder), mailIds, colorLabel);
    }

    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws MailException {
        delegatee.updateMessageFlags(getRealFullname(folder), mailIds, flags, set);
    }

    private String getRealFullname(final String fullname) {
        return prependPath2Fullname(path, separator, fullname);
    }

    private Set<String> getContainedUIDLs(final String virtualFullname, final String[] mailIDs) throws MailException {
        final Set<String> retval = new HashSet<String>(mailIDs.length);
        for (int i = 0; i < mailIDs.length; i++) {
            final String uidl = uidlMap.getUIDL(new FullnameUIDPair(virtualFullname, mailIDs[i]));
            if (null != uidl) {
                retval.add(uidl);
            }
        }
        return retval;
    }
}
