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

package com.openexchange.messaging.twitter;

import static com.openexchange.messaging.twitter.TwitterMessagingUtility.parseUnsignedLong;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.generic.AttachmentFinderHandler;
import com.openexchange.messaging.generic.MessageParser;
import com.openexchange.session.Session;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.TwitterAccess;

/**
 * {@link TwitterMessagingMessageAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingMessageAccess implements MessagingMessageAccess {

    private static String EMPTY = MessagingFolder.ROOT_FULLNAME;

    private void checkFolder(final String fullname) throws OXException {
        if (!EMPTY.equals(fullname)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                fullname,
                Integer.valueOf(id),
                TwitterMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
    }

    // private final TwitterService twitterService;

    private final TwitterAccess twitterAccess;

    private final int id;

    private final int user;

    private final int cid;

    private final Session session;

    /**
     * Initializes a new {@link TwitterMessagingMessageAccess}.
     */
    public TwitterMessagingMessageAccess(final TwitterAccess twitterAccess, final MessagingAccount account, final Session session) {
        super();
        this.twitterAccess = twitterAccess;
        id = account.getId();
        user = session.getUserId();
        cid = session.getContextId();
        this.session = session;
    }

    @Override
    public MessagingPart getAttachment(final String folder, final String messageId, final String sectionId) throws OXException {
        final AttachmentFinderHandler handler = new AttachmentFinderHandler(sectionId);
        new MessageParser().parseMessage(getMessage(folder, messageId, true), handler);
        final MessagingPart part = handler.getMessagingPart();
        if (null == part) {
            throw MessagingExceptionCodes.ATTACHMENT_NOT_FOUND.create(sectionId, messageId, folder);
        }
        return part;
    }

    @Override
    public void appendMessages(final String folder, final MessagingMessage[] messages) throws OXException {
        checkFolder(folder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        checkFolder(sourceFolder);
        checkFolder(destFolder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws OXException {
        checkFolder(folder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws OXException {
        return searchMessages(folder, IndexRange.NULL, sortField, order, null, fields);
    }

    @Override
    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws OXException {
        checkFolder(folder);
        try {
            return get(parseUnsignedLong(id));
        } catch (final OXException e) {
            throw e;
        }
    }

    private TwitterMessagingMessage get(final long id) throws OXException {
        return new TwitterMessagingMessage(twitterAccess.showStatus(id), session);
    }

    @Override
    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws OXException {
        checkFolder(folder);
        try {
            final long[] ids = strings2longs(messageIds);
            final List<MessagingMessage> l = new ArrayList<MessagingMessage>(ids.length);
            for (final long id2 : ids) {
                l.add(get(id2));
            }
            return l;
        } catch (final OXException e) {
            throw e;
        }
    }



    @Override
    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        checkFolder(sourceFolder);
        checkFolder(destFolder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    @Override
    public MessagingMessage perform(final String folder, final String id, final String action) throws OXException {
        checkFolder(folder);
        if (TwitterConstants.TYPE_RETWEET.equalsIgnoreCase(action)) {
            try {
                return new TwitterRetweetMessage(twitterAccess.showStatus(parseUnsignedLong(id)), twitterAccess.getUser());
            } catch (final OXException e) {
                throw e;
            }
        }
        if (TwitterConstants.TYPE_DIRECT_MESSAGE.equalsIgnoreCase(action)) {
            try {
                return new TwitterDirectMessage(twitterAccess.showStatus(parseUnsignedLong(id)).getUser(), twitterAccess.getUser());
            } catch (final OXException e) {
                throw e;
            }
        }
        if (TwitterConstants.TYPE_RETWEET_NEW.equalsIgnoreCase(action)) {
            try {
                twitterAccess.retweetStatus(parseUnsignedLong(id));
                return null;
            } catch (final OXException e) {
                throw e;
            }
        }
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public MessagingMessage perform(final String action) throws OXException {
        /*
         * No supported actions for this perform() method
         */
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public MessagingMessage perform(final MessagingMessage message, final String action) throws OXException {
        if (TwitterConstants.TYPE_TWEET.equalsIgnoreCase(action)) {
            try {
                final StringContent content = TwitterMessagingUtility.checkContent(StringContent.class, message);
                twitterAccess.updateStatus(content.toString());
                return null;
            } catch (final OXException e) {
                throw e;
            }
        }
        throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    @Override
    public List<MessagingMessage> searchMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws OXException {
        checkFolder(folder);
        try {
            final List<MessagingMessage> msgs;
            if (null != searchTerm) {
                final List<Status> friendsTimeline = twitterAccess.getHomeTimeline();
                msgs = new ArrayList<MessagingMessage>(friendsTimeline.size());
                for (final Status status : friendsTimeline) {
                    final TwitterMessagingMessage message = new TwitterMessagingMessage(status, session);
                    if (searchTerm.matches(message)) {
                        msgs.add(message);
                    }
                }
            } else {
                final List<Status> friendsTimeline = twitterAccess.getHomeTimeline();
                msgs = new ArrayList<MessagingMessage>(friendsTimeline.size());
                for (final Status status : friendsTimeline) {
                    final TwitterMessagingMessage message = new TwitterMessagingMessage(status, session);
                    msgs.add(message);
               }
            }
            /*
             * Sort
             */
            if (null != sortField) {
                org.slf4j.LoggerFactory.getLogger(TwitterMessagingMessageAccess.class).warn("Sort not yet supported");
            }
            /*
             * Return
             */
            return msgs;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void updateMessage(final MessagingMessage message, final MessagingField[] fields) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    /**
     * Converts specified strings to an array of <code>long</code>.
     *
     * @param strings The strings
     * @return An array of <code>long</code>
     */
    private static final long[] strings2longs(final String[] strings) {
        if (null == strings) {
            return null;
        }
        final long[] retval = new long[strings.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = parseUnsignedLong(strings[i]);
        }
        return retval;
    }

//    /**
//     * Converts specified numbers to an array of <code>String</code>.
//     *
//     * @param longs The numbers
//     * @return An array of <code>String</code>
//     */
//    private static final String[] longs2strings(final long[] longs) {
//        if (null == longs) {
//            return null;
//        }
//        final String[] retval = new String[longs.length];
//        for (int i = 0; i < retval.length; i++) {
//            final long l = longs[i];
//            if (-1 == l) {
//                retval[i] = null;
//            } else {
//                retval[i] = String.valueOf(longs[i]);
//            }
//        }
//        return retval;
//    }

    @Override
    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws OXException {
        throw new UnsupportedOperationException();
    }

}
