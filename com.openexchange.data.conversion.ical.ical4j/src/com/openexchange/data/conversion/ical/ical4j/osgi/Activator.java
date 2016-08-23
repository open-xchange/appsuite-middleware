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

package com.openexchange.data.conversion.ical.ical4j.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import com.openexchange.data.conversion.ical.ical4j.ICal4JITipEmitter;
import com.openexchange.data.conversion.ical.ical4j.ICal4JITipParser;
import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.OXResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.OXUserResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.CreatedBy;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.data.conversion.ical.itip.ITipEmitter;
import com.openexchange.data.conversion.ical.itip.ITipParser;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;

/**
 * Publishes the iCal4j parser and emitter services.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ConfigurationService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final OXUserResolver userResolver = new OXUserResolver();
        track(UserService.class, new UserServiceTrackerCustomizer(context, userResolver));
        Participants.userResolver = userResolver;
        CreatedBy.userResolver = userResolver;

        ConfigurationService configurationService = getService(ConfigurationService.class);
        String updateTimezones = configurationService.getProperty("com.openexchange.ical.updateTimezones", "true");
        System.setProperty("net.fortuna.ical4j.timezone.update.enabled", updateTimezones);

        final OXResourceResolver resourceResolver = new OXResourceResolver();
        track(ResourceService.class, new ResourceServiceTrackerCustomizer(context, resourceResolver));
        Participants.resourceResolver = resourceResolver;

        track(CalendarCollectionService.class, new CalendarServiceTracker(context));
        track(GroupService.class, new GroupServiceTracker(context));
        openTrackers();

        ICal4JParser parser = new ICal4JParser();
        parser.setLimit(configurationService.getIntProperty("com.openexchange.import.ical.limit", -1));
		registerService(ICalParser.class, parser, null);
        registerService(ICalEmitter.class, new ICal4JEmitter(), null);
        registerService(ITipParser.class, new ICal4JITipParser(), null);
        registerService(ITipEmitter.class, new ICal4JITipEmitter(), null);
    }

    @Override
    public void stopBundle() {
        unregisterServices();
        closeTrackers();
        cleanUp();
    }
}
