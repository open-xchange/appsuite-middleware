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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.drive.events.apn.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.drive.events.apn.internal.APNAccess;
import com.openexchange.drive.events.apn.internal.APNDriveEventPublisher;
import com.openexchange.drive.events.apn.internal.IOSDriveEventPublisher;
import com.openexchange.drive.events.apn.internal.MacOSDriveEventPublisher;
import com.openexchange.drive.events.apn.internal.Services;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link APNDriveEventsActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class APNDriveEventsActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(APNDriveEventsActivator.class);

    /**
     * Initializes a new {@link APNDriveEventsActivator}.
     */
    public APNDriveEventsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DriveEventService.class, DriveSubscriptionStore.class, ConfigurationService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.drive.events.apn");
        com.openexchange.drive.events.apn.internal.Services.set(this);
        ConfigurationService configService = getService(ConfigurationService.class);
        DriveEventService eventService = getService(DriveEventService.class);
        /*
         * iOS
         */
        if (configService.getBoolProperty("com.openexchange.drive.events.apn.ios.enabled", false)) {
            IOSDriveEventPublisher publisher = new IOSDriveEventPublisher(
                getAccess(configService, "com.openexchange.drive.events.apn.ios."));
            eventService.registerPublisher(publisher);
            String feedbackQueryInterval = configService.getProperty(
                "com.openexchange.drive.events.apn.ios.feedbackQueryInterval", (String)null);
            setupFeedbackQueries(publisher, feedbackQueryInterval);
        } else {
            LOG.info("Drive events for iOS clients via APN are disabled, skipping publisher registration.");
        }
        /*
         * Mac OS
         */
        if (configService.getBoolProperty("com.openexchange.drive.events.apn.macos.enabled", false)) {
            MacOSDriveEventPublisher publisher = new MacOSDriveEventPublisher(
                getAccess(configService, "com.openexchange.drive.events.apn.macos."));
            eventService.registerPublisher(publisher);
            String feedbackQueryInterval = configService.getProperty(
                "com.openexchange.drive.events.apn.macos.feedbackQueryInterval", (String)null);
            setupFeedbackQueries(publisher, feedbackQueryInterval);
        } else {
            LOG.info("Drive events for Mac OS clients via APN are disabled, skipping publisher registration.");
        }
    }

    private static void setupFeedbackQueries(final APNDriveEventPublisher publisher, String feedbackQueryInterval) throws OXException {
        if (false == Strings.isEmpty(feedbackQueryInterval)) {
            long interval = TimeSpanParser.parseTimespan(feedbackQueryInterval);
            if (60 * 1000 <= interval) {
                Services.getService(TimerService.class).scheduleWithFixedDelay(new Runnable() {

                    @Override
                    public void run() {
                        publisher.queryFeedbackService();
                    }
                }, interval, interval);
            } else {
                LOG.warn("Ignoring too small value '" + feedbackQueryInterval + " for APN feedback query interval.");
            }
        }
    }

    private static APNAccess getAccess(ConfigurationService configService, String prefix) throws OXException {
        String keystore = configService.getProperty(prefix + "keystore");
        if (Strings.isEmpty(keystore)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "keystore");
        }
        String password = configService.getProperty(prefix + "password");
        if (Strings.isEmpty(password)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(prefix + "password");
        }
        boolean production = configService.getBoolProperty(prefix + "production", true);
        return new APNAccess(keystore, password, production);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: com.openexchange.drive.events.apn");
        Services.set(null);
        super.stopBundle();
    }

}
