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
import com.openexchange.twitter.User;
import twitter4j.Status;

/**
 * {@link RetweetDetailsImpl} - The retweet details implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RetweetDetailsImpl implements RetweetDetails {

    private final twitter4j.Status retweetStatus;

    private User user;

    /**
     * Initializes a new {@link RetweetDetailsImpl}.
     *
     * @param retweetStatus
     * @param retweetCount
     */
    public RetweetDetailsImpl(final Status retweetStatus, final long retweetCount) {
        super();
        this.retweetStatus = retweetStatus;
    }

    public int getRateLimitLimit() {
        return retweetStatus.getRateLimitStatus().getLimit();
    }

    public int getRateLimitRemaining() {
        return retweetStatus.getRateLimitStatus().getRemaining();
    }

    public long getRateLimitReset() {
        return retweetStatus.getRateLimitStatus().getResetTimeInSeconds();
    }

    @Override
    public Date getRetweetedAt() {
        return retweetStatus.getCreatedAt();
    }

    @Override
    public long getRetweetId() {
        return retweetStatus.getId();
    }

    @Override
    public User getRetweetingUser() {
        if (null == user) {
            user = new UserImpl(retweetStatus.getUser());
        }
        return user;
    }

    @Override
    public String toString() {
        return retweetStatus.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((retweetStatus == null) ? 0 : retweetStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RetweetDetailsImpl)) {
            return false;
        }
        final RetweetDetailsImpl other = (RetweetDetailsImpl) obj;
        if (retweetStatus == null) {
            if (other.retweetStatus != null) {
                return false;
            }
        } else if (!retweetStatus.equals(other.retweetStatus)) {
            return false;
        }
        return true;
    }

}
