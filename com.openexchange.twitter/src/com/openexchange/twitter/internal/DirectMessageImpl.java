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
import com.openexchange.twitter.DirectMessage;
import com.openexchange.twitter.User;

/**
 * {@link DirectMessageImpl} - The direct message implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DirectMessageImpl implements DirectMessage {

    private final twitter4j.DirectMessage twitter4jDirectMessage;

    private User sender;

    private User recipient;

    /**
     * Initializes a new {@link DirectMessageImpl}.
     *
     * @param twitter4jDirectMessage The twitter4j direct message
     */
    public DirectMessageImpl(final twitter4j.DirectMessage twitter4jDirectMessage) {
        super();
        this.twitter4jDirectMessage = twitter4jDirectMessage;
    }

    @Override
    public Date getCreatedAt() {
        return twitter4jDirectMessage.getCreatedAt();
    }

    @Override
    public long getId() {
        return twitter4jDirectMessage.getId();
    }

    public int getRateLimitLimit() {
        return twitter4jDirectMessage.getRateLimitStatus().getLimit();
    }

    public int getRateLimitRemaining() {
        return twitter4jDirectMessage.getRateLimitStatus().getRemaining();
    }

    public long getRateLimitReset() {
        return twitter4jDirectMessage.getRateLimitStatus().getResetTimeInSeconds();
    }

    @Override
    public User getRecipient() {
        if (null == recipient) {
            recipient = new UserImpl(twitter4jDirectMessage.getRecipient());
        }
        return recipient;
    }

    @Override
    public long getRecipientId() {
        return twitter4jDirectMessage.getRecipientId();
    }

    @Override
    public String getRecipientScreenName() {
        return twitter4jDirectMessage.getRecipientScreenName();
    }

    @Override
    public User getSender() {
        if (null == sender) {
            sender = new UserImpl(twitter4jDirectMessage.getSender());
        }
        return sender;
    }

    @Override
    public long getSenderId() {
        return twitter4jDirectMessage.getSenderId();
    }

    @Override
    public String getSenderScreenName() {
        return twitter4jDirectMessage.getSenderScreenName();
    }

    @Override
    public String getText() {
        return twitter4jDirectMessage.getText();
    }

    @Override
    public String toString() {
        return twitter4jDirectMessage.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((twitter4jDirectMessage == null) ? 0 : twitter4jDirectMessage.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DirectMessageImpl)) {
            return false;
        }
        final DirectMessageImpl other = (DirectMessageImpl) obj;
        if (twitter4jDirectMessage == null) {
            if (other.twitter4jDirectMessage != null) {
                return false;
            }
        } else if (!twitter4jDirectMessage.equals(other.twitter4jDirectMessage)) {
            return false;
        }
        return true;
    }

}
