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
import com.openexchange.drive.events.apn.internal.Services;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;

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
        return new Class<?>[] { DriveEventService.class, DriveSubscriptionStore.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: " + context.getBundle().getSymbolicName());
        com.openexchange.drive.events.apn.internal.Services.set(this);
        getService(DriveEventService.class).registerPublisher(new APNDriveEventPublisher(getAccess()));
    }

    private static APNAccess getAccess() throws OXException {
        ConfigurationService configService = Services.getService(ConfigurationService.class, true);
        String property = "com.openexchange.drive.events.apn.keystore";
        String keystore = configService.getProperty(property);
        if (Strings.isEmpty(keystore)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property);
        }
        property = "com.openexchange.drive.events.apn.password";
        String password = configService.getProperty(property);
        if (Strings.isEmpty(password)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(property);
        }
        property = "com.openexchange.drive.events.apn.production";
        boolean production = configService.getBoolProperty(property, true);
        return new APNAccess(keystore, password, production);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: " + context.getBundle().getSymbolicName());
        Services.set(null);
        super.stopBundle();
    }

}
