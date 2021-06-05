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

package com.openexchange.twitter.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.twitter.TwitterService;
import com.openexchange.twitter.internal.TwitterConfiguration;
import com.openexchange.twitter.internal.TwitterServiceImpl;

/**
 * {@link TwitterActivator} - The activator for twitter bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link TwitterActivator}.
     */
    public TwitterActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TwitterActivator.class);
        try {
            log.info("starting bundle: com.openexchange.twitter");
            /*
             * Service trackers
             */
            track(ConfigurationService.class, new ConfigurationServiceTrackerCustomizer(context));
            openTrackers();
            /*
             * Register
             */
            registerService(TwitterService.class, new TwitterServiceImpl());
            registerService(Reloadable.class, TwitterConfiguration.getInstance());
        } catch (Exception e) {
            log.error("Failed start-up of bundle com.openexchange.twitter", e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

}
