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

import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3Exception;
import com.openexchange.pop3.storage.FullnameUIDPair;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.pop3.storage.mailaccount.util.Utility;
import com.openexchange.session.Session;

/**
 * {@link MailAccountPOP3MessageStorage} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountPOP3MessageStorage implements IMailMessageStorage {

    private final IMailMessageStorage delegatee;

    private final Session session;

    private final int accountId;

    private final String path;

    private final char separator;

    private final POP3StorageUIDLMap uidlMap;

    MailAccountPOP3MessageStorage(final IMailMessageStorage delegatee, final POP3Access pop3Access, final String path, final char separator, final POP3StorageUIDLMap uidlMap) {
        super();
        this.delegatee = delegatee;
        this.path = path;
        this.separator = separator;
        this.session = pop3Access.getSession();
        this.accountId = pop3Access.getAccountId();
        this.uidlMap = uidlMap;
    }

    private String[] getMailIDs(final String fullname, final String[] uidls) throws POP3Exception {
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

    private String getMailID(final String fullname, final String uidl) throws POP3Exception {
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
        final String[] uids = delegatee.appendMessages(Utility.prependPath2Fullname(path, separator, destFolder), msgs);
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uidls.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new FullnameUIDPair(destFolder, uids[i]);
        }
        uidlMap.addMappings(uidls, pairs);
        return uidls;
    }

    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] uidls, final boolean fast) throws MailException {
        final String[] mailIds = getMailIDs(destFolder, uidls);
        return delegatee.copyMessages(Utility.prependPath2Fullname(path, separator, sourceFolder), Utility.prependPath2Fullname(
            path,
            separator,
            destFolder), mailIds, fast);
    }

    public void deleteMessages(final String folder, final String[] uidls, final boolean hardDelete) throws MailException {
        final String[] mailIds = getMailIDs(destFolder, uidls);
        delegatee.deleteMessages(
            Utility.prependPath2Fullname(path, separator, folder),
            Utility.getRealIDs(mailIds, accountId, session),
            hardDelete);
    }

    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws MailException {
        // TODO: Map UIDL
        final MailMessage[] mails = delegatee.getAllMessages(
            Utility.prependPath2Fullname(path, separator, folder),
            indexRange,
            sortField,
            order,
            fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(Utility.stripPathFromFullname(path, mailMessage.getFolder()));
            }
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(Utility.getUIDL(mailMessage.getMailId(), accountId, session));
            }
        }
        return mails;
    }

    public MailPart getAttachment(final String folder, final String uidl, final String sequenceId) throws MailException {
        return delegatee.getAttachment(
            Utility.prependPath2Fullname(path, separator, folder),
            getMailID(folder, uidl),
            sequenceId);
    }

    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws MailException {
        return delegatee.getImageAttachment(Utility.prependPath2Fullname(path, separator, folder), Utility.getRealID(
            mailId,
            accountId,
            session), contentId);
    }

    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws MailException {
        final MailMessage mail = delegatee.getMessage(folder, mailId, markSeen);
        if (mail.containsFolder() && null != mail.getFolder()) {
            mail.setFolder(Utility.stripPathFromFullname(path, mail.getFolder()));
        }
        if (null != mail.getMailId()) {
            mail.setMailId(Utility.getUIDL(mail.getMailId(), accountId, session));
        }
        return mail;
    }

    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.getMessages(Utility.prependPath2Fullname(path, separator, folder), mailIds, fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(Utility.stripPathFromFullname(path, mailMessage.getFolder()));
            }
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(Utility.getUIDL(mailMessage.getMailId(), accountId, session));
            }
        }
        return mails;
    }

    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        final MailMessage[] mails = delegatee.getThreadSortedMessages(
            Utility.prependPath2Fullname(path, separator, folder),
            indexRange,
            searchTerm,
            fields);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(Utility.stripPathFromFullname(path, mailMessage.getFolder()));
            }
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(Utility.getUIDL(mailMessage.getMailId(), accountId, session));
            }
        }
        return mails;
    }

    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws MailException {
        // TODO: Map UIDL
        final MailMessage[] mails = delegatee.getUnreadMessages(
            Utility.prependPath2Fullname(path, separator, folder),
            sortField,
            order,
            fields,
            limit);
        for (final MailMessage mailMessage : mails) {
            if (mailMessage.containsFolder() && null != mailMessage.getFolder()) {
                mailMessage.setFolder(Utility.stripPathFromFullname(path, mailMessage.getFolder()));
            }
            if (null != mailMessage.getMailId()) {
                mailMessage.setMailId(Utility.getUIDL(mailMessage.getMailId(), accountId, session));
            }
        }
        return mails;
    }

    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws MailException {
        // TODO: Map UIDL
        return delegatee.moveMessages(Utility.prependPath2Fullname(path, separator, sourceFolder), Utility.prependPath2Fullname(
            path,
            separator,
            destFolder), mailIds, fast);
    }

    public void releaseResources() throws MailException {
        delegatee.releaseResources();
    }

    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws MailException {
        // TODO: Map UIDL
        return delegatee.saveDraft(Utility.prependPath2Fullname(path, separator, draftFullname), draftMail);
    }

    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws MailException {
        // TODO: Map UIDL
        return delegatee.searchMessages(
            Utility.prependPath2Fullname(path, separator, folder),
            indexRange,
            sortField,
            order,
            searchTerm,
            fields);
    }

    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws MailException {
        // TODO: Map UIDL
        delegatee.updateMessageColorLabel(Utility.prependPath2Fullname(path, separator, folder), mailIds, colorLabel);
    }

    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws MailException {
        // TODO: Map UIDL
        delegatee.updateMessageFlags(Utility.prependPath2Fullname(path, separator, folder), mailIds, flags, set);
    }

}
