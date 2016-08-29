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
        } catch (final Exception e) {
            log.error("Failed start-up of bundle com.openexchange.twitter", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TwitterActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.twitter");
            /*
             * Unregister
             */
            unregisterServices();
            /*
             * Close trackers
             */
            closeTrackers();
        } catch (final Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.twitter", e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // Nothing to do
        return EMPTY_CLASSES;
    }

}
