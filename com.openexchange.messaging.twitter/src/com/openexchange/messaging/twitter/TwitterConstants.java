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
