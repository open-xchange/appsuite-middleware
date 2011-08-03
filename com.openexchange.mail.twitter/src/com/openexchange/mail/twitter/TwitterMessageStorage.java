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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mail.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.enhanced.MailMessageStorageLong;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.Searcher;
import com.openexchange.mail.twitter.converters.TwitterStatusConverter;
import com.openexchange.mail.twitter.services.TwitterServiceRegistry;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.user.UserService;

/**
 * {@link TwitterMessageStorage} - The twitter message storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessageStorage extends MailMessageStorageLong {

    private final TwitterAccess twitterAccess;

    private final Session session;

    private final Context ctx;

    private final int accountId;

    private Locale locale;

    private MailAccount mailAccount;

    /**
     * Initializes a new {@link TwitterMessageStorage}.
     *
     * @param twitterAccess The access to twitter API
     * @param session The session
     * @param accountId The account ID
     * @throws OXException If initialization fails
     */
    public TwitterMessageStorage(final TwitterAccess twitterAccess, final Session session, final int accountId) throws OXException {
        super();
        this.twitterAccess = twitterAccess;
        this.session = session;
        this.accountId = accountId;
        try {
            final ContextService contextService = TwitterServiceRegistry.getServiceRegistry().getService(ContextService.class, true);
            ctx = contextService.getContext(session.getContextId());
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    private MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                final MailAccountStorageService storageService =
                    TwitterServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
                mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (final OXException e) {
                throw new OXException(e);
            }
        }
        return mailAccount;
    }

    /**
     * Gets session user's locale
     *
     * @return The session user's locale
     * @throws OXException If retrieving user's locale fails
     */
    private Locale getLocale() throws OXException {
        if (null == locale) {
            try {
                final UserService userService = TwitterServiceRegistry.getServiceRegistry().getService(UserService.class, true);
                locale = userService.getUser(session.getUserId(), ctx).getLocale();
            } catch (final OXException e) {
                throw new OXException(e);
            }
        }
        return locale;
    }

    @Override
    public long[] appendMessagesLong(final String destFolder, final MailMessage[] msgs) throws OXException {
        if ("INBOX".equals(destFolder) || MailFolder.DEFAULT_FOLDER_ID.equals(destFolder)) {
            throw MailExceptionCode.NO_CREATE_ACCESS.create(destFolder);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(destFolder);
    }

    @Override
    public long[] copyMessagesLong(final String sourceFolder, final String destFolder, final long[] mailIds, final boolean fast) throws OXException {
        if ("INBOX".equals(sourceFolder) || MailFolder.DEFAULT_FOLDER_ID.equals(sourceFolder)) {
            throw MailExceptionCode.NO_CREATE_ACCESS.create(sourceFolder);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(sourceFolder);
    }

    @Override
    public void deleteMessagesLong(final String folder, final long[] mailIds, final boolean hardDelete) throws OXException {
        if ("INBOX".equals(folder) || MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
            throw MailExceptionCode.NO_DELETE_ACCESS.create(folder);
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(folder);
    }

    @Override
    public MailMessage[] getMessagesLong(final String folder, final long[] mailIds, final MailField[] fields) throws OXException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
            throw MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES.create(folder);
        }
        if (!"INBOX".equals(folder)) {
            throw MailExceptionCode.FOLDER_NOT_FOUND.create(folder);
        }
        try {
            final List<Status> timeline = twitterAccess.getHomeTimeline();
            final Status[] statuses = new Status[mailIds.length];
            for (int i = 0; i < mailIds.length; i++) {
                final long mailId = mailIds[i];
                Inner: for (final Status status : timeline) {
                    if (status.getId() == mailId) {
                        statuses[i] = status;
                        break Inner;
                    }
                }
            }

            final MailMessage[] msgs = new MailMessage[statuses.length];
            Arrays.fill(msgs, null);
            final String accountName = getMailAccount().getName();
            for (int i = 0; i < statuses.length; i++) {
                final Status s = statuses[i];
                if (null != s) {
                    msgs[i] = TwitterStatusConverter.convertStatus2Message(s, accountId, accountName);
                }
            }

            return msgs;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void releaseResources() throws OXException {
        // Nothing to do
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
            throw MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES.create(folder);
        }
        if (!"INBOX".equals(folder)) {
            throw MailExceptionCode.FOLDER_NOT_FOUND.create(folder);
        }
        List<MailMessage> msgs = null;
        try {
            /*
             * Get friends' time line
             */
            final List<Status> timeline = twitterAccess.getHomeTimeline();
            final int size = timeline.size();
            msgs = new ArrayList<MailMessage>(size);
            final String accountName = getMailAccount().getName();
            for (int i = 0; i < size; i++) {
                msgs.add(TwitterStatusConverter.convertStatus2Message(timeline.get(i), accountId, accountName));
            }
        } catch (final OXException e) {
            throw e;
        }
        if (null != searchTerm) {
            // Filter them
            msgs = Searcher.matches(msgs, searchTerm);
        }
        // Sort them
        Collections.sort(msgs, new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), getLocale()));
        final int size = msgs.size();
        if (indexRange != null) {
            final int fromIndex = indexRange.start;
            int toIndex = indexRange.end;
            if (size == 0) {
                return EMPTY_RETVAL;
            }
            if ((fromIndex) > size) {
                /*
                 * Return empty iterator if start is out of range
                 */
                return EMPTY_RETVAL;
            }
            /*
             * Reset end index if out of range
             */
            if (toIndex >= size) {
                toIndex = size;
            }
            final List<MailMessage> subList = msgs.subList(fromIndex, toIndex);
            return subList.toArray(new MailMessage[subList.size()]);
        }
        return msgs.toArray(new MailMessage[size]);
    }

    @Override
    public void updateMessageFlagsLong(final String folder, final long[] mailIds, final int flags, final boolean set) throws OXException {
        if ("INBOX".equals(folder) || MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
            return;
        }
        throw MailExceptionCode.FOLDER_NOT_FOUND.create(folder);
    }

}
