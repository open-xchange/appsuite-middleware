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

package com.openexchange.messaging.twitter;

/**
 * {@link TwitterConstants} - Provides useful constants for twitter.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterConstants {

    /**
     * The max. length of a tweet: 140 characters.
     */
    public static final int MAX_TWEET_LENGTH = 140;

    /**
     * The type denoting a common twitter tweet.
     */
    public static final String TYPE_TWEET = "tweet";

    /**
     * The type denoting twitter retweet.
     */
    public static final String TYPE_RETWEET = "retweet";

    /**
     * The type denoting twitter retweet new.
     */
    public static final String TYPE_RETWEET_NEW = "retweetNew";

    /**
     * The type denoting twitter direct message.
     */
    public static final String TYPE_DIRECT_MESSAGE = "directMessage";

    /**
     * The Status-Id header.
     */
    public static final String HEADER_STATUS_ID = "X-Twitter-Status-Id";

    /**
     * The twitter time line length.
     */
    public static final int TIMELINE_LENGTH = 20;

    /**
     * The constant for account.
     */
    public static final String TWITTER_OAUTH_ACCOUNT = "account";

    /**
     * The configuration property name for twitter token.
     */
    public static final String TWITTER_TOKEN = "twitterToken";

    /**
     * The configuration property name for twitter token secret.
     */
    public static final String TWITTER_TOKEN_SECRET = "twitterTokenSecret";

    /**
     * Initializes a new {@link TwitterConstants}.
     */
    private TwitterConstants() {
        super();
    }

}
