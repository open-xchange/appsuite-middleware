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

package com.openexchange.twitter.internal;

import twitter4j.OXTwitter;
import twitter4j.Twitter;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import com.openexchange.twitter.Paging;
import com.openexchange.twitter.TwitterAccess;
import com.openexchange.twitter.TwitterAccessToken;
import com.openexchange.twitter.TwitterException;
import com.openexchange.twitter.TwitterExceptionCodes;
import com.openexchange.twitter.TwitterService;

/**
 * {@link TwitterServiceImpl} - The twitter service implementation based on <a
 * href="http://repo1.maven.org/maven2/net/homeip/yusuke/twitter4j/">twitter4j</a>.
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

    public TwitterAccess getTwitterAccess(final String twitterId, final String password) {
        return new TwitterAccessImpl(new twitter4j.OXTwitter(twitterId, password));
    }

    public TwitterAccess getUnauthenticatedTwitterAccess() {
        return new TwitterAccessImpl(new twitter4j.OXTwitter());
    }

    public Paging newPaging() {
        return new PagingImpl(new twitter4j.Paging());
    }

    public TwitterAccess getOAuthTwitterAccess(final String twitterToken, final String twitterTokenSecret) {
        final OXTwitter twitter = new twitter4j.OXTwitter();
        /*
         * Insert the appropriate consumer key and consumer secret here
         */
        twitter.setOAuthConsumer(TwitterConfiguration.getConsumerKey(), TwitterConfiguration.getConsumerSecret());
        final AccessToken accessToken = new AccessToken(twitterToken, twitterTokenSecret);
        twitter.setOAuthAccessToken(accessToken);
        return new TwitterAccessImpl(twitter);
    }

    public TwitterAccessToken getTwitterAccessToken(final String twitterId, final String password) throws TwitterException {
        try {
            final Twitter twitter = new Twitter();
            /*
             * Insert the appropriate consumer key and consumer secret here
             */
            twitter.setOAuthConsumer(TwitterConfiguration.getConsumerKey(), TwitterConfiguration.getConsumerSecret());
            final RequestToken requestToken = twitter.getOAuthRequestToken();
            /*
             * TODO: Start parsing twitter web site and confirm using specified credentials
             */
            final String pin = "sdfasdfsdfasdf";
            final AccessToken accessToken;
            if (pin.length() > 0) {
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            } else {
                accessToken = twitter.getOAuthAccessToken(requestToken);
            }
            /*
             * Return token
             */
            return new TwitterAccessToken() {

                public String getTokenSecret() {
                    return accessToken.getTokenSecret();
                }

                public String getToken() {
                    return accessToken.getToken();
                }
            };
        } catch (final twitter4j.TwitterException e) {
            throw TwitterExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
