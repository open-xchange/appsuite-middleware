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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.pop3.storage.mailaccount.util.Utility.prependPath2Fullname;
import gnu.trove.map.TIntObjectMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.mail.Message;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageMimeSupport;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.dataobjects.MimeRawSource;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.FullnameUIDPair;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.session.Session;

/**
 * {@link MailAccountPOP3MessageStorage} - POP3 storage message storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountPOP3MessageStorage implements ISimplifiedThreadStructure, IMailMessageStorage, IMailMessageStorageMimeSupport {

    private final IMailMessageStorage delegatee;
    private final MailAccountPOP3Storage storage;
    private final int pop3AccountId;
    private final Session session;
    private MailAccountPOP3FolderStorage folderStorage;
    private final String path;
    private final char separator;
    private final POP3StorageUIDLMap uidlMap;
    private final POP3StorageTrashContainer trashContainer;
    private MailAccount mailAccount;

    MailAccountPOP3MessageStorage(final IMailMessageStorage delegatee, final MailAccountPOP3Storage storage, final int pop3AccountId, final Session session) throws OXException {
        super();
        this.session = session;
        this.pop3AccountId = pop3AccountId;
        this.delegatee = delegatee;
        this.storage = storage;
        folderStorage = (MailAccountPOP3FolderStorage) storage.getFolderStorage();
        path = storage.getPath();
        separator = storage.getSeparator();
        uidlMap = storage.getUIDLMap();
        trashContainer = storage.getTrashContainer();
    }

    private MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                final MailAccountFacade mailAccountFacade = POP3ServiceRegistry.getServiceRegistry().getService(MailAccountFacade.class,
                    true);
                mailAccount = mailAccountFacade.getMailAccount(pop3AccountId, session.getUserId(), session.getContextId());
            } catch (final OXException e) {
                throw e;
            }
        }
        return mailAccount;
    }

    private MailAccountPOP3FolderStorage getFolderStorage() throws OXException {
        if (null == folderStorage) {
            folderStorage = (MailAccountPOP3FolderStorage) storage.getFolderStorage();
        }
        return folderStorage;
    }

    @Override
    public boolean isMimeSupported() throws OXException {
        return ((delegatee instanceof IMailMessageStorageMimeSupport) && ((IMailMessageStorageMimeSupport) delegatee).isMimeSupported());
    }

    @Override
    public String[] appendMimeMessages(String destFolder, Message[] msgs) throws OXException {
        if (delegatee instanceof IMailMessageStorageMimeSupport) {
            final IMailMessageStorageMimeSupport streamSupport = (IMailMessageStorageMimeSupport) delegatee;
            if (streamSupport.isMimeSupported()) {
                return streamSupport.appendMimeMessages(destFolder, msgs);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public Message getMimeMessage(String fullName, String id, boolean markSeen) throws OXException {
        if (delegatee instanceof IMailMessageStorageMimeSupport) {
            final IMailMessageStorageMimeSupport streamSupport = (IMailMessageStorageMimeSupport) delegatee;
            if (streamSupport.isMimeSupported()) {
                return streamSupport.getMimeMessage(fullName, id, markSeen);
            }
        }
        throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
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
     * @throws OXException If append operation fails
     */
    public void appendPOP3Messages(final Message[] pop3Messages, final TIntObjectMap<String> seqnum2uidl) throws OXException {
        if (null == pop3Messages || 0 == pop3Messages.length) {
            return;
        }
        final String[] uidls = new String[pop3Messages.length];
        for (int i = 0; i < uidls.length; i++) {
            final Message msg = pop3Messages[i];
            if (null != msg) {
                uidls[i] = seqnum2uidl.get(msg.getMessageNumber());
            }
        }
        final String[] uids;
        if ((delegatee instanceof IMailMessageStorageMimeSupport)) {
            final IMailMessageStorageMimeSupport streamSupport = (IMailMessageStorageMimeSupport) delegatee;
            if (streamSupport.isMimeSupported()) {
                uids = streamSupport.appendMimeMessages(getRealFullname("INBOX"), pop3Messages);
            } else {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }
        } else {
            throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
        }
        /*
         * Add mappings
         */
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uids.length];
        for (int i = 0; i < pairs.length; i++) {
            final String mailId = uids[i];
            if (null != mailId) {
                pairs[i] = FullnameUIDPair.newINBOXInstance(mailId);
            }
        }
        uidlMap.addMappings(uidls, pairs);
    }

    /**
     * Appends specified POP3 messages fetched from POP3 account to this storage's INBOX folder.
     * <p>
     * The new UIDLs are automatically added to used {@link POP3StorageUIDLMap UIDL map}.
     *
     * @param pop3Messages The POP3 messages
     * @throws OXException If append operation fails
     */
    public void appendPOP3Messages(final MailMessage[] pop3Messages) throws OXException {
        if (null == pop3Messages || 0 == pop3Messages.length) {
            return;
        }
        /*
         * This method has a special meaning since it's called during synchronization of actual POP3 content with storage content
         */
        final MailMessage[] pop3Msgs;
        {
            final int length = pop3Messages.length;
            final List<MailMessage> tmp = new LinkedList<MailMessage>();
            boolean failee = false;
            for (int i = 0; i < length; i++) {
                final MailMessage m = pop3Messages[i];
                if (null != m && null != m.getMailId()) {
                    tmp.add(m);
                } else {
                    failee = true;
                }
            }
            pop3Msgs = failee ? tmp.toArray(new MailMessage[tmp.size()]) : pop3Messages;
        }
        if (null == pop3Msgs || 0 == pop3Msgs.length) {
            return;
        }
        final String[] uidls = new String[pop3Msgs.length];
        for (int i = 0; i < uidls.length; i++) {
            final MailMessage mailMessage = pop3Msgs[i];
            if (null != mailMessage) {
                uidls[i] = mailMessage.getMailId();
            }
        }
        /*
         * Append to mail account storage
         */
        final String[] uids;
        if ((pop3Msgs[0] instanceof MimeRawSource) && (delegatee instanceof IMailMessageStorageMimeSupport)) {
            final IMailMessageStorageMimeSupport streamSupport = (IMailMessageStorageMimeSupport) delegatee;
            if (streamSupport.isMimeSupported()) {
                final List<Message> tmp = new LinkedList<Message>();
                for (final MailMessage pop3Message : pop3Msgs) {
                    tmp.add((Message) ((MimeRawSource) pop3Message).getPart());
                }
                uids = streamSupport.appendMimeMessages(getRealFullname("INBOX"), tmp.toArray(new Message[0]));
            } else {
                uids = delegatee.appendMessages(getRealFullname("INBOX"), pop3Msgs);
            }
        } else {
            uids = delegatee.appendMessages(getRealFullname("INBOX"), pop3Msgs);
        }
        /*
         * Add mappings
         */
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uids.length];
        for (int i = 0; i < pairs.length; i++) {
            final String mailId = uids[i];
            if (null != mailId) {
                pairs[i] = FullnameUIDPair.newINBOXInstance(mailId);
            }
        }
        uidlMap.addMappings(uidls, pairs);
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIDs, final boolean fast) throws OXException {
        return delegatee.copyMessages(getRealFullname(sourceFolder), getRealFullname(destFolder), mailIDs, fast);
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIDs, final boolean hardDelete) throws OXException {
        if (hardDelete || performHardDelete(folder)) {
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
            moveMessages(folder, getFolderStorage().getTrashFolder(), mailIDs, true);
        }
    }

    private boolean performHardDelete(final String fullname) throws OXException {
        final String trashFullname = getFolderStorage().getTrashFolder();
        if (fullname.startsWith(trashFullname)) {
            // A subfolder of trash folder
            return true;
        }
        return !getFolderStorage().getFolder(trashFullname).isHoldsFolders();
    }

    @Override
    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        final MailMessage[] mails = delegatee.getAllMessages(getRealFullname(folder), indexRange, sortField, order, fields);
        for (final MailMessage mailMessage : mails) {
            setFolderAndAccount(folder, mailMessage);
        }
        return mails;
    }

    @Override
    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws OXException {
        return delegatee.getAttachment(getRealFullname(folder), mailId, sequenceId);
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        return delegatee.getImageAttachment(getRealFullname(folder), mailId, contentId);
    }

    @Override
    public String[] getPrimaryContents(final String folder, final String[] mailIds) throws OXException {
        return delegatee.getPrimaryContents(folder, mailIds);
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        final MailMessage mail = delegatee.getMessage(getRealFullname(folder), mailId, markSeen);
        if (null != mail && mail.containsFolder() && null != mail.getFolder()) {
            mail.setFolder(folder);
            if (mail.containsAccountName()) {
                setAccountInfo(mail);
            }
        }
        return mail;
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        final MailMessage[] mails = delegatee.getMessages(getRealFullname(folder), mailIds, fields);
        for (final MailMessage mailMessage : mails) {
            setFolderAndAccount(folder, mailMessage);
        }
        return mails;
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        final MailMessage[] mails = delegatee.getThreadSortedMessages(
            getRealFullname(folder),
            indexRange,
            sortField,
            order,
            searchTerm,
            fields);
        for (final MailMessage mailMessage : mails) {
            setFolderAndAccount(folder, mailMessage);
        }
        return mails;
    }

    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        final MailMessage[] mails = delegatee.getUnreadMessages(getRealFullname(folder), sortField, order, fields, limit);
        for (final MailMessage mailMessage : mails) {
            setFolderAndAccount(folder, mailMessage);
        }
        return mails;
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIDs, final boolean fast) throws OXException {
        // Move to destination folder
        final String realSourceFullname = getRealFullname(sourceFolder);
        final String realDestFullname = getRealFullname(destFolder);
        // Invoke with fast=false to be able to update UIDLs
        final String[] newMailIds = delegatee.moveMessages(realSourceFullname, realDestFullname, mailIDs, false);
        // Update UIDL map
        if (storage.isDeleteWriteThrough() && getFolderStorage().getTrashFolder().equals(destFolder)) {
            // Look-up UIDLs for mail IDs
            final Set<String> cleanedUIDLs = getContainedUIDLs(sourceFolder, mailIDs);
            if (!cleanedUIDLs.isEmpty()) {
                // Clean from UIDL map
                uidlMap.deleteUIDLMappings(cleanedUIDLs.toArray(new String[cleanedUIDLs.size()]));
                trashContainer.addAllUIDL(cleanedUIDLs);
            }
        } else {
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
        }
        // Return
        return fast ? new String[0] : newMailIds;
    }

    @Override
    public void releaseResources() throws OXException {
        delegatee.releaseResources();
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws OXException {
        return delegatee.saveDraft(getRealFullname(draftFullname), draftMail);
    }

    @Override
    public int getUnreadCount(final String folder, final SearchTerm<?> searchTerm) throws OXException {
        return delegatee.getUnreadCount(folder, searchTerm);
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        final MailMessage[] mails = delegatee.searchMessages(getRealFullname(folder), indexRange, sortField, order, searchTerm, fields);
        for (final MailMessage mailMessage : mails) {
            setFolderAndAccount(folder, mailMessage);
        }
        return mails;
    }

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(final String folder, final boolean includeSent, final boolean cache, final IndexRange indexRange, final long max, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        if (!(delegatee instanceof ISimplifiedThreadStructure)) {
            throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
        }
        final List<List<MailMessage>> messagesList = ((ISimplifiedThreadStructure) delegatee).getThreadSortedMessages(getRealFullname(folder), includeSent, cache, indexRange, max, sortField, order, fields);
        for (final List<MailMessage> messages : messagesList) {
            for (final MailMessage mailMessage : messages) {
                setFolderAndAccount(folder, mailMessage);
            }
        }
        return messagesList;
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        delegatee.updateMessageColorLabel(getRealFullname(folder), mailIds, colorLabel);
    }

    @Override
    public void updateMessageUserFlags(final String folder, final String[] mailIds, final String[] flags, final boolean set) throws OXException {
        delegatee.updateMessageUserFlags(folder, mailIds, flags, set);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        delegatee.updateMessageFlags(getRealFullname(folder), mailIds, flags, set);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, String[] userFlags, final boolean set) throws OXException {
        delegatee.updateMessageFlags(getRealFullname(folder), mailIds, flags, userFlags, set);
    }

    @Override
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException {
        final MailMessage[] mails = delegatee.getDeletedMessages(folder, fields);
        for (final MailMessage mailMessage : mails) {
            setFolderAndAccount(folder, mailMessage);
        }
        return mails;

    }

    @Override
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException {
        final MailMessage[] mails = delegatee.getNewAndModifiedMessages(folder, fields);
        for (final MailMessage mailMessage : mails) {
            setFolderAndAccount(folder, mailMessage);
        }
        return mails;
    }

    private String getRealFullname(final String fullname) {
        return prependPath2Fullname(path, separator, fullname);
    }

    private Set<String> getContainedUIDLs(final String virtualFullname, final String[] mailIDs) throws OXException {
        final Set<String> retval = new HashSet<String>(mailIDs.length);
        for (int i = 0; i < mailIDs.length; i++) {
            final String uidl = uidlMap.getUIDL(new FullnameUIDPair(virtualFullname, mailIDs[i]));
            if (null != uidl) {
                retval.add(uidl);
            }
        }
        return retval;
    }

    private void setFolderAndAccount(final String folder, final MailMessage mailMessage) throws OXException {
        if (null != mailMessage) {
            if (mailMessage.containsFolder()) {
                mailMessage.setFolder(folder);
            }
            if (mailMessage.containsAccountName()) {
                setAccountInfo(mailMessage);
            }
        }
    }

    /**
     * Sets account ID and name in given instance of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instance
     * @return The given instance of {@link MailMessage} with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private MailMessage setAccountInfo(final MailMessage mailMessage) throws OXException {
        if (null == mailMessage) {
            return mailMessage;
        }
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        mailMessage.setAccountId(id);
        mailMessage.setAccountName(name);
        return mailMessage;
    }

}
