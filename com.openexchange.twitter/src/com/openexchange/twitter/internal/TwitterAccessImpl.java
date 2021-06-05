/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.twitter.internal;

import static com.openexchange.twitter.internal.TwitterUtils.handleTwitterException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.twitter.DirectMessage;
import com.openexchange.twitter.Paging;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.twitter.User;
import twitter4j.OXTwitter;
import twitter4j.StatusUpdate;

/**
 * {@link TwitterAccessImpl} - The twitter access implementation based on <a
 * href="http://repo1.maven.org/maven2/net/homeip/yusuke/twitter4j/">twitter4j</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterAccessImpl implements TwitterAccess {

	private final OXTwitter twitter4jTwitter;

    private volatile User user;

    /**
     * Initializes a new {@link TwitterAccessImpl}.
     *
     * @param twitter4jTwitter The authenticated <code>twitter4j.Twitter</code> instance
     * @throws IllegalArgumentException If specified <code>twitter4jTwitter</code> argument is <code>null</code>
     */
    public TwitterAccessImpl(final OXTwitter twitter4jTwitter) {
        super();
        if (null == twitter4jTwitter) {
            throw new IllegalArgumentException("twitter4jTwitter is null.");
        }
        this.twitter4jTwitter = twitter4jTwitter;
    }

    @Override
    public List<DirectMessage> getDirectMessages() throws OXException {
        try {
            final List<twitter4j.DirectMessage> l = twitter4jTwitter.getDirectMessages();

            final List<DirectMessage> ret = new ArrayList<DirectMessage>(l.size());
            for (final twitter4j.DirectMessage dm : l) {
                ret.add(new DirectMessageImpl(dm));
            }
            return ret;
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public List<DirectMessage> getDirectMessages(final Paging paging) throws OXException {
        try {
            final List<twitter4j.DirectMessage> l = twitter4jTwitter.getDirectMessages(pagingFrom(paging));

            final List<DirectMessage> ret = new ArrayList<DirectMessage>(l.size());
            for (final twitter4j.DirectMessage dm : l) {
                ret.add(new DirectMessageImpl(dm));
            }
            return ret;
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public List<Status> getFriendsTimeline() throws OXException {
        try {
            final List<twitter4j.Status> l = twitter4jTwitter.getFavorites();

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public List<Status> getFriendsTimeline(final Paging paging) throws OXException {
        try {
            final List<twitter4j.Status> l = twitter4jTwitter.getFavorites(pagingFrom(paging));

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public List<Status> getHomeTimeline() throws OXException {
        try {
            final List<twitter4j.Status> l = twitter4jTwitter.getHomeTimeline();

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public List<Status> getHomeTimeline(final Paging paging) throws OXException {
        try {
            final List<twitter4j.Status> l = twitter4jTwitter.getHomeTimeline(pagingFrom(paging));

            final List<Status> ret = new ArrayList<Status>(l.size());
            for (final twitter4j.Status status : l) {
                ret.add(new StatusImpl(status));
            }
            return ret;
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public DirectMessage sendDirectMessage(final String id, final String text) throws OXException {
        try {
            return new DirectMessageImpl(twitter4jTwitter.sendDirectMessage(id, text));
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public Status updateStatus(final String status) throws OXException {
        try {
            return new StatusImpl(twitter4jTwitter.updateStatus(status));
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public Status updateStatus(final String status, final long inReplyToStatusId) throws OXException {
        try {
            final StatusUpdate statusUpdate = new StatusUpdate(status);
            statusUpdate.setInReplyToStatusId(inReplyToStatusId);
            return new StatusImpl(twitter4jTwitter.updateStatus(statusUpdate));
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public Status retweetStatus(final long statusId) throws OXException {
        try {
            return new StatusImpl(twitter4jTwitter.retweetStatus(statusId));
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public Status destroyStatus(final long statusId) throws OXException {
        try {
            return new StatusImpl(twitter4jTwitter.destroyStatus(statusId));
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public Status showStatus(final long statusId) throws OXException {
        try {
            return new StatusImpl(twitter4jTwitter.showStatusAuthenticated(statusId));
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    @Override
    public User getUser() throws OXException {
        User tmp = user;
        if (null == tmp) {
            // No need for synchronization
            try {
                user = tmp = new UserImpl(twitter4jTwitter.verifyCredentials());
            } catch (twitter4j.TwitterException e) {
                throw handleTwitterException(e);
            }
        }
        return tmp;
    }

    @Override
    public User showUser(final String id) throws OXException {
        try {
            return new UserImpl(twitter4jTwitter.showUser(id));
        } catch (twitter4j.TwitterException e) {
            throw handleTwitterException(e);
        }
    }

    private static twitter4j.Paging pagingFrom(final Paging paging) {
        final twitter4j.Paging twPaging = new twitter4j.Paging();

        final int page = paging.getPage();
        if (page >= 1) {
            twPaging.setPage(page);
        }
        final int count = paging.getCount();
        if (count >= 1) {
            twPaging.setCount(count);
        }
        final long sinceId = paging.getSinceId();
        if (sinceId >= 1) {
            twPaging.setSinceId(sinceId);
        }
        final long maxId = paging.getMaxId();
        if (maxId >= 1) {
            twPaging.setMaxId(maxId);
        }
        return twPaging;
    }

}
