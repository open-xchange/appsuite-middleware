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

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;

/**
 * {@link TwitterConfiguration} - Configuration of twitter bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterConfiguration implements Reloadable {

    private static final TwitterConfiguration INSTANCE = new TwitterConfiguration();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static TwitterConfiguration getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------ //

    private final AtomicReference<String> consumerKey;

    private final AtomicReference<String> consumerSecret;

    /**
     * Initializes a new {@link TwitterConfiguration}.
     */
    private TwitterConfiguration() {
        super();
        consumerKey = new AtomicReference<String>();
        consumerSecret = new AtomicReference<String>();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        configure(configService);
    }

    /**
     * Configures twitter bundle.
     *
     * @param configurationService The configuration service needed to read properties
     */
    public void configure(final ConfigurationService configurationService) {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TwitterConfiguration.class);
        {
            final String property = configurationService.getProperty("com.openexchange.oauth.twitter.apiKey");
            if (null == property) {
                log.error("Missing property \"com.openexchange.oauth.twitter.apiKey\"");
            } else {
                consumerKey.set(property);
            }
        }
        {
            final String property = configurationService.getProperty("com.openexchange.oauth.twitter.apiSecret");
            if (null == property) {
                log.error("Missing property \"com.openexchange.oauth.twitter.apiSecret\"");
            } else {
                consumerSecret.set(property);
            }
        }

        // TODO: Configuration.setProperty("twitter4j.source", "Open-Xchange");

    }

    /**
     * Gets the configured consumer key.
     *
     * @return The consumer key
     */
    public String getConsumerKey() {
        return consumerKey.get();
    }

    /**
     * Gets the configured consumer secret.
     *
     * @return The consumer secret
     */
    public String getConsumerSecret() {
        return consumerSecret.get();
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.oauth.twitter.apiKey", "com.openexchange.oauth.twitter.apiSecret");
    }

}
