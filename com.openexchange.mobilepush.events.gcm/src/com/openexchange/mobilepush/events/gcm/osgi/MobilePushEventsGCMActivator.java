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

package com.openexchange.mobilepush.events.gcm.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.mobilepush.events.MobilePushEventService;
import com.openexchange.mobilepush.events.gcm.impl.MobilePushGCMPublisherImpl;
import com.openexchange.mobilepush.events.storage.MobilePushStorageService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MobilePushEventsGCMActivator}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobilePushEventsGCMActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilePushEventsGCMActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, MobilePushStorageService.class, MobilePushEventService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: {}", context.getBundle().getSymbolicName());
        Services.set(this);

        ConfigurationService configService = Services.getService(ConfigurationService.class, true);
        if (configService.getBoolProperty("com.openxchange.mobilepush.events.gcm.enabled", false)) {
            /*
             * register GCM key provider
             */
            final String configuredKey = configService.getProperty("com.openxchange.mobilepush.events.gcm.key");
            if (false == Strings.isEmpty(configuredKey)) {
                /*
                 * register publisher
                 */
                getService(MobilePushEventService.class).registerPushPublisher(new MobilePushGCMPublisherImpl());
            } else {
                LOG.info("Mobile push events via GCM are disabled. A GCM key is not set in the properties.");
            }
        } else {
            LOG.info("Mobile push events via GCM are disabled, skipping publisher registration.");
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: {}", context.getBundle().getSymbolicName());
        Services.set(null);
        super.stopBundle();
    }
}
