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

package com.openexchange.twitter;

import java.util.Date;

/**
 * {@link Status} - One single status of a user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Status {

    /**
     * Gets the creation date.
     *
     * @return The creation date
     */
    Date getCreatedAt();

    /**
     * Gets the id of the status
     *
     * @return the id
     */
    long getId();

    /**
     * Gets the text of the status
     *
     * @return the text
     */
    String getText();

    /**
     * Gets the source
     *
     * @return the source
     */
    String getSource();

    /**
     * Test if the status is truncated
     *
     * @return <code>true</code> if truncated
     */
    boolean isTruncated();

    /**
     * Gets the in_reply_to status id
     *
     * @return the in_reply_to status id
     */
    long getInReplyToStatusId();

    /**
     * Gets the in-reply user id.
     *
     * @return The in-reply user id
     */
    long getInReplyToUserId();

    /**
     * Gets the in-reply-to screen name.
     *
     * @return The in-reply-to screen name
     */
    String getInReplyToScreenName();

    /**
     * Test if the status is favorited.
     *
     * @return <code>true</code> if favorited; otherwise <code>false</code>
     */
    boolean isFavorited();

    /**
     * Gets this status' user.
     *
     * @return The user
     */
    User getUser();

    /**
     * Check if this status is re-tweeted.
     *
     * @return <code>true</code> if this status is re-tweeted; otherwise <code>false</code>
     */
    boolean isRetweet();

    /**
     * Gets the re-tweet details.
     *
     * @return The re-tweet details.
     */
    RetweetDetails getRetweetDetails();

}
