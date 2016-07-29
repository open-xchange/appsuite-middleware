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
