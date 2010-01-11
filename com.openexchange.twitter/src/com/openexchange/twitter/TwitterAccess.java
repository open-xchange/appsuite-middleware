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

package com.openexchange.twitter;

import java.util.List;

/**
 * {@link TwitterAccess} - The <a href="http://twitter.com/">twitter</a> access offering <a
 * href="http://apiwiki.twitter.com/Twitter-API-Documentation">twitter API</a>.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface TwitterAccess {

    /**
     * Gets the 20 most recent statuses posted in the last 24 hours from the authenticating user and that user's friends. It's also possible
     * to request another user's friends_timeline via the id parameter below.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/friends_timeline">http://twitter.com/statuses/friends_timeline</a>
     * 
     * @return A list of the friends' time line
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getFriendsTimeline() throws TwitterException;

    /**
     * Returns the 20 most recent statuses posted in the last 24 hours from the specified user id.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/friends_timeline">http://twitter.com/statuses/friends_timeline</a>
     * 
     * @param paging The controls pagination
     * @return A list of the friends' time line
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getFriendsTimeline(Paging paging) throws TwitterException;

    /**
     * Returns the 20 most recent statuses, including re-tweets, posted by the authenticating user and that user's friends. This is the
     * equivalent of /timeline/home on the Web.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/home_timeline">http://twitter.com/statuses/home_timeline</a>
     * 
     * @return A list of the home time line
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getHomeTimeline() throws TwitterException;

    /**
     * Returns the 20 most recent statuses, including re-tweets, posted by the authenticating user and that user's friends. This is the
     * equivalent of /timeline/home on the Web.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/home_timeline">http://twitter.com/statuses/home_timeline</a>
     * 
     * @param paging The controls pagination
     * @return A list of the home time line
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<Status> getHomeTimeline(Paging paging) throws TwitterException;

    /**
     * Gets a list of the direct messages sent to the authenticating user.
     * <p>
     * This method calls <a href="http://twitter.com/direct_messages">http://twitter.com/direct_messages</a>
     * 
     * @return A list of direct messages
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<DirectMessage> getDirectMessages() throws TwitterException;

    /**
     * Gets a list of the direct messages sent to the authenticating user.
     * <p>
     * This method calls <a href="http://twitter.com/direct_messages">http://twitter.com/direct_messages</a>
     * 
     * @param paging The controls pagination
     * @return A list of direct messages
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public List<DirectMessage> getDirectMessages(Paging paging) throws TwitterException;

    /**
     * Sends a new direct message to the specified user from the authenticating user. Requires both the user and text parameters below. The
     * text will be trimmed if the length of the text is exceeding 140 characters.
     * <p>
     * This method calls <a href="http://twitter.com/direct_messages/new">http://twitter.com/direct_messages/new</a>
     * 
     * @param id The ID or screen name of the user to whom send the direct message
     * @param text The text of the message to send
     * @return The direct message sent to recipient
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public DirectMessage sendDirectMessage(String id, String text) throws TwitterException;

    /**
     * Updates the user's status. The text will be trimmed if the length of the text is exceeding 140 characters.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/update">http://twitter.com/statuses/update</a>
     * 
     * @param status The text of your status update
     * @return The latest status
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public Status updateStatus(String status) throws TwitterException;

    /**
     * Updates the user's status. The text will be trimmed if the length of the text is exceeding 140 characters.
     * <p>
     * This method calls <a href="http://twitter.com/statuses/update">http://twitter.com/statuses/update</a>
     * 
     * @param status The text of your status update
     * @param inReplyToStatusId The ID of an existing status that the status to be posted is in reply to. This implicitly sets the
     *            in_reply_to_user_id attribute of the resulting status to the user ID of the message being replied to. Invalid/missing
     *            status IDs will be ignored.
     * @return The latest status
     * @throws TwitterException If <a href="http://twitter.com/">twitter</a> service or network is unavailable
     */
    public Status updateStatus(String status, long inReplyToStatusId) throws TwitterException;

}
