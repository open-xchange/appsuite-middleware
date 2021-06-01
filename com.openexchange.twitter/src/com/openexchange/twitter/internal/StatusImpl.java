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

import java.util.Date;
import com.openexchange.twitter.RetweetDetails;
import com.openexchange.twitter.Status;
import com.openexchange.twitter.User;

/**
 * {@link StatusImpl} - The status implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StatusImpl implements Status {

    private final twitter4j.Status twitter4jStatus;

    private User user;

    private RetweetDetailsImpl retweetDetails;

    /**
     * Initializes a new {@link StatusImpl}.
     *
     * @param twitter4jStatus The twitter4j status
     */
    public StatusImpl(final twitter4j.Status twitter4jStatus) {
        super();
        this.twitter4jStatus = twitter4jStatus;
    }

    @Override
    public Date getCreatedAt() {
        return twitter4jStatus.getCreatedAt();
    }

    @Override
    public long getId() {
        return twitter4jStatus.getId();
    }

    @Override
    public String getInReplyToScreenName() {
        return twitter4jStatus.getInReplyToScreenName();
    }

    @Override
    public long getInReplyToStatusId() {
        return twitter4jStatus.getInReplyToStatusId();
    }

    @Override
    public long getInReplyToUserId() {
        return twitter4jStatus.getInReplyToUserId();
    }

    @Override
    public RetweetDetails getRetweetDetails() {
        if (null == retweetDetails) {
            retweetDetails = new RetweetDetailsImpl(twitter4jStatus.getRetweetedStatus(), twitter4jStatus.getRetweetCount());
        }
        return retweetDetails;
    }

    @Override
    public String getSource() {
        return twitter4jStatus.getSource();
    }

    @Override
    public String getText() {
        return twitter4jStatus.getText();
    }

    @Override
    public User getUser() {
        if (null == user) {
            user = new UserImpl(twitter4jStatus.getUser());
        }
        return user;
    }

    @Override
    public boolean isFavorited() {
        return twitter4jStatus.isFavorited();
    }

    @Override
    public boolean isRetweet() {
        return twitter4jStatus.isRetweet();
    }

    @Override
    public boolean isTruncated() {
        return twitter4jStatus.isTruncated();
    }

    @Override
    public String toString() {
        return twitter4jStatus.toString();
    }

}
