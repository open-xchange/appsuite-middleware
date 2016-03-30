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
