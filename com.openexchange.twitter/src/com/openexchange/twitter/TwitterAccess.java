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

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link TwitterAccess} - The <a href="http://twitter.com/">twitter</a> access offering <a
 * href="http://apiwiki.twitter.com/Twitter-API-Documentation">twitter API</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface TwitterAccess {

    /**
     * Gets authenticating user.
     *
     * @return The authenticating user
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public User getUser() throws OXException;

    /**
     * Gets extended information of a given user, specified by identifier or screen name. This information includes design settings, so
     * third party developers can theme their widgets according to a given user's preferences.
     * <p>
     * This method calls <a href="http://twitter.com/users/show">http://twitter.com/users/show</a>
     *
     * @param id The identifier or screen name of the user
     * @return The twitter user
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public User showUser(String id) throws OXException;

    /**
     * Gets the 20 most recent statuses posted in the last 24 hours from the authenticating user and that user's friends. It's also possible
     * to request another user's friends_timeline via the id parameter below.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/friends_timeline">http://twitter.com/statuses/friends_timeline</a>
     *
     * @return A list of the friends' time line
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getFriendsTimeline() throws OXException;

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified user id.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/friends_timeline">http://twitter.com/statuses/friends_timeline</a>
     *
     * @param paging The controls pagination
     * @return A list of the friends' time line
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getFriendsTimeline(Paging paging) throws OXException;

    /**
     * Returns the 20 most recent statuses, including re-tweets, posted by the authenticating user and that user's friends. This is the
     * equivalent of /timeline/home on the Web.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/home_timeline">http://twitter.com/statuses/home_timeline</a>
     *
     * @return A list of the home time line
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getHomeTimeline() throws OXException;

    /**
     * Returns the 20 most recent statuses, including re-tweets, posted by the authenticating user and that user's friends. This is the
     * equivalent of /timeline/home on the Web.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/home_timeline">http://twitter.com/statuses/home_timeline</a>
     *
     * @param paging The controls pagination
     * @return A list of the home time line
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getHomeTimeline(Paging paging) throws OXException;

    /**
     * Gets a list of the direct messages sent to the authenticating user.
     * <p>
     * This method calls <a href="http://twitter.com/direct_messages">http://twitter.com/direct_messages</a>
     *
     * @return A list of direct messages
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<DirectMessage> getDirectMessages() throws OXException;

    /**
     * Gets a list of the direct messages sent to the authenticating user.
     * <p>
     * This method calls <a href="http://twitter.com/direct_messages">http://twitter.com/direct_messages</a>
     *
     * @param paging The controls pagination
     * @return A list of direct messages
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<DirectMessage> getDirectMessages(Paging paging) throws OXException;

    /**
     * Destroys the status specified by the required ID parameter. The authenticating user must be the author of the specified status.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/destroy">http://twitter.com/statuses/destroy</a>
     *
     * @param statusId The ID of the status to destroy.
     * @return The deleted status
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public Status destroyStatus(long statusId) throws OXException;

    /**
     * Retweets a tweet. Requires the id parameter of the tweet being retweeted. Returns the original tweet with retweet details embedded.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/retweet">http://twitter.com/statuses/retweet</a>
     *
     * @param statusId The ID of the status to retweet.
     * @return The retweeted status
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public Status retweetStatus(long statusId) throws OXException;

    /**
     * Gets a single status, specified by the id parameter. The status's author will be returned inline.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/show">http://twitter.com/statuses/show</a>
     *
     * @param id The numerical ID of the status
     * @return The single status
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public Status showStatus(long statusId) throws OXException;

    /**
     * Sends a new direct message to the specified user from the authenticating user. Requires both the user and text parameters below. The
     * text will be trimmed if the length of the text is exceeding 140 characters.
     * <p>
     * This method calls <a href="http://twitter.com/direct_messages/new">http://twitter.com/direct_messages/new</a>
     *
     * @param id The ID or screen name of the user to whom send the direct message
     * @param text The text of the message to send
     * @return The direct message sent to recipient
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public DirectMessage sendDirectMessage(String id, String text) throws OXException;

    /**
     * Updates the user's status. The text will be trimmed if the length of the text is exceeding 140 characters.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/update">http://twitter.com/statuses/update</a>
     *
     * @param status The text of your status update
     * @return The latest status
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public Status updateStatus(String status) throws OXException;

    /**
     * Updates the user's status. The text will be trimmed if the length of the text is exceeding 140 characters.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/update">http://twitter.com/statuses/update</a>
     *
     * @param status The text of your status update
     * @param inReplyToStatusId The ID of the status to retweet
     * @return The latest status
     * @throws OXException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public Status updateStatus(String status, long inReplyToStatusId) throws OXException;

}
