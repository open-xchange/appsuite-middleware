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

package com.openexchange.messaging.twitter;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.twitter.TwitterException;

/**
 * {@link TwitterMessagingMessageAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingMessageAccess implements MessagingMessageAccess {

    private static String EMPTY = MessagingFolder.ROOT_FULLNAME;

    private void checkFolder(final String fullname) throws MessagingException {
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

    /**
     * Initializes a new {@link TwitterMessagingMessageAccess}.
     */
    public TwitterMessagingMessageAccess(final TwitterAccess twitterAccess, final MessagingAccount account, final Session session) {
        super();
        this.twitterAccess = twitterAccess;
        id = account.getId();
        user = session.getUserId();
        cid = session.getContextId();
    }

    public void appendMessages(final String folder, final MessagingMessage[] messages) throws MessagingException {
        checkFolder(folder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws MessagingException {
        checkFolder(sourceFolder);
        checkFolder(destFolder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws MessagingException {
        checkFolder(folder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws MessagingException {
        return searchMessages(folder, IndexRange.NULL, sortField, order, null, fields);
    }

    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws MessagingException {
        checkFolder(folder);
        try {
            return new TwitterMessagingMessage(twitterAccess.showStatus(parseUnsignedLong(id)));
        } catch (final TwitterException e) {
            throw new MessagingException(e);
        }
    }

    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws MessagingException {
        checkFolder(folder);
        try {
            final long[] ids = strings2longs(messageIds);
            final List<MessagingMessage> l = new ArrayList<MessagingMessage>(ids.length);
            for (int i = 0; i < ids.length; i++) {
                l.add(new TwitterMessagingMessage(twitterAccess.showStatus(ids[i])));
            }
            return l;
        } catch (final TwitterException e) {
            throw new MessagingException(e);
        }
    }

    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws MessagingException {
        checkFolder(sourceFolder);
        checkFolder(destFolder);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(TwitterMessagingService.getServiceId());
    }

    public MessagingMessage perform(final String folder, final String id, final String action) throws MessagingException {
        if ("retweet".equalsIgnoreCase(action)) {
            // TODO
            return null;
        } else if ("directMessage".equalsIgnoreCase(action)) {
            // TODO
            return null;
        } else {
            throw MessagingExceptionCodes.UNKNOWN_ACTION.create(action);
        }
    }

    public List<MessagingMessage> searchMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws MessagingException {
        checkFolder(folder);
        try {
            final List<MessagingMessage> msgs;
            if (null != searchTerm) {
                final List<Status> friendsTimeline = twitterAccess.getFriendsTimeline();
                msgs = new ArrayList<MessagingMessage>(friendsTimeline.size());
                for (final Status status : friendsTimeline) {
                    final TwitterMessagingMessage message = new TwitterMessagingMessage(status);
                    if (searchTerm.matches(message)) {
                        msgs.add(message);
                    }
                }
            } else {
                final List<Status> friendsTimeline = twitterAccess.getFriendsTimeline();
                msgs = new ArrayList<MessagingMessage>(friendsTimeline.size());
                for (final Status status : friendsTimeline) {
                    msgs.add(new TwitterMessagingMessage(status));
                }
            }
            /*
             * Sort
             */
            if (null != sortField) {
                org.apache.commons.logging.LogFactory.getLog(TwitterMessagingMessageAccess.class).warn("Sort not yet supported");
            }
            /*
             * Return
             */
            return msgs;
        } catch (final TwitterException e) {
            throw new MessagingException(e);
        }
    }

    public void updateMessage(final MessagingMessage message, final MessagingField[] fields) throws MessagingException {
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

    /**
     * Converts specified numbers to an array of <code>String</code>.
     * 
     * @param longs The numbers
     * @return An array of <code>String</code>
     */
    private static final String[] longs2strings(final long[] longs) {
        if (null == longs) {
            return null;
        }
        final String[] retval = new String[longs.length];
        for (int i = 0; i < retval.length; i++) {
            final long l = longs[i];
            if (-1 == l) {
                retval[i] = null;
            } else {
                retval[i] = String.valueOf(longs[i]);
            }
        }
        return retval;
    }

    private static final long DEFAULT = -1L;

    private static final int RADIX = 10;

    private static long parseUnsignedLong(final String s) {
        if (s == null) {
            return DEFAULT;
        }
        final int max = s.length();
        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        long result = 0;
        int i = 0;

        final long limit = -Long.MAX_VALUE;
        final long multmin = limit / RADIX;
        int digit;

        if (i < max) {
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return DEFAULT;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = Character.digit(s.charAt(i++), RADIX);
            if (digit < 0) {
                return DEFAULT;
            }
            if (result < multmin) {
                return DEFAULT;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return DEFAULT;
            }
            result -= digit;
        }
        return -result;
    }

}
