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

package com.openexchange.twitter.internal;

import java.util.ArrayList;
import java.util.List;
import twitter4j.Twitter;
import com.openexchange.twitter.DirectMessage;
import com.openexchange.twitter.Paging;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.TwitterException;
import com.openexchange.twitter.TwitterExceptionCodes;
import com.openexchange.twitter.TwitterService;

/**
 * {@link TwitterServiceImpl} - The twitter service implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterServiceImpl implements TwitterService {

    /**
     * Initializes a new {@link TwitterServiceImpl}.
     */
    public TwitterServiceImpl() {
        super();
    }

    public List<DirectMessage> getDirectMessages(final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            final List<twitter4j.DirectMessage> l = twitter.getDirectMessages();

            final List<DirectMessage> ret = new ArrayList<DirectMessage>(l.size());
            for (final twitter4j.DirectMessage dm : l) {
                ret.add(new DirectMessageImpl(dm));
            }
            return ret;
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public List<DirectMessage> getDirectMessages(final Paging paging, final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            final List<twitter4j.DirectMessage> l =
                twitter.getDirectMessages(new twitter4j.Paging(paging.getPage(), paging.getCount(), paging.getSinceId(), paging.getMaxId()));

            final List<DirectMessage> ret = new ArrayList<DirectMessage>(l.size());
            for (final twitter4j.DirectMessage dm : l) {
                ret.add(new DirectMessageImpl(dm));
            }
            return ret;
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public List<Status> getFriendsTimeline(final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            final List<twitter4j.Status> l = twitter.getFriendsTimeline();

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public List<Status> getFriendsTimeline(final Paging paging, final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            final List<twitter4j.Status> l =
                twitter.getFriendsTimeline(new twitter4j.Paging(paging.getPage(), paging.getCount(), paging.getSinceId(), paging.getMaxId()));

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public List<Status> getHomeTimeline(final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            final List<twitter4j.Status> l = twitter.getHomeTimeline();

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public List<Status> getHomeTimeline(final Paging paging, final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            final List<twitter4j.Status> l =
                twitter.getHomeTimeline(new twitter4j.Paging(paging.getPage(), paging.getCount(), paging.getSinceId(), paging.getMaxId()));

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public DirectMessage sendDirectMessage(final String id, final String text, final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            return new DirectMessageImpl(twitter.sendDirectMessage(id, text));
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public Status updateStatus(final String status, final String twitterId, final String password) throws TwitterException {
        final Twitter twitter = new Twitter(twitterId, password);
        try {
            return new StatusImpl(twitter.updateStatus(status));
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
