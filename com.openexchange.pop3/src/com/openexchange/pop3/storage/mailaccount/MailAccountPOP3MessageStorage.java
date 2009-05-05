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
import java.util.Arrays;
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
import com.openexchange.pop3.POP3Exception;
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

    private final MailAccountPOP3FolderStorage folderStorage;

    private final Session session;

    private final int accountId;

    private final String path;

    private final char separator;

    private final POP3StorageUIDLMap uidlMap;

    private final POP3StorageTrashContainer trashContainer;

    MailAccountPOP3MessageStorage(final IMailMessageStorage delegatee, final MailAccountPOP3Storage storage) throws MailException {
        super();
        this.delegatee = delegatee;
        this.folderStorage = (MailAccountPOP3FolderStorage) storage.getFolderStorage();
        this.path = folderStorage.getPath();
        this.separator = folderStorage.getSeparator();
        this.session = folderStorage.getSession();
        this.accountId = folderStorage.getAccountId();
        this.uidlMap = storage.getUIDLMap();
        this.trashContainer = storage.getTrashContainer();
    }

    private String[] getMailIDs(final String fullname, final String[] uidls) throws MailException {
        final String[] mailIds = new String[uidls.length];
        final FullnameUIDPair[] pairs = uidlMap.getFullnameUIDPairs(uidls);
        for (int i = 0; i < mailIds.length; i++) {
            final FullnameUIDPair pair = pairs[i];
            if (!fullname.equals(pair.getFullname())) {
                throw new POP3Exception(POP3Exception.Code.UIDL_INCONSISTENCY);
            }
            mailIds[i] = pair.getMailId();
        }
        return mailIds;
    }

    private String getMailID(final String fullname, final String uidl) throws MailException {
        final FullnameUIDPair pair = uidlMap.getFullnameUIDPair(uidl);
        if (!fullname.equals(pair.getFullname())) {
            throw new POP3Exception(POP3Exception.Code.UIDL_INCONSISTENCY);
        }
        return pair.getMailId();
    }

    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws MailException {
        /*
         * This method has a special meaning since it's called during synchronization of actual POP3 content with storage content
         */
        final String[] uidls = new String[msgs.length];
        for (int i = 0; i < uidls.length; i++) {
            uidls[i] = msgs[i].getMailId();
        }
        /*
         * Append to mail account storage
         */
        final String[] uids = delegatee.appendMessages(getRealFullname(destFolder), msgs);
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uidls.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new FullnameUIDPair(destFolder, uids[i]);
        }
        uidlMap.addMappings(uidls, pairs);
        return uidls;
    }

    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] uidls, final boolean fast) throws MailException {
        /*-
         * If we allow copy operation, multiple messages of the same POP3 message would exist in storage.
         * Then no unique mapping 'UIDL <-> fullname-UID-pair' is possible.
         */
        throw new POP3Exception(POP3Exception.Code.COPY_MSGS_DENIED);
    }

    public void deleteMessages(final String folder, final String[] uidls, final boolean hardDelete) throws MailException {
        final String[] mailIds = getMailIDs(folder, uidls);
        if (hardDelete) {
            // Clean from storage
            delegatee.deleteMessages(getRealFullname(folder), mailIds, hardDelete);
            uidlMap.deleteUIDLMappings(uidls);
            trashContainer.addAllUIDL(Arrays.asList(uidls));
            // TODO: Remember cleansed UIDLs for later "write-through" to POP3 account if option enabled
        } else {
            // Move to trash
            final String realFullname = getRealFullname(folder);
            final String trashFullname = folderStorage.getTrashFolder();
            final String realTrashFullname = getRealFullname(trashFullname);

            final String[] newMailIds = delegatee.moveMessages(realFullname, realTrashFullname, mailIds, false);

            final FullnameUIDPair[] newPairs = new FullnameUIDPair[newMailIds.length];
            for (int i = 0; i < newPairs.length; i++) {
                newPairs[i] = new FullnameUIDPair(folder, newMailIds[i]);
            }
            uidlMap.addMappings(uidls, newPairs);
        }
    }

    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.getAllMessages(getRealFullname(folder), indexRange, sortField, order, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(uidlMap.getUIDL(new FullnameUIDPair(folder, mailMessage.getMailId())));
            }
        }
        return mails;
    }

    public MailPart getAttachment(final String folder, final String uidl, final String sequenceId) throws MailException {
        return delegatee.getAttachment(getRealFullname(folder), getMailID(folder, uidl), sequenceId);
    }

    public MailPart getImageAttachment(final String folder, final String uidl, final String contentId) throws MailException {
        return delegatee.getImageAttachment(getRealFullname(folder), getMailID(folder, uidl), contentId);
    }

    public MailMessage getMessage(final String folder, final String uidl, final boolean markSeen) throws MailException {
        final MailMessage mail = delegatee.getMessage(getRealFullname(folder), getMailID(folder, uidl), markSeen);
        if (mail.containsFolder() && null != mail.getFolder()) {
            mail.setFolder(folder);
        }
        if (null != mail.getMailId()) {
            mail.setMailId(uidlMap.getUIDL(new FullnameUIDPair(folder, mail.getMailId())));
        }
        return mail;
    }

    public MailMessage[] getMessages(final String folder, final String[] uidls, final MailField[] fields) throws MailException {
        final String[] mailIds = getMailIDs(folder, uidls);
        final MailMessage[] mails = delegatee.getMessages(getRealFullname(folder), mailIds, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(uidlMap.getUIDL(new FullnameUIDPair(folder, mailMessage.getMailId())));
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
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(uidlMap.getUIDL(new FullnameUIDPair(folder, mailMessage.getMailId())));
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
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(uidlMap.getUIDL(new FullnameUIDPair(folder, mailMessage.getMailId())));
            }
        }
        return mails;
    }

    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] uidls, final boolean fast) throws MailException {
        final String[] mailIds = getMailIDs(sourceFolder, uidls);
        final String[] newMailIds = delegatee.moveMessages(
            getRealFullname(sourceFolder),
            prependPath2Fullname(path, separator, destFolder),
            mailIds,
            fast);
        final FullnameUIDPair[] newPairs = new FullnameUIDPair[newMailIds.length];
        for (int i = 0; i < newPairs.length; i++) {
            newPairs[i] = new FullnameUIDPair(destFolder, newMailIds[i]);
        }
        uidlMap.addMappings(uidls, newPairs);
        /*
         * UIDLs never change through move operation
         */
        return uidls;
    }

    public void releaseResources() throws MailException {
        delegatee.releaseResources();
    }

    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws MailException {
        /*-
         * If we allow safe-draft operation, a new message is created in storage without a corresponding POP3 message.
         * Then no unique mapping 'UIDL <-> fullname-UID-pair' is possible.
         */
        throw new POP3Exception(POP3Exception.Code.DRAFTS_NOT_SUPPORTED);
    }

    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.searchMessages(getRealFullname(folder), indexRange, sortField, order, searchTerm, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(folder);
            }
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(uidlMap.getUIDL(new FullnameUIDPair(folder, mailMessage.getMailId())));
            }
        }
        return mails;
    }

    public void updateMessageColorLabel(final String folder, final String[] uidls, final int colorLabel) throws MailException {
        final String[] mailIds = getMailIDs(folder, uidls);
        delegatee.updateMessageColorLabel(getRealFullname(folder), mailIds, colorLabel);
    }

    public void updateMessageFlags(final String folder, final String[] uidls, final int flags, final boolean set) throws MailException {
        final String[] mailIds = getMailIDs(folder, uidls);
        delegatee.updateMessageFlags(getRealFullname(folder), mailIds, flags, set);
    }

    private String getRealFullname(final String fullname) {
        return prependPath2Fullname(path, separator, fullname);
    }
}
