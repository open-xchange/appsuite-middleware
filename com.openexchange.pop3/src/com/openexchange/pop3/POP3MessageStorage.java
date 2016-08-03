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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.api.MailMessageStorage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.session.Session;

/**
 * {@link POP3MessageStorage} - The POP3 message storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3MessageStorage extends MailMessageStorage implements ISimplifiedThreadStructure {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1467121647337217270L;

    /*-
     * Members
     */

    private final IMailMessageStorage pop3MessageStorage;

    private final int accountId;

    private final Session session;

    private MailAccount mailAccount;

    /**
     * Initializes a new {@link POP3MessageStorage}.
     *
     * @param pop3Storage The POP3 storage
     * @param accountId The account ID
     * @param session The session
     * @throws OXException If initialization fails
     */
    public POP3MessageStorage(final POP3Storage pop3Storage, final int accountId, final Session session) throws OXException {
        super();
        pop3MessageStorage = pop3Storage.getMessageStorage();
        this.accountId = accountId;
        this.session = session;
    }

    private MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                final MailAccountStorageService storageService = POP3ServiceRegistry.getServiceRegistry().getService(
                    MailAccountStorageService.class,
                    true);
                mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (final OXException e) {
                throw e;
            }
        }
        return mailAccount;
    }

    @Override
    public void releaseResources() throws OXException {
        pop3MessageStorage.releaseResources();
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        return pop3MessageStorage.appendMessages(destFolder, msgs);
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        return pop3MessageStorage.copyMessages(sourceFolder, destFolder, mailIds, fast);
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        pop3MessageStorage.deleteMessages(folder, mailIds, hardDelete);
    }

    @Override
    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        final MailMessage[] mails = pop3MessageStorage.getAllMessages(folder, indexRange, sortField, order, fields);
        /*
         * Check for account name in used fields
         */
        final MailFields mailFields = new MailFields(fields);
        final MailField sort = MailField.toField(sortField.getListField());
        if (null != sort) {
            mailFields.add(sort);
        }
        if (mailFields.contains(MailField.ACCOUNT_NAME) || mailFields.contains(MailField.FULL)) {
            setAccountInfo(mails);
        }
        return mails;
    }

    @Override
    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws OXException {
        return pop3MessageStorage.getAttachment(folder, mailId, sequenceId);
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        return pop3MessageStorage.getImageAttachment(folder, mailId, contentId);
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        final MailMessage mail = pop3MessageStorage.getMessage(folder, mailId, markSeen);
        setAccountInfo(mail);
        return mail;
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        final MailMessage[] mails = pop3MessageStorage.getMessages(folder, mailIds, fields);
        final MailFields mailFields = new MailFields(fields);
        if (mailFields.contains(MailField.ACCOUNT_NAME) || mailFields.contains(MailField.FULL)) {
            setAccountInfo(mails);
        }
        return mails;
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        final MailMessage[] mails = pop3MessageStorage.getThreadSortedMessages(folder, indexRange, sortField, order, searchTerm, fields);
        final MailFields mailFields = new MailFields(fields);
        if (mailFields.contains(MailField.ACCOUNT_NAME) || mailFields.contains(MailField.FULL)) {
            setAccountInfo(mails);
        }
        return mails;
    }

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(final String folder, final boolean includeSent, final boolean cache, final IndexRange indexRange, final long max, final MailSortField sortField, final OrderDirection order, final MailField[] fields, SearchTerm<?> searchTerm) throws OXException {
        if (!(pop3MessageStorage instanceof ISimplifiedThreadStructure)) {
            throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
        }
        return ((ISimplifiedThreadStructure) pop3MessageStorage).getThreadSortedMessages(folder, includeSent, cache, indexRange, max, sortField, order, fields, searchTerm);
    }

    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        final MailMessage[] mails = pop3MessageStorage.getUnreadMessages(folder, sortField, order, fields, limit);
        /*
         * Check for account name in used fields
         */
        final MailFields mailFields = new MailFields(fields);
        final MailField sort = MailField.toField(sortField.getListField());
        if (null != sort) {
            mailFields.add(sort);
        }
        if (mailFields.contains(MailField.ACCOUNT_NAME)) {
            setAccountInfo(mails);
        }
        return mails;
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        return pop3MessageStorage.moveMessages(sourceFolder, destFolder, mailIds, fast);
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws OXException {
        return pop3MessageStorage.saveDraft(draftFullname, draftMail);
    }

    @Override
    public int getUnreadCount(final String folder, final SearchTerm<?> searchTerm) throws OXException {
        return pop3MessageStorage.getUnreadCount(folder, searchTerm);
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        final MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE : sortField;
        final MailMessage[] mails = pop3MessageStorage.searchMessages(folder, indexRange, effectiveSortField, order, searchTerm, fields);
        /*
         * Check for account name in used fields
         */
        final Set<MailField> set = EnumSet.copyOf(Arrays.asList(fields));
        final MailField sort = MailField.toField(effectiveSortField.getListField());
        if (null != sort) {
            set.add(sort);
        }
        if (null != searchTerm) {
            searchTerm.addMailField(set);
        }
        if (set.contains(MailField.ACCOUNT_NAME)) {
            setAccountInfo(mails);
        }
        return mails;
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        pop3MessageStorage.updateMessageColorLabel(folder, mailIds, colorLabel);
    }

    @Override
    public void updateMessageUserFlags(final String folder, final String[] mailIds, final String[] flags, final boolean set) throws OXException {
        pop3MessageStorage.updateMessageUserFlags(folder, mailIds, flags, set);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        pop3MessageStorage.updateMessageFlags(folder, mailIds, flags, set);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, String[] userFlags, final boolean set) throws OXException {
        pop3MessageStorage.updateMessageFlags(folder, mailIds, flags, userFlags, set);
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @throws OXException If mail account cannot be obtained
     */
    private void setAccountInfo(final MailMessage[] mailMessages) throws OXException {
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        for (int i = 0; i < mailMessages.length; i++) {
            final MailMessage mailMessage = mailMessages[i];
            if (null != mailMessage) {
                mailMessage.setAccountId(id);
                mailMessage.setAccountName(name);
            }
        }
    }

    /**
     * Sets account ID and name in given instance of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instance
     * @throws OXException If mail account cannot be obtained
     */
    private void setAccountInfo(final MailMessage mailMessage) throws OXException {
        if (null != mailMessage) {
            final MailAccount account = getMailAccount();
            final String name = account.getName();
            final int id = account.getId();
            mailMessage.setAccountId(id);
            mailMessage.setAccountName(name);
        }
    }
}
